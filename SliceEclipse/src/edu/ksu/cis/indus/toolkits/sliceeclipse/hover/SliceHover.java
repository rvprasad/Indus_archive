
/*
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

/*
 * Created on Mar 30, 2004
 *
 * 
 */
package edu.ksu.cis.indus.toolkits.sliceeclipse.hover;

import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;

import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.IEditorPart;


/**
 * The indus hover.
 *
 * @author Ganeshan 
 */
public class SliceHover
  implements IJavaEditorTextHover {
	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setEditor(final IEditorPart editor) {
	}

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer,
	 * 		org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		return SliceEclipsePlugin.getDefault().getIndusConfiguration().getIndusAnnotationManager().getRegionInfo(hoverRegion);
	}

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		return null;
	}
}
