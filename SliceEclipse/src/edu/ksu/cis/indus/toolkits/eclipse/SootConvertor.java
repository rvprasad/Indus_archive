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
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ksu.cis.indus.toolkits.eclipse;

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.toolkits.sliceeclipse.common.SECommons;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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


/**
 * 
 * This class is responsible for converting java to its soot equivalent.
 * Format returned is:
 * ArrayList(0) : SootClass
 * ArrayList(1) : SootMethod
 * ArrayList(2) : Size : List of Stmt
 * @author Ganeshan
 *
 */
public final class SootConvertor {

	/**
	 * Constructor.
	 *
	 */
	private SootConvertor() {
		// Dummy Constructor
	}

	/**
	 * Returns the set of jimple stmt corresponding to the given java line.
	 * @param thefile The Java file
	 * @param theclass The JDT class
	 * @param themethod The JDT method
	 * @param theline The chosen line
	 * @return ArrayList The list of SootClass, SootMethod, list of Jimple Stmts
	 */
	public static ArrayList getStmtForLine(final IFile thefile,
			final IType theclass, final IMethod themethod, final int theline) {
		ArrayList _stmtlist = null;
		String _sootClassPath = ""; 
		//G.reset();
		IPath _jreclasspath = JavaCore.getClasspathVariable(Messages
				.getString("SootConvertor.1")); //$NON-NLS-1$
		_jreclasspath = JavaCore.getClasspathVariable(Messages
				.getString("SootConvertor.2")); /* Added for "compatibilty" with 
		eclipse 3MX versions. Remove in the future */		
		final String _pathseparator = System.getProperty(Messages
				.getString("SootConvertor.3")); //$NON-NLS-1$
		final String _fileseparator = System.getProperty(Messages
				.getString("SootConvertor.4")); //$NON-NLS-1$
		if (_jreclasspath != null) {
			_sootClassPath = _jreclasspath.toOSString();
			_sootClassPath += _pathseparator;
			final IPath _path = thefile.getProject().getLocation();
			_sootClassPath += _path.toOSString();
			_sootClassPath += _fileseparator + _pathseparator;
			//G.reset();
			final Scene _scene = Scene.v();
			Options.v().parse(Util.getSootOptions());
			Options.v().set_src_prec(Options.src_prec_java);
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
			SootClass _sootclass = null;
			try {
				_sootclass = _scene.loadClassAndSupport(theclass
						.getFullyQualifiedName());
			} catch (RuntimeException _rme) {
				// Unavoidable to catch this. Soot throws 
				// this sometimes.
				SECommons.handleException(_rme);
			}
			if (_sootclass != null) {
				_stmtlist = getStmtLine(_sootclass, themethod, theline);
			}
		}
		//G.reset();
		return _stmtlist;
	}

	/**
	 * Returns the ArrayList of corresponding jimple.
	 * @param sootclass The coressponding SootClass
	 * @param themethod The JDT method
	 * @param theline The chosen Java line
	 * @return ArrayList The list of SootClass, SootMethod, list of Jimple Stmts 
	 *  postcondition: ArrayList.firstElement.kindOf(SootClass), ArrayList.secondElement.kindOf(SootMethod),
	 * ArrayList.remainingElements.kindOf(Stmt)
	 */
	private static ArrayList getStmtLine(final SootClass sootclass,
			final IMethod themethod, final int theline) {
		ArrayList _stmtlist = null;
		SootMethod _method = null;
		try {
			if (themethod.getDeclaringType().getElementName().equals(
					themethod.getElementName())) {
				_method = sootclass.getMethodByName(Messages
						.getString("SootConvertor.6")); // Constructor

			} else {
				_method = sootclass.getMethodByName(themethod.getElementName()); //getCorrSootMethod(themethod, sootclass);
			}
		} catch (RuntimeException _ame) {
			if (_ame.getMessage().equals(Messages.getString("SootConvertor.7"))) { //$NON-NLS-1$
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
			ArrayList _stmtslst;
			_stmtslst = isStmtPresent(_body, theline);
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
	 * Returns the SootMethod corresponding to a given IMethod.
	 * @param themethod The JDT method
	 * @param sootclass The SootClass in which the coressponding SootMethod lies.
	 * @return SootMethod The SootMethod coressponding to the IMethod
	 */
	private static SootMethod getCorrSootMethod(final IMethod themethod,
			final SootClass sootclass) {
		String _methodPattern = PrettySignature.getMethodSignature(themethod);
		//final int _index = _methodPattern.lastIndexOf("."); //$NON-NLS-1$
		//if (_index > 0) {
		//	_methodPattern = _methodPattern.substring(_index + 1);
		//}
		
		// Remove class name qualifiers on the method as soot doesn't add the full classname
		// to the method name.
		final int _index = _methodPattern.indexOf('(');
		final String _fullclassString = _methodPattern.substring(0, _index);
		final int _index1 = _fullclassString.lastIndexOf(".");
		if (_index1 > 0) {
		final String _funcname = _fullclassString.substring(_index1 + 1, _index);
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
					_methodPattern.replaceFirst(themethod.getElementName(),
							Messages.getString("SootConvertor.9")); 
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
	 * Returns the list of Stmts with the given line number.
	 * @param body The Jimple Body
	 * @param theline The selected Java line.
	 * @return ArrayList The list of SootClass, SootMethod, list of Jimple Stmts
	 */
	private static ArrayList isStmtPresent(final Body body, final int theline) {
		final ArrayList _stmt = new ArrayList();
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
	 * Returns a search pattern for the IMethod corressponding to the given SootMethod.
	 * @param selectedSootMethod The SootMethod from which to generate the search pattern
	 * @return The string pattern of the type function-name(type-arg1, type-arg2...)
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
					_pattern.append(","); //$NON-NLS-1$
					_tempPattern = _paramTypes[_nCtr].toString();
					_index = _tempPattern.lastIndexOf("."); //$NON-NLS-1$
					// Eclipse doesn't add class names to the parameter type. Hence this.
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

}