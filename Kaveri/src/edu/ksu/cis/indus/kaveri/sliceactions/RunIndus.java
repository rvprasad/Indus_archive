/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

/*
 * Created on Apr 1, 2004
 *
 * The main popup implementation
 *
 */
package edu.ksu.cis.indus.kaveri.sliceactions;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.kaveri.driver.J2BIndusRunner;
import edu.ksu.cis.indus.kaveri.driver.KaveriIndusRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import soot.G;

/**
 * Runs the slicer on the chosen file or project. Popup action.
 * 
 * @author Ganeshan
 */
public class RunIndus
		extends BasicSliceFunctions
		implements IObjectActionDelegate {

	/**
	 * The java file.
	 */
	ISelection selection;

	/**
	 * The parent of this action.
	 */
	private IWorkbenchPart targetPart;

	/**
	 * (non-Javadoc).
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(@SuppressWarnings("unused") final IAction action,
			@SuppressWarnings("hiding") final IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	/**
	 * Performs the slice file action.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(@SuppressWarnings("unused") final IAction action) {
		final Shell _parentShell = targetPart.getSite().getShell();
		List _fileList = preRunProcessing(selection, _parentShell);

		if (_fileList != null && _fileList.size() > 0) {
			KaveriPlugin.getDefault().getIndusConfiguration().reset();
			if (!KaveriPlugin.getDefault().getIndusConfiguration().isDoResidualize()) {
				performNormalSlice(_parentShell, _fileList);
			} else {
				performJ2BSlice(_parentShell, _fileList);
			}
		}
	}

	/**
	 * Perform the J2B slice.
	 * 
	 * @param parentShell
	 * @param fileList
	 */
	private void performJ2BSlice(Shell parentShell, List fileList) {
		final SliceProgressBar _dialog = new SliceProgressBar(parentShell);

		final J2BIndusRunner _runner = new J2BIndusRunner(fileList, _dialog, cpSet);

		if (!_runner.doWork()) {
			return;
		}

		try {
			_dialog.run(true, true, _runner);
			KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
			performNormalSlice(parentShell, fileList);
			KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
		} catch (InvocationTargetException _ie) {
			KaveriErrorLog.logException("Invocation Target Exception", _ie);
			SECommons.handleException(_ie);
			G.reset();
			KaveriPlugin.getDefault().getIndusConfiguration().resetAll();
			KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
		} catch (InterruptedException _ie) {
			KaveriErrorLog.logException("Interrupted Exception", _ie);
			SECommons.handleException(_ie);
			G.reset();
			KaveriPlugin.getDefault().getIndusConfiguration().resetAll();
			KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
		}

	}

	/**
	 * Perform the normal slice.
	 * 
	 * @param parentShell The parent shell
	 * @param fileList The list of files to slice.
	 */
	private void performNormalSlice(final Shell parentShell, final List fileList) {
		final SliceProgressBar _dialog = new SliceProgressBar(parentShell);
		final KaveriIndusRunner _runner = new KaveriIndusRunner(fileList, _dialog, cpSet);

		if (!_runner.doWork()) {
			return;
		}

		try {
			_dialog.run(true, true, _runner);
		} catch (InvocationTargetException _ie) {
			KaveriErrorLog.logException("Invocation Target Exception", _ie);
			SECommons.handleException(_ie);
			G.reset();
			KaveriPlugin.getDefault().getIndusConfiguration().resetAll();
			KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
		} catch (InterruptedException _ie) {
			KaveriErrorLog.logException("Interrupted Exception", _ie);
			SECommons.handleException(_ie);
			G.reset();
			KaveriPlugin.getDefault().getIndusConfiguration().resetAll();
			KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver().reset();
		}
	}

	/**
	 * Stores the new selection.
	 * 
	 * @see org.eclipse.ui.IActionDelegate #selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(@SuppressWarnings("unused") final IAction action, final ISelection iselection) {
		this.selection = iselection;
	}

}
