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
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

public final class SlicerDOM {
	
    public IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }
    
    public IWorkspace getWorkSpace() {
        return getWorkspace();
    }
    
    public ArrayList getFileFor(final SootClass sc) throws CoreException {
        final ArrayList _result = new ArrayList();
        final String[] _s = sc.getName().split("$");
        final SearchEngine _se = new SearchEngine();
        final SearchPattern _sp = SearchPattern.createPattern(_s[0], IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
        final SearchRequestor _r = new SearchRequestor() {

        	@SuppressWarnings("unchecked") public void acceptSearchMatch(final SearchMatch match) {
                _result.add(match.getResource());
            }            
        };
        _se.search(_sp, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, SearchEngine.createWorkspaceScope(), _r, new NullProgressMonitor());
        return _result;
    }
        
    public int getLineFor(final Stmt stmt) {
        return getLineNumber(stmt);
    }
    
    public boolean createMarkerForSootStmtMethod(final Pair stmtMethod, final String message, final long timeStamp) throws CoreException {
        SootMethod _sootMethod = (SootMethod) stmtMethod.getSecond();
        ArrayList _files = getFileFor(_sootMethod.getDeclaringClass());
        assert _files.size() <= 1;
        final boolean _ret = !_files.isEmpty();
        if (_ret) {
            Stmt _sootStmt = (Stmt) stmtMethod.getFirst();
            int line = getLineFor(_sootStmt);
            IFile _file = (IFile) _files.get(0);
			IMarker marker = (_file).createMarker(IMarker.BOOKMARK);
            marker.setAttribute(IMarker.MESSAGE, "[" + timeStamp + "] - " + message);
            if (line != -1) {
            	marker.setAttribute(IMarker.LINE_NUMBER, line);
            } else {
            	IMethod _methodForIn = getMethodForIn(_sootMethod, _file);
            	if (_methodForIn == null) {
            		marker.setAttribute(IMarker.MESSAGE, "[" + timeStamp + "] - No line# - " + message);
            	} else {
            		ASTParser _ap = ASTParser.newParser(AST.JLS3);
            		_ap.setSource(getCompilationUnit(_file));
            		CompilationUnit _cu = (CompilationUnit) _ap.createAST(null);
					marker.setAttribute(IMarker.LINE_NUMBER, _cu.lineNumber(_methodForIn.getSourceRange().getOffset()));
            	}
            }
        }
        return _ret;
    }
    
    public IMethod getMethodForIn(final SootMethod sm, final IFile file) throws JavaModelException {
    	final ICompilationUnit _c = getCompilationUnit(file);
    	final String _smName = sm.getName().indexOf("<init>") == -1 ? sm.getName() : sm.getDeclaringClass().getJavaStyleName();
    	for (IType _type : _c.getAllTypes()) {
    		if (_type.getFullyQualifiedName().equals(sm.getDeclaringClass().getName())) {
				for (IMethod _method : _type.getMethods()) {
					if (_method.getElementName().equals(_smName)) {
						final String[] _p = _method.getParameterTypes();
						boolean _flag = true;
						for (int _k = _method.getNumberOfParameters() - 1; _k >= 0; _k--) {
							_flag &= sm.getParameterType(_k).toString().equals(_p[_k]);
						}
						if (_flag) {
							return _method;
						}
					}
				}
    		}
		}
		return null;
	}

	public void displayDialog(final String message) {
        MessageDialog.openInformation( getWorkbench().getActiveWorkbenchWindow().getShell(), "Kaveri Scripting Dialog", message);
    }
    
    public ISelection getSelection() {
        final IEditorPart _e = getWorkbench().getWorkbenchWindows()[0].getActivePage().getActiveEditor();
        final ISelection _selection = _e.getEditorSite().getSelectionProvider().getSelection();
        return _selection;
    }
    
    public IFile getSelectionContainingFile() {
    	final IEditorPart _e = getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        return ((IFileEditorInput) _e.getEditorInput()).getFile();
    }
    
    public ICompilationUnit getCompilationUnit(IFile file) {
    	return JavaCore.createCompilationUnitFrom(file);
    }
    
    public ICompilationUnit getSelectionContainingCompilationUnit() {
    	return getCompilationUnit(getSelectionContainingFile());
    }
    
    public SootMethod getJimpleMethodForSelection() throws JavaModelException {
    	final ITextSelection _s = (ITextSelection) getSelection();
    	final ICompilationUnit _c = getSelectionContainingCompilationUnit();
    	final IMethod _e = (IMethod) _c.getElementAt(_s.getOffset());
    	final Scene _scene = Scene.v();
    	for (final Iterator _i = _scene.getClasses().iterator(); _i.hasNext();) {
    		final SootClass _sc = (SootClass) _i.next();
    		if (_sc.getName().equals(_e.getDeclaringType().getFullyQualifiedName())) {
    			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
    				final SootMethod _sm = (SootMethod) _j.next();
    				final String _smName = _sm.getName().indexOf("<init>") == -1 ? _sm.getName() : _sc.getJavaStyleName(); 
    				if (_smName.equals(_e.getElementName())) {
    					final String[] _p = _e.getParameterTypes();
    					boolean _flag = true;
    					for (int _k = _e.getNumberOfParameters() - 1; _k >= 0; _k--) {
    						_flag &= _sm.getParameterType(_k).toString().equals(_p[_k]);
    					}
    					if (_flag) {
    						return _sm;
    					}
    				}
    			}
    		}
    	}
    	return null;
    }
    
    public Collection<Stmt> getJimpleStmtsForSelection() throws JavaModelException {
    	final IFile _f = getSelectionContainingFile();
    	final ITextSelection _s = (ITextSelection) getSelection();
    	final ICompilationUnit _c = getSelectionContainingCompilationUnit();
    	final IMethod e = (IMethod) _c.getElementAt(_s.getOffset());
    	int _lineNumber = _s.getStartLine() + 1;  // 0-offset line numbering is used in Eclipse; hence, I add 1.
		final Collection<Stmt> _r = getJimpleStmtsFor(_f, e.getDeclaringType(), e, _lineNumber);
    	return _r;
    }
    
	public Collection<Stmt> getJimpleStmtsFor(final IFile thefile, final IType theclass, 
            final IMethod themethod, final int theline) {
		@SuppressWarnings("unchecked") final List<Stmt> _r = getStmtForLine(thefile, theclass, themethod, theline);
        _r.remove(0);
        _r.remove(0);
        return _r;
    }
    
    public KaveriPlugin getKaveriPlugin() {
        return getDefault();
    }
    
    public IEscapeInfo getEscapeInfo() {
        return getSlicerTool().getECBA().getEscapeInfo();
    }
    
    public IReadWriteInfo getReadWriteInfo() {
        return getSlicerTool().getECBA().getReadWriteInfo();
    }
    
    public IEnvironment getEnvironment() {
        return getSlicerTool().getSystem();
    }
    
    public SlicerTool getSlicerTool() {
        return getKaveriPlugin().getSlicerTool();
    }
    
    public BasicBlockGraphMgr getBasicBlockGraphManager() {
        return getSlicerTool().getBasicBlockGraphManager();
    }
    
    public ICallGraphInfo getCallGraph() {
        return getSlicerTool().getCallGraph(); 
    }
    
    public IMonitorInfo getMonitorInfo() {
        return getSlicerTool().getMonitorInfo();
    }
    
    @SuppressWarnings("unchecked") public Collection<SootMethod> getRootMethods() {
        return getSlicerTool().getRootMethods();
    }
    
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
    
    @SuppressWarnings("unchecked") public Collection<IDependencyAnalysis> getDAs() {
        return getSlicerTool().getDAs();
    }
    
    public IStmtGraphFactory<?> getStmtGraphFactory() {
        return getSlicerTool().getStmtGraphFactory();
    }
}
