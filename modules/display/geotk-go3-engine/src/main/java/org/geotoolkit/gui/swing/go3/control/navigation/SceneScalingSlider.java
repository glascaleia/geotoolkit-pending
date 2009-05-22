
package org.geotoolkit.gui.swing.go3.control.navigation;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.geotoolkit.display3d.canvas.A3DCanvas;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class SceneScalingSlider extends JSlider{

    private A3DCanvas map = null;

    public SceneScalingSlider() {
        super(1, 100);

        addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                if(map != null){
                    map.getContainer2().setScaling(SceneScalingSlider.this.getValue()/100d);
                }
            }
        });

    }

    public void setMap(A3DCanvas map) {
        this.map = map;
    }

    public A3DCanvas getMap() {
        return map;
    }


}
