/*
 * IWorkList.java
 *
 * Created on March 13, 2005, 1:48 PM
 */

package edu.ksu.cis.peq.datastructures;

import java.util.Collection;

/**
 * This is the interface for the Work List.
 * @author ganeshan
 */
public interface IWorkList {
    
    /** 
     * Adds the given work object to the work list if it doesn't already exist 
     * 
     */
    void addWork(final Object work);
    
    /** Adds the given work objects in the collection to the work list if it doesn't already exist */
    void addAll(final Collection c);
    
    /** Clear the worklist */
    void clear();
    
    /**
     * Removes and returns the object at the head of the worklist.
     * @pre Worklist.size > 0
     * @post Result != null
     * @return Object The work in the worklist
     */
    Object getWork();
    
    /**
     * Indicates if there is work left in the worklist.
     * @pre True
     * @post Result = true => Worklist.size > 0, false otherwise
     * @return boolean Whether a work is present
     */
    boolean hasWork();
    
}
