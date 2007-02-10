/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author ganeshan
 * 
 * This class is used to maintain a navigation history. The current place in the
 * history is recorded internally. The semantics of the class are: Add
 * operations add the item to the end of the internal list - the current index
 * is then moved to that item. Navigate action moves the internal index and
 * returns the item at that point. If an add operation is performed when the
 * internal index is not at the end, all the items after that point are lost.
 */
public class HistoryTracker {
    /**
     * The list containing the history items.
     */
    private List historyItems;

    /**
     * The current position in the history.
     */
    private int currentIndex;

    /**
     * Constructor.
     *  
     */
    public HistoryTracker() {
        historyItems = new Vector();
        currentIndex = -1;
    }

    /**
     * Adds the given item to the history.
     * 
     * @param objHistoryItem
     *            The item to add to the history.
     * @invariant historyItems[0..currentIndex] := HistoryItems
     */
    public synchronized void addHistoryItem(final Object objHistoryItem) {
        if (currentIndex == historyItems.size() - 1) {
            historyItems.add(objHistoryItem);
            currentIndex++;
        }
        /* Add in the middle of navigation, say goodbye to items after the index :) */
        if (currentIndex < historyItems.size() - 1 && currentIndex > -1) {
            historyItems.subList(currentIndex + 1, historyItems.size()).clear();
            historyItems.add(objHistoryItem);
            currentIndex++;

        }
    }

    /**
     * Move back in the history by one step.
     *  
     */
    public synchronized void moveBack() {
        if (currentIndex > 0) {
            currentIndex--;
        }
    }

    /**
     * Move forward in the history by one step.
     *  
     */
    public synchronized void moveForward() {
        if (currentIndex < historyItems.size() - 1) {
            currentIndex++;
        }
    }

    /**
     * Get the item at the current location.
     * 
     * @return Object The object at the current location.
     */
    public synchronized Object getCurrentItem() {
        if (currentIndex < 0) {
            throw new IllegalArgumentException("No items present");
        }
        return historyItems.get(currentIndex);
    }

    /**
     * Get the history size.
     * 
     * @return int The number of items present in the history.
     */
    public synchronized int getCurrentSize() {
        return historyItems.size();
    }

    /**
     * Get the current items in the list. The items are returned as a stack. The
     * list is not connected to the internal list to preserve its security.
     * 
     * @return List The list of items.
     */
    public synchronized List getCurrentItemsStack() {
        final List _retLst = new ArrayList();
        for (int _ctr = currentIndex; _ctr >= 0; _ctr--) {
            _retLst.add(historyItems.get(_ctr));
        }

        return _retLst;
    }

    /**
     * Indicates if the history can be navigated backwards.
     * 
     * @return boolean True If backward navigation is possible.
     */
    public synchronized boolean isBackNavigationPossible() {
        if (currentIndex == -1) {
            return false;
        }
        if (currentIndex > 0 && currentIndex <= historyItems.size() - 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Indicates if the history can be navigated forward.
     * 
     * @return boolean True If forward navigation is possible.
     */
    public synchronized boolean isForwardNavigationPossible() {
        if (currentIndex == -1) {
            return false;
        }
        if (currentIndex >= 0 && currentIndex < historyItems.size() - 1) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Reset the history.
     *  
     */
    public synchronized void reset() {
        historyItems.clear();
        currentIndex = -1;
    }

}

