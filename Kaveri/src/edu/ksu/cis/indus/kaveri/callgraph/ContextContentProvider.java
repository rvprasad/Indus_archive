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

import java.util.Collection;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author ganeshan
 * 
 * Provide the content for the context dialog tvLeft.
 */
public class ContextContentProvider implements IStructuredContentProvider {

    private IJavaProject jProject;
    /**
     * Constructor.
     *
     */
    public ContextContentProvider(final IJavaProject project) {
       this.jProject = project; 
    }
    
    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(final Object inputElement) {
        Object[] _retObj = new Object[0]; 
        if (inputElement instanceof ContextRepository) {
            Collection _c = null;
            if (jProject != null) {
             _c = ((ContextRepository) inputElement).getContext(jProject);
            } else {
                _c = ((ContextRepository) inputElement).getAllContexts();
            }
            if (_c != null) {
                _retObj = _c.toArray();    
            }            
        } 
        return _retObj;
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {

    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    }

}
