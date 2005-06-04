/*
 * Created on Jun 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ksu.cis.indus.kaveri.infoView;

import edu.ksu.cis.indus.kaveri.views.IDeltaListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ganeshan
 *
 * Maintains the information view listeners and
 * sends out updates.
 */
public class InfoUpdateBroadcaster {

    /**
     * The list of all listeners.
     */
    private List listenerList;
    
    /**
     * Constructor.
     *
     */
    public InfoUpdateBroadcaster() {
        listenerList = new ArrayList();
    }
    
    /**
     * Add a listener.
     * @param listener
     */
    public void addListener(final IDeltaListener listener) {
        listenerList.add(listener);
    }
    
    /**
     * Sends an update message to all the registered listeners.
     *
     */
    public void update() {
        for (int _i = 0; _i < listenerList.size(); _i++) {
            ((IDeltaListener) listenerList.get(_i)).propertyChanged();
        }
    }
    
    /**
     * Remove the listener.
     * @param listener
     */
    public void removeListenere(final IDeltaListener listener) {
        listenerList.remove(listener);
    }
}
