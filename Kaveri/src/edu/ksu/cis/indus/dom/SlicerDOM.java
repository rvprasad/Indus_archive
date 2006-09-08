/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.dom;

import static edu.ksu.cis.indus.kaveri.KaveriPlugin.getDefault;
import static edu.ksu.cis.indus.kaveri.soot.SootConvertor.getLineNumber;
import static edu.ksu.cis.indus.kaveri.soot.SootConvertor.getStmtForLine;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

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
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This class encapsulates the logic to extract dependencies from a dependence
 * analysis based on slice direction. This class is meant for internal use only.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerDOM {

	/**
	 * DOCUMENT ME!
	 */
	private static int counter;

	/**
	 * DOCUMENT ME!
	 */
	public int getCounter() {
		return counter++;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IWorkspace getWorkSpace() {
		return getWorkspace();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
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
	 * DOCUMENT ME!
	 * 
	 * @return
	 */

	public int getLineFor(final Stmt stmt) {
		return getLineNumber(stmt);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public boolean createMarkerForSootStmtMethod(final Pair stmtMethod,
			final String message, final long timeStamp) throws CoreException {
		SootMethod _sootMethod = (SootMethod) stmtMethod.getSecond();
		ArrayList _files = getFileFor(_sootMethod.getDeclaringClass());
		assert _files.size() <= 1;
		final boolean _ret = !_files.isEmpty();
		if (_ret) {
			Stmt _sootStmt = (Stmt) stmtMethod.getFirst();
			int line = getLineFor(_sootStmt);
			IFile _file = (IFile) _files.get(0);
			IMarker marker = (_file).createMarker(IMarker.BOOKMARK);
			marker.setAttribute(IMarker.MESSAGE, "[" + timeStamp + "] - "
					+ message);
			if (line != -1) {
				marker.setAttribute(IMarker.LINE_NUMBER, line);
			} else {
				IMethod _methodForIn = getMethodForIn(_sootMethod, _file);
				if (_methodForIn == null) {
					marker.setAttribute(IMarker.MESSAGE, "[" + timeStamp
							+ "] - No line# - " + message);
				} else {
					ASTParser _ap = ASTParser.newParser(AST.JLS3);
					_ap.setSource(getCompilationUnit(_file));
					CompilationUnit _cu = (CompilationUnit) _ap.createAST(null);
					marker.setAttribute(IMarker.LINE_NUMBER, _cu
							.lineNumber(_methodForIn.getSourceRange()
									.getOffset()));
				}
			}
		}
		return _ret;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
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
	 * DOCUMENT ME!
	 * 
	 */
	public void displayDialog(final String message) {
		MessageDialog.openInformation(getWorkbench().getActiveWorkbenchWindow()
				.getShell(), "Kaveri Scripting Dialog", message);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public ISelection getSelection() {
		final IEditorPart _e = getWorkbench().getWorkbenchWindows()[0]
				.getActivePage().getActiveEditor();
		final ISelection _selection = _e.getEditorSite().getSelectionProvider()
				.getSelection();
		return _selection;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IFile getSelectionContainingFile() {
		final IEditorPart _e = getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		return ((IFileEditorInput) _e.getEditorInput()).getFile();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public ICompilationUnit getCompilationUnit(IFile file) {
		return JavaCore.createCompilationUnitFrom(file);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public ICompilationUnit getSelectionContainingCompilationUnit() {
		return getCompilationUnit(getSelectionContainingFile());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
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
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	private boolean checkParameterTypeConformance(final IMethod method,
			final IType declaringType, final SootMethod sm)
			throws JavaModelException {
		final String[] _p = method.getParameterTypes();
		boolean _flag = true;
		for (int _k = method.getNumberOfParameters() - 1; _k >= 0; _k--) {
			_flag &= sm.getParameterType(_k).toString().equals(
					JavaModelUtil.getResolvedTypeName(_p[_k], declaringType));
		}
		return _flag;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public Collection<Stmt> getJimpleStmtsForSelection()
			throws JavaModelException {
		final IFile _f = getSelectionContainingFile();
		final ITextSelection _s = (ITextSelection) getSelection();
		final ICompilationUnit _c = getSelectionContainingCompilationUnit();
		final IMethod e = (IMethod) _c.getElementAt(_s.getOffset());
		int _lineNumber = _s.getStartLine() + 1; // 0-offset line numbering
		// is used in Eclipse;
		// hence, I add 1.
		final Collection<Stmt> _r = getJimpleStmtsFor(_f, e.getDeclaringType(),
				e, _lineNumber);
		return _r;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
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
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public KaveriPlugin getKaveriPlugin() {
		return getDefault();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IEscapeInfo getEscapeInfo() {
		return getSlicerTool().getECBA().getEscapeInfo();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IReadWriteInfo getReadWriteInfo() {
		return getSlicerTool().getECBA().getReadWriteInfo();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IEnvironment getEnvironment() {
		return getSlicerTool().getSystem();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public SlicerTool getSlicerTool() {
		return getKaveriPlugin().getSlicerTool();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public BasicBlockGraphMgr getBasicBlockGraphManager() {
		return getSlicerTool().getBasicBlockGraphManager();
	}

	public IValueAnalyzer<Value> getOFAnalyzer() {
		return getSlicerTool().getOFAnalyzer();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public ICallGraphInfo getCallGraph() {
		return getSlicerTool().getCallGraph();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IMonitorInfo getMonitorInfo() {
		return getSlicerTool().getMonitorInfo();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<SootMethod> getRootMethods() {
		return getSlicerTool().getRootMethods();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
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
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<IDependencyAnalysis> getDAs() {
		return getSlicerTool().getDAs();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public IStmtGraphFactory<?> getStmtGraphFactory() {
		return getSlicerTool().getStmtGraphFactory();
	}
}
