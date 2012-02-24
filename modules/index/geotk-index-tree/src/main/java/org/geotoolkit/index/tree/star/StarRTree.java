/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.index.tree.star;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.geotoolkit.geometry.Envelopes;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.index.tree.*;
import org.geotoolkit.index.tree.calculator.Calculator;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;


/**Create R*Tree in Euclidean space.
 *
 * @author Rémi Maréchal (Geomatys)
 * @author Johann Sorel  (Geomatys).
 */
public class StarRTree extends DefaultAbstractTree {

    /**
     * In accordance with R*Tree properties.
     * To avoid unnecessary split permit to 
     * reinsert some elements just one time.
     */
    boolean insertAgain = true;
    
    /**Create R*Tree.
     * 
     * @param maxElements max elements number permit by cells. 
     * @param crs  
     */
    public StarRTree(int nbMaxElement, CoordinateReferenceSystem crs, Calculator calculator) {
        super(nbMaxElement, crs, calculator);
        setRoot(new DefaultNode(this));
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void search(final GeneralEnvelope regionSearch, final List<GeneralEnvelope> result) throws TransformException{
        ArgumentChecks.ensureNonNull("search : region search", regionSearch);
        ArgumentChecks.ensureNonNull("search : result", result);
        final CoordinateReferenceSystem entryCRS = regionSearch.getCoordinateReferenceSystem();
        if(!entryCRS.equals(crs)){
            regionSearch.setEnvelope(Envelopes.transform(regionSearch, crs));
        }
        final DefaultNode root = this.getRoot();
        if(root != null){
            defaultNodeSearch(root, regionSearch, result);
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void insert(final GeneralEnvelope entry) throws TransformException{
        ArgumentChecks.ensureNonNull("insert : entry", entry);
        final CoordinateReferenceSystem entryCRS = entry.getCoordinateReferenceSystem();
        if(!entryCRS.equals(crs)){
            entry.setEnvelope(Envelopes.transform(entry, crs));
        }
        final DefaultNode root = getRoot();
        if (root.isEmpty()) {
            root.getEntries().add(entry);
        } else {
            defaultNodeInsert(root, entry);
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void delete(final GeneralEnvelope entry) throws TransformException{
        final CoordinateReferenceSystem entryCRS = entry.getCoordinateReferenceSystem();
        if(!entryCRS.equals(crs)){
            entry.setEnvelope(Envelopes.transform(entry, crs));
        }
        final DefaultNode root = getRoot();
        if (root != null) {
            deleteNode(root, entry);
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public DefaultNode createNode(final Tree tree, final DefaultNode parent, final List<DefaultNode> listChildren, final List<GeneralEnvelope> listEntries, final double... coordinates) {
        final int ddim = coordinates.length;
        if((ddim % 2) != 0){
            throw new IllegalArgumentException("coordinate dimension is not correct");
        }
        if(ddim == 0){
            return new DefaultNode(tree, parent, null, null, listChildren, listEntries);
        }
        final int dim = coordinates.length/2;
        final double[] dp1Coords = new double[dim];
        final double[] dp2Coords = new double[dim];
        System.arraycopy(coordinates, 0, dp1Coords, 0, dim);
        System.arraycopy(coordinates, dim, dp2Coords, 0, dim);
        
        final DirectPosition dp1 = new GeneralDirectPosition(crs);
        final DirectPosition dp2 = new GeneralDirectPosition(crs);
        for(int i =0; i<dim; i++){
            dp1.setOrdinate(i, dp1Coords[i]);
            dp2.setOrdinate(i, dp2Coords[i]);
        }
        return new DefaultNode(tree, parent, dp1, dp2, listChildren, listEntries);
    }
    
    /**Find all {@code GeneralEnvelope} which intersect regionSearch parameter in {@code Tree}. 
     * 
     * @param regionSearch area of search.
     * @param result {@code List} where is add search resulting.
     */
    private static void defaultNodeSearch(final DefaultNode candidate, final GeneralEnvelope regionSearch, final List<GeneralEnvelope> resultList){
        final GeneralEnvelope bound = candidate.getBoundary();
        if(bound != null){
            if(bound.intersects(regionSearch, true)){
                if(candidate.isLeaf()){
                    for(GeneralEnvelope gn : candidate.getEntries()){
                        if(gn.intersects(regionSearch, true)){
                            resultList.add(gn);
                        }
                    }
                }else{
                    for(DefaultNode child : candidate.getChildren()){
                        defaultNodeSearch(child, regionSearch, resultList);
                    }
                }
            }
        }
    }
    
    /**Insert new {@code Entry} in branch and re-organize {@code DefaultNode} if it's necessary.
     * 
     * <blockquote><font size=-1>
     * <strong>NOTE: insertion is in accordance with R*Tree properties.</strong> 
     * </font></blockquote>
     * 
     * @param candidate where to insert entry.
     * @param entry to add.
     * @throws IllegalArgumentException if {@code DefaultNode} candidate is null.
     * @throws IllegalArgumentException if {@code GeneralEnvelope} entry is null.
     */
    private static void defaultNodeInsert(final DefaultNode candidate, final GeneralEnvelope entry) throws TransformException{
        if(candidate.isLeaf()){
            candidate.getEntries().add(entry);
        }else{
            defaultNodeInsert(chooseSubTree(candidate, entry), entry);
        }
        
        final StarRTree tree = (StarRTree) candidate.getTree();
        final int maxElmts = tree.getMaxElements();
        if (DefaultTreeUtils.countElements(candidate) > maxElmts && tree.getIA()) {
            tree.setIA(false);
            final List<GeneralEnvelope> lsh30 = getElementAtMore33PerCent(candidate);
            for (GeneralEnvelope ent : lsh30) {
                deleteNode(candidate, ent);
            }
            for (GeneralEnvelope ent : lsh30) {
                tree.insert(ent);
            }
            tree.setIA(true);
        }
        
        if(!candidate.isLeaf()){
            final List<DefaultNode> children = candidate.getChildren();
            int size = children.size();
            if(children.get(0).isLeaf()){
                for(int i = 0; i<size-1; i++){
                    for(int j = i+1; j<size; j++){
                        final DefaultNode nodeA = children.get(i);
                        final DefaultNode nodeB = children.get(j);
                        if(nodeA.getBoundary().intersects(nodeB.getBoundary(), false) && nodeA.isLeaf() && nodeB.isLeaf()){
                            branchGrafting(nodeA, nodeB);
                        }
                    }
                }
            }
            
            for(int i =0; i<size; i++){
                if (DefaultTreeUtils.countElements(children.get(i)) > candidate.getTree().getMaxElements()) {
                    final DefaultNode child = children.remove(i);
                    final List<DefaultNode> l = defaultNodeSplit(child);
                    final DefaultNode l0 = l.get(0);
                    final DefaultNode l1 = l.get(1);
                    l0.setParent(candidate);
                    l1.setParent(candidate);
                    children.addAll(l);
                }
            }
        }
        
        if (candidate.getParent() == null) {
            if (DefaultTreeUtils.countElements(candidate) > candidate.getTree().getMaxElements()) {
                List<DefaultNode> l = defaultNodeSplit(candidate);
                final DefaultNode l0 = l.get(0);
                final DefaultNode l1 = l.get(1);
                l0.setParent(candidate);
                l1.setParent(candidate);
                candidate.getEntries().clear();
                candidate.getChildren().clear();
                candidate.getChildren().addAll(l);
            }
        }
    }
    
    /**Find appropriate {@code DefaultNode} to insert {@code GeneralEnvelope}.
     * <blockquote><font size=-1>
     * <strong>To define appropriate Node, R*Tree criterion are : 
     *      - require minimum area enlargement to cover GeneralEnvelope.
     *      - or put into Node with lesser elements number in case area equals.
     * </strong> 
     * </font></blockquote>
     * 
     * @param parent Find in its children {@code DefaultNode}.
     * @param entry {@code GeneralEnvelope} to add.
     * @throws IllegalArgumentException if {@code DefaultNode} listSubnode is empty.
     * @return {@code DefaultNode} which will be appropriate to contain entry.
     */
    private static DefaultNode chooseSubTree(final DefaultNode parent, final GeneralEnvelope entry) {
        
        final List<DefaultNode> childrenList = parent.getChildren();
        
        if (childrenList.isEmpty()) {
            throw new IllegalArgumentException("impossible to find subtree from empty list");
        }
        
        final int size = childrenList.size();
        if (size == 1) {
            return childrenList.get(0);
        }
        final Calculator calc = parent.getTree().getCalculator();
        if(childrenList.get(0).isLeaf()){
            final List<DefaultNode> listOverZero = new ArrayList<DefaultNode>();
            double overlapsRef = -1;
            int index = -1;
            double overlapsTemp = 0;
            for(int i = 0; i < size; i++){
                final GeneralEnvelope gnTemp = new GeneralEnvelope(childrenList.get(i).getBoundary());
                gnTemp.add(entry);
                for(int j = 0; j < size; j++){
                    if(i != j){
                        final GeneralEnvelope gET = childrenList.get(j).getBoundary();
                        overlapsTemp += calc.getOverlaps(gnTemp, gET);
                    }
                }
                if(overlapsTemp == 0){
                    listOverZero.add(childrenList.get(i));
                }else{
                    if((overlapsTemp<overlapsRef) || overlapsRef==-1){
                        overlapsRef = overlapsTemp;
                        index = i;
                    }else if(overlapsTemp == overlapsRef){
                        if(DefaultTreeUtils.countElements(childrenList.get(i))<DefaultTreeUtils.countElements(childrenList.get(index))){
                            overlapsRef = overlapsTemp;
                            index = i;
                        }
                    }
                }
                overlapsTemp = 0;
            }
            if(!listOverZero.isEmpty()){
                double areaRef = -1;
                int indexZero = -1;
                double areaTemp;
                for(int i = 0, s = listOverZero.size(); i<s;i++){
                    final GeneralEnvelope gE = new GeneralEnvelope(listOverZero.get(i).getBoundary());
                    gE.add(entry);
                    areaTemp = calc.getEdge(gE);
                    if(areaTemp<areaRef || areaRef == -1){
                        areaRef = areaTemp;
                        indexZero = i;
                    }
                }
                return listOverZero.get(indexZero);
            }
            if(index == -1){
                throw new IllegalStateException("chooseSubTree : no subLeaf find");
            }
            return childrenList.get(index);
        }
        
        for (DefaultNode no : childrenList) {
            final GeneralEnvelope ge = no.getBoundary();
            if (ge.contains(entry, true)) {
                return no;
            }
        }
        
        double enlargRef = -1;
        int indexEnlarg = -1;
        for(int i = 0, s = childrenList.size(); i<s;i++){
            final DefaultNode n3d = childrenList.get(i);
            final GeneralEnvelope gEN = n3d.getBoundary();
            final GeneralEnvelope GE = new GeneralEnvelope(gEN);
            GE.add(entry);
            double enlargTemp = calc.getEnlargement(gEN, GE);
            if(enlargTemp<enlargRef || enlargRef == -1){
                enlargRef = enlargTemp;
                indexEnlarg = i;
            }
        }
        return childrenList.get(indexEnlarg); 
    }
    
    /**Compute and define how to split {@code Node} candidate.
     * 
     * <blockquote><font size=-1>
     * <strong>NOTE: To choose which {@code Node} couple, split algorithm sorts the entries (for tree leaf), or {@code Node} (for tree branch) 
     *               in accordance to their lower or upper boundaries on the selected dimension (see defineSplitAxis method) and examines all possible divisions.
     *               Two {@code Node} resulting, is the final division which has the minimum overlaps between them.</strong> 
     * </font></blockquote>
     * 
     * @return Two appropriate {@code DefaultNode} in List in accordance with R*Tree split properties.
     */
    private static List<DefaultNode> defaultNodeSplit(final DefaultNode candidate){
        
        final int splitIndex = defineSplitAxis(candidate);
        final boolean isLeaf = candidate.isLeaf();
        final Tree tree = candidate.getTree();
        final Calculator calc = tree.getCalculator();
        List eltList;
        if(isLeaf){
            eltList = candidate.getEntries(); 
        }else{
            eltList = candidate.getChildren();
        }
        final int size = eltList.size();
        final double size04 = size*0.4;
        final int demiSize = (int) ((size04>=1)?size04:1);
        final List splitListA = new ArrayList();
        final List splitListB = new ArrayList();
        final List<CoupleGE> listCGE = new ArrayList<CoupleGE>();
        GeneralEnvelope gESPLA, gESPLB;
        double bulkTemp;
        double bulkRef = -1;
        CoupleGE coupleGE;
        int index = 0;
        int lower_or_upper = 0;
        int cut2;
        Comparator comp;
        for(int lu = 0; lu<2; lu++){
            if(isLeaf){
            if(lu == 0){
                    comp = calc.sortFrom(splitIndex, true, false);
                }else{
                    comp = calc.sortFrom(splitIndex, false, false);
                }
            }else{
                if(lu == 0){
                    comp = calc.sortFrom(splitIndex, true, true);
                }else{
                    comp = calc.sortFrom(splitIndex, false, true);
                }
            }
            Collections.sort(eltList, comp);
            
            for(int cut = demiSize; cut<=size - demiSize;cut++){
                for(int i = 0;i<cut;i++){
                    splitListA.add(eltList.get(i));
                }
                for(int j = cut;j<size;j++){
                    splitListB.add(eltList.get(j));
                }
                cut2 = size - cut;
                if(isLeaf){
                    gESPLA = new GeneralEnvelope((GeneralEnvelope)splitListA.get(0));
                    gESPLB = new GeneralEnvelope((GeneralEnvelope)splitListB.get(0));
                    for(int i = 1; i<cut;i++){
                        gESPLA.add((GeneralEnvelope)splitListA.get(i));
                    }
                    for(int i = 1; i<cut2;i++){
                        gESPLB.add((GeneralEnvelope)splitListB.get(i));
                    }
                }else{
                    gESPLA = new GeneralEnvelope(((DefaultNode)splitListA.get(0)).getBoundary());
                    gESPLB = new GeneralEnvelope(((DefaultNode)splitListB.get(0)).getBoundary());
                    for(int i = 1; i<cut;i++){
                        gESPLA.add(((DefaultNode)splitListA.get(i)).getBoundary());
                    }
                    for(int i = 1; i<cut2;i++){
                        gESPLB.add(((DefaultNode)splitListB.get(i)).getBoundary());
                    }
                }
                coupleGE = new CoupleGE(gESPLA, gESPLB, calc);
                bulkTemp = coupleGE.getOverlaps();
                if(bulkTemp <bulkRef||bulkRef==-1){
                    bulkRef = bulkTemp;
                    index = cut;
                    lower_or_upper = lu;
                }else if(bulkTemp == 0){
                    coupleGE.setUserProperty("cut", cut);
                    coupleGE.setUserProperty("lower_or_upper", lu);
                    listCGE.add(coupleGE);
                }
                splitListA.clear();
                splitListB.clear();
            }
        }
          
        if(!listCGE.isEmpty()){
            double areaRef = -1;
            double areaTemp;
            for(CoupleGE cge : listCGE){
                areaTemp = cge.getEdge();
                if(areaTemp<areaRef || areaRef == -1){
                    areaRef = areaTemp;
                    index = (Integer) cge.getUserProperty("cut");
                    lower_or_upper = (Integer) cge.getUserProperty("lower_or_upper");
                }
            }
        }
        
        if(isLeaf){
            if(lower_or_upper == 0){
                comp = calc.sortFrom(splitIndex, true, false);
            }else{
                comp = calc.sortFrom(splitIndex, false, false);
            }
        }else{
            if(lower_or_upper == 0){
                comp = calc.sortFrom(splitIndex, true, true);
            }else{
                comp = calc.sortFrom(splitIndex, false, true);
            }
        }
        Collections.sort(eltList, comp);
        
        for(int i = 0;i<index;i++){
            splitListA.add(eltList.get(i));
        }
        for(int i =index; i<size; i++){
            splitListB.add(eltList.get(i));
        }
        
        if(isLeaf){
            return UnmodifiableArrayList.wrap((DefaultNode)tree.createNode(tree, null, null, splitListA),
                                              (DefaultNode)tree.createNode(tree, null, null, splitListB));
        }else{
            final DefaultNode resultA = (DefaultNode) ((splitListA.size() == 1)?splitListA.get(0):tree.createNode(tree, null, splitListA, null));
            final DefaultNode resultB = (DefaultNode) ((splitListB.size() == 1)?splitListB.get(0):tree.createNode(tree, null, splitListB, null));
            return UnmodifiableArrayList.wrap(resultA, resultB);
        }
    }
    
    /**Compute and define which axis to split {@code Node} candidate.
     * 
     * <blockquote><font size=-1>
     * <strong>NOTE: Define split axis method decides a split axis among all dimensions.
     *               The choosen axis is the one with smallest overall perimeter or area (in fonction with dimension size).
     *               It work by sorting all entry or {@code Node}, from their left boundary coordinates. 
     *               Then it considers every divisions of the sorted list that ensure each node is at least 40% full.
     *               The algorithm compute perimeters or area of two result {@code Node} from every division.
     *               A second pass repeat this process with respect their right boundary coordinates. 
     *               Finally the overall perimeter or area on one axis is the som of all perimeter or area obtained from the two pass.</strong> 
     * </font></blockquote>
     * 
     * @throws IllegalArgumentException if candidate is null.
     * @return prefered ordinate index to split.
     */
    private static int defineSplitAxis(final DefaultNode candidate){
        ArgumentChecks.ensureNonNull("defineSplitAxis : ", candidate);
        final boolean isLeaf = candidate.isLeaf();
        List eltList;
        if(isLeaf){
            eltList = candidate.getEntries(); 
        }else{
            eltList = candidate.getChildren();
        }
        final Calculator calc = candidate.getTree().getCalculator();
        final int dim = candidate.getBoundary().getDimension();
        final int size = eltList.size();
        final double size04 = size*0.4;
        final int demiSize = (int) ((size04>=1)?size04:1);
        final List splitListA = new ArrayList();
        final List splitListB = new ArrayList();
        GeneralEnvelope gESPLA, gESPLB;
        double bulkTemp, bulkRef = -1;
        int index = 0;
        int cut2;
        Comparator comp;
        for(int indOrg=0;indOrg<dim;indOrg++){
            bulkTemp = 0;
            for(int left_or_right = 0;left_or_right<2;left_or_right++){
                if(isLeaf){
                    if(left_or_right == 0){
                        comp = calc.sortFrom(indOrg, true, false);
                    }else{
                        comp = calc.sortFrom(indOrg, false, false);
                    }
                }else{
                    if(left_or_right == 0){
                        comp = calc.sortFrom(indOrg, true, true);
                    }else{
                        comp = calc.sortFrom(indOrg, false, true);
                    }
                }
                Collections.sort(eltList, comp);
                for(int cut = demiSize, sdem = size - demiSize; cut<=sdem;cut++){
                    splitListA.clear();
                    splitListB.clear();
                    for(int i = 0;i<cut;i++){
                        splitListA.add(eltList.get(i));
                    }
                    for(int j = cut;j<size;j++){
                        splitListB.add(eltList.get(j));
                    }
                    cut2 = size - cut;
                    if(isLeaf){
                        gESPLA = new GeneralEnvelope((GeneralEnvelope)splitListA.get(0));
                        gESPLB = new GeneralEnvelope((GeneralEnvelope)splitListB.get(0));
                        for(int i = 1; i<cut;i++){
                            gESPLA.add((GeneralEnvelope)splitListA.get(i));
                        }
                        for(int i = 1; i<cut2;i++){
                            gESPLB.add((GeneralEnvelope)splitListB.get(i));
                        }
                    }else{
                        gESPLA = new GeneralEnvelope(((DefaultNode)splitListA.get(0)).getBoundary());
                        gESPLB = new GeneralEnvelope(((DefaultNode)splitListB.get(0)).getBoundary());
                        for(int i = 1; i<cut;i++){
                            gESPLA.add(((DefaultNode)splitListA.get(i)).getBoundary());
                        }
                        for(int i = 1; i<cut2;i++){
                            gESPLB.add(((DefaultNode)splitListB.get(i)).getBoundary());
                        }
                    }
                    bulkTemp += calc.getEdge(gESPLA);
                    bulkTemp += calc.getEdge(gESPLB);
                }
            }
            if(indOrg == 0){
                bulkRef = bulkTemp;
                index = indOrg;
            }else{
                if(bulkTemp < bulkRef){
                    bulkRef = bulkTemp;
                    index = indOrg;
                }
            }
        }
        return index;
    }
        
    /**
     * Travel {@code Tree}, find {@code Entry} if it exist and delete it.
     * 
     * <blockquote><font size=-1>
     * <strong>NOTE: Moreover {@code Tree} is condensate after a deletion to stay conform about R-Tree properties.</strong> 
     * </font></blockquote>
     * 
     * @param candidate {@code DefaultNode}  where to delete.
     * @param entry {@code GeneralEnvelope} to delete.
     * @throws IllegalArgumentException if candidate or entry is null.
     * @return true if entry is find and deleted else false.
     */
    private static boolean deleteNode(final DefaultNode candidate, final GeneralEnvelope entry) throws TransformException{
        ArgumentChecks.ensureNonNull("DeleteNode3D : Node3D candidate", candidate);
        ArgumentChecks.ensureNonNull("DeleteNode3D : Node3D candidate", candidate);
        if(candidate.getBoundary().intersects(entry, true)){
            if(candidate.isLeaf()){
                final boolean removed = candidate.getEntries().remove(entry);
                if(removed){
                    trim(candidate);
                    return true;
                }
            }else{
                for(DefaultNode no : candidate.getChildren()){
                    final boolean removed = deleteNode(no, entry);
                    if(removed){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**Condense R-Tree.
     * 
     * Condense made, travel up from leaf to tree trunk.
     * 
     * @param candidate {@code DefaultNode} to begin condense.
     * @throws IllegalArgumentException if candidate is null.
     */
    private static void trim(final DefaultNode candidate) throws TransformException {
        ArgumentChecks.ensureNonNull("trim : Node3D candidate", candidate);
        final List<DefaultNode> children = candidate.getChildren();
        final Tree tree = candidate.getTree();
        final List<GeneralEnvelope> reinsertList = new ArrayList<GeneralEnvelope>();
        for(int i = children.size()-1;i>=0;i--){
            final DefaultNode child = children.get(i);
            if(child.isEmpty()){
                children.remove(i);
            }else if(child.getChildren().size() ==1){
                final DefaultNode n = children.remove(i);
                for(DefaultNode n2d : n.getChildren()){
                    n2d.setParent(candidate);
                }
                children.addAll(n.getChildren());
            }else if(child.isLeaf()&&child.getEntries().size()<=tree.getMaxElements()/3){
                reinsertList.addAll(child.getEntries());
                children.remove(i);
            }
        }
        if(candidate.getParent()!=null){
            trim(candidate.getParent());
        }
        for(GeneralEnvelope ent : reinsertList){
            tree.insert(ent);
        }
    }
    
    /**
     * Exchange some entry(ies) between two nodes in aim to find best form with lesser overlaps.
     * Also branchGrafting will be able to avoid splitting node.
     * 
     * @param nodeA DefaultNode
     * @param nodeB DefaultNode
     * @throws IllegalArgumentException if nodeA or nodeB are not tree leaf.
     * @throws IllegalArgumentException if nodeA or nodeB, and their subnodes, don't contains some {@code Entry}.
     */
    private static void branchGrafting(final DefaultNode nodeA, final DefaultNode nodeB ) throws TransformException{
        if(!nodeA.isLeaf() || !nodeB.isLeaf()){
            throw new IllegalArgumentException("branchGrafting : not leaf");
        }
        final List<GeneralEnvelope> listGlobale = new ArrayList<GeneralEnvelope>(nodeA.getEntries());
        listGlobale.addAll(new ArrayList<GeneralEnvelope>(nodeB.getEntries()));
        nodeA.getEntries().clear();
        nodeB.getEntries().clear();
        if(listGlobale.isEmpty()){
            throw new IllegalArgumentException("branchGrafting : empty list");
        }
        final Calculator calc = nodeA.getTree().getCalculator();
        final GeneralEnvelope globalE = new GeneralEnvelope(listGlobale.get(0));
        final int size = listGlobale.size();
        for(int i = 1;i<size; i++){
            globalE.add(listGlobale.get(i));
        }
        double lengthDimRef = -1;
        int indexSplit = -1;
        for(int i = 0, dim = globalE.getDimension(); i<dim; i++){
            double lengthDimTemp = globalE.getSpan(i);
            if(lengthDimTemp>lengthDimRef){
                lengthDimRef = lengthDimTemp;
                indexSplit = i;
            }
        }
        final Comparator comp = calc.sortFrom(indexSplit, true, false);
        Collections.sort(listGlobale, comp);
        GeneralEnvelope envB;
        final GeneralEnvelope envA = new GeneralEnvelope(listGlobale.get(0));
        double overLapsRef = -1;
        int index =-1;
        final int size04 = (int)((size*0.4 >= 1) ? size*0.4 : 1);
        for(int cut = size04; cut < size-size04; cut++){
            for(int i = 1; i<cut;i++){
                envA.add(listGlobale.get(i));
            }
            envB = new GeneralEnvelope(listGlobale.get(cut));
            for(int i = cut+1; i<size;i++){
                envB.add(listGlobale.get(i));
            }
            double overLapsTemp = calc.getOverlaps(envA, envB);
            if(overLapsTemp < overLapsRef || overLapsRef == -1){
                overLapsRef = overLapsTemp;
                index = cut;
            }
        }
        for(int i = 0; i<index;i++){
            defaultNodeInsert(nodeA, listGlobale.get(i));
        }
        for(int i = index; i<size;i++){
            defaultNodeInsert(nodeB, listGlobale.get(i));
        }
    }
    
    /**Get statement from re-insert state.
     * 
     * @return true if it's permit to re-insert else false.
     */
    private boolean getIA() {
        return insertAgain;
    }

    /**Affect statement to permit or not, re-insertion.
     * @param insertAgain
     */
    private void setIA(boolean insertAgain) {
        this.insertAgain = insertAgain;
    }
    
    /**Recover lesser 33% largest of {@code DefaultNode} candidate within it.
     * 
     * @throws IllegalArgumentException if {@code Node2D} candidate is null.
     * @return all Entry within subNodes at more 33% largest of {@code this Node}.
     */
    private static List<GeneralEnvelope> getElementAtMore33PerCent(final DefaultNode candidate) {
        ArgumentChecks.ensureNonNull("getElementAtMore33PerCent : candidate", candidate);
        final Calculator calc = candidate.getTree().getCalculator();
        final List<GeneralEnvelope> lsh = new ArrayList<GeneralEnvelope>();
        final GeneralEnvelope boundGE = candidate.getBoundary();
        final DirectPosition candidateCentroid = boundGE.getMedian();
        final double distPermit = calc.getDistance(boundGE.getLowerCorner(), boundGE.getUpperCorner()) / 1.666666666;
        defaultNodeSearch(candidate, boundGE, lsh);
        for (int i = lsh.size() - 1; i >= 0; i--) {
            if (calc.getDistance(candidateCentroid, lsh.get(i).getMedian()) < distPermit) {
                lsh.remove(i);
            }
        }
        return lsh;
    }
}
