
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
 * Created on Apr 14, 2004
 *
 *
 *
 */
package edu.ksu.cis.indus.toolkits.sliceeclipse.execute;

import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.presentation.AddIndusAnnotation;


import java.util.Map;

import org.eclipse.core.resources.IFile;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;


/**
 * Toggles the slice higlighting.
 *
 * @author Ganeshan The slice annotate action
 */
public class SliceAnnotate
  implements IEditorActionDelegate {
	/** 
	 * Used to toggle the showing/hiding of annotations.
	 */
	private static boolean showAnnotations;

	/** 
	 * The java editor.
	 */
	CompilationUnitEditor editor;

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.editor = (CompilationUnitEditor) targetEditor;
	}

	/**
	 * Toggles the higlighting in the editor.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		final AddIndusAnnotation _indusA =
			SliceEclipsePlugin.getDefault().getIndusConfiguration().getIndusAnnotationManager();

		if (_indusA.isAreAnnotationsPresent(editor)) {
			_indusA.setEditor(editor, false);
		} else {
			final IFile _fl = (IFile) ((IFileEditorInput) editor.getEditorInput()).getFile();

			if (_indusA.annotationPresent(_fl)) {
				_indusA.setEditor(editor, true);
			} else {
				final Map _map = SliceEclipsePlugin.getDefault().getIndusConfiguration().getLineNumbers();
				_indusA.setEditor(editor, _map);
			}
		}
	}

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
	}
}
