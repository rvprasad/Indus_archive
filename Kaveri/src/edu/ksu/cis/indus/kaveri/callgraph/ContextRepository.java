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

package edu.ksu.cis.indus.kaveri.callgraph;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ganeshan
 * 
 * This class holds the set of contexts already defined.
 */
public class ContextRepository {

    /**
     * The set of calling contexts.
     * 
     * @inv contextCollections.oclIsKindOf(Collection(MethodCallContext))
     */
    private Collection contextCollections;

    /**
     * Constructor.
     *  
     */
    public ContextRepository() {
        contextCollections = new ArrayList();
    }

    /**
     * Adds the given stack to the set of contexts.
     * 
     * @param context
     *            The call stack
     *  
     */
    public void addCallStack(final MethodCallContext context) {
        if (!contextCollections.contains(context)) {
            contextCollections.add(context);
        }
    }

    /**
     * Returns the set of defined contexts.
     * 
     * @return Returns the contextCollections.
     */
    public Collection getContexts() {
        return contextCollections;
    }

    /**
     * 
     * Reset the contexts.
     *  
     */
    public void reset() {
        contextCollections.clear();
    }
}