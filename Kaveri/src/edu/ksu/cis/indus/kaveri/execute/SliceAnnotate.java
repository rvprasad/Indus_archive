
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
package edu.ksu.cis.indus.kaveri.execute;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;


/**
 * This class implements the slice toggle action.
 *
 * @author Ganeshan
 */
public class SliceAnnotate
  implements IEditorActionDelegate {
	/** 
	 * The java editor instance.
	 */
	private CompilationUnitEditor editor;

	/**
	 * Indicates the current java editor.
	 *
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.editor = (CompilationUnitEditor) targetEditor;
		final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
		boolean _properNature = false;
		try {
		 _properNature = _file.getProject().hasNature("org.eclipse.jdt.core.javanature");
		}
		catch (CoreException _ce) {			
			return;
		}
		if (!_properNature) {
			action.setEnabled(false);
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					final Map _map = KaveriPlugin.getDefault().getIndusConfiguration().getLineNumbers();

					if (editor != null && _map != null && _map.size() > 0) {
						final IDecoratorManager _manager = KaveriPlugin.getDefault().getWorkbench().getDecoratorManager();

						if (_manager.getEnabled("edu.ksu.cis.indus.kaveri.decorator")) {
							final IFile _fl = ((IFileEditorInput) editor.getEditorInput()).getFile();
							final AddIndusAnnotation _indusA =
								KaveriPlugin.getDefault().getIndusConfiguration().getIndusAnnotationManager();

							if (!_indusA.isAreAnnotationsPresent(editor) && classesPresent(_fl, _map)) {
								_indusA.setEditor(editor, _map);
							}
						}
					}
				}

				/**
				 * Indicates if atleast one class in the file has a slice associated.
				 *
				 * @param fl The file in which to check.
				 * @param map The map of class names to line numbers
				 *
				 * @return boolean Whether the file is worth annotating
				 */
				private boolean classesPresent(final IFile fl, final Map map) {
					final ICompilationUnit _icunit = (ICompilationUnit) JavaCore.create(fl);
					boolean _result = false;

					try {
						if (_icunit != null) {
							IType[] _types = null;
							_types = _icunit.getAllTypes();

							for (int _nrun = 0; _nrun < _types.length; _nrun++) {
								final IType _type =  _types[_nrun];
								final String _className = _type.getFullyQualifiedName();

								if (map.keySet().contains(_className)) {
									_result = true;
									break;
								}
							}
						}
					} catch (JavaModelException _jme) {
						SECommons.handleException(_jme);
					}
					return _result;
				}
			});
	}

	/**
	 * Toggles the higlighting in the editor.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		final AddIndusAnnotation _indusA = KaveriPlugin.getDefault().getIndusConfiguration().getIndusAnnotationManager();
		IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
		boolean _properNature = false;
		try {
		 _properNature = _file.getProject().hasNature("org.eclipse.jdt.core.javanature");
		}
		catch (CoreException _ce) {			
			return;
		}
		if (! _properNature) {
			return;
		}
		if (_indusA.isAreAnnotationsPresent(editor)) {
			_indusA.setEditor(editor, false);
		} else {
			final IFile _fl =  ((IFileEditorInput) editor.getEditorInput()).getFile();
			final Map _map = KaveriPlugin.getDefault().getIndusConfiguration().getLineNumbers();
			_indusA.setEditor(editor, _map);
		}
	}

	/**
	 * Handles the selection change event.
	 *
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
		boolean _properNature = false;
		try {
		 _properNature = _file.getProject().hasNature("org.eclipse.jdt.core.javanature");
		}
		catch (CoreException _ce) {			
			return;
		}
		if (!_properNature) {
			action.setEnabled(false);
		}
	}
}
