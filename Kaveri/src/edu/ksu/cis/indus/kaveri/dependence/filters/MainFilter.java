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

import edu.ksu.cis.indus.kaveri.dependence.RightPaneTreeParent;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author ganeshan
 * 
 * The abstract Filter class
 */
public class MainFilter extends ViewerFilter {

    protected boolean bForwardDirection;

    protected String dependencyToTrack;

    /**
     * Constructor
     * 
     * @param dependencyToTrack
     *            The type of dependency to track
     * @fwdDirection The direction of the dependency to filter. True for
     *               dependents,False for Dependees.
     */
    public MainFilter(String dependencyToTrack, final boolean fwdDirection) {
        this.dependencyToTrack = dependencyToTrack;
        this.bForwardDirection = fwdDirection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (parentElement instanceof RightPaneTreeParent) {
            final RightPaneTreeParent _rtp = (RightPaneTreeParent) parentElement;
            if ((bForwardDirection && _rtp.getStatement().equals("Dependents"))
                    || (!bForwardDirection && _rtp.getStatement().equals(
                            "Dependees"))) {
                if (element instanceof RightPaneTreeParent
                        && ((RightPaneTreeParent) element).getStatement()
                                .equals(dependencyToTrack)) {
                    return false;
                }
            }
        }
        return true;
    }

}