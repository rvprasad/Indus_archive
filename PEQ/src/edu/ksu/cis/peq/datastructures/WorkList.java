/*
 * WorkList.java
 *
 * Created on March 13, 2005, 1:51 PM
 */

package edu.ksu.cis.peq.datastructures;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This is the concrete implementation of the worklist.
 * The methods are not synchronized.
 * @author ganeshan
 */
public class WorkList implements IWorkList {
    /** The container for the work objects */
    private List workList;
    
    /** The size of the list */
    private int size;
    
    /** Creates a new instance of WorkList */
    public WorkList() {
        workList = new LinkedList();
        size = 0;
    }

    /**
     * @see edu.ksu.cis.peq.datastructures.IWorkList.#getWork()
     */
    public Object getWork() {
        Object _retObj = null;
        if (size > 0) {
            _retObj = workList.remove(0);
            size--;
        } else {
            throw new NoSuchElementException("No item in the worklist");
        }
        return _retObj;
    }

    /**
     * @see edu.ksu.cis.peq.datastructures.IWorkList.#hasWork()
     */
    public  boolean hasWork() {
        return size > 0;
    }

    /**
     * @see edu.ksu.cis.peq.datastructures.IWorkList.#addAll()
     */
    public void addAll(java.util.Collection c) {
        final Iterator _cIter = c.iterator();
        for (; _cIter.hasNext();) {
            workList.add(_cIter.next());
            size++;
        }
    }

    /**
     * @see edu.ksu.cis.peq.datastructures.IWorkList.#addWork()
     */
    public void addWork(Object work) {
        workList.add(work);
        size++;
    }
    
    /**
     * @see edu.ksu.cis.peq.datastructures.IWorkList.#clear()
     */
    public void clear() {
        workList.clear();
    }
}
