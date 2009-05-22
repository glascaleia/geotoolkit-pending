

package org.geotoolkit.display3d.style;

import org.geotoolkit.display2d.style.CachedSymbolizer;

import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class CachedExtrudedSymbolizer3D extends CachedSymbolizer<ExtrudedSymbolizer3D>{

    public CachedExtrudedSymbolizer3D(ExtrudedSymbolizer3D ext) {
        super(ext);
    }

    public Expression getHeight(){
        return styleElement.getHeight();
    }

    @Override
    public float getMargin(Feature feature, float coeff) {
        return 0;
    }

    @Override
    protected void evaluate() {
    }

    @Override
    public boolean isVisible(Feature feature) {
        return true;
    }

}
