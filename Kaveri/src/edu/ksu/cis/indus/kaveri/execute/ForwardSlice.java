
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
 */
package edu.ksu.cis.indus.kaveri.execute;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.driver.SootConvertor;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;


/**
 * Runs a forward slice on the chosen Java statement.
 *
 * @author Ganeshan
 */
public class ForwardSlice
  implements IEditorActionDelegate {
	/** 
	 * The Java editor.
	 */
	private CompilationUnitEditor editor;

	/** 
	 * The text selection.
	 */
	private ISelection textSelection;

	/**
	 * Indicates the current Java editor instance.
	 *
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		editor = (CompilationUnitEditor) targetEditor;
	}

	/**
	 * Run the forward slice action.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					runForwardSlice();
				}
			});
	}

	/**
	 * Store the current selection.
	 *
	 * @param action The action
	 * @param selection The selected text
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.textSelection = selection;
	}

	/**
	 * Runs the Forward slice.
	 */
	private void runForwardSlice() {
		final ITextSelection _tselection = (ITextSelection) textSelection;
		final String _text = _tselection.getText();
		final int _nSelLine = _tselection.getEndLine() + 1;  // Havent figured out whats the 1 for

		try {
			final IType _type = SelectionConverter.getTypeAtOffset(editor);
			final IJavaElement _element = SelectionConverter.getElementAtOffset(editor);

			if (_element != null && _element instanceof IMethod) {
				final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
				final boolean _properNature = _file.getProject().hasNature("org.eclipse.jdt.core.javanature");
				if (! _properNature) {
					throw new IllegalArgumentException("File does not have java nature");
				}
				final List _stmtlist = SootConvertor.getStmtForLine(_file, _type, (IMethod) _element, _nSelLine);

				if (_stmtlist != null && _stmtlist.size() >= 3) {
					final List _storeLst = new ArrayList();

					// Format: Classname: qualified signature, method: signature, line no
					final int _noStmts = _stmtlist.size() - 2;
					final Criteria _c = new Criteria();
					_c.setStrClassName(PrettySignature.getSignature(_type));
					_c.setStrMethodName(PrettySignature.getSignature(_element));
					_c.setNLineNo(_nSelLine);
					_c.setNJimpleIndex(_noStmts - 1);
					_c.setBConsiderValue(true);

					final String _configuration =
						KaveriPlugin.getDefault().getPreferenceStore().getString("forwardConfiguration");
					KaveriPlugin.getDefault().getIndusConfiguration().setAdditive(false);
					KaveriPlugin.getDefault().getIndusConfiguration().reset();
					KaveriPlugin.getDefault().getIndusConfiguration().getCriteria().clear();
					KaveriPlugin.getDefault().getIndusConfiguration().setCurrentConfiguration(_configuration);
					KaveriPlugin.getDefault().getIndusConfiguration().setCriteria(_c);

					final List _lst = SECommons.checkForRootMethods(_file);

					final IndusRunner _runner = new IndusRunner(_lst);
					_runner.setEditor(editor);
					if (!_runner.doWork()) {
						return;
					}

					final Display _display = Display.getCurrent();
					final Shell _shell = new Shell();

					try {
						final ProgressMonitorDialog _dialog = new ProgressMonitorDialog(_shell);
						_dialog.run(true, false, _runner);
					} catch (InvocationTargetException _ie) {
						SECommons.handleException(_ie);
					} catch (InterruptedException _ie) {
						SECommons.handleException(_ie);
					}
				}
			}
		} catch (JavaModelException _jme) {
			SECommons.handleException(_jme);
		} 
		catch (IllegalArgumentException _ile) {
			SECommons.handleException(_ile);
		}
		catch (CoreException _ce) {
				SECommons.handleException(_ce);
			}
	}
}
