/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.dom;

import static edu.ksu.cis.indus.kaveri.KaveriPlugin.getDefault;
import static edu.ksu.cis.indus.kaveri.soot.SootConvertor.getLineNumber;
import static edu.ksu.cis.indus.kaveri.soot.SootConvertor.getStmtForLine;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import soot.ArrayType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.Stmt;
import sun.misc.Signal;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This class encapsulates the logic to extract dependencies from a dependence
 * analysis based on slice direction.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerDOM {

	/**
	 * A counter variable.
	 */
	private static int counter;

	/**
	 * Retrieve the current counter value.
	 */
	public int getCounter() {
		return counter++;
	}

	/**
	 * Get a handle to the workbench.
	 * 
	 * @return the workbench.
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/**
	 * Get a handle to the workspace.
	 * 
	 * @return the workspace.
	 */
	public IWorkspace getWorkSpace() {
		return getWorkspace();
	}

	/**
	 * Retrieves the files corresponding to the class represented by the given class.
	 * 
	 * @return the Java files containing the definition of the class.
	 * @pre result.size() = 0 or result.size() = 1 
	 */
	public ArrayList getFileFor(final SootClass sc) throws CoreException {
		final ArrayList _result = new ArrayList();
		final String[] _s = sc.getName().split("$");
		final SearchEngine _se = new SearchEngine();
		final SearchPattern _sp = SearchPattern.createPattern(_s[0],
				IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		final SearchRequestor _r = new SearchRequestor() {

			@SuppressWarnings("unchecked")
			public void acceptSearchMatch(final SearchMatch match) {
				_result.add(match.getResource());
			}
		};
		_se.search(_sp, new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() }, SearchEngine
				.createWorkspaceScope(), _r, new NullProgressMonitor());
		return _result;
	}

	/**
	 * Creates a marker for the given statement-method pair with the given message and timestamp.
	 * @param stmtMethod is a pair of statement and method that is to be bookmarked.
	 * @param message is the message of the bookmark.
	 * @param timeStamp to be included in the message.
	 * 
	 * @return <code>true</code> if a marker was added; <code>false</code>, otherwise.
	 */
	public boolean createMarkerForSootStmtMethod(final Pair stmtMethod,
			final String message, final long timeStamp) throws CoreException {
		SootMethod _sootMethod = (SootMethod) stmtMethod.getSecond();
		ArrayList _files = getFileFor(_sootMethod.getDeclaringClass());
		assert _files.size() <= 1;
		boolean _ret = !_files.isEmpty();
		if (_ret) {
			Stmt _sootStmt = (Stmt) stmtMethod.getFirst();
			final int line = _sootStmt != null ? getLineNumber(_sootStmt) : -1;
			final Object _o = _files.get(0);
			if (_o instanceof IFile) {
				final IFile _file = (IFile) _o;
				final IMarker _marker = (_file).createMarker(IMarker.BOOKMARK);
				_marker.setAttribute(IMarker.MESSAGE, "[" + timeStamp + "] - "
						+ message);
				if (line != -1) {
					_marker.setAttribute(IMarker.LINE_NUMBER, line);
				} else {
					final IMethod _methodForIn = getMethodForIn(_sootMethod, _file);
					if (_methodForIn == null) {
						_marker.setAttribute(IMarker.MESSAGE, "[" + timeStamp
								+ "] - No line# - " + message);
					} else {
						final ASTParser _ap = ASTParser.newParser(AST.JLS3);
						_ap.setSource(getCompilationUnit(_file));
						final CompilationUnit _cu = (CompilationUnit) _ap.createAST(null);
						_marker.setAttribute(IMarker.LINE_NUMBER, _cu
								.lineNumber(_methodForIn.getSourceRange()
										.getOffset()));
					}
				}
			} else {
				_ret = false;
			}
		}
		return _ret;
	}

	/**
	 * Retrieves the JDT method corresponding the given Soot method. 
	 * @param sm is the Soot method.
	 * @param file in which the corresponding JDT method is to be searched for.
	 * @return the JDT method. 
	 */
	public IMethod getMethodForIn(final SootMethod sm, final IFile file)
			throws JavaModelException {
		final ICompilationUnit _c = getCompilationUnit(file);
		final String _smName = sm.getName().indexOf("<init>") == -1 ? sm
				.getName() : sm.getDeclaringClass().getJavaStyleName();
		for (IType _type : _c.getAllTypes()) {
			if (_type.getFullyQualifiedName().equals(
					sm.getDeclaringClass().getName())) {
				for (IMethod _method : _type.getMethods()) {
					final IType _declaringType = _method.getDeclaringType();
					if (_method.getElementName().equals(_smName)) {
						if (checkParameterTypeConformance(_method,
								_declaringType, sm)) {
							return _method;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Displays a dialog with the given message.
	 * @param message to be displayed.
	 */
	public void displayDialog(final String message) {
		MessageDialog.openInformation(getWorkbench().getActiveWorkbenchWindow()
				.getShell(), "Kaveri Scripting Dialog", message);
	}

	/**
	 * Retrieves the current selection (if any) in the active editor (if any).
	 * 
	 * @return the selection, if one exists; <code>null</code>, otherwise.
	 */
	public ISelection getSelection() {
		final IEditorPart _e = getWorkbench().getWorkbenchWindows()[0]
				.getActivePage().getActiveEditor();
		final ISelection _selection = _e.getEditorSite().getSelectionProvider()
				.getSelection();
		return _selection;
	}

	/**
	 * Retrieves the file containing the selection.
	 * 
	 * @return the file.
	 */
	public IFile getSelectionContainingFile() {
		final IEditorPart _e = getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		return ((IFileEditorInput) _e.getEditorInput()).getFile();
	}

	/**
	 * Retrieves the compilation unit corresponding to the contents of the given file.  
	 * 
	 * @return the compilation unit.
	 */
	public ICompilationUnit getCompilationUnit(IFile file) {
		return JavaCore.createCompilationUnitFrom(file);
	}

	/**
	 * Retrieves the compilation unit corresponding to the file in which the current selection occurs.
	 * 
	 * @return the compilation unit.
	 */
	public ICompilationUnit getSelectionContainingCompilationUnit() {
		return getCompilationUnit(getSelectionContainingFile());
	}

	/**
	 * Retrieves the Jimple method corresponding to the method that contains the current selection. 
	 * 
	 * @return the Jimple method.
	 */
	public SootMethod getJimpleMethodForSelection() throws JavaModelException {
		final ITextSelection _s = (ITextSelection) getSelection();
		final ICompilationUnit _c = getSelectionContainingCompilationUnit();
		final IMethod _e = (IMethod) _c.getElementAt(_s.getOffset());
		final Scene _scene = Scene.v();
		for (final Iterator _i = _scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();
			final IType _declaringType = _e.getDeclaringType();
			if (_sc.getName().equals(_declaringType.getFullyQualifiedName())) {
				for (final Iterator _j = _sc.getMethods().iterator(); _j
						.hasNext();) {
					final SootMethod _sm = (SootMethod) _j.next();
					final String _smName = _sm.getName().indexOf("<init>") == -1 ? _sm
							.getName()
							: _sc.getJavaStyleName();
					if (_smName.equals(_e.getElementName())) {
						if (checkParameterTypeConformance(_e, _declaringType,
								_sm)) {
							return _sm;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the given JDT method and Soot method have type conforming parameters.
	 * 
	 * @return <code>true</code> if the parameter types are identical; <code>false</code>, otherwise.
	 */
	private boolean checkParameterTypeConformance(final IMethod method,
			final IType declaringType, final SootMethod sm)
			throws JavaModelException {
		final String[] _p = method.getParameterTypes();
		boolean _flag = true;
		for (int _k = method.getNumberOfParameters() - 1; _k >= 0 && _flag; _k--) {
			final Type _parameterType = sm.getParameterType(_k);
			_flag &= _parameterType.toString().replace("[]","").equals(
					JavaModelUtil.getResolvedTypeName(_p[_k], declaringType));
			if (_parameterType instanceof ArrayType) {
				ArrayType _t = (ArrayType) _parameterType;
				_flag &= _t.numDimensions == Signature.getArrayCount(_p[_k]);
			}
		}
		return _flag;
	}

	/**
	 * Retrieves the Jimple statements for the given selection.
	 * 
	 * @return the corresponding Jimple statements.
	 */
	public Collection<Stmt> getJimpleStmtsForSelection()
			throws JavaModelException {
		final IFile _f = getSelectionContainingFile();
		final ITextSelection _s = (ITextSelection) getSelection();
		final ICompilationUnit _c = getSelectionContainingCompilationUnit();
		final IJavaElement _elementAt = _c.getElementAt(_s.getOffset());
		final Collection<Stmt> _r;
		
		if (_elementAt instanceof IMethod) {
			final IMethod e = (IMethod) _elementAt;
			int _lineNumber = _s.getStartLine() + 1; // 0-offset line numbering is used in Eclipse; hence, I add 1.
			_r = getJimpleStmtsFor(_f, e.getDeclaringType(),
					e, _lineNumber);
		} else {
			_r = Collections.<Stmt>emptySet();
		}
		
		return _r;
	}

	/**
	 * Retrieves the Jimple statements for the given line in the given method in the given type in the given file. 
	 * @param thefile containing theclass.
	 * @param theclass containing themethod.
	 * @param themethod containing theline.
	 * @param theline of interest.
	 * @return the corresponding Jimple statements. 
	 */
	public Collection<Stmt> getJimpleStmtsFor(final IFile thefile,
			final IType theclass, final IMethod themethod, final int theline) {
		@SuppressWarnings("unchecked")
		final List<Stmt> _r = getStmtForLine(thefile, theclass, themethod,
				theline);
		_r.remove(0);
		_r.remove(0);
		return _r;
	}

	/**
	 * Retrieves a handle to the Kaveri plugin.
	 * 
	 * @return a handle to the plugin.
	 */
	public KaveriPlugin getKaveriPlugin() {
		return getDefault();
	}

	/**
	 * Retrieves a handle to the escape info.
	 * 
	 * @return a handle to the escape info.
	 */
	public IEscapeInfo getEscapeInfo() {
		return getSlicerTool().getECBA().getEscapeInfo();
	}

	/**
	 * Retrieves a handle to the read-write info.
	 * 
	 * @return a handle to the read-write info.
	 */
	public IReadWriteInfo getReadWriteInfo() {
		return getSlicerTool().getECBA().getReadWriteInfo();
	}

	/**
	 * Retrieves a handle to the environment.
	 * 
	 * @return a handle to the environment.
	 */
	public IEnvironment getEnvironment() {
		return getSlicerTool().getSystem();
	}

	/**
	 * Retrieves a handle to the slicer tool.
	 * 
	 * @return a handle to the slicer tool.
	 */
	public SlicerTool getSlicerTool() {
		return getKaveriPlugin().getSlicerTool();
	}

	/**
	 * Retrieves a handle to the basic block manager.
	 * 
	 * @return a handle to the basic block manager.
	 */
	public BasicBlockGraphMgr getBasicBlockGraphManager() {
		return getSlicerTool().getBasicBlockGraphManager();
	}

	/**
	 * Retrieves a handle to the object-flow analyzer.
	 * 
	 * @return a handle to the object-flow analyzer.
	 */
	public IValueAnalyzer<Value> getOFAnalyzer() {
		return getSlicerTool().getOFAnalyzer();
	}
	
	/**
	 * Retrieves a handle to the call graph.
	 * 
	 * @return a handle to the call graph.
	 */
	public ICallGraphInfo getCallGraph() {
		return getSlicerTool().getCallGraph();
	}

	/**
	 * Retrieves a handle to the monitor information.
	 * 
	 * @return a handle to the monitor information.
	 */
	public IMonitorInfo getMonitorInfo() {
		return getSlicerTool().getMonitorInfo();
	}

	/**
	 * Retrieves the collection root/entry methods. 
	 * 
	 * @return the collection of root/entry methods.
	 */
	@SuppressWarnings("unchecked")
	public Collection<SootMethod> getRootMethods() {
		return getSlicerTool().getRootMethods();
	}

	/**
	 * Retrieves a handle to the requested dependence analysis info.
	 * 
	 * @return a handle to the requested dependence analysis info.
	 */
	public Collection<IDependencyAnalysis> getDA(final Comparable<?> id) {
		final Collection<IDependencyAnalysis> _r = new ArrayList<IDependencyAnalysis>();
		for (final Object _i : getSlicerTool().getDAs()) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i;
			if (_da.getIds().contains(id)) {
				_r.add(_da);
			}
		}
		return _r;
	}

	/**
	 * Retrieves a handle to all dependence analyses.
	 * 
	 * @return a handle to all dependence analyses.
	 */
	@SuppressWarnings("unchecked")
	public Collection<IDependencyAnalysis> getDAs() {
		return getSlicerTool().getDAs();
	}

	/**
	 * Retrieves a handle to the CFG factory.
	 * 
	 * @return a handle to the CFG factory.
	 */
	public IStmtGraphFactory<?> getStmtGraphFactory() {
		return getSlicerTool().getStmtGraphFactory();
	}


	/**
	 * Creates an instance of this class.
	 */
	SlicerDOM() {
		super();
	}
}
