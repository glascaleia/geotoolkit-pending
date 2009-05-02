
package org.geotools.display3d.container;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.geom.BufferUtils;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.display3d.canvas.A3DCanvas;
import org.geotools.display3d.primitive.A3DGraphic;
import org.geotoolkit.map.GraphicBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.geometry.Envelope;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class ContextNode extends A3DGraphic{

    private static final GraphicBuilder<A3DGraphic> DEFAULT_BUILDER = new A3DGraphicBuilder();

    private final MapContext context;

    public ContextNode(A3DCanvas canvas, MapContext context) {
        this.context = context;

//        attachChild(buildPlan());

        for(final MapLayer layer : context.layers()){

            GraphicBuilder<? extends A3DGraphic> builder = layer.getGraphicBuilder(A3DGraphic.class);

            if(builder == null){
                builder = DEFAULT_BUILDER;
            }

            Collection<? extends A3DGraphic> graphics = builder.createGraphics(layer, canvas);

            for(A3DGraphic gra : graphics){
                this.attachChild(gra);
            }
        }

        final Envelope env;
        try {
            env = context.getBounds();

            float minX = (float) env.getMinimum(0);
            float minY = (float) env.getMinimum(1);
            float maxX = (float) env.getMaximum(0);
            float maxY = (float) env.getMaximum(1);

            setScale(0.1f,1, 0.1f);
            setTranslation(-env.getMedian(0)/10, 1f, -env.getMedian(1)/10);



        } catch (IOException ex) {
            Logger.getLogger(ContextNode.class.getName()).log(Level.SEVERE, null, ex);
        }


    }


    private Node buildPlan(){
        final Node plan = new Node("plan");

        final Envelope env;
        try {
            env = context.getBounds();
        } catch (IOException ex) {
            Logger.getLogger(ContextNode.class.getName()).log(Level.SEVERE, null, ex);
            return plan;
        }

        float minX = (float) env.getMinimum(0);
        float minY = (float) env.getMinimum(1);
        float maxX = (float) env.getMaximum(0);
        float maxY = (float) env.getMaximum(1);

        System.out.println(minX);
        System.out.println(maxX);
        System.out.println(minY);
        System.out.println(maxY);

        System.out.println(minX-env.getMedian(0));
        System.out.println(minY-env.getMedian(1));


        final Quad back = new Quad();
        back.initialize(maxX-minX, maxY-minY);
        back.setLightCombineMode(LightCombineMode.Off);
        back.setDefaultColor(ColorRGBA.RED);
        back.setRotation(new Matrix3().fromAngleNormalAxis(Math.PI * -0.5, new Vector3(1, 0, 0)));
        back.setTranslation(minX, 0f, minY);


        plan.attachChild(back);

        return plan;
    }

}
