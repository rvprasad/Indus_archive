
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
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package edu.ksu.cis.indus.toolkits.sliceeclipse.execute;

import edu.ksu.cis.indus.toolkits.eclipse.SootConvertor;
import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.ExceptionDialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;

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
 * DOCUMENT ME!
 *
 * @author Ganeshan TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 * 		   Generation - Code and Comments
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
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		editor = (CompilationUnitEditor) targetEditor;		
	}

	/**
	 * (non-Javadoc).
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
	 * (non-Javadoc).
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
				final ArrayList _stmtlist = SootConvertor.getStmtForLine(_file, _type, (IMethod) _element, _nSelLine);

				if (_stmtlist != null && _stmtlist.size() >= 3) {
					final ArrayList _storeLst = new ArrayList();

					// Format: Classname: qualified signature, method: signature, line no
					final int _noStmts = _stmtlist.size() - 2;
					_storeLst.add(PrettySignature.getSignature(_type));
					_storeLst.add(PrettySignature.getSignature(_element));
					_storeLst.add(new Integer(_nSelLine));
					_storeLst.add(new Integer(_noStmts - 1));

					final String _configuration =
						SliceEclipsePlugin.getDefault().getPreferenceStore().getString("forwardConfiguration");
					SliceEclipsePlugin.getDefault().getIndusConfiguration().reset();
					SliceEclipsePlugin.getDefault().getIndusConfiguration().getCriteria().clear();
					SliceEclipsePlugin.getDefault().getIndusConfiguration().setCurrentConfiguration(_configuration);
					SliceEclipsePlugin.getDefault().getIndusConfiguration().setCriteria(_storeLst);

					final List _lst = new LinkedList();
					_lst.add(_file);

					final IndusRunner _runner = new IndusRunner(_lst);

					if (!_runner.doWork()) {
						return;
					}

					final Display _display = Display.getCurrent();
					final Shell _shell = new Shell();

					try {
						final ProgressMonitorDialog _dialog = new ProgressMonitorDialog(_shell);
						_dialog.run(true, false, _runner);
					} catch (InvocationTargetException _ie) {
						final StringWriter _sw = new StringWriter();
						final PrintWriter _pw = new PrintWriter(_sw);
						_ie.printStackTrace(_pw);

						// _ie.printStackTrace();
						final ExceptionDialog _ed =
							new ExceptionDialog(Display.getDefault().getActiveShell(), _sw.getBuffer().toString());
						_ed.open();
					} catch (InterruptedException _ie) {
						final StringWriter _sw = new StringWriter();
						final PrintWriter _pw = new PrintWriter(_sw);
						_ie.printStackTrace(_pw);

						// _ie.printStackTrace();
						final ExceptionDialog _ed =
							new ExceptionDialog(Display.getDefault().getActiveShell(), _sw.getBuffer().toString());
						_ed.open();
					}
				}
			}
		} catch (JavaModelException _jme) {
			_jme.printStackTrace();
		}
	}
}
