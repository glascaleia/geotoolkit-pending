/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotoolkit.index.tree.io;

import java.awt.Shape;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotoolkit.index.tree.Node;
import org.geotoolkit.index.tree.Node2D;
import org.geotoolkit.index.tree.Tree;

/**Create TreeWriter object.
 *
 * <br/>
 * Example : <br/>
 * <pre>
 * {@code
 * final TreeWriter writer = new TreeWriter();
 * writer.setOutput(output);
 * writer.write(tree);
 * writer.dispose();
 * writer.reset(); 
 * 
 * writer.setOutput(input2);...//for another output
 * }
 * </pre>
 * And should be used like :<br/>
 * <pre>
 * {@code
 * TreeWriter.write(arbre, fil);
 * }
 * </pre>
 * 
 * @author Rémi Marechal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class TreeWriter {
    int inc = 0;
    private boolean closeOnDispose = false;
    private OutputStream sourceStream = null;
    private DataOutputStream dataOPStream = null;
    private Map<Node, Integer> index = null;

    public TreeWriter() {
    }

    /**
     * Set the output for this writer.<br/>
     * Handle types are :<br/>
     * - java.io.File<br/>
     * - java.io.OutputStream<br/>
     * 
     * @param output
     * @throws IOException
     * @throws XMLStreamException
     */
    public void setOutput(final Object output) throws IOException {
        index = new HashMap<Node, Integer>();
        if (output instanceof OutputStream) {
            sourceStream = (OutputStream) output;
            dataOPStream = new DataOutputStream(sourceStream);
            closeOnDispose = false;
            return;
        }

        closeOnDispose = true;
        if (output instanceof File) {
            sourceStream = new FileOutputStream((File) output);
            dataOPStream = new DataOutputStream(sourceStream);
        } else {
            throw new IOException("Unsuported output : " + output.getClass());
        }

    }
    
    /**
     * Write tree in binary.
     * 
     * @param tree
     * @throws IOException 
     */
    public void write(final Tree tree) throws IOException{
        Node2D root = tree.getRoot();
        createIndex(root);
        serializeNode(root, dataOPStream);
    }

    /**
     * Write all node to binary.
     * 
     * @param root
     * @param dops
     * @throws IOException 
     */
    private void serializeNode(final Node2D root, final DataOutputStream dops) throws IOException{
        nodeToBinary(root, dops);
        for(Node2D child : root.getChildren()){
            serializeNode(child, dops);
        }
    }
    
    /**
     * Write node in binary.
     * 
     * @param node
     * @param dops
     * @throws IOException 
     */
    private void nodeToBinary(final Node2D node, final DataOutputStream dops) throws IOException{
        
        final List<Node2D> listChild = node.getChildren();
        final List<Shape> listEntries = node.getEntries();
        
        int nbrSubNode = listChild.size();
        dops.writeInt(index.get(node));
        dops.writeInt(nbrSubNode);
        
        for(Node2D child : listChild){
            dops.writeInt(index.get(child));//sur 4 octets
        }
        List<Node2D> listup = (List<Node2D>)node.getUserProperty("cells");
        if(listup!=null){
            for(Node2D n : listup){
                listEntries.addAll(n.getEntries());
            }
        }
        
        dops.writeInt(listEntries.size());
        for(Shape shape : listEntries){
            final ByteArrayOutputStream temp = new ByteArrayOutputStream(); //tableau de byte dans lequel on peut ecrire         
            final ObjectOutputStream     ost = new ObjectOutputStream(temp);//on ouvre un flux sur un tableau de byte 
            ost.writeObject(shape); //on ecrit le shape dans le tableau de byte
            temp.flush();//on vide le buffer
            final byte[] array = temp.toByteArray();
            dops.writeInt(array.length);
            dops.write(array);
        }
    }
    
    /**
     * Find all tree node and affect an id for each them.
     * 
     * @param node tree root node.
     */
    private void createIndex(final Node2D node){
        index.put(node, inc);
        for(Node2D child : node.getChildren()){
            inc++;
            createIndex(child);
        }
    }
    
    /**
     * Release potential locks or opened stream.
     * Must be called when the writer is not needed anymore.
     * It should not be used after this method has been called.
     */
    public void dispose() throws IOException {
        if (closeOnDispose) {
            sourceStream.close();
        }
        dataOPStream.close();
    }

    /**
     * close potential previous stream and cache if there are some.
     * This way the writer can be reused for a different output later.
     * The underlying stax writer will be closed.
     */
    public void reset() {
        closeOnDispose = false;
        sourceStream = null;
        dataOPStream = null;
        inc = 0;
    }
    
    /**
     * To write one time without TreeWriter re-utilization.
     * 
     * @param tree
     * @param output
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void write(final Tree tree, final Object output) throws IOException{
        final TreeWriter tW = new TreeWriter();
        tW.setOutput(output);
        tW.write(tree);
        tW.dispose();
    }
}
