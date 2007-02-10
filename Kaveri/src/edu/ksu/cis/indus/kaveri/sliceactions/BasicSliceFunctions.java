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
 * Created on Aug 17, 2004
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.sliceactions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;

import soot.G;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.PrettySignature;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.IndusConfigurationDialog2;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.kaveri.driver.KaveriIndusRunner;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * The basic slice functions class. This class implements the basic functions needed by the remaining slice action classes.
 * 
 * @author ganeshan
 */
abstract public class BasicSliceFunctions {

	/**
	 * The set of classpath entries.
	 */
	protected Set cpSet;

	/**
	 * Returns the set of files chosen for slicing. If a file is chosen only it is returned. If a project has been chosen, it
	 * returns all the files present in the project.
	 * 
	 * @param selection The chosen element.
	 * @param parentShell The parent shell
	 * @return List The list of java files.
	 */
	protected List preRunProcessing(ISelection selection, Shell parentShell) {
		List _filelst = new LinkedList();
		IJavaProject _jproject = null;
		IResource _resource = null;
		if ((selection instanceof IStructuredSelection)) {
			final IStructuredSelection _structuredSelection = (IStructuredSelection) selection;

			if ((_structuredSelection.getFirstElement() instanceof IJavaProject)) {
				_jproject = (IJavaProject) _structuredSelection.getFirstElement();
			} else if (_structuredSelection.getFirstElement() instanceof IProject) {
				final IProject _project = (IProject) _structuredSelection.getFirstElement();
				_jproject = JavaCore.create(_project);
			} else if (_structuredSelection.getFirstElement() instanceof CompilationUnit) {
				final CompilationUnit _cunit = (CompilationUnit) _structuredSelection.getFirstElement();

				try {
					_resource = _cunit.getCorrespondingResource();
					if (_resource != null && _resource.getType() == IResource.FILE) {
						_jproject = JavaCore.create(_resource.getProject());
					}
				} catch (JavaModelException _e) {
					KaveriErrorLog.logException("Java Model Exception", _e);
					SECommons.handleException(_e);
					return null;
				}
			} else if (_structuredSelection.getFirstElement() instanceof IFile) {
				_resource = ((IFile) _structuredSelection.getFirstElement());
				if (_resource != null && _resource.getType() == IResource.FILE) {
					_jproject = JavaCore.create(_resource.getProject());
				}
			}
			if (_jproject != null) {
				final IProject _prj = _jproject.getProject();
				if (_prj == null || !hasJavaNature(_prj)) {
					return null;
				}
				final IndusConfigurationDialog2 _indusDialog = new IndusConfigurationDialog2(parentShell, _jproject);
				KaveriPlugin.getDefault().getIndusConfiguration().getCriteria().clear();
				KaveriPlugin.getDefault().getIndusConfiguration().getChosenContext().clear();
				cpSet = _indusDialog.getClassPathSet();
				if (_indusDialog.open() == IDialogConstants.OK_ID) {
					_filelst = SECommons.processForFiles(_jproject);
				}
			}
		}

		return _filelst;
	}

	/**
	 * Check the nature.
	 * 
	 * @param _prj
	 * @return
	 */
	private boolean hasJavaNature(IProject prj) {
		boolean _hasNature = false;
		try {
			_hasNature = prj.hasNature("org.eclipse.jdt.core.javanature");
		} catch (CoreException e) {
			_hasNature = false;
		}
		return _hasNature;
	}

	/**
	 * Runs the backward slice.
	 * 
	 * @param sliceType The slice type - either "backward-executable" or "forward"
	 * @param editor The Java editor
	 * @param textSelection The selected text
	 * @param postExec Whether post execution is enabled.
	 * @param parentShell The parent shell
	 */
	protected void runSlice(final String sliceType, final CompilationUnitEditor editor, ISelection textSelection,
			final boolean postExec, Shell parentShell) {
		final ITextSelection _tselection = (ITextSelection) textSelection;
		final int _nSelLine = _tselection.getEndLine() + 1; // Havent figured
		// out whats the 1
		// for

		try {
			final IType _type = SelectionConverter.getTypeAtOffset(editor);
			final IJavaElement _element = SelectionConverter.getElementAtOffset(editor);

			if (_element != null && _element instanceof IMethod) {
				final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
				if (!hasJavaNature(_file)) {
					return;
				}
				final List _stmtlist = SootConvertor.getStmtForLine(_file, _type, (IMethod) _element, _nSelLine);

				if (_stmtlist != null && _stmtlist.size() >= 3) {

					// Format: Classname: qualified signature, method:
					// signature, line no
					final int _noStmts = _stmtlist.size() - 2;
					final Criteria _c = new Criteria();
					_c.setStrClassName(PrettySignature.getSignature(_type));
					_c.setStrMethodName(PrettySignature.getSignature(_element));
					_c.setNLineNo(_nSelLine);
					_c.setNJimpleIndex(_noStmts - 1);
					_c.setBConsiderValue(postExec);

					KaveriPlugin.getDefault().getIndusConfiguration().setAdditive(false);
					KaveriPlugin.getDefault().getIndusConfiguration().reset();
					KaveriPlugin.getDefault().getIndusConfiguration().setDoResidualize(false);
					KaveriPlugin.getDefault().getIndusConfiguration().resetChosenContext();
					KaveriPlugin.getDefault().getIndusConfiguration().getCriteria().clear();
					final SlicerTool _sTool = KaveriPlugin.getDefault().getSlicerTool();
					if (_sTool.getCurrentConfiguration() == null) {
						KaveriPlugin.getDefault().loadConfigurations();
					}
					KaveriPlugin.getDefault().getIndusConfiguration().setCurrentConfiguration(sliceType);
					KaveriPlugin.getDefault().getIndusConfiguration().setCriteria(_c);
					KaveriPlugin.getDefault().getIndusConfiguration().setScopeSpecification("");
					final List _lst = SECommons.checkForRootMethods(_file);
					final SliceProgressBar _dialog = new SliceProgressBar(parentShell);
					final KaveriIndusRunner _runner = new KaveriIndusRunner(_lst, _dialog, Collections.EMPTY_SET);
					_runner.setEditor(editor);

					if (!_runner.doWork()) {
						return;
					}

					try {
						_dialog.run(true, true, _runner);

					} catch (InvocationTargetException _ie) {
						KaveriErrorLog.logException("Invocation Target Exception", _ie);
						// Reset.
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
			}
		} catch (JavaModelException _jme) {
			SECommons.handleException(_jme);
		}
	}

	/**
	 * Checks for java nature.
	 * 
	 * @param _file
	 * @return
	 */
	private boolean hasJavaNature(IFile file) {
		boolean _hasNature = false;
		try {
			_hasNature = file.getProject().hasNature("org.eclipse.jdt.core.javanature");
		} catch (CoreException e) {
			_hasNature = false;
		}
		return _hasNature;
	}

	/**
	 * Validates the given configuration name
	 * 
	 * @param configName The name of the configuration.
	 * @return boolean True if the configuration is present
	 */
	protected boolean validateConfiguration(final String configName) {
		boolean _result = false;
		final SlicerTool _sTool = KaveriPlugin.getDefault().getSlicerTool();
		if (_sTool.getCurrentConfiguration() == null) {
			KaveriPlugin.getDefault().loadConfigurations();
		}
		final Collection _configColl = KaveriPlugin.getDefault().getSlicerTool().getConfigurations();
		for (Iterator iter = _configColl.iterator(); iter.hasNext();) {
			final IToolConfiguration _config = (IToolConfiguration) iter.next();
			if (_config.getConfigName().equals(configName)) {
				_result = true;
				break;
			}

		}
		return _result;
	}
}
