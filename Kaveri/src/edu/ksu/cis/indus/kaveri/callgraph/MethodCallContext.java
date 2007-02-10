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
     * 
     * @inv contextStacks.oclIsKindOf(Collection(Stack(CallTriple))) with all
     *      the stacks having the same source and destination.
     */
    private Collection contextStacks;

    /**
     * The source of the call chain.
     */
    private IMethod callSource;

    /**
     * The root of the call chain.
     */
    private IMethod callRoot;

    /**
     * Contexts.
     *  
     * @param src The original method.
     * @param destn The final method in the chain.
     */
    public MethodCallContext(final IMethod src, final IMethod destn) {
        contextStacks = new ArrayList();
        this.callSource = src;
        this.callRoot = destn;
    }

    /**
     * Adds the given link as a context.
     * @param context The call stack between the source and the root. 
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
     * 
     * @return Returns the contextStacks.
     */
    public Collection getContextStacks() {
        return contextStacks;
    }
}
