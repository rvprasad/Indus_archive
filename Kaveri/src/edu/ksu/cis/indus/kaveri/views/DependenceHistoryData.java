/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

/*
 * Created on Aug 2, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.views;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.datastructures.HistoryTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * This class maintains the set of partial jimple statements for the chosen java
 * statement. This acts as the domain model for the partial slice view.
 * 
 * @author ganeshan
 */
public class DependenceHistoryData {
    /**
     * The list of Jimple statements.
     */
    private HistoryTracker dependenceHistory;

    /**
     * The link between stack history elements.
     */
    private String elemHistoryLink;

    /**
     * The viewers listening to this model.
     */
    private List listeners;

    /**
     * Constructor.
     *  
     */
    public DependenceHistoryData() {
        listeners = new ArrayList();
        dependenceHistory = new HistoryTracker();
        elemHistoryLink = "";
    }

    /**
     * @param history
     *            The current history.
     */
    public void addHistory(final Pair history) {
        dependenceHistory.addHistoryItem(history);
        if (!listeners.isEmpty()) {
            for (int _i = 0; _i < listeners.size(); _i++) {
                ((IDeltaListener) listeners.get(_i)).propertyChanged();
            }
        }
    }

    /**
     * Adds the listener to notify in case of change.
     * 
     * @param listener
     *            The objects interested in viewing the data
     */
    public void addListener(final IDeltaListener listener) {
        listeners.add(listener);
    }

    /**
     * Resets the stack.
     *  
     */
    public void reset() {
        dependenceHistory.reset();
        for (int _i = 0; _i < listeners.size(); _i++) {
            ((IDeltaListener) listeners.get(_i)).propertyChanged();
        }
    }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            The listener to remove. the data
     */
    public void removeListener(final IDeltaListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get the current items.
     * 
     * @return List The list of history items.
     */
    public List getContents() {
        return dependenceHistory.getCurrentItemsStack();
    }

    /**
     * Get the current item.
     *  
     */
    public Pair getCurrentItem() {
        return (Pair) dependenceHistory.getCurrentItem();
    }

    /**
     * Move forward.
     *  
     */
    public void navigateForward() {
        dependenceHistory.moveForward();
        if (!listeners.isEmpty()) {
            for (int _i = 0; _i < listeners.size(); _i++) {
                ((IDeltaListener) listeners.get(_i)).propertyChanged();
            }
        }
    }

    /**
     * Indicates if backward navigation is possible.
     *  
     */
    public boolean isBackNavPossible() {
        return dependenceHistory.isBackNavigationPossible();
    }

    /**
     * 
     * Indicates if forward navigation is possible.
     */
    public boolean isFwdNavPossible() {
        return dependenceHistory.isForwardNavigationPossible();
    }

    /**
     * Get the size of the history.
     * 
     * @return int The size
     */
    public int getSize() {
        return dependenceHistory.getCurrentSize();
    }

    /**
     * Move back.
     *  
     */
    public void navigateBack() {
        dependenceHistory.moveBack();
        if (!listeners.isEmpty()) {
            for (int _i = 0; _i < listeners.size(); _i++) {
                ((IDeltaListener) listeners.get(_i)).propertyChanged();
            }
        }
    }

    /**
     * Navigate to this item.
     * 
     * @param toIndex
     */
    public void navigateTo(int toIndex) {
        if (toIndex > -1 && toIndex <= dependenceHistory.getCurrentSize()) {
            final int _moveCount = dependenceHistory.getCurrentSize() - toIndex
                    - 1;
            if (_moveCount > 0) {
                for (int _ctr = 0; _ctr < _moveCount; _ctr++) {
                    dependenceHistory.moveBack();
                }
                if (!listeners.isEmpty()) {
                    for (int _i = 0; _i < listeners.size(); _i++) {
                        ((IDeltaListener) listeners.get(_i)).propertyChanged();
                    }
                }
            }
        }
    }

}
