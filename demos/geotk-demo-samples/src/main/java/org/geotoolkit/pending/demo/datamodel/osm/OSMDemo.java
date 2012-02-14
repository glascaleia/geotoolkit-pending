

package org.geotoolkit.pending.demo.datamodel.osm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.storage.DataStoreException;
import org.opengis.feature.type.Name;

public class OSMDemo {

    public static void main(String[] args) throws DataStoreException {

        final Map<String,Serializable> parameters = new HashMap<String,Serializable>();
        parameters.put("url", OSMDemo.class.getResource("/data/sampleOSM.osm"));

        final DataStore store = DataStoreFinder.get(parameters);

        System.out.println("=================== Feature types ====================");
        final Set<Name> names = store.getNames();
        for(Name name : names){
            System.out.println(store.getFeatureType(name));
        }

    }

}
