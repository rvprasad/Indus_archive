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
            final Collection _c = ((ContextRepository) inputElement).getContext(jProject);
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