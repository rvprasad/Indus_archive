
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

package edu.ksu.cis.indus.kaveri.popup.actions;


import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.driver.SootConvertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;


/**
 * Implements the view slice action. Updates the slice
 * view to show the Jimple elements in the slice of the
 * currently selected Java statement.
 *
 * @author Ganeshan
 */
public class PartialSlice
  implements IEditorActionDelegate {
	/** 
	 * The current java editor.
	 */
	CompilationUnitEditor editor;

	/** 
	 * The current selection.
	 */
	ISelection selection;

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
		}
	}

	/**
	 * Add the statement to the criteria if feasible.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		if (selection instanceof ITextSelection) {
			final ITextSelection _tselection = (ITextSelection) selection;
			final String _text = _tselection.getText();
			final int _nSelLine = _tselection.getEndLine() + 1;			
			try {
				final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
				final boolean _properNature = _file.getProject().hasNature("org.eclipse.jdt.core.javanature");
				if (! _properNature) {
					throw new IllegalArgumentException("File does not have java nature");
				} 
				final IType _type = SelectionConverter.getTypeAtOffset(editor);
				final IJavaElement _element = SelectionConverter.getElementAtOffset(editor);
				
				if (_element != null && (_element instanceof IMethod)) {
				    final Map _map = KaveriPlugin.getDefault().getIndusConfiguration()
					.getLineNumbers();
				    final String _classname = _type.getFullyQualifiedName();
				    
				    if (_map != null && _map.get(_classname) != null
				    		 && ((Map) _map.get(_classname)).size() > 0) {
				    	final IFile _file1 =  ((IFileEditorInput) editor.getEditorInput()).getFile();
						processStmtListForFile(_file1, _type, _element, _nSelLine);	
				    }
					
				}
			} catch (JavaModelException _e) {
				SECommons.handleException(_e);
			}
			catch (CoreException _ce) {
				SECommons.handleException(_ce);
			}
			catch (IllegalArgumentException _ile) {
				SECommons.handleException(_ile);
			}
		}
	}

	/**
	 * Stores the current selection in the editor.
	 *
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection textselection) {
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
		this.selection = textselection;
	}

	/**
	 * Sets up the given stmts for the view to display.
	 *
	 * @param stmtlist The list of Jimple Stmts
	 * 
	 */
	private void setupStmts(final List stmtlist) {
		final List _stlist = new ArrayList();

		for (int _i = 2; _i < stmtlist.size(); _i++) {
			_stlist.add(stmtlist.get(_i));
		}
		KaveriPlugin.getDefault().getIndusConfiguration()
		.setStmtList(_stlist);
	}

	/**
	 * Processes the criteria for the given file and Java Statement.
	 *
	 * @param file The Java file in which the criteria was picked
	 * @param type The class of the chosen Java statement
	 * @param element The IMethod of the chosen Java statement
	 * @param nSelLine The selected line number.
	 */
	private void processStmtListForFile(final IFile file, final IType type, final IJavaElement element, final int nSelLine) {
		final SootConvertor _sc;
		//final String _className = type.getElementName();

		try {
			final List _stmtlist = SootConvertor.getStmtForLine(file, type, (IMethod) element, nSelLine);

			if (_stmtlist != null && _stmtlist.size() >= 3) {
				
				final List _storeLst = new ArrayList();
				final int _noStmts = _stmtlist.size() - 2;				
				if (_noStmts >= 1) {
					setupStmts(_stmtlist);
				}
			}
		} catch (NullPointerException _ie) {
			SECommons.handleException(_ie);
		}
	}
}
