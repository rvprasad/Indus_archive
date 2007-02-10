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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author ganeshan
 * 
 * Provide the labels for the context dialog tvLeft.
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
