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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

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
    private Map contextCollections;

    /**
     * Constructor.
     *  
     */
    public ContextRepository() {
        contextCollections = new HashMap();
    }

    /**
     * Adds the given stack to the set of contexts.
     * 
     * @param context
     *            The call stack
     *  
     */
    public void addCallStack(final IJavaProject project, final MethodCallContext context) {
        if (contextCollections.containsKey(project)) {
            final Collection _coll = (Collection) contextCollections.get(project);
            if (!_coll.contains(context)) {
                _coll.add(context);            
            }
        } else {
            final Collection _coll = new ArrayList();
            _coll.add(context);
            contextCollections.put(project, _coll);
        }
        
    }

    /**
     * Returns the set of defined contexts.
     * 
     * @return Returns the contextCollections.
     */
    public Collection getContext(final IJavaProject project) {
        return (Collection) contextCollections.get(project);
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