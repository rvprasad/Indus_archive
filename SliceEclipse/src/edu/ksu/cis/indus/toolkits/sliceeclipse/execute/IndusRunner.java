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
package edu.ksu.cis.indus.toolkits.sliceeclipse.execute;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import soot.SootMethod;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.toolkits.eclipse.EclipseIndusDriver;
import edu.ksu.cis.indus.toolkits.eclipse.SootConvertor;
import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.decorator.IndusDecorator;
import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.ExceptionDialog;
import edu.ksu.cis.indus.toolkits.sliceeclipse.presentation.AddIndusAnnotation;

/**
 * This does the bulk of the call to the eclipse indus driver.
 *
 * @author Ganeshan
 */
public class IndusRunner implements IRunnableWithProgress {
	/**
	 * <p>
	 * The eclipse indus driver.
	 * </p>
	 */
	EclipseIndusDriver driver;

	/**
	 * <p>
	 * Files pointing to the java file to be sliced.
	 * </p>
	 */
	List fileList;

	/** Java Editor. */
	CompilationUnitEditor editor;
	
	/**
	 * The output location.
	 */
	private String outputLocation = "."; //$NON-NLS-1$

	/**
	 * The length of the .java extension.
	 */
	private final short extensionLength = 5;

	/**
	 * Creates a new IndusRunner object.
	 *
	 * @param filesList The file pointing to the java file being sliced
	 * 
	 */
	public IndusRunner(final List filesList) {
		this.fileList = filesList;
		driver = SliceEclipsePlugin.getDefault().getIndusConfiguration()
				.getEclipseIndusDriver();
		editor = null;
	}

	/**
	 * <p>
	 * Indirect call to setup().
	 * @return boolean True if the slicer was set up properly.
	 * </p>
	 */
	public boolean doWork() {
		return setUp();
	}

	/**
	 * Runs the driver and provides a progress monitor to indicate progress.
	 *
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask(
				Messages.getString("IndusRunner.1"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		driver.execute();
		monitor.done();
		SliceEclipsePlugin.getDefault().getIndusConfiguration().setLineNumbers(
				driver.getAnnotationLineNumbers());
		final IndusDecorator _decorator = IndusDecorator.getIndusDecorator();
		SliceEclipsePlugin.getDefault().getIndusConfiguration()
				.setSliceFileList(fileList);
		if (_decorator != null) {
			_decorator.refesh();
		}
		if (editor != null) {
			highlightEditor();
		}
		//final Collection _coll = driver.getSlicer().getCriteria();
		//final Iterator _it = _coll.iterator();
		//while (_it.hasNext()) {
		// System.out.println(_it.next());
		// }
		//dumpJimple();

	}

	/**
	 * Highlights the editor with annotations.
	 */
	private void highlightEditor() {
		final HashMap _map = SliceEclipsePlugin.getDefault()
				.getIndusConfiguration().getLineNumbers();
		final AddIndusAnnotation _manager = SliceEclipsePlugin.getDefault()
				.getIndusConfiguration().getIndusAnnotationManager();
		_manager.setEditor(editor, _map);
	}

	/**
	 * 
	 */
	private void dumpJimple() {
		final IFile _file = (IFile) fileList.get(0);
		final String _op = _file.getLocation().removeLastSegments(1)
				.toOSString();
		driver.dumpJimple(_op);

	}

	/**
	 * Sets up the driver with the correct values.
	 * @return boolean True if the slicer is setup correctly.
	 */
	protected boolean setUp() {
		driver.reset();
		removeAnnotations();
		final String _configString = SliceEclipsePlugin.getDefault()
				.getIndusConfiguration().getCurrentConfiguration();
		boolean _indusRun = true;
		if (_configString == null || _configString.equals("")) { //$NON-NLS-1$
			final URL _url = SliceEclipsePlugin.getDefault().getBundle()
					.getEntry(Messages.getString("IndusRunner.3")); //$NON-NLS-1$
			driver.setConfiguration(_url);
		} else {
			driver.setConfiguration(_configString);
		}
		String _sootClassPath = ""; //$NON-NLS-1$
		IPath _jreclasspath = JavaCore.getClasspathVariable(Messages
				.getString("IndusRunner.5")); //$NON-NLS-1$
		_jreclasspath = JavaCore.getClasspathVariable(Messages
				.getString("IndusRunner.6")); //$NON-NLS-1$		
		final String _pathseparator = System.getProperty(Messages
				.getString("IndusRunner.7")); //$NON-NLS-1$
		final String _fileseparator = System.getProperty(Messages
				.getString("IndusRunner.8")); //$NON-NLS-1$
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
				final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
						.create(_file);
				if (_icunit != null) {
					IType[] _types = null;
					try {
						_types = _icunit.getAllTypes();
					} catch (JavaModelException _e) {
						MessageDialog.openError(null, Messages
								.getString("IndusRunner.9"), //$NON-NLS-1$
								Messages.getString("IndusRunner.10")); //$NON-NLS-1$
						_e.printStackTrace();
					}

					for (int _nrun = 0; _nrun < _types.length; _nrun++) {
						final String _elemName = _types[_nrun]
								.getFullyQualifiedName();
						_classNamesList.add(_elemName);
					}
				}
			}

			driver.setClassNames(_classNamesList);
			setupCriteria();
			try {
				driver.initializeSlicer();
			} catch (RuntimeException _rme) {
				final StringWriter _sw = new StringWriter();
				final PrintWriter _pw = new PrintWriter(_sw);
				_rme.printStackTrace(_pw);

				final ExceptionDialog _ed = new ExceptionDialog(Display
						.getDefault().getActiveShell(), _sw.getBuffer()
						.toString());
				_ed.open();
			}
		} else {
			_indusRun = false;
			MessageDialog.openError(null, Messages.getString("IndusRunner.9"),
					Messages.getString("IndusRunner.12")); //$NON-NLS-1$ //$NON-NLS-2$

		}
		return _indusRun;
	}

	/**
	 * Removes all current slice  annotations on all open Java editors.
	 * 
	 */
	private void removeAnnotations() {
		final IWorkbenchWindow[] _windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		final AddIndusAnnotation _manager = SliceEclipsePlugin.getDefault()
				.getIndusConfiguration().getIndusAnnotationManager();
		for (int _i = 0; _windows != null && _i < _windows.length; _i++) {
			final IWorkbenchWindow _window = _windows[_i];
			final IWorkbenchPage[] _pages = _window.getPages();
			for (int _j = 0; _pages != null && _j < _pages.length; _j++) {
				final IEditorReference[] _references = _pages[_j]
						.getEditorReferences();
				for (int _k = 0; _references != null && _k < _references.length; _k++) {
					final IEditorReference _reference = _references[_k];
					final String _id = _reference.getId();
					if (_id.equals("org.eclipse.jdt.ui.CompilationUnitEditor")) {
						final CompilationUnitEditor _edPart = (CompilationUnitEditor) _reference
								.getEditor(false);
						if (_edPart != null) {
							_manager.setEditor(_edPart, false);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets up the criteria properly. 
	 */
	private void setupCriteria() {		
		final ArrayList _crArray = fetchCriteria();
		if (_crArray != null) {
			for (int _i = 0; _i < fileList.size(); _i++) {
				final IFile _file = (IFile) fileList.get(_i);
				final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
						.create(_file);
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
						MessageDialog.openError(null, Messages
								.getString("IndusRunner.9"), //$NON-NLS-1$
								Messages.getString("IndusRunner.14")); //$NON-NLS-1$
						_e.printStackTrace();
					}

				}
			}
		}
	}

	/**
	 * Finds the correct set of criteria to match with the chosen set of files.
	 * @param file One of the chosen Java files.
	 * @param type The JDT class
	 * @param array The set of criteria
	 */
	private void matchAndSet(final IFile file, final IType type,
			final ArrayList array) {
		for (int _i = 0; _i < array.size(); _i++) {
			final ArrayList _lst = (ArrayList) array.get(_i);
			final int _criteriaExpSize = 4;
			if (_lst.size() == _criteriaExpSize) {
				final String _classname = (String) _lst.get(0);
				final String _methodname = (String) _lst.get(1);
				final int _nLine = ((Integer) _lst.get(2)).intValue();
				final int _stindex = ((Integer) _lst.get(3)).intValue();

				if (PrettySignature.getSignature(type).equals(_classname)) {
					setCriteria(file, type, array, _methodname, _stindex,
							_nLine);
				}
			}
		}
	}

	/**
	 * Sets the criteria.
	 * @param file The Java file in which the criteria is present
	 * @param type The JDT class of the criteria.
	 * @param array The set of criteria chosen.
	 * @param methodName The method name
	 * @param stindex The index of the chosen Jimple Stmt in the list of Stmts 
	 * @param nLine The selected line number.
	 */
	private void setCriteria(final IFile file, final IType type,
			final ArrayList array, final String methodName, final int stindex,
			final int nLine) {
		try {
			final IMethod[] _methods = type.getMethods();
			for (int _j = 0; _j < _methods.length; _j++) {
				final IMethod _method = _methods[_j];
				if (PrettySignature.getMethodSignature(_method).equals(
						methodName)) {
					final ArrayList _stmtlist = SootConvertor.getStmtForLine(
							file, type, _method, nLine);
					if (_stmtlist != null && _stmtlist.size() >= 3) {
						final SootMethod _sootmethod = (SootMethod) _stmtlist
								.get(1);
						final Stmt _stmt = (Stmt) _stmtlist.get(2 + stindex);
						driver.setCriteria(_sootmethod, _stmt);
						System.out
								.println(Messages.getString("IndusRunner.15") + _stmt); //$NON-NLS-1$
					}
					break;
				}
			}
		} catch (JavaModelException _e) {
			_e.printStackTrace();
		}

	}

	/**
	 * Returns the selected criteria.
	 * @return ArrayList The list of criteria.
	 */
	private ArrayList fetchCriteria() {
		return SliceEclipsePlugin.getDefault().getIndusConfiguration()
				.getCriteria();
	}
	/**
	 * Sets the current editor. Used to show the highlighting.
	 * @param ceditor The editor to set.
	 */
	public void setEditor(final CompilationUnitEditor ceditor) {
		this.editor = ceditor;
	}
}