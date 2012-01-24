package com.mycompany.rtree2d;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.geotoolkit.gui.swing.tree.Trees;
import org.geotoolkit.index.tree.JTreePanel;
import org.geotoolkit.index.tree.Tree;
import org.geotoolkit.index.tree.TreeFactory;
import org.geotoolkit.index.tree.TreeUtils;
import org.geotoolkit.index.tree.basic.SplitCase;
import org.geotoolkit.util.converter.Classes;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws InterruptedException
    {
        
        
        int time = 0;
//        Tree arbre = TreeFactory.createBasicRTree2D(SplitCase.LINEAR, 4);
        Tree arbre = TreeFactory.createStarRTree2D(4);//declenchement split ou ajout a revoir
//        Tree arbre = TreeFactory.createHilbertRTree2D(4, 3);
        int compteur = 0;
////          
        
        for(;compteur<=1000000;compteur++){
            double signeX = (Math.random()<0.5)?-1:1;
            double signeY = (Math.random()<0.5)?1:-1;
            double x = 200*Math.random()*signeX;
            double y = 120*Math.random()*signeY;
            arbre.insert(new Ellipse2D.Double(x, y, 0.5, 0.5));
        }
        
        ///////////////////////////////////////////////////////////////////////////////////////
        
//        for(int j= -120;j<=120;j+=4){
//            for(int i = -200;i<=200;i+=4){
//                arbre.insert(new Ellipse2D.Double(i, j, 1, 1));
//                compteur++;
//            }
//        }
//////        int test = 0;
//        for(int j= -120;j<=120;j+=4){
//            for(int i = -200;i<=200;i+=4){
//                arbre.delete(new Ellipse2D.Double(i, j, 1, 1));
//                compteur--;
//            }
//        }
        ///////////////////////////////////////////////////////////////////////////////////////////
//        System.out.println("ok pour la premiere vague");
//        
//        for(int j= 121;j>=-119;j-=2){
//            for(int i = 201;i>=-199;i-=2){
//                Shape s = new Ellipse2D.Double(i, j, 0.5, 0.5);
//                arbre.insert(s);
//                compteur++;
////                pan.setArbre(arbre);
////                pan.repaint();
////                Thread.sleep(time);
//            }
//        }
////        
////        System.out.println("ok pour la deuxieme vague");
//////        
//        for(int j= -196;j<=204;j+=2){
//            for(int i = 124;i>=-116;i-=2){
//                Shape s = new Ellipse2D.Double(j, i, 0.5, 0.5);
//                arbre.insert(new Entry(s, s.getBounds2D()));
//                compteur++;
////                pan.setArbre(arbre);
////                pan.repaint();
////                Thread.sleep(time);
//            }
//        }
//        System.out.println("ok pour la troisieme vague");
//        ok pour la troisieme
//        System.out.println("le compteur = "+compteur);
//        System.out.println("max element = "+(((HilbertLeaf)((HilbertRTree)arbre).getTreeTrunk())).getAllEntry().size());
        
        
            Shape s1 = new Ellipse2D.Double(-60, -21, 5, 5);
            arbre.insert(s1);
            
            Shape s2 = new Ellipse2D.Double(-60, 0, 5, 5);
            arbre.insert(s2);
            
            Shape s3 = new Ellipse2D.Double(-60, 21, 5, 5);
            arbre.insert(s3);
            
            Shape s4 = new Ellipse2D.Double(-60, 45, 5, 5);
            arbre.insert(s4);
            
            Shape s5 =new Ellipse2D.Double(-60, 60, 5, 5);
            arbre.insert(s5);
            
            Shape s6 = new Ellipse2D.Double(-45, 60, 5, 5);
            arbre.insert(s6);
            
            Shape s7 = new Ellipse2D.Double(-21, 60, 5, 5);
            arbre.insert(s7);
            
            Shape s8 =new Ellipse2D.Double(0, 60, 5, 5);
            arbre.insert(s8);
            
            Shape s9 = new Ellipse2D.Double(21, 60, 5, 5);
            arbre.insert(s9);
           
            Shape s10 = new Ellipse2D.Double(45, 60, 5, 5);
            arbre.insert(s10);
            
            Shape s11 = new Ellipse2D.Double(60, 60, 5, 5);
            arbre.insert(s11);
            
            Shape s12 = new Ellipse2D.Double(60, 45, 5, 5);
            arbre.insert(s12);
            
            Shape s13 = new Ellipse2D.Double(60, 21, 5, 5);
            arbre.insert(s13);
            
            Shape s14 = new Ellipse2D.Double(60, 0, 5, 5);
            arbre.insert(s14);
            
            Shape s15 = new Ellipse2D.Double(60, -21, 5, 5);
            arbre.insert(s15);
            
            Shape s16 = new Ellipse2D.Double(60, -45, 5, 5);
            arbre.insert(s16);
            
            Shape s17 = new Ellipse2D.Double(60, -60, 5, 5);
            arbre.insert(s17);
            
            Shape s18 = new Ellipse2D.Double(45, -60, 5, 5);
             arbre.insert(s18);
            
            Shape s19 = new Ellipse2D.Double(21, -60, 5, 5);
            arbre.insert(s19);
            
            Shape s20= new Ellipse2D.Double(0, -60, 5, 5);
            arbre.insert(s20);
            
            Shape s21= new Ellipse2D.Double(-21, -60, 5, 5);
            arbre.insert(s21);
            
            Shape s22 = new Ellipse2D.Double(-21, 45, 5, 5);
            arbre.insert(s22);
            
            Shape s23 = new Ellipse2D.Double(-21, -21, 5, 5);
            arbre.insert(s23);
            
            Shape s24 = new Ellipse2D.Double(-21, 0, 5, 5);
            arbre.insert(s24);
            
            Shape s25 = new Ellipse2D.Double(-21, 21, 5, 5);
            arbre.insert(s25);
            
            Shape s26 = new Ellipse2D.Double(0, 21, 5, 5);
            arbre.insert(s26);
            
            Shape s27 = new Ellipse2D.Double(21, 21, 5, 5);
            arbre.insert(s27);
           
            Shape s28 = new Ellipse2D.Double(21, 0, 5, 5);
            arbre.insert(s28);
            Shape s29 = new Ellipse2D.Double(21, -21, 5, 5);
            arbre.insert(s29);
            
            Shape s30 = new Ellipse2D.Double(0, -21, 5, 5);
            arbre.insert(s30);
            
            Shape s31 = new Ellipse2D.Double(0, 0, 5, 5);
            arbre.insert(s31);
            
            Shape s32 = new Ellipse2D.Double(-60, -45, 5, 5);
            arbre.insert(s32);
            
            ////////////affiner delete methode !!!!!!!!
            
            
//            arbre.delete(s1);
//            arbre.delete(s2);
//            arbre.delete(s3);
//            arbre.delete(s4);
//            arbre.delete(s5);
//            arbre.delete(s6);
//            arbre.delete(s7);
//            arbre.delete(s8);
//            arbre.delete(s9);
//            arbre.delete(s10);
//            arbre.delete(s11);
//            arbre.delete(s12);
//            arbre.delete(s13);
//            arbre.delete(s14);
//            arbre.delete(s15);
//            arbre.delete(s16);
//            arbre.delete(s17);
//            arbre.delete(s18);
//            arbre.delete(s19);
//            arbre.delete(s20);
//            arbre.delete(s21);
//            arbre.delete(s22);
//            arbre.delete(s23);
//            arbre.delete(s24);
//            arbre.delete(s25);
//            arbre.delete(s26);
//            arbre.delete(s27);
//            arbre.delete(s28);
//            arbre.delete(s29);
//            arbre.delete(s30);
//            arbre.delete(s31);
//            arbre.delete(s32);
            
        Rectangle2D searc = new Rectangle2D.Double(-10, -50, 50, 50);
        List<Shape> lEbis = new ArrayList<Shape>();
        long timeBase = System.nanoTime();
        arbre.search(searc, lEbis);
        long timeSearch = System.nanoTime();
        
//        System.out.println(arbre);
        
        
        
        System.out.println("/////////////////////////////////////////////////////");
        System.out.println("le compteur = "+(compteur+32));
        List<Shape> lE = new ArrayList<Shape>();
        System.out.println("enveloppe de root = "+arbre.getRoot().getBoundary());
        arbre.search(arbre.getRoot().getBoundary().getBounds2D(), lE);
        System.out.println("compteur reeel = "+lE.size());
        System.out.println("timeBase   = "+timeBase);
        System.out.println("timeSearch = "+timeSearch);
        System.out.println("le temps de recherche est de (en nano): "+(timeSearch-timeBase));
        System.out.println("le temps de recherche est de (en ms): "+(timeSearch-timeBase)*10E-7);
        System.out.println("taille de la list de recherche = "+lE.size());
        System.out.println("/////////////////////////////////////////////////////");
//        
        
            JFrame fen = new JFrame();
            JTreePanel pan = new JTreePanel(arbre, lEbis);
            fen.add(pan);
            fen.setSize(1600, 900);
            fen.setLocationRelativeTo(null);
            fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fen.setVisible(true);
            fen.setTitle("R-Tree");
            Thread.sleep(time);
        
    }
}
