/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
package edu.ksu.cis.indus.kaveri.preferencedata;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ganeshan
 *
 * Stores the list of exceptions to ignore.
 */
public class ExceptionListStore {

    /** The set of exceptions to ignore */
    private Collection exceptionCollection;
    
    /**
     * Constructor.
     *
     */
    public ExceptionListStore() {
        exceptionCollection = new ArrayList();
    }
    
    /**
     * Returns the set of exceptions stored.
     * @return Collection The exception ignore collection.
     */
    public Collection getExceptionCollection() {
        return exceptionCollection;
    }
    
    /**
     * Adds the given exception to the list.
     * @param fqnExceptionName The fully qualified exception name.
     */
    public boolean addException(final String fqnExceptionName) {
        if (!exceptionCollection.contains(fqnExceptionName)) {
            exceptionCollection.add(fqnExceptionName);
            return true;
        } else {
            return false;
        }
        
    }

    /**
     * Removes the given exception.
     * @param name The exception to remove.
     */
    public void removeException(String name) {
        exceptionCollection.remove(name);
        
    }
    
}
