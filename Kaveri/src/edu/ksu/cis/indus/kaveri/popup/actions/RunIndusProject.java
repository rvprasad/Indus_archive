
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
 * Created on Apr 1, 2004
 *
 * The main popup implementation
 *
 */
package edu.ksu.cis.indus.kaveri.popup.actions;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.IndusConfigurationDialog;
import edu.ksu.cis.indus.kaveri.execute.IndusRunner;

import java.lang.reflect.InvocationTargetException;

import java.util.List;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Runs the slicer on the chosen file. Popup action.
 *
 * @author Ganeshan
 */
public class RunIndusProject
  implements IObjectActionDelegate {
	/** 
	 * The selected java project.
	 */
	ISelection selection;

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}

	/**
	 * Performs the slice project action.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		List _fileList = null;
		_fileList = preRunProcessing();

		if (_fileList != null && _fileList.size() > 0) {
			KaveriPlugin.getDefault().getIndusConfiguration().reset();

			final IndusRunner _runner = new IndusRunner(_fileList);

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

	/**
	 * Stores the current selection.
	 *
	 * @see org.eclipse.ui.IActionDelegate     #selectionChanged(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection iselection) {
		// 
		this.selection = iselection;
	}

	/**
	 * Shows the IndusConfigurationDialog.
	 *
	 * @return List The list of java files.
	 */
	private List preRunProcessing() {
		List _filelst = null;
		IJavaProject _jproject = null;

		if ((selection instanceof IStructuredSelection)) {
			final IStructuredSelection _structuredSelection = (IStructuredSelection) selection;

			if ((_structuredSelection.getFirstElement() instanceof IJavaProject)) {
				_jproject = (IJavaProject) _structuredSelection.getFirstElement();
			} else if (_structuredSelection.getFirstElement() instanceof IProject) {
				final IProject _project = (IProject) _structuredSelection.getFirstElement();
				_jproject = JavaCore.create(_project);
			}
		}

		if (_jproject != null) {
			final IndusConfigurationDialog _indusDialog = new IndusConfigurationDialog(new Shell(), _jproject);

			if (_indusDialog.open() == IDialogConstants.OK_ID) {
				_filelst = SECommons.processForFiles(_jproject);
			}
		}
		return _filelst;
	}
}
