
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
 * Created on Apr 5, 2004
 *
 *
 *
 */
package edu.ksu.cis.indus.kaveri.execute;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.decorator.IndusDecorator;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.kaveri.driver.SootConvertor;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;
import edu.ksu.cis.indus.slicer.ISliceCriterion;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This does the bulk of the call to the eclipse indus driver.
 * The settings for the slice are currently stored in IndusConfiguration  
 *
 * @author Ganeshan
 */
public class IndusRunner
  implements IRunnableWithProgress {
	/** 
	 * Java Editor instance.
	 */
	CompilationUnitEditor editor;

	/** 
	 * <p>
	 * The eclipse indus driver.
	 * </p>
	 */
	EclipseIndusDriver driver;

	/** 
	 * <p>
	 * List of java files to be sliced.
	 * </p>
	 */
	List fileList;
	
	/**
	 * All the java files in the given project.
	 */
	List completeFileList;

	/**
	 * Creates a new IndusRunner object.
	 *
	 * @param filesList The file pointing to the java file being sliced
	 */
	public IndusRunner(final List filesList) {
		this.fileList = filesList;
		driver = KaveriPlugin.getDefault().getIndusConfiguration().getEclipseIndusDriver();
		editor = null;
		completeFileList = null;
	}

	/**
	 * Sets the current editor. Used to show the highlighting 
	 * in case of backward and forward slicing.
	 * @param ceditor The editor to set.
	 */
	public void setEditor(final CompilationUnitEditor ceditor) {
		this.editor = ceditor;
	}

	/**
	 * Sets up the slice parameters.
	 *
	 * @return boolean True if the slicer was set up properly.
	 */
	public boolean doWork() {
		return setUp();
	}

	/**
	 * Runs the slice using the driver.
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IProgressMonitor monitor)
	  throws InvocationTargetException, InterruptedException {
		monitor.beginTask(Messages.getString("IndusRunner.1"), IProgressMonitor.UNKNOWN);  //$NON-NLS-1$
		driver.execute();
		monitor.done();
		KaveriPlugin.getDefault().getIndusConfiguration().setLineNumbers(driver.getAnnotationLineNumbers());

		final IndusDecorator _decorator = IndusDecorator.getIndusDecorator();
		if (fileList.size() > 0) {
			final IFile _file = (IFile) fileList.get(0);
			final IProject _pr = _file.getProject();
			final IJavaProject _jp = JavaCore.create(_pr);
			final List _flist = completeFileList;
			if (_flist != null) {
			KaveriPlugin.getDefault().getIndusConfiguration().setSliceFileList(_flist);
			}
			completeFileList = _flist;
		}
		
		if (_decorator != null) {
			_decorator.refesh();
		}

		if (editor != null) {
			highlightEditor();
		}
		returnCriteriaToPool();
	}

	/**
	 * Sets up the driver with the correct values.
	 *
	 * @return boolean True if the slicer is setup correctly.
	 */
	protected boolean setUp() {
		driver.reset();
		removeAnnotations();

		final String _configString = KaveriPlugin.getDefault().getIndusConfiguration().getCurrentConfiguration();
		boolean _indusRun = true;

		try {
			if (_configString == null || _configString.equals("")) {  //$NON-NLS-1$

				final URL _url = KaveriPlugin.getDefault().getBundle().getEntry(Messages.getString("IndusRunner.3"));
				driver.setConfiguration(_url);
			} else {
				driver.setConfiguration(_configString);
			}

			String _sootClassPath = "";  //$NON-NLS-1$
			IPath _jreclasspath = JavaCore.getClasspathVariable(Messages.getString("IndusRunner.5"));  //$NON-NLS-1$
			_jreclasspath = JavaCore.getClasspathVariable(Messages.getString("IndusRunner.6"));  //$NON-NLS-1$		

			final String _pathseparator = System.getProperty(Messages.getString("IndusRunner.7"));  //$NON-NLS-1$
			final String _fileseparator = System.getProperty(Messages.getString("IndusRunner.8"));  //$NON-NLS-1$

			if (_jreclasspath != null) {
				_sootClassPath = _jreclasspath.toOSString();
				_sootClassPath += _pathseparator;

				if (fileList.size() > 0) {
					final IFile _file = (IFile) fileList.get(0);
					final IPath _path = _file.getProject().getLocation();
					_sootClassPath += _path.toOSString();
					_sootClassPath += _fileseparator + _pathseparator;
				}

				driver.addToPath(_sootClassPath);

				final List _classNamesList = new LinkedList();

				for (int _i = 0; _i < fileList.size(); _i++) {
					final IFile _file = (IFile) fileList.get(_i);
					final ICompilationUnit _icunit = (ICompilationUnit) JavaCore.create(_file);

					if (_icunit != null) {
						IType[] _types = null;

						_types = _icunit.getAllTypes();

						for (int _nrun = 0; _nrun < _types.length; _nrun++) {
							final String _elemName = _types[_nrun].getFullyQualifiedName();
							_classNamesList.add(_elemName);
						}
					}
				}

				driver.setClassNames(_classNamesList);
				driver.initializeSlicer();
				setApplicationClasses();
				setupCriteria();
			} else {
				_indusRun = false;
				MessageDialog.openError(null, Messages.getString("IndusRunner.9"), Messages.getString("IndusRunner.12"));
			}
		} catch (IOException _ie) {
			SECommons.handleException(_ie);
			_indusRun = false;
		} catch (JavaModelException _jme) {
			SECommons.handleException(_jme);
			_indusRun = false;
		}
		return _indusRun;
	}

	/**
	 * Sets all the classes in the file to application classes.
	 * 
	 */
	private void setApplicationClasses() {
		if (fileList.size() > 0) {
			final IFile _file = (IFile) fileList.get(0);
			final IProject _project = _file.getProject();
			final IJavaProject _jproject = JavaCore.create(_project);
			final List _listoffiles = SECommons.processForFiles(_jproject);
			if (_listoffiles != null && _listoffiles.size() > 0) {
				completeFileList = _listoffiles;
				final List _classlist = new LinkedList();
				for (int _i = 0; _i < _listoffiles.size(); _i++) {
					final IFile _jfile = (IFile) _listoffiles.get(_i);
					_classlist.addAll(getClassesInFile(_jfile));
				}
				driver.setApplicationClasses(_classlist);
			}
		}
	}

	/**
	 * Returns a list of class names in the given java file.
	 * 
	 * @param jfile
	 *            The file in which to find the class names.
	 * @return List The list of classnames in the file.
	 */
	private List getClassesInFile(final IFile jfile) {
		final List _cllist = new LinkedList();
		final ICompilationUnit _icunit = (ICompilationUnit) JavaCore.create(jfile);
		

		try {
			if (_icunit != null) {
				IType[] _types = null;
				_types = _icunit.getAllTypes();

				for (int _nrun = 0; _nrun < _types.length; _nrun++) {
					final IType _type = _types[_nrun];
					_cllist.add(_type.getFullyQualifiedName());
				}
			}
		} catch (JavaModelException _jme) {
			SECommons.handleException(_jme);
		}		

		return _cllist;
	}

	/**
	 * Sets the criteria.
	 *
	 * @param file The Java file in which the criteria is present
	 * @param type The JDT class of the criteria.
	 * @param array The set of criteria chosen.
	 * @param methodName The method name
	 * @param stindex The index of the chosen Jimple Stmt in the list of Stmts
	 * @param nLine The selected line number.
	 * @param considerVal Consider value for execution.
	 */
	private void setCriteria(final IFile file, final IType type, final List array, final String methodName,
		final int stindex, final int nLine, final boolean considerVal) {
		try {
			final IMethod[] _methods = type.getMethods();

			for (int _j = 0; _j < _methods.length; _j++) {
				final IMethod _method = _methods[_j];

				if (PrettySignature.getMethodSignature(_method).equals(methodName)) {
					final List _stmtlist = SootConvertor.getStmtForLine(file, type, _method, nLine);

					if (_stmtlist != null && _stmtlist.size() >= 3) {
						final SootMethod _sootmethod = (SootMethod) _stmtlist.get(1);
						final Stmt _stmt = (Stmt) _stmtlist.get(2 + stindex);
						driver.setCriteria(_sootmethod, _stmt, considerVal);
					//	System.out.println(Messages.getString("IndusRunner.15") + _stmt);  //$NON-NLS-1$
					}
					break;
				}
			}
		}  catch (JavaModelException _e) {
			SECommons.handleException(_e);
		}
	}

//	/**
//	 * Dumps the jimple representation of the classes in
//	 * the directory where the sliced files are present.
//	 */
//	private void dumpJimple() {
//		final IFile _file = (IFile) fileList.get(0);
//		final String _op = _file.getLocation().removeLastSegments(1).toOSString();
//
//		try {
//			driver.dumpJimple(_op);
//		} catch (KaveriException _ie) {
//			SECommons.handleException(_ie);
//		}
//	}

	/**
	 * Returns the list of selected criteria.
	 *
	 * @return List The list of criteria.
	 */
	private List fetchCriteria() {
		return KaveriPlugin.getDefault().getIndusConfiguration().getCriteria();
	}

	/**
	 * Highlights the editor with annotations.
	 * Used only with backward or forward slicing.
	 */
	private void highlightEditor() {
		final Map _map = KaveriPlugin.getDefault().getIndusConfiguration().getLineNumbers();
		final AddIndusAnnotation _manager =
			KaveriPlugin.getDefault().getIndusConfiguration().getIndusAnnotationManager();
		_manager.setEditor(editor, _map);
	}

	/**
	 * Matches the set of criteria with the set of files chosen.
	 *
	 * @param file One of the chosen Java files.
	 * @param type The JDT class
	 * @param array The set of criteria
	 */
	private void matchAndSet(final IFile file, final IType type, final List array) {
		for (int _i = 0; _i < array.size(); _i++) {
			final Criteria _c = (Criteria) array.get(_i);
			final String _classname = _c.getStrClassName();
			final String _methodname = _c.getStrMethodName();
			final int _nLine = _c.getNLineNo();
			final int _stindex = _c.getNJimpleIndex();
			final boolean _considerVal = _c.isBConsiderValue();

			if (PrettySignature.getSignature(type).equals(_classname)) {
				setCriteria(file, type, array, _methodname, _stindex, _nLine, _considerVal);
			}
		}
	}

	/**
	 * Removes all current slice  annotations on all open Java editors.
	 */
	private void removeAnnotations() {
		final IWorkbenchWindow[] _windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		final AddIndusAnnotation _manager =
			KaveriPlugin.getDefault().getIndusConfiguration().getIndusAnnotationManager();

		for (int _i = 0; _windows != null && _i < _windows.length; _i++) {
			final IWorkbenchWindow _window = _windows[_i];
			final IWorkbenchPage[] _pages = _window.getPages();

			for (int _j = 0; _pages != null && _j < _pages.length; _j++) {
				final IEditorReference[] _references = _pages[_j].getEditorReferences();

				for (int _k = 0; _references != null && _k < _references.length; _k++) {
					final IEditorReference _reference = _references[_k];
					final String _id = _reference.getId();

					if (_id.equals("org.eclipse.jdt.ui.CompilationUnitEditor")) {
						final CompilationUnitEditor _edPart = (CompilationUnitEditor) _reference.getEditor(false);

						if (_edPart != null) {
							_manager.setEditor(_edPart, false);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the criteria to the pool.
	 */
	private void returnCriteriaToPool() {
		final Collection _coll = driver.getSlicer().getCriteria();
		final Iterator _it = _coll.iterator();

		while (_it.hasNext()) {
			final ISliceCriterion _crt = (ISliceCriterion) _it.next();
			_crt.returnToPool();
		}
	}

	/**
	 * Sets up the criteria.
	 */
	private void setupCriteria() {
		final List _crArray = fetchCriteria();

		if (_crArray != null) {
			for (int _i = 0; _i < fileList.size(); _i++) {
				final IFile _file = (IFile) fileList.get(_i);
				final ICompilationUnit _icunit = (ICompilationUnit) JavaCore.create(_file);

				if (_icunit != null) {
					IType[] _types = null;

					try {
						_types = _icunit.getAllTypes();

						for (int _j = 0; _j < _types.length; _j++) {
							final IType _type = _types[_j];

							if (_crArray.size() > 0) {
								matchAndSet(_file, _type, _crArray);
							}
						}
					} catch (JavaModelException _e) {
						SECommons.handleException(_e);
					}
				}
			}
		}
	}
}
