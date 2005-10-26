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
package edu.ksu.cis.indus.kaveri.driver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.callgraph.MethodCallContext;
import edu.ksu.cis.indus.kaveri.common.PrettySignature;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.SliceProgressBar;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;
import edu.ksu.cis.indus.kaveri.rootmethodtrapper.RootMethodCollection;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import soot.Body;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.util.Chain;

/**
 * This does the bulk of the call to the eclipse indus driver. The settings for
 * the slice are currently stored in IndusConfiguration
 * 
 * @author Ganeshan
 */
public abstract class AbstractIndusRunner implements IRunnableWithProgress {
    /**
     * Java Editor instance.
     */
    CompilationUnitEditor editor;

    protected boolean opCancelled;

    /**
     * The user modified classpathset.
     */
    private Set classpathSet;
    
    /**
     * <p>
     * The eclipse indus driver.
     * </p>
     */
    EclipseIndusDriver driver;

    /**
     * The slice progress bar.
     */
    SliceProgressBar bar;

    /**
     * <p>
     * List of java files to be sliced.
     * </p>
     */
    List fileList;

    /**
     * Reachable warning given
     */
    boolean reachableWarningGiven = false;
    
    /**
     * Residualize the scene. 
     * The entity that does this is resposible for 
     * restoring the changes.
     * 
     */
    boolean residualize = false;
    
    /**
     * All the java files in the given project.
     */
    List completeFileList;

    /**
     * Creates a new KaveriIndusRunner object.
     * 
     * @param filesList
     *            The file pointing to the java file being sliced
     * @param bar
     *            The slice progress bar to which to report the messages.
     */
    public AbstractIndusRunner(final List filesList, SliceProgressBar bar
            , final Set classPathSet) {
        this.fileList = filesList;
        driver = KaveriPlugin.getDefault().getIndusConfiguration()
                .getEclipseIndusDriver();
        editor = null;
        completeFileList = null;
        opCancelled = false;
        this.bar = bar;
        this.classpathSet = classPathSet;
    }

    /**
     * Sets the current editor. Used to show the highlighting in case of
     * backward and forward slicing.
     * 
     * @param ceditor
     *            The editor to set.
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
     * 
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public abstract void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException ;

    /**
     * Process the contexts.
     * 
     * @param ctx
     *            The collection of contexts.
     * @pre ctx.oclIsKindOf(Collection(MethodCallContext))
     */
    protected void processContexts(Collection ctx) throws InterruptedException {
        final EclipseIndusDriver _driver = KaveriPlugin.getDefault()
                .getIndusConfiguration().getEclipseIndusDriver();
        for (Iterator iter = ctx.iterator(); iter.hasNext();) {
            final MethodCallContext _mcc = (MethodCallContext) iter.next();
            final Collection _stkColl = _mcc.getContextStacks();
            for (Iterator iterator = _stkColl.iterator(); iterator.hasNext();) {
                final Stack _stk = (Stack) iterator.next();
                final Stack _ctxStack = new Stack();
                for (Iterator _iter = _stk.iterator(); _iter.hasNext();) {
                    final Triple _triple = (Triple) _iter.next();
                    final MethodWrapper _m1 = (MethodWrapper) _triple
                            .getFirst();
                    final MethodWrapper _m2 = (MethodWrapper) _triple
                            .getSecond();
                    final CallLocation _cl = (CallLocation) _triple.getThird();
                    final SootMethod _sm1 = SootConvertor
                            .getSootMethod((IMethod) _m1.getMember());
                    final SootMethod _sm2 = SootConvertor
                            .getSootMethod((IMethod) _m2.getMember());
                    if (_sm1 == null || _sm2 == null) {
                        throw new InterruptedException(
                                "Unable to find a binding for the context");
                    }
                    final CallTriple _ctriple = fetchInvokeExpr(_sm1, _sm2, _cl
                            .getLineNumber());
                    if (_ctriple != null) {

                    } else {
                        throw new InterruptedException("Unable to convert call chain between " +
                                _m1.getName() + " and " + _m2.getName());
                    }
                    _ctxStack.push(_ctriple);
                }
                _driver.addToContext(_ctxStack);

            }

        }

    }

    /**
     * Fetch the invoke expression between
     * 
     * @param sm1
     * @param sm2
     * @return
     */
    private CallTriple fetchInvokeExpr(final SootMethod sm1,
            final SootMethod sm2, final int nLineno) {
        CallTriple _triple = null;
        Body _body = null;
        if (sm1.hasActiveBody()) {
            _body = sm1.getActiveBody();
        } else {
            _body = sm1.retrieveActiveBody();
        }
        final List _lst = SootConvertor.getStmts(_body, nLineno);
        if (_lst != null && _lst.size() > 0) {
            final Chain _chain = _body.getUnits();
            for (Iterator iter = _lst.iterator(); iter.hasNext();) {
                final Stmt _stmt = (Stmt) iter.next();
                if (_stmt.containsInvokeExpr()) {
                    final InvokeExpr _expr1 = _stmt.getInvokeExpr();
                    if (_expr1.getMethod().equals(sm2)) {
                        _triple = new CallTriple(sm1, _stmt, _expr1);
                        break;
                    }
                }
            }
        }

        return _triple;

    }

    /**
     * Sets up the driver with the correct values.
     * 
     * @return boolean True if the slicer is setup correctly.
     */
    protected boolean setUp() {
        final String _currConfig = KaveriPlugin.getDefault()
                .getIndusConfiguration().getCurrentConfiguration();
        driver.setSlicer(KaveriPlugin.getDefault().getSlicerTool());
        driver.reset();
        
        setupRootMethodCollection();                

        driver.getSlicer().setActiveConfiguration(_currConfig);
        removeAnnotations();

        boolean _indusRun = true;

        try {
            
            
            String _sootClassPath = ""; //$NON-NLS-1$
            IPath _jreclasspath = JavaCore.getClasspathVariable(Messages
                    .getString("AbstractIndusRunner.5")); //$NON-NLS-1$
            _jreclasspath = JavaCore.getClasspathVariable(Messages
                    .getString("AbstractIndusRunner.6")); //$NON-NLS-1$		

            final String _pathseparator = System.getProperty(Messages
                    .getString("AbstractIndusRunner.7")); //$NON-NLS-1$
            final String _fileseparator = System.getProperty(Messages
                    .getString("AbstractIndusRunner.8")); //$NON-NLS-1$

            if (_jreclasspath != null) {
                
                
                if (classpathSet.size() == 0) {
                    final Set _classPathCollection = new HashSet();
                    _classPathCollection.add(_jreclasspath.toOSString() + _pathseparator);
               

                    if (fileList.size() > 0) {
                        final IFile _file = (IFile) fileList.get(0);
                        final IJavaProject _jproject = JavaCore.create(_file
                            .getProject());
                        final Set _set = SECommons.getClassPathForProject(
                            _jproject, new HashSet(), false, true);
                        _classPathCollection.addAll(_set);
                        for (Iterator iter = _set.iterator(); iter.hasNext();) {
                            _sootClassPath += (String) iter.next();
                        }
                    }
                } else {
                    for (Iterator iter = classpathSet.iterator(); iter.hasNext();) {
                        _sootClassPath += (String) iter.next();
                        
                    }
                }
               
                driver.addToPath(_sootClassPath);

                final Set _classNamesList = new HashSet();

                for (int _i = 0; _i < fileList.size(); _i++) {
                    final IFile _javaFile = (IFile) fileList.get(_i);
                    final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
                            .create(_javaFile);

                    if (_icunit != null) {
                        IType[] _types = null;

                        _types = _icunit.getAllTypes();

                        for (int _nrun = 0; _nrun < _types.length; _nrun++) {
                            final String _elemName = _types[_nrun]
                                    .getFullyQualifiedName();
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
                MessageDialog.openError(null, Messages
                        .getString("AbstractIndusRunner.9"), Messages
                        .getString("AbstractIndusRunner.12"));
            }
        } catch (JavaModelException _jme) {
            SECommons.handleException(_jme);
            KaveriErrorLog.logException("Java Model Exception", _jme);
            _indusRun = false;
        }
        return _indusRun;
    }

    /**
     * Sets up the root methods.
     */
    protected void setupRootMethodCollection() {
        final KaveriRootMethodTrapper _rm = KaveriPlugin.getDefault().getRmTrapper();
        _rm.reset();
        if (fileList.size() > 0) {
            final IFile _file = (IFile) fileList.get(0);
            final IProject _project = _file.getProject();
            final IJavaProject _jp = JavaCore.create(_project);
            if (_jp != null) {
                try {
                    final IResource _resource = _jp.getCorrespondingResource();
                    final QualifiedName _name = new QualifiedName("edu.ksu.cis.indus.kaveri", "rootMethodCollection");
                    final String _propVal =  _resource.getPersistentProperty(_name);
                    final XStream _xstream = new XStream(new DomDriver());
                    _xstream.alias("RootMethodCollection", RootMethodCollection.class);
                    RootMethodCollection _rmc = null;
                    if (_propVal != null) {
                        _rmc = (RootMethodCollection) _xstream.fromXML(_propVal);
                        _rm.addRootMethodSignatures(_rmc.getRootMethodCollection());
                    }
                } catch (JavaModelException _e) {
                    SECommons.handleException(_e);
                    KaveriErrorLog.logException("Java Model Exception", _e);
                } catch (CoreException _e) {
                    SECommons.handleException(_e);
                    KaveriErrorLog.logException("Java Model Exception", _e);
                }
                
            }
        }
        driver.setRootMethodTrapper(_rm);
    }

    /**
     * Sets all the classes in the file to application classes.
     *  
     */
    protected void setApplicationClasses() {
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
                    _classlist.addAll(SECommons.getClassesInFile(_jfile));
                }
                driver.setApplicationClasses(_classlist);
            }
        }
    }

    /**
     * Sets the criteria.
     * 
     * @param file
     *            The Java file in which the criteria is present
     * @param type
     *            The JDT class of the criteria.
     * @param array
     *            The set of criteria chosen.
     * @param methodName
     *            The method name
     * @param stindex
     *            The index of the chosen Jimple Stmt in the list of Stmts
     * @param nLine
     *            The selected line number.
     * @param considerVal
     *            Consider value for execution.
     */
    private void setCriteria(final IFile file, final IType type,
            final List array, final String methodName, final int stindex,
            final int nLine, final boolean considerVal) {
        try {
            final IMethod[] _methods = type.getMethods();

            for (int _j = 0; _j < _methods.length; _j++) {
                final IMethod _method = _methods[_j];

                if (PrettySignature.getMethodSignature(_method).equals(
                        methodName)) {
                    final List _stmtlist = SootConvertor.getStmtForLine(file,
                            type, _method, nLine);

                    if (_stmtlist != null && _stmtlist.size() >= 3) {
                        final SootMethod _sootmethod = (SootMethod) _stmtlist
                                .get(1);
                        final Stmt _stmt = (Stmt) _stmtlist.get(2 + stindex);
                        driver.setCriteria(_sootmethod, _stmt, considerVal);
                        //	System.out.println(Messages.getString("AbstractIndusRunner.15")
                        // + _stmt); //$NON-NLS-1$
                    }
                    break;
                }
            }
        } catch (JavaModelException _e) {
            KaveriErrorLog.logException("Java Model Exception", _e);
            SECommons.handleException(_e);
        }
    }

    //	/**
    //	 * Dumps the jimple representation of the classes in
    //	 * the directory where the sliced files are present.
    //	 */
    //	private void dumpJimple() {
    //		final IFile _file = (IFile) fileList.get(0);
    //		final String _op =
    // _file.getLocation().removeLastSegments(1).toOSString();
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
     * Highlights the editor with annotations. Used only with backward or
     * forward slicing.
     */
    protected void highlightEditor() {
        final Map _map = KaveriPlugin.getDefault().getIndusConfiguration()
                .getLineNumbers();
        final AddIndusAnnotation _manager = KaveriPlugin.getDefault()
                .getIndusConfiguration().getIndusAnnotationManager();
        _manager.setEditor(editor, _map);        
    }

    /**
     * Matches the set of criteria with the set of files chosen.
     * 
     * @param file
     *            One of the chosen Java files.
     * @param type
     *            The JDT class
     * @param array
     *            The set of criteria
     */
    private void matchAndSet(final IFile file, final IType type,
            final List array) {
        for (int _i = 0; _i < array.size(); _i++) {
            final Criteria _c = (Criteria) array.get(_i);
            final String _classname = _c.getStrClassName();
            final String _methodname = _c.getStrMethodName();
            final int _nLine = _c.getNLineNo();
            final int _stindex = _c.getNJimpleIndex();
            final boolean _considerVal = _c.isBConsiderValue();

            if (PrettySignature.getSignature(type).equals(_classname)) {
                setCriteria(file, type, array, _methodname, _stindex, _nLine,
                        _considerVal);
            }
        }
    }

    /**
     * Removes all current slice annotations on all open Java editors.
     */
    protected void removeAnnotations() {
        final IWorkbenchWindow[] _windows = PlatformUI.getWorkbench()
                .getWorkbenchWindows();
        final AddIndusAnnotation _manager = KaveriPlugin.getDefault()
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
     * Returns the criteria to the pool.
     */
    protected void returnCriteriaToPool() {
        final Collection _coll = driver.getSlicer().getCriteria();
        final Iterator _it = _coll.iterator();
        
        
        while (_it.hasNext()) {
            final ISliceCriterion _crt = (ISliceCriterion) _it.next();
            final SootMethod _sm = _crt.getOccurringMethod();
            final SlicerTool _slicer = driver.getSlicer();
            if (_slicer != null) {
                if(!_slicer.getCallGraph().getReachableMethods().contains(_sm) && !reachableWarningGiven) {
                    reachableWarningGiven = true;
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                        final String _msg = "Some criteria were in unreachable part of the system.  " +
                		"Please use appropriate root methods or select criteria appropriately.";
                        MessageDialog.openError(null, "Warning", _msg);                        
                        }
                    });
                    
                }
                
                
            }
        }
    }

    /**
     * Sets up the criteria.
     */
    protected void setupCriteria() {
        final List _crArray = fetchCriteria();

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
                        KaveriErrorLog.logException("Java Model Exception", _e);
                        SECommons.handleException(_e);
                    }
                }
            }
        }
    }
}

