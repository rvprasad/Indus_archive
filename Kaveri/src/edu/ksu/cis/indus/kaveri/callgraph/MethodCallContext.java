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
import java.util.Stack;

import org.eclipse.jdt.core.IMethod;


/**
 * @author ganeshan
 *
 * Holds all the context stacks between two points.
 */
public class MethodCallContext {

    /**
     * The collections of all the possible contexts.
     * @inv contextStacks.oclIsKindOf(Collection(Stack(CallTriple))) with
     * all the stacks having the same source and destination.
     */
    private Collection contextStacks;
    
    
    private IMethod callSource;
    
    private IMethod callRoot;
    
    
    /**
     * Contexts.
     *
     */
    public MethodCallContext(final IMethod src, final IMethod destn) {
        contextStacks = new ArrayList();
        this.callSource = src;
        this.callRoot = destn;
    }
    
    
    /**
     * Adds the given link as a context.
     * @pre context.oclIsKindOf(Stack(Triple(IMethod, lineNo, IMethod)))
     */
    public void addContext(final Stack context) {
        contextStacks.add(context);        
    }
    
    /**
     * @return Returns the callRoot.
     */
    public IMethod getCallRoot() {
        return callRoot;
    }
    /**
     * @return Returns the callSource.
     */
    public IMethod getCallSource() {
        return callSource;
    }
    
    /**
     * Returns the set of all contexts.
     * @return Returns the contextStacks.
     */
    public Collection getContextStacks() {
        return contextStacks;
    }
}