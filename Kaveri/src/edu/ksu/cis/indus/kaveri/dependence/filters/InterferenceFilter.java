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
 
package edu.ksu.cis.indus.kaveri.dependence.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import edu.ksu.cis.indus.kaveri.dependence.RightPaneTreeParent;

/**
 * @author ganeshan
 *
 * Filter the control dependence.
 */
public class InterferenceFilter extends ViewerFilter {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (parentElement instanceof RightPaneTreeParent) {
            final RightPaneTreeParent _rtp = (RightPaneTreeParent) parentElement;
            if (_rtp.getStatement().equals("Dependees") || _rtp.getStatement().equals("Dependents")) {
                if (element instanceof RightPaneTreeParent && ((RightPaneTreeParent) element).getStatement().equals("Interference")) {
                    return false;
                }
            }            
        }
        return true;
    }

}
