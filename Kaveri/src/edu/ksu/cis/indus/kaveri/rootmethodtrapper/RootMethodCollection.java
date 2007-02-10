/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
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
