
package org.geotoolkit.display3d.container;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.geom.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.display3d.canvas.A3DCanvas;
import org.geotoolkit.display3d.primitive.A3DGraphic;
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
        super(canvas);
        this.context = context;
        
        try {
            this.attachChild(buildPlan(context.getBounds()));
        } catch (IOException ex) {
            Logger.getLogger(ContextNode.class.getName()).log(Level.SEVERE, null, ex);
        }

        for(final MapLayer layer : context.layers()){

            GraphicBuilder<? extends A3DGraphic> builder = layer.getGraphicBuilder(A3DGraphic.class);

            if(builder == null){
                builder = DEFAULT_BUILDER;
            }

            for(A3DGraphic gra : builder.createGraphics(layer, canvas)){
                this.attachChild(gra);
            }
        }
    }

    private Node buildPlan(final Envelope env){
        final Node plan = new Node("plan");
        plan.setLightCombineMode(LightCombineMode.Off);

        final float over = -10f;
        final float width = 1f;
        final float minx = (float) env.getMinimum(0);
        final float maxx = (float) env.getMaximum(0);
        final float miny = (float) env.getMinimum(1);
        final float maxy = (float) env.getMaximum(1);

        Box back = new Box("ceiling", new Vector3(minx, -env.getSpan(0)/20 -11, miny), new Vector3(maxx, -11, maxy));
        back.setDefaultColor(new ColorRGBA(1, 1, 1, 0.6f));
        back.setModelBound(new BoundingBox());
        back.updateModelBound();

        final BlendState blend = new BlendState();
        blend.setBlendEnabled(true);
        blend.setSourceFunction(SourceFunction.SourceAlpha);
        blend.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);

        final int nbGrid = 10;
        float step = (float) (env.getSpan(0) / nbGrid);

        for(int i=0;i<=nbGrid;i++){
            final FloatBuffer verts = BufferUtils.createVector3Buffer(2);
            verts.put(minx +step*i).put(over).put(miny);
            verts.put(minx +step*i).put(over).put(maxy);
            Line line = new Line("Lines", verts, null, null, null);
            line.getMeshData().setIndexMode(IndexMode.Lines);
            line.setLineWidth(width);
            line.setDefaultColor(ColorRGBA.LIGHT_GRAY);
            line.setAntialiased(true);
            line.setModelBound(new BoundingBox());
            line.updateModelBound();
            plan.attachChild(line);
        }

        step = (float) (env.getSpan(1) / nbGrid);

        for(int i=0;i<=nbGrid;i++){
            final FloatBuffer verts = BufferUtils.createVector3Buffer(2);
            verts.put(minx).put(over).put(miny +step*i);
            verts.put(maxx).put(over).put(miny +step*i);
            Line line = new Line("Lines", verts, null, null, null);
            line.getMeshData().setIndexMode(IndexMode.Lines);
            line.setLineWidth(width);
            line.setDefaultColor(ColorRGBA.LIGHT_GRAY);
            line.setAntialiased(true);
            line.setModelBound(new BoundingBox());
            line.updateModelBound();
            plan.attachChild(line);
        }


        plan.attachChild(back);
//        plan.setRenderState(blend);
//        plan.setCullHint(Spatial.CullHint.Never);

//        plan.setRotation(new Matrix3().fromAngleNormalAxis(Math.PI * -0.5, new Vector3(1, 0, 0)));

        return plan;
    }

}
