/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
 
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
    public void addException(final String fqnExceptionName) {
        
    }

    /**
     * Removes the given exception.
     * @param name The exception to remove.
     */
    public void removeException(String name) {
        exceptionCollection.remove(name);
        
    }
    
}
