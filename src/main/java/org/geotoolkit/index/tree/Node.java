/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotoolkit.index.tree;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.EventListenerList;

/**Create "generic" Node.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class Node {

    private final EventListenerList listenerList = new EventListenerList();
    private Map<String, Object> userProperties;

    /**
     * @param key
     * @return user property for given key
     */
    public Object getUserProperty(final String key) {
        if (userProperties == null) {
            return null;
        }
        return userProperties.get(key);
    }

    /**Add user property with key access.
     * 
     * @param key 
     * @param value Object will be stocked.
     */
    public void setUserProperty(final String key, final Object value) {
        if (userProperties == null) {
            userProperties = new HashMap<String, Object>();
        }
        userProperties.put(key, value);
    }

    public void addListener(PropertyChangeListener l) {
        listenerList.add(PropertyChangeListener.class, l);
    }

    public void removeListener(PropertyChangeListener l) {
        listenerList.remove(PropertyChangeListener.class, l);
    }

    protected void fireCollectionEvent() {

        final PropertyChangeListener[] listeners = listenerList.getListeners(PropertyChangeListener.class);

        for (PropertyChangeListener l : listeners) {
            l.propertyChange(null);
        }
    }
}
