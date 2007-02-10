/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Returns the set of all contexts.
     * @return Collection The set of all contexts.
     */
    public Collection getAllContexts() {
        final  Set _entrySet =  contextCollections.entrySet();
        final List _retList = new ArrayList();
        for (Iterator iter = _entrySet.iterator(); iter.hasNext();) {
            final Map.Entry _entry = (Map.Entry) iter.next();
            _retList.addAll((Collection) _entry.getValue());            
        }
        return _retList;
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
