
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.core.CompilationUnit;

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
public class RunIndus
  implements IObjectActionDelegate {
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
		IResource _resource = null;
		_resource = preRunProcessing();

		if (_resource != null) {
			final IFile _file = (IFile) _resource;
			KaveriPlugin.getDefault().getIndusConfiguration().reset();

			final List _fileList = SECommons.checkForRootMethods(_file);
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

	/**
	 * Returns the resource corresponding to the chosen file.
	 *
	 * @return IResource The resource
	 */
	private IResource preRunProcessing() {
		CompilationUnit _cunit;
		IResource _resource = null;

		if ((selection instanceof IStructuredSelection)) {
			final IStructuredSelection _structuredSelection = (IStructuredSelection) selection;

			if ((_structuredSelection.getFirstElement() instanceof CompilationUnit)) {
				_cunit = (CompilationUnit) _structuredSelection.getFirstElement();

				try {
					_resource = _cunit.getCorrespondingResource();
				} catch (JavaModelException _e) {
					_resource = null;
					SECommons.handleException(_e);
				}
			} else if (_structuredSelection.getFirstElement() instanceof IFile) {
				_resource = ((IFile) _structuredSelection.getFirstElement());
			}
		}

		if (_resource != null && _resource.getType() != IResource.FILE) {
			_resource = null;
		}

		if (_resource != null) {
			final IProject _rproject = _resource.getProject();
			final IJavaProject _jproject = JavaCore.create(_rproject);

			if (_jproject != null) {
				final IndusConfigurationDialog _indusDialog = new IndusConfigurationDialog(new Shell(), _jproject);

				if (_indusDialog.open() != IDialogConstants.OK_ID) {
					_resource = null;
				}
			}
		}
		return _resource;
	}
}
