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

package edu.ksu.cis.indus.kaveri.common;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.search.PrettySignature;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.dialogs.ExceptionDialog;
import edu.ksu.cis.indus.kaveri.driver.Messages;

/**
 * Common functions used throughout Kaveri.
 * 
 * @author ganeshan
 */
public final class SECommons {

    /** Map between class names and the IFile they belong to. */
    private static Map classNameToFileMap = new HashMap();

    /** The java file to Document map. */
    private static Map javaFileToDocumentMap = new HashMap();

    /**
     * Constructor.
     */
    private SECommons() {
    }

    /**
     * Returns a proper name for the IMethod to match with sootmethod.
     * 
     * @param method
     *            The IMethod
     * 
     * @return String The proper string representation of the method
     *         declaration.
     */
    public static String getProperMethodName(final IMethod method) {
        String _methodPattern = PrettySignature.getMethodSignature(method);
        final int _index = _methodPattern.indexOf('(');
        final String _fullclassString = _methodPattern.substring(0, _index);
        final int _index1 = _fullclassString.lastIndexOf(".");

        if (_index1 > 0) {
            final String _funcname = _fullclassString.substring(_index1 + 1,
                    _index);
            _methodPattern = _funcname + _methodPattern.substring(_index);
        }

        if (method.getDeclaringType().getElementName().equals(
                method.getElementName())) {
            _methodPattern = _methodPattern.replaceFirst(method
                    .getElementName(), "<init>");
            // Change method name to <init> for compatibility
        }
        return _methodPattern;
    }

    /**
     * Returns the classes in the file.
     * 
     * @param file
     *            The Java file
     * @return List The list of classes in the file.
     *  
     */
    public static List getClassesInFile(final IFile file) {
        final List _lst = new LinkedList();
        final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
                .create(file);

        if (_icunit != null) {
            IType[] _types = null;
            try {
                _types = _icunit.getAllTypes();

                for (int _nrun = 0; _nrun < _types.length; _nrun++) {
                    final String _elemName = _types[_nrun]
                            .getFullyQualifiedName();
                    _lst.add(_elemName);
                }
            } catch (JavaModelException _jme) {
                KaveriErrorLog.logException("Java Mode Exception", _jme);
            }
        }
        return _lst;
    }

    /**
     * Returns a set of strings indicating the class path.
     * 
     * @author Daniel Berg jdt-dev@eclipse.org.
     * @param jproject The java project.
     * @param visitedProjects The set of visited projets.
     * @param isFirst Whether the entry is exported.
     * @param addTrailingSep Indicates if the trailing path separator is to be added
     * @return Set The set of all the classpaths.
     */
    public static Set getClassPathForProject(final IJavaProject jproject,
            final Set visitedProjects, final boolean isFirst, boolean addTrailingSep) {
        final Set _retSet = new HashSet();
        final String _pathseparator = System.getProperty(Messages
                .getString("SootConvertor.3")); //$NON-NLS-1$		
        if (jproject == null || visitedProjects.contains(jproject)) {
            return _retSet;
        }
        visitedProjects.add(jproject);
        try {
            final IClasspathEntry[] _entries = jproject.getResolvedClasspath(true);
            for (int _i = 0; _i < _entries.length; _i++) {
                    switch (_entries[_i].getEntryKind()) {
                    case IClasspathEntry.CPE_SOURCE:
                        IPath _opLoc = _entries[_i].getOutputLocation();
                        if (_opLoc == null) {
                            _opLoc = jproject.getOutputLocation();
                        }
                        if (_opLoc != null) {
                            if (_opLoc.segmentCount() == 1) {
                                _opLoc = jproject.getProject().getLocation();
                            } else {
                                _opLoc = jproject.getProject().getParent()
                                        .getFile(_opLoc).getLocation();
                            }
                            
                            String _entry = _opLoc.addTrailingSeparator()
                            .toOSString();
                            if (addTrailingSep) {
                                _entry += _pathseparator;
                            }
                            _retSet.add(_entry);
                        }
                        IPath _srcpath = _entries[_i].getPath();
                        if (_srcpath.segmentCount() == 1) {
                            _srcpath = jproject.getProject().getLocation();
                        } else {
                            _srcpath = jproject.getProject().getParent()
                                    .getFile(_srcpath).getLocation();
                        }
                        String _entry = _srcpath.addTrailingSeparator()
                        .toOSString();
                        if (addTrailingSep) {
                            _entry += _pathseparator;
                        }
                        _retSet.add(_entry);
                        break;
                    case IClasspathEntry.CPE_LIBRARY:
                        final IContainer parent = jproject.getProject()
                                .getParent();
                        IPath _libPath = parent.getFile(_entries[_i].getPath())
                                .getLocation();
                        if (_libPath == null) {
                            _libPath = _entries[_i].getPath();
                        }
                        String _centry = _libPath.toOSString();
                        if (addTrailingSep) {
                            _centry += _pathseparator;
                        }
                        _retSet.add(_centry);
                        break;
                    case IClasspathEntry.CPE_PROJECT:
                        if (isFirst || _entries[_i].isExported()) {
                        final IProject _dProject = ResourcesPlugin
                                .getWorkspace().getRoot().getProject(
                                        _entries[_i].getPath().segment(0));
                        final IJavaProject _jdproject = JavaCore
                                .create(_dProject);
                        _retSet.addAll(getClassPathForProject(_jdproject,
                                visitedProjects, false, addTrailingSep));
                        }
                        break;
                     default: break;   

                    }                
            }
        } catch (JavaModelException _jme) {
            SECommons.handleException(_jme);
            KaveriErrorLog.logException("Java Mode Exception", _jme);
        }
        return _retSet;
    }

    /**
     * Returns a search pattern for the IMethod corressponding to the given
     * SootMethod.
     * 
     * @param selectedSootMethod
     *            The SootMethod from which to generate the search pattern
     * 
     * @return The string pattern of the type function-name(type-arg1,
     *         type-arg2...)
     */
    public static String getSearchPattern(final SootMethod selectedSootMethod) {
        final StringBuffer _pattern = new StringBuffer("");
        _pattern.append(selectedSootMethod.getName() + "("); //$NON-NLS-1$

        final int _parameterCount = selectedSootMethod.getParameterCount();
        String _retString = null;

        if (_parameterCount < 1) {
            _retString = _pattern + ")"; //$NON-NLS-1$
        } else {
            final Type[] _paramTypes = new Type[_parameterCount];

            for (int _i = 0; _i < _parameterCount; _i++) {
                _paramTypes[_i] = (selectedSootMethod.getParameterType(_i));
            }

            if (_paramTypes.length > 0) {
                String _tempPattern = _paramTypes[0].toString();
                int _index = _tempPattern.lastIndexOf("."); //$NON-NLS-1$

                if (_index > 0 && _index < _tempPattern.length()) {
                    _tempPattern = _tempPattern.substring(_index + 1);
                }
                _pattern.append(_tempPattern);

                for (int _nCtr = 1; _nCtr < _paramTypes.length; _nCtr++) {
                    _pattern.append(", "); //$NON-NLS-1$
                    _tempPattern = _paramTypes[_nCtr].toString();
                    _index = _tempPattern.lastIndexOf("."); //$NON-NLS-1$

                    // Eclipse doesn't add class names to the parameter type.
                    // Hence this.
                    if (_index > 0 && _index < _tempPattern.length()) {
                        _tempPattern = _tempPattern.substring(_index + 1);
                    }
                    _pattern.append(_tempPattern);
                }
            }
            _pattern.append(")"); //$NON-NLS-1$
            _retString = _pattern.toString();
        }
        return _retString;
    }

    /**
     * Checks if the object is an instance of the specified class.
     * 
     * @param objChk
     *            The object to be checked.
     * @param classChk
     *            The class to be checked against.
     * 
     * @return boolean True if the object is of the specified class.
     */
    public static boolean checkForClassEquality(final Object objChk,
            final Class classChk) {
        boolean _returnVal = false;

        if (!(checkForNull(objChk) && checkForNull(classChk))) {
            _returnVal = classChk.isInstance(objChk);
        }
        return _returnVal;
    }

    /**
     * Returns true if the object is null.
     * 
     * @param objChk
     *            The object to be checked for nullness.
     * 
     * @return boolean True if the object is null.
     */
    public static boolean checkForNull(final Object objChk) {
        boolean _retVal = false;

        if (objChk == null) {
            _retVal = true;
        } else {
            _retVal = false;
        }
        return _retVal;
    }

    /**
     * Checks if root methods exist in the given file. Else finds a file with a
     * root method in the project in which the file belongs.
     * 
     * @param file
     *            The file to check.
     * 
     * @return List The list of necessary files.
     */
    public static List checkForRootMethods(final IFile file) {
        final List _lst = new LinkedList();
        final boolean _isMainPresent = isMainPresent(file);
        _lst.add(file);

        if (!_isMainPresent) {
            searchAndFindRoots(_lst, file);
        }
        return _lst;
    }

    /**
     * Displays the exception trace in a dialog box.
     * 
     * @param exception
     *            The exception to be handled.
     */
    public static void handleException(final Exception exception) {
        final StringWriter _sw = new StringWriter();
        final PrintWriter _pw = new PrintWriter(_sw);
        exception.printStackTrace(_pw);
        exception.printStackTrace();
        final IWorkbenchWindow _iww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell _shell = null;
        if (_iww != null) {
            _shell = _iww.getShell();
        } else {
            _shell = Display.getDefault().getActiveShell();
        }
        final ExceptionDialog _ed = new ExceptionDialog(_shell, _sw.getBuffer().toString());
        _ed.open();
    }

    /**
     * Returns list of java files in the currently chosen project.
     * 
     * @param jproject
     *            The java project.
     * 
     * @return List The list of java files. PostCondition:
     *         result.oclIsKindOf(List) and
     *         result.elements.oclIsKindOf(IResource)
     */
    public static List processForFiles(final IJavaProject jproject) {
        List _javaFileList = new ArrayList();

        try {
            final IPackageFragment[] _fragments = jproject
                    .getPackageFragments();

            for (int _i = 0; _i < _fragments.length; _i++) {
                final IPackageFragment _fragment = _fragments[_i];

                if (_fragment.containsJavaResources()) {
                    final ICompilationUnit[] _units = _fragment
                            .getCompilationUnits();

                    for (int _j = 0; _j < _units.length; _j++) {
                        if (_units[_j].getElementType() == IJavaElement.COMPILATION_UNIT) {
                            final IResource _resource = _units[_j]
                                    .getCorrespondingResource();
                            _javaFileList.add(_resource);
                        }
                    }
                }
            }
        } catch (JavaModelException _jme) {
            SECommons.handleException(_jme);
            _javaFileList = null;
            KaveriErrorLog.logException("Java Model Exception", _jme);
        }
        return _javaFileList;
    }

    /**
     * Return whether the file has a main method present or not.
     * 
     * @param javafile
     *            The java file
     * 
     * @return boolean Whether a main function exists in the file
     */
    private static boolean isMainPresent(final IFile javafile) {
        final ICompilationUnit _icunit = (ICompilationUnit) JavaCore
                .create(javafile);
        boolean _result = false;

        try {
            if (_icunit != null) {
                IType[] _types = null;
                _types = _icunit.getAllTypes();

                for (int _nrun = 0; _nrun < _types.length; _nrun++) {
                    final IType _type = _types[_nrun];
                    final IMethod[] _methods = _type.getMethods();

                    for (int _i = 0; _i < _methods.length; _i++) {
                        final IMethod _method = _methods[_i];

                        if (_method.isMainMethod()) {
                            _result = true;
                            break;
                        }
                    }
                }
            }
        } catch (JavaModelException _jme) {
            KaveriErrorLog.logException("Java Model Exception", _jme);
            SECommons.handleException(_jme);
        }
        return _result;
    }

    /**
     * Search for files with main methods in the project where file exists.
     * 
     * @param lst
     *            The list to add the roots to
     * @param file
     *            The file to search for the main() function
     */
    private static void searchAndFindRoots(final List lst, final IFile file) {
        final IProject _project = file.getProject();
        final IJavaProject _jProject = JavaCore.create(_project);
        final List _lst = processForFiles(_jProject);

        for (int _i = 0; _i < _lst.size(); _i++) {
            final IFile _newfile = (IFile) _lst.get(_i);

            if (isMainPresent(_newfile)) {
                lst.add(_newfile);
            }
        }
    }

    /**
     * Loads the given classes into the scene.
     * 
     * @param lst
     * @param jfile
     */
    public static void loadupClasses(List lst, IFile jfile) {
        String _sootClassPath = "";
        IPath _jreclasspath = JavaCore.getClasspathVariable("JRE_LIB");
        final String _pathseparator = System.getProperty(Messages
                .getString("SootConvertor.3")); //$NON-NLS-1$
        final String _fileseparator = System.getProperty(Messages
                .getString("SootConvertor.4")); //$NON-NLS-1$
        if (_jreclasspath != null) {
            _sootClassPath = _jreclasspath.toOSString();
            _sootClassPath += _pathseparator;

            final IProject _project = jfile.getProject();
            final IJavaProject _jproject = JavaCore.create(_project);
            final Set _set = SECommons.getClassPathForProject(_jproject,
                    new HashSet(), false, true);
            for (Iterator iter = _set.iterator(); iter.hasNext();) {
                _sootClassPath += (String) iter.next();
            }
            //G.reset();
            final Scene _scene = Scene.v();
            Options.v().parse(Util.getSootOptions());

            /** Fix for the soot.CompilationDeathException. */
            //Options.v().set_src_prec(Options.src_prec_java);
            Options.v().set_keep_line_number(true);

            String _cpString = _scene.getSootClassPath();

            if (_cpString != null) {
                _cpString += File.pathSeparator + _sootClassPath
                        + File.pathSeparator
                        + System.getProperty("java.class.path");
            } else {
                _cpString = _sootClassPath;
            }

            _scene.setSootClassPath(_cpString);

            for (int _i = 0; _i < lst.size(); _i++) {
                try {
                    final String _classname = (String) lst.get(_i);
                    _scene.loadClassAndSupport(_classname);
                } catch (RuntimeException _rme) {
                    KaveriErrorLog.logException("Error loading soot class ",
                            _rme);
                    SECommons.handleException(_rme);
                    throw _rme;
                }

            }

        }
    }

    /**
     * @param _st
     * @return
     */
    public static int getLineNumberForStmt(Stmt _st) {
        int _nLine = -1;
        final LineNumberTag _lntag = (LineNumberTag) _st.getTag(Messages
                .getString("EclipseIndusDriver.4")); //$NON-NLS-1$
        final SourceLnPosTag _stag = (SourceLnPosTag) _st.getTag(Messages
                .getString("EclipseIndusDriver.5")); //$NON-NLS-1$

        if (_stag != null) {
            _nLine = _stag.startLn();
        } else {
            if (_lntag != null) {
                //_nLine = Integer.parseInt(_lntag.toString()); // To be used
                // with unpatched soot versions
                _nLine = _lntag.getLineNumber();
            }
        }

        return _nLine;
    }

    /**
     * Returns the Java file containing the method whose soot equivalent is
     * sootMethod TODO Add fix for multiple projects.
     * 
     * @param sootMethod
     * @param sampleFile
     *            A Java file from the project. Used to obtain the project.
     * @return IFile The file containing the given method, or null if no such
     *         file is present.
     */
    public static IFile getFileContainingClass(SootMethod sootMethod,
            final IFile sampleFile) {
        IFile _file = null;

        final IProject _project = sampleFile.getProject();
        final IJavaProject _jProject = JavaCore.create(_project);
        final Object _mapVal = classNameToFileMap.get(sootMethod
                .getDeclaringClass().getName());
        if (_mapVal != null) {
            _file = (IFile) _mapVal;
        } else {
            final List _fileList = SECommons.processForFiles(_jProject);
            for (Iterator iter = _fileList.iterator(); iter.hasNext();) {
                final IFile _jfile = (IFile) iter.next();
                final List _classNameList = SECommons.getClassesInFile(_jfile);
                for (Iterator iterator = _classNameList.iterator(); iterator
                        .hasNext();) {
                    final String _className = (String) iterator.next();
                    if (_className.equals(sootMethod.getDeclaringClass()
                            .getName())) {
                        _file = _jfile;
                        classNameToFileMap.put(sootMethod.getDeclaringClass()
                                .getName(), _file);
                    } else {
                        classNameToFileMap.put(_className, _jfile);
                    }

                }
            }
        }

        return _file;
    }

    public static Document getDocumentForJavaFile(final IFile javaFile) {
        Document _d = (Document) javaFileToDocumentMap.get(javaFile);
        if (_d == null) {
            try {
                _d = new Document(JavaCore.createCompilationUnitFrom(javaFile)
                        .getSource());
                javaFileToDocumentMap.put(javaFile, _d);
            } catch (JavaModelException e) {
                KaveriErrorLog.logException("Java Model Exception", e);
                SECommons.handleException(e);
            }

        }

        return _d;
    }

    /**
     * @param _sm
     * @param statement
     * @param file
     * @param lineNumber
     * @return
     */
    public static String getUniqueSignature(SootMethod _sm, String statement,
            int lineNumber) {
        return _sm.hashCode() + _sm.getSignature() + statement + ""
                + lineNumber;
    }
}

