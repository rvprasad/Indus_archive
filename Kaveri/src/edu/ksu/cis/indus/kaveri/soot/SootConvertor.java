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
 * Created on Jun 10, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.soot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.search.PrettySignature;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.driver.Messages;

/**
 * This class is responsible for performing the conversion from a specified Java
 * statement to its equivalent Jimple statements.
 * 
 * @author Ganeshan
 */
public final class SootConvertor {
    /**
     * Constructor.
     */
    private SootConvertor() {
        // Dummy Constructor
    }

    /**
     * Returns the set of jimple statemetns corresponding to the given java
     * statement.
     * 
     * @param thefile
     *            The Java file in which the statement is present
     * @param theclass
     *            The JDT class in which the statement is present
     * @param themethod
     *            The JDT method in which the statement is present
     * @param theline
     *            The line number of the chosen Java statement
     * 
     * @return List The list of SootClass, SootMethod and the equivalent Jimple
     *         Stmts <br>
     *         <b>Postcondition: </b> result.firstElement.oclIsKindOf(SootClass)
     *         result.secondElement.oclIsKindOf(SootMethod)
     *         result.remainingElements.oclIsKindOf(Stmt)
     * 
     * @throws NullPointerException
     *             Throws NullPointerException on null parameters
     */
    public static List getStmtForLine(final IFile thefile,
            final IType theclass, final IMethod themethod, final int theline)
            throws NullPointerException {
        List _stmtlist = null;
        String _sootClassPath = "";

        if (SECommons.checkForNull(thefile) || SECommons.checkForNull(theclass)
                || SECommons.checkForNull(themethod)) {
            throw new NullPointerException(
                    "getStmtForLine expects non-null arguments");
        }

        final SootClass _sootclass = loadClass(theclass);

        if (_sootclass != null) {
            _stmtlist = getStmtLine(_sootclass, themethod, theline);
        }

        //G.reset();
        return _stmtlist;
    }

    /**
     * Returns the SootMethod corresponding to a given IMethod. Postcondition:
     * result = null if a corresponding SootMethod could be found, else the Soot
     * Method
     * 
     * @param themethod
     *            The JDT method
     * @param sootclass
     *            The SootClass in which the coressponding SootMethod lies.
     * 
     * @return SootMethod The SootMethod coressponding to the IMethod
     */
    private static SootMethod getCorrSootMethod(final IMethod themethod,
            final SootClass sootclass) {
        String _methodPattern = PrettySignature.getMethodSignature(themethod);

        //final int _index = _methodPattern.lastIndexOf("."); //$NON-NLS-1$
        //if (_index > 0) {
        //	_methodPattern = _methodPattern.substring(_index + 1);
        //}
        // Remove class name qualifiers on the method as soot doesn't add the
        // full classname
        // to the method name.
        final int _index = _methodPattern.indexOf('(');
        final String _fullclassString = _methodPattern.substring(0, _index);
        final int _index1 = _fullclassString.lastIndexOf(".");

        if (_index1 > 0) {
            final String _funcname = _fullclassString.substring(_index1 + 1,
                    _index);
            _methodPattern = _funcname + _methodPattern.substring(_index);
        }

        final List _v = sootclass.getMethods();
        SootMethod _resultMethod = null;

        if (_v != null && _v.size() > 0) {
            for (int _nCtr = 0; _nCtr < _v.size(); _nCtr++) {
                final SootMethod _tempMethod = (SootMethod) _v.get(_nCtr);

                //final String _javam = _tempMethod.getDavaDeclaration();
                final String _sootPattern = getSearchPattern(_tempMethod);

                if (themethod.getDeclaringType().getElementName().equals(
                        themethod.getElementName())) {
                    _methodPattern = _methodPattern.replaceFirst(themethod
                            .getElementName(), Messages
                            .getString("SootConvertor.9"));
                    // Change method name to <init> for compatibility
                }

                if (_sootPattern.equals(_methodPattern)) {
                    _resultMethod = (SootMethod) _v.get(_nCtr);
                    break;
                }
            }
        }
        return _resultMethod;
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
    private static String getSearchPattern(final SootMethod selectedSootMethod) {
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
     * Returns a list of of corresponding jimple statements.
     * 
     * @param sootclass
     *            The coressponding SootClass
     * @param themethod
     *            The JDT method
     * @param theline
     *            The chosen Java line
     * 
     * @return List The list of SootClass, SootMethod and Jimple Stmts
     *         Postcondition: result.firstElement.kindOf(SootClass),
     *         result.secondElement.kindOf(SootMethod),
     *         result.remainingElements.kindOf(Stmt)
     */
    private static List getStmtLine(final SootClass sootclass,
            final IMethod themethod, final int theline) {
        List _stmtlist = null;
        SootMethod _method = null;

        try {
            if (themethod.getDeclaringType().getElementName().equals(
                    themethod.getElementName())) {
                _method = sootclass.getMethodByName(Messages
                        .getString("SootConvertor.6")); // Constructor
            } else {
                _method = sootclass.getMethodByName(themethod.getElementName()); //getCorrSootMethod(themethod,
                // sootclass);
            }
        } catch (RuntimeException _ame) {
            // Unavoidable, soot creates the exception.
            // Need to catch this to have a proper
            // method mapping. Would be better if soot
            // threw a user defined exception

            if (_ame.getMessage().equals(Messages.getString("SootConvertor.7"))) {
                _method = getCorrSootMethod(themethod, sootclass);
            }
        }

        if (_method != null) {
            Body _body = null;

            if (_method.hasActiveBody()) {
                _body = _method.getActiveBody();
            } else {
                _body = _method.retrieveActiveBody();
            }

            List _stmtslst;
            _stmtslst = getStmts(_body, theline);

            if (_stmtslst != null) {
                _stmtlist = new ArrayList();
                _stmtlist.add(sootclass);
                _stmtlist.add(_method);
                _stmtlist.addAll(_stmtslst);
            }
        }

        return _stmtlist;
    }

    /**
     * Returns the list of Stmts with the given line number.
     * 
     * @param body
     *            The Jimple Body
     * @param theline
     *            The selected Java line.
     * 
     * @return List The list of Jimple Stmts
     */
    public static List getStmts(final Body body, final int theline) {
        final List _stmt = new ArrayList();
        final Chain _unitchain = body.getUnits();
        final Iterator _iterator = _unitchain.snapshotIterator();
        int _nLine;

        while (_iterator.hasNext()) {
            final Stmt _tempStmt = (Stmt) _iterator.next();
            _nLine = -1;

            final LineNumberTag _lntag = (LineNumberTag) _tempStmt
                    .getTag(Messages.getString("EclipseIndusDriver.4")); //$NON-NLS-1$
            final SourceLnPosTag _stag = (SourceLnPosTag) _tempStmt
                    .getTag(Messages.getString("EclipseIndusDriver.5")); //$NON-NLS-1$		

            if (_stag != null) {
                _nLine = _stag.startLn();
            } else {
                if (_lntag != null) {
                    //_nLine = Integer.parseInt(_lntag.toString());
                    _nLine = _lntag.getLineNumber();
                }
            }

            if (_nLine != -1 && _nLine == theline) {
                _stmt.add(_tempStmt);
            }
        }
        return _stmt;
    }

    /**
     * Returns the java line number for the given statement;
     * 
     * @param stmt
     * @return
     */
    public static int getLineNumber(Stmt stmt) {
        int _nLine = -1;
        final LineNumberTag _lntag = (LineNumberTag) stmt.getTag(Messages
                .getString("EclipseIndusDriver.4")); //$NON-NLS-1$
        final SourceLnPosTag _stag = (SourceLnPosTag) stmt.getTag(Messages
                .getString("EclipseIndusDriver.5")); //$NON-NLS-1$		

        if (_stag != null) {
            _nLine = _stag.startLn();
        } else {
            if (_lntag != null) {
                //_nLine = Integer.parseInt(_lntag.toString());
                _nLine = _lntag.getLineNumber();
            }
        }
        return _nLine;
    }

    /**
     * Get the soot method for the given JDT Method.
     * 
     * @param method
     *            The JDT method.
     * @return
     */
    public static SootMethod getSootMethod(final IMethod method) {
        SootMethod _sm = null;
        final IType _type = method.getDeclaringType();
        if (Scene.v().containsClass(_type.getFullyQualifiedName())) {
            _sm = getCorrSootMethod(method, Scene.v().getSootClass(
                    _type.getFullyQualifiedName()));
        } else {
            //  Load the class.
            final SootClass _sootClass = loadClass(method.getDeclaringType());
            if (_sootClass != null) {
                _sm = getCorrSootMethod(method, _sootClass);
            }
        }

        return _sm;
    }

    /**
     * Load the given JDT class.
     * 
     * @param javaClass
     * @return
     */
    private static SootClass loadClass(final IType javaClass) {
        SootClass _sootclass = null;
        final ICompilationUnit _unit = javaClass.getCompilationUnit();
        if (_unit == null) {
            System.out.println("Nothing found");
            return _sootclass;
        }
        IFile _file = null;
        try {
            _file = (IFile) _unit.getCorrespondingResource();
        } catch (JavaModelException _jme) {
            SECommons.handleException(_jme);
            KaveriErrorLog.logException("Java Model Exception", _jme);
        }
        if (_file == null)
            return _sootclass;

        IPath _jreclasspath = JavaCore.getClasspathVariable(Messages
                .getString("SootConvertor.1")); //$NON-NLS-1$
        _jreclasspath = JavaCore.getClasspathVariable(Messages
                .getString("SootConvertor.2"));
        /*
         * Added for "compatibilty" with eclipse 3MX versions. Remove in the
         * future
         */
        String _sootClassPath = "";
        final String _pathseparator = System.getProperty(Messages
                .getString("SootConvertor.3")); //$NON-NLS-1$
        final String _fileseparator = System.getProperty(Messages
                .getString("SootConvertor.4")); //$NON-NLS-1$

        if (_jreclasspath != null) {
            _sootClassPath = _jreclasspath.toOSString();
            _sootClassPath += _pathseparator;

            final IProject _project = _file.getProject();
            final IJavaProject _jproject = JavaCore.create(_project);

            final Set _set = SECommons.getClassPathForProject(_jproject,
                    new HashSet(), false);
            for (Iterator iter = _set.iterator(); iter.hasNext();) {
                _sootClassPath += (String) iter.next();
            }

            //G.reset();
            final Scene _scene = Scene.v();
            Options.v().parse(Util.getSootOptions());
            // Fix for the soot.CompilationDeathError.
            // Options.v().set_src_prec(Options.src_prec_java);
            Options.v().set_keep_line_number(true);

            String _cpString = _scene.getSootClassPath();

            if (_cpString != null) {
                _cpString += File.pathSeparator
                        + _sootClassPath
                        + File.pathSeparator
                        + System.getProperty(Messages
                                .getString("SootConvertor.5")); //$NON-NLS-1$
            } else {
                _cpString = _sootClassPath;
            }

            _scene.setSootClassPath(_cpString);
            Scene.v().loadNecessaryClasses();
            try {
                _sootclass = _scene.loadClassAndSupport(javaClass
                        .getFullyQualifiedName());
            } catch (RuntimeException _rme) {
                KaveriErrorLog.logException("Unable to load soot class", _rme);
                SECommons.handleException(_rme);
            }
        }

        return _sootclass;
    }

}