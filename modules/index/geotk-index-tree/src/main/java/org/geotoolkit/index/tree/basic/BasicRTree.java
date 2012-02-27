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
package org.geotoolkit.index.tree.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.index.tree.DefaultAbstractTree;
import org.geotoolkit.index.tree.DefaultNode;
import org.geotoolkit.index.tree.DefaultTreeUtils;
import org.geotoolkit.index.tree.Tree;
import org.geotoolkit.index.tree.calculator.Calculator;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**Create R-Tree (Basic)
 *
 * @author Rémi Marechal (Geomatys)
 * @author Yohann Sorel (Geomatys)
 */
public class BasicRTree extends DefaultAbstractTree {

    private SplitCase choice;

    /**Create R-Tree.
     * 
     * @param maxElements max value of elements per tree cell.
     * @param choice Split made "linear" or "quadratic".
     */
    public BasicRTree(final int maxElements, CoordinateReferenceSystem crs, final SplitCase choice, Calculator calculator) {
        super(maxElements, crs, calculator);
        this.choice = choice;
        setRoot(new DefaultNode(this));
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void search(final GeneralEnvelope regionSearch, final List<GeneralEnvelope> result) {
        final DefaultNode root = getRoot();
        if (root != null) {
            defaultNodeSearch(root, regionSearch, result);
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void insert(final GeneralEnvelope entry) throws TransformException {
        final DefaultNode root = getRoot();
        if (root != null) {
            if (root.isEmpty()) {
                root.getEntries().add(entry);
            } else {
                defaultNodeInsert(root, entry);
            }
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void delete(final GeneralEnvelope entry) throws TransformException {
        final DefaultNode root = getRoot();
        if (root != null) {
            deleteNode(root, entry);
        }
    }

    /**
     * @return split case chosen to split. 
     */
    public SplitCase getSplitCase() {
        return this.choice;
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
            defaultNodeInsert(chooseSubtree(candidate, entry), entry);
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
                    final List<DefaultNode> l = splitNode(child);
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
                List<DefaultNode> l = splitNode(candidate);
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

    /**
     * Exchange some entry(ies) between two nodes in aim to find best form with lesser overlaps.
     * Also branchGrafting will be able to avoid splitting node.
     * 
     * @param nodeA DefaultNode
     * @param nodeB DefaultNode
     * @throws IllegalArgumentException if nodeA or nodeB are null.
     * @throws IllegalArgumentException if nodeA and nodeB have different "parent".
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
        
        final Calculator calc = nodeA.getTree().getCalculator();
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
    
    /**
     * Split a overflow {@code DefaultNode} in accordance with R-Tree properties.
     * 
     * @param candidate {@code DefaultNode} to Split.
     * @throws IllegalArgumentException if candidate is null.
     * @throws IllegalArgumentException if candidate elements number is lesser 2.
     * @return {@code DefaultNode} List which contains two {@code DefaultNode} (split result of candidate).
     */
    private static List<DefaultNode> splitNode(final DefaultNode candidate) {
        ArgumentChecks.ensureNonNull("splitNode : candidate", candidate);
        if (DefaultTreeUtils.countElements(candidate) < 2) {
            throw new IllegalArgumentException("not enought elements within " + candidate + " to split.");
        }
        
        final int dim = candidate.getBoundary().getDimension();
        final Tree tree = candidate.getTree();
        Calculator calc = tree.getCalculator();
        final int maxElmnts = tree.getMaxElements();
        boolean leaf = candidate.isLeaf();
        List<?> ls;
        Object s1, s2;

        if (leaf) {
            ls = candidate.getEntries();
            s1 = ls.get(0);
            s2 = ls.get(1);
        } else {
            ls = candidate.getChildren();
            s1 = ls.get(0);
            s2 = ls.get(1);
        }

        double refValue = 0;
        double tempValue;
        int index1 = 0;
        int index2 = 0;

        switch (((BasicRTree) tree).getSplitCase()) {
            case LINEAR: {
                for (int i = 0; i < ls.size() - 1; i++) {
                    for (int j = i + 1; j < ls.size(); j++) {
                        tempValue = (leaf) ? calc.getDistance(((GeneralEnvelope) ls.get(i)), ((GeneralEnvelope) ls.get(j)))
                                           : calc.getDistance(((DefaultNode) ls.get(i)).getBoundary(), ((DefaultNode) ls.get(j)).getBoundary());
                        if (tempValue > refValue) {
                            s1 = ls.get(i);
                            s2 = ls.get(j);
                            index1 = i;
                            index2 = j;
                            refValue = tempValue;
                        }
                    }
                }
            }
            break;

            case QUADRATIC: {
                GeneralEnvelope rectGlobal, bound1, bound2;
                for (int i = 0; i < ls.size() - 1; i++) {
                    for (int j = i + 1; j < ls.size(); j++) {
                        if (leaf) {
                            bound1 = ((GeneralEnvelope) ls.get(i));
                            bound2 = ((GeneralEnvelope) ls.get(j));
                            rectGlobal = DefaultTreeUtils.getEnveloppeMin(UnmodifiableArrayList.wrap((GeneralEnvelope) bound1, (GeneralEnvelope) bound2));

                        } else {
                            bound1 = ((DefaultNode) ls.get(i)).getBoundary();
                            bound2 = ((DefaultNode) ls.get(j)).getBoundary();
                            rectGlobal = DefaultTreeUtils.getEnveloppeMin(UnmodifiableArrayList.wrap((GeneralEnvelope) bound1, bound2));

                        }
                        tempValue = calc.getSpace(rectGlobal)-calc.getSpace(bound1)-calc.getSpace(bound2);
                        if (tempValue > refValue) {
                            s1 = ls.get(i);
                            s2 = ls.get(j);
                            index1 = i;
                            index2 = j;
                            refValue = tempValue;
                        }
                    }
                }
            }
            break;
        }

        ls.remove(Math.max(index1, index2));
        ls.remove(Math.min(index1, index2));
        GeneralEnvelope r1Temp, r2Temp;
        GeneralEnvelope boundS1, boundS2;
        DefaultNode result1, result2;
        
        if(leaf){
            boundS1 = ((GeneralEnvelope)s1);
            boundS2 = ((GeneralEnvelope)s2);
        }else{
            boundS1 = ((DefaultNode)s1).getBoundary();
            boundS2 = ((DefaultNode)s2).getBoundary();
        }
        double[] tabS1 = new double[2*dim];
        double[] tabS2 = new double[2*dim];
        for(int i = 0; i<dim;i++){
            tabS1[i] = boundS1.getLower(i);
            tabS2[i] = boundS2.getLower(i);
        }
        for(int i = 0; i<dim;i++){
            tabS1[i] = boundS1.getUpper(i);
            tabS2[i] = boundS2.getUpper(i);
        }
        
        if(leaf){
            result1 = (DefaultNode)tree.createNode(tree, null, null, UnmodifiableArrayList.wrap((GeneralEnvelope) s1), tabS1); 
            result2 = (DefaultNode)tree.createNode(tree, null, null, UnmodifiableArrayList.wrap((GeneralEnvelope) s2), tabS2);
        }else{
            result1 = (DefaultNode)tree.createNode(tree, null, UnmodifiableArrayList.wrap((DefaultNode) s1), null, tabS1); 
            result2 = (DefaultNode)tree.createNode(tree, null, UnmodifiableArrayList.wrap((DefaultNode) s2), null, tabS2);
        }
        
        double demimaxE = maxElmnts / 3;
        demimaxE = Math.max(demimaxE, 1);
        
        for (Object ent : ls) {
            r1Temp = (leaf) ? DefaultTreeUtils.getEnveloppeMin(UnmodifiableArrayList.wrap((GeneralEnvelope) s1, (GeneralEnvelope)ent))
                            : DefaultTreeUtils.getEnveloppeMin(UnmodifiableArrayList.wrap(((DefaultNode) s1).getBoundary(), ((DefaultNode)ent).getBoundary()));
            r2Temp = (leaf) ? DefaultTreeUtils.getEnveloppeMin(UnmodifiableArrayList.wrap((GeneralEnvelope)s2, (GeneralEnvelope)ent))
                            : DefaultTreeUtils.getEnveloppeMin(UnmodifiableArrayList.wrap(((DefaultNode) s2).getBoundary(), ((DefaultNode)ent).getBoundary()));


            double area1 = calc.getSpace(r1Temp);
            double area2 = calc.getSpace(r2Temp);
            int r1nbE = DefaultTreeUtils.countElements(result1);
            int r2nbE = DefaultTreeUtils.countElements(result2);
            if (area1 < area2) {
                if (r1nbE <= demimaxE && r2nbE > demimaxE) {
                    if(leaf){
                        result1.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result1.getChildren().add((DefaultNode)ent);
                    }

                } else if (r2nbE <= demimaxE && r1nbE > demimaxE) {
                    if(leaf){
                        result2.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result2.getChildren().add((DefaultNode)ent);
                    }
                } else {
                    if(leaf){
                        result1.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result1.getChildren().add((DefaultNode)ent);
                    }
                }
            } else if (area1 == area2) {
                if (r1nbE < r2nbE) {
                    if(leaf){
                        result1.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result1.getChildren().add((DefaultNode)ent);
                    }
                } else {
                    if(leaf){
                        result2.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result2.getChildren().add((DefaultNode)ent);
                    }
                }
            } else {
                if (r1nbE <= demimaxE && r2nbE > demimaxE) {
                    if(leaf){
                        result1.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result1.getChildren().add((DefaultNode)ent);
                    }
                } else if (r2nbE <= demimaxE && r1nbE > demimaxE) {
                    if(leaf){
                        result2.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result2.getChildren().add((DefaultNode)ent);
                    }
                } else {
                    if(leaf){
                        result2.getEntries().add((GeneralEnvelope)ent);
                    }else{
                        result2.getChildren().add((DefaultNode)ent);
                    }
                }
            }
        }
        
        if(!leaf){
            final List<DefaultNode> lR1 = result1.getChildren();
            if(lR1.size() == 1){
                result1 = lR1.get(0);
                result1.setParent(null);
            }
            final List<DefaultNode> lR2 = result2.getChildren();
            if(lR2.size() == 1){
                result2 = lR2.get(0);
                result2.setParent(null);
            }
            for (DefaultNode nod2d : lR1) {
                nod2d.setParent(result1);
            }
            for (DefaultNode nod2d : lR2) {
                nod2d.setParent(result2);
            }
        }
        return UnmodifiableArrayList.wrap(result1, result2);
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
     * Find appropriate {@code DefaultNode} to insert {@code GeneralEnvelope}.
     * To define appropriate Node, criterion are : 
     *      - require minimum area enlargement to cover shape.
     *      - or put into {@code DefaultNode} with lesser elements number in case of area equals.
     * 
     * @param children List of {@code DefaultNode}.
     * @param entry {@code GeneralEnvelope} to add.
     * @throws IllegalArgumentException if children or entry are null.
     * @throws IllegalArgumentException if children is empty.
     * @return {@code DefaultNode} which is appropriate to contain shape.
     */
    private static DefaultNode chooseSubtree(final DefaultNode candidate, final GeneralEnvelope entry) {
        ArgumentChecks.ensureNonNull("chooseSubtree : candidate", candidate);
        ArgumentChecks.ensureNonNull("chooseSubtree : GeneralEnvelope entry", entry);
        final Calculator calc = candidate.getTree().getCalculator();
        final List<DefaultNode> children = candidate.getChildren();
        if (children.isEmpty()) {
            throw new IllegalArgumentException("chooseSubtree : ln is empty");
        }

        if (children.size() == 1) {
            return children.get(0);
        }

        DefaultNode n = children.get(0);

        for (DefaultNode nod : children) {
            if (nod.getBoundary().contains(entry, true)) {
                return nod;
            }
        }

        final List<GeneralEnvelope> lGE = new ArrayList<GeneralEnvelope>();
        for(DefaultNode dn : children){
            lGE.add(dn.getBoundary());
        }
        double area = calc.getSpace(DefaultTreeUtils.getEnveloppeMin(lGE));
        double nbElmt = DefaultTreeUtils.countElements(n);
        double areaTemp;
        for (DefaultNode dn : children) {
            final GeneralEnvelope rnod = new GeneralEnvelope(dn.getBoundary());
            rnod.add(entry);
            final int nbe = DefaultTreeUtils.countElements(dn);
            areaTemp = calc.getEnlargement(dn.getBoundary(), rnod);
            if (areaTemp < area) {
                n = dn;
                area = areaTemp;
                nbElmt = nbe;
            } else if (areaTemp== area) {
                if (nbe < nbElmt) {
                    n = dn;
                    area = areaTemp;
                    nbElmt = nbe;
                }
            }
        }
        return n;
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
        
        final DirectPosition dp1, dp2;
        if(crs != null){
            dp1 = new GeneralDirectPosition(crs);
            dp2 = new GeneralDirectPosition(crs);
            for(int i =0; i<dim; i++){
                dp1.setOrdinate(i, dp1Coords[i]);
                dp2.setOrdinate(i, dp2Coords[i]);
            }
        }else{
            dp1 = new GeneralDirectPosition(dp1Coords);
            dp2 = new GeneralDirectPosition(dp2Coords);
        }
        return new DefaultNode(tree, parent, dp1, dp2, listChildren, listEntries);
    }
}