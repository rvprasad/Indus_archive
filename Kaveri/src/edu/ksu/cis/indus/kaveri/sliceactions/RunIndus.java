
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
package edu.ksu.cis.indus.kaveri.sliceactions;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.kaveri.driver.IndusRunner;

import java.lang.reflect.InvocationTargetException;

import java.util.List;


import org.eclipse.jface.action.IAction;



import org.eclipse.jface.viewers.ISelection;


import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Runs the slicer on the chosen file or project. Popup action.
 *
 * @author Ganeshan
 */
public class RunIndus
  extends BasicSliceFunctions implements IObjectActionDelegate {
	/** 
	 * The java file.
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
	 * Performs the slice file action.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		List _fileList = null;
		_fileList = preRunProcessing(selection);

		if (_fileList != null && _fileList.size() > 0) {
			KaveriPlugin.getDefault().getIndusConfiguration().reset();

			final Display _display = Display.getCurrent();
			final Shell _shell = new Shell();
			final SliceProgressBar _dialog = new SliceProgressBar(_shell);
			final IndusRunner _runner = new IndusRunner(_fileList, _dialog);

			if (!_runner.doWork()) {
				return;
			}


			try {
				_dialog.run(true, true, _runner);
			} catch (InvocationTargetException _ie) {
				SECommons.handleException(_ie);
			} catch (InterruptedException _ie) {
				SECommons.handleException(_ie);
			}
		}
	}

	//	/**
	//	 * Opens the file in an editor.
	//	 * @param cunit The compilation unit
	//	 * @param file The sliced Java file
	//	 */
	//	private void openElementInEditor(final IFile file, final CompilationUnit cunit) {
	//		try {
	//			final CompilationUnitEditor _ed = (CompilationUnitEditor) JavaUI.openInEditor(cunit);
	//
	//			if (_ed != null) {
	//				final AddIndusAnnotation _manager = KaveriPlugin
	//						.getDefault().getIndusConfiguration()
	//						.getIndusAnnotationManager();
	//				_manager.setEditor(_ed, KaveriPlugin.getDefault()
	//						.getIndusConfiguration().getLineNumbers());
	//			}
	//		} catch (JavaModelException _e) {
	//			SECommons.handleException(_e);			
	//		} catch (PartInitException _pe) {
	//			_pe.printStackTrace();
	//			SECommons.handleException(_pe);			
	//		}
	//	}

	/**
	 * Stores the new selection.
	 *
	 * @see org.eclipse.ui.IActionDelegate     #selectionChanged(org.eclipse.jface.action.IAction,
	 * 		org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection iselection) {
		// 
		this.selection = iselection;
	}
	
}
