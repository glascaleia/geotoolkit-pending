/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotoolkit.index.tree;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.geotoolkit.util.ArgumentChecks;

/**
 * Test some queries.
 *
 * @author Rémi Marechal (Geomatys).
 */
public abstract class TreeTest extends TestCase{
    protected final Tree tree;
    protected final List<Shape> lData = new ArrayList<Shape>();
    
    public TreeTest(Tree tree) {
        ArgumentChecks.ensureNonNull("tree", tree);
        this.tree = tree;
        for(int j= -120;j<=120;j+=4){
            for(int i = -200;i<=200;i+=4){
                lData.add(new Ellipse2D.Double(i, j, 1, 1));
            }
        }
        Collections.shuffle(lData);
        insert();
    }
    
    private void insert(){
        for(Shape shape : lData){
            tree.insert(shape);
        }
    }
    
    /**
     * Test if tree contain all elements inserted.
     */
    protected void insertTest(){
        assertTrue(tree.getRoot().getBoundary().getBounds2D().equals(TreeUtils.getEnveloppeMin(lData)));
        System.out.println(tree.getRoot().getBoundary().getBounds2D());
        List<Shape> listSearch = new ArrayList<Shape>();
        tree.search(tree.getRoot().getBoundary(), listSearch);
        System.out.println("listS.size : "+listSearch.size());
        System.out.println("lData.size : "+lData.size());
        assertTrue(listSearch.size() == lData.size());
    }
    
    /**
     * Test search query inside tree.
     */
    protected void queryInsideTest(){
        
        List<Shape> listRef = new ArrayList<Shape>();
        for(int j = 20; j<50;j+=4){
            for(int i = -48;i<=20;i+=4){
                listRef.add(new Ellipse2D.Double(i, j, 1, 1));
            }
        }
        List<Shape> listSearch = new ArrayList<Shape>();
        tree.search(TreeUtils.getEnveloppeMin(listRef), listSearch);
        assertTrue(compareList(listRef, listSearch));
        
    }
    
    /**
     * Test query outside of tree area.
     */
    protected void queryOutsideTest(){
        Rectangle2D areaSearch = new Rectangle2D.Double(202, 20, 70, 30);
        List<Shape> listSearch = new ArrayList<Shape>();
        tree.search(areaSearch, listSearch);
        assertTrue(listSearch.isEmpty());
    }
    
    /**
     * Test query on tree boundary border. 
     */
    protected void queryOnBorderTest(){
        Rectangle2D areaSearch = new Rectangle2D.Double(150, 80, 100, 80);
        List<Shape> listSearch = new ArrayList<Shape>();
        tree.search(areaSearch, listSearch);
        
        List<Shape> listRef = new ArrayList<Shape>();
        for(int j = 80; j<=120;j+=4){
            for(int i = 152;i<=200;i+=4){
                listRef.add(new Ellipse2D.Double(i, j, 1, 1));
            }
        }
        
        assertTrue(compareList(listRef, listSearch));
    }
    
    /**
     * Test query with search area contain all tree boundary. 
     */
    protected void queryAllTest(){
        Rectangle2D areaSearch = new Rectangle2D.Double(-250, -150, 500, 300);
        List<Shape> listSearch = new ArrayList<Shape>();
        tree.search(areaSearch, listSearch);
        assertTrue(compareList(lData, listSearch));
    }
    
    /**
     * Test insertion and deletion in tree.
     */
    protected void insertDelete(){
        for(int j= -120;j<=120;j+=4){
            for(int i = -200;i<=200;i+=4){
                tree.delete(new Ellipse2D.Double(i, j, 1, 1));
            }
        }
        
        final List<Shape> listSearch = new ArrayList<Shape>();
        tree.search(new Rectangle2D.Double(-200, -120, 400, 240), listSearch);
        assertTrue(listSearch.isEmpty());
        System.out.println("boundary imaginaire : "+tree.getRoot().getBoundary());
//        assertTrue(tree.getRoot().getBoundary() == null);
        
        final List<Shape> listToAdd = new ArrayList<Shape>();
        
        final Shape s1  = new Ellipse2D.Double(-60, -21, 5, 5);
        final Shape s2  = new Ellipse2D.Double(-60, 0, 5, 5);
        final Shape s3  = new Ellipse2D.Double(-60, 21, 5, 5);
        final Shape s4  = new Ellipse2D.Double(-60, 45, 5, 5);
        final Shape s5  = new Ellipse2D.Double(-60, 60, 5, 5);
        final Shape s6  = new Ellipse2D.Double(-45, 60, 5, 5);
        final Shape s7  = new Ellipse2D.Double(-21, 60, 5, 5);
        final Shape s8  = new Ellipse2D.Double(0, 60, 5, 5);
        final Shape s9  = new Ellipse2D.Double(21, 60, 5, 5);
        final Shape s10 = new Ellipse2D.Double(45, 60, 5, 5);
        final Shape s11 = new Ellipse2D.Double(60, 60, 5, 5);
        final Shape s12 = new Ellipse2D.Double(60, 45, 5, 5);
        final Shape s13 = new Ellipse2D.Double(60, 21, 5, 5);
        final Shape s14 = new Ellipse2D.Double(60, 0, 5, 5);
        final Shape s15 = new Ellipse2D.Double(60, -21, 5, 5);
        final Shape s16 = new Ellipse2D.Double(60, -45, 5, 5);
        final Shape s17 = new Ellipse2D.Double(60, -60, 5, 5);
        final Shape s18 = new Ellipse2D.Double(45, -60, 5, 5);
        final Shape s19 = new Ellipse2D.Double(21, -60, 5, 5);
        final Shape s20 = new Ellipse2D.Double(0, -60, 5, 5);
        final Shape s21 = new Ellipse2D.Double(-21, -60, 5, 5);
        final Shape s22 = new Ellipse2D.Double(-21, 45, 5, 5);
        final Shape s23 = new Ellipse2D.Double(-21, -21, 5, 5);
        final Shape s24 = new Ellipse2D.Double(-21, 0, 5, 5);
        final Shape s25 = new Ellipse2D.Double(-21, 21, 5, 5);
        final Shape s26 = new Ellipse2D.Double(0, 21, 5, 5);
        final Shape s27 = new Ellipse2D.Double(21, 21, 5, 5);
        final Shape s28 = new Ellipse2D.Double(21, 0, 5, 5);
        final Shape s29 = new Ellipse2D.Double(21, -21, 5, 5);
        final Shape s30 = new Ellipse2D.Double(0, -21, 5, 5);
        final Shape s31 = new Ellipse2D.Double(0, 0, 5, 5);
        final Shape s32 = new Ellipse2D.Double(-60, -45, 5, 5);
        listToAdd.add(s1); listToAdd.add(s2); listToAdd.add(s3); listToAdd.add(s4);
        listToAdd.add(s5); listToAdd.add(s6); listToAdd.add(s7); listToAdd.add(s8);
        listToAdd.add(s9); listToAdd.add(s10);listToAdd.add(s11);listToAdd.add(s12);
        listToAdd.add(s13);listToAdd.add(s14);listToAdd.add(s15);listToAdd.add(s16);
        listToAdd.add(s17);listToAdd.add(s18);listToAdd.add(s19);listToAdd.add(s20);
        listToAdd.add(s24);listToAdd.add(s23);listToAdd.add(s22);listToAdd.add(s21);
        listToAdd.add(s25);listToAdd.add(s26);listToAdd.add(s27);listToAdd.add(s28);
        listToAdd.add(s29);listToAdd.add(s30);listToAdd.add(s31);listToAdd.add(s32);
        insert();
        for(Shape sh : listToAdd){
            tree.insert(sh);
        }
        for(Shape sh : lData){
            tree.delete(sh);
        }
        
        listSearch.clear();
        tree.search(tree.getRoot().getBoundary(), listSearch);
        System.out.println("listS.size : "+listSearch.size());
        System.out.println("lData.size : "+lData.size());
        assertTrue(compareList(listSearch, listToAdd));
    }
    
    /**
     * Compare 2 lists elements.
     * 
     * <blockquote><font size=-1>
     * <strong>NOTE: return {@code true} if listA and listB are empty.</strong> 
     * </font></blockquote>
     * 
     * @param listA
     * @param listB
     * @throws IllegalArgumentException if listA or ListB is null.
     * @return true if listA contains same elements from listB.
     */
    protected boolean compareList(final List<Shape> listA, final List<Shape>listB){
        ArgumentChecks.ensureNonNull("compareList : listA", listA);
        ArgumentChecks.ensureNonNull("compareList : listB", listB);
        
        if(listA.isEmpty()&&listB.isEmpty()){
            return true;
        }
        
        if(listA.size()!=listB.size()){
            return false;
        }
        
        boolean shapequals = false;
        for(Shape shs : listA){
            for(Shape shr : listB){
                if(shs.equals(shr)){
                    shapequals = true;
                }
            }
            if(!shapequals){
                return false;
            }
            shapequals = false;
        }
        return true;
    }
}
