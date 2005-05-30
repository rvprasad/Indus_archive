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
 
package edu.ksu.cis.indus.kaveri.rootmethodtrapper;

import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author ganeshan
 *
 * This class holds the set of root methods for a project.
 */
public class RootMethodCollection {
    /**
     * The collection of pairs of classname and methodsignatures.
     */
    private Collection rootMethodCollection;
    
    /**
     * Constructor.
     *
     */
    public RootMethodCollection() {
        rootMethodCollection = new ArrayList();
    }
    
    /**
     * Returns the root method collection.
     * @return
     */
    public Collection getRootMethodCollection() {
        return rootMethodCollection;
    }
    
    /**
     * Adds the given root method to the collection.
     * @param className
     * @param methodSignature
     */
    public void addRootMethod(final String className, final String methodSignature) {
        final Pair _pair = new Pair(className, methodSignature);
        if (!rootMethodCollection.contains(_pair)) {
            rootMethodCollection.add(new Pair(className, methodSignature));
        }
    }
}
