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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author ganeshan
 * 
 * Provide the labels for the context dialog viewer.
 */
public class ContextLabelProvider extends LabelProvider implements
        ITableLabelProvider {

    /**
     * Constructor.
     *
     */
    public ContextLabelProvider() {
        
    }
    
    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(final Object element, final int columnIndex) {
        return null;
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(final Object element, final int columnIndex) {
        String _retString = "";
        if (element instanceof MethodCallContext) {
            switch (columnIndex) {
            case 1:
                _retString = ((MethodCallContext) element).getCallSource()
                        .getElementName();
                break;
            case 2:
                _retString = ((MethodCallContext) element).getCallRoot()
                        .getElementName();
                break;
            default: break;   
            }
        }
        return _retString;
    }

}