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
package edu.ksu.cis.indus.toolkits.sliceeclipse.popup.actions;


import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.common.SECommons;
import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.IndusConfigurationDialog;
import edu.ksu.cis.indus.toolkits.sliceeclipse.execute.IndusRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
 * 
 * 
 */
public class RunIndusProject implements IObjectActionDelegate {

	/**
	 * The java project.
	 */
	ISelection selection;

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(final IAction action,
			final IWorkbenchPart targetPart) {

	}

	/**
	 * Run indus on the project.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		List _fileList = null;
		_fileList = preRunProcessing();
		if (_fileList != null && _fileList.size() > 0) {
			SliceEclipsePlugin.getDefault().getIndusConfiguration().reset();
			final IndusRunner _runner = new IndusRunner(_fileList);
			if (!_runner.doWork()) {
				return;
			}
			final Display _display = Display.getCurrent();
			final Shell _shell = new Shell();
			try {
				final ProgressMonitorDialog _dialog = new ProgressMonitorDialog(
						_shell);
				_dialog.run(true, false, _runner);
			} catch (InvocationTargetException _ie) {
				SECommons.handleException(_ie);
			} catch (InterruptedException _ie) {
				SECommons.handleException(_ie);
			}
		}
	}

	/**
	 * Returns the resource corresponding to the chosen file.
	 * 
	 * @return IResource The resource
	 */
	private List preRunProcessing() {
		List _filelst = null;
		IJavaProject _jproject = null;

		if ((selection instanceof IStructuredSelection)) {
			final IStructuredSelection _structuredSelection = (IStructuredSelection) selection;
			if ((_structuredSelection.getFirstElement() instanceof IJavaProject)) {
				_jproject = (IJavaProject) _structuredSelection
						.getFirstElement();
			} else if (_structuredSelection.getFirstElement() instanceof IProject) {
				final IProject _project = (IProject) _structuredSelection
						.getFirstElement();
				_jproject = JavaCore.create(_project);
			}
		}
		if (_jproject != null) {
			final IndusConfigurationDialog _indusDialog = new IndusConfigurationDialog(
					new Shell(), _jproject);
			if (_indusDialog.open() == IDialogConstants.OK_ID) {
				_filelst = processForFiles(_jproject);
			}
		}
		return _filelst;
	}

	/**
	 * Returns list of java files in project.
	 * @param jproject The java project.
	 * @return List The list of java files.
	 */
	private List processForFiles(final IJavaProject jproject) {
		ArrayList _javaFileList = new ArrayList();
		try {
			final IPackageFragment[] _fragments = jproject
					.getPackageFragments();
			for (int _i = 0; _i < _fragments.length; _i++) {
				final IPackageFragment _fragment = _fragments[_i];
				if (_fragment.containsJavaResources()) {
					final ICompilationUnit[] _units = _fragment
							.getCompilationUnits();
					for (int _j = 0; _j < _units.length; _j++) {
						if (_units[_j].getElementType() == IJavaElement.COMPILATION_UNIT) {
							final IResource _resource = _units[_j]
									.getCorrespondingResource();
							_javaFileList.add(_resource);
						}
					}
				}
			}
		} catch (JavaModelException _jme) {
			SECommons.handleException(_jme);
			_javaFileList = null;
		}
		return _javaFileList;
	}

	/**
	 * (non-Javadoc).
	 *
	 * @see org.eclipse.ui.IActionDelegate     #selectionChanged(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action,
			final ISelection iselection) {
		// 
		this.selection = iselection;
	}
}