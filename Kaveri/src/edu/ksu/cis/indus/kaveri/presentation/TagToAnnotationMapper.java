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
 * Created on Aug 18, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.presentation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.driver.Messages;

/**
 * This class is responsible for returning the mappings between classes and the
 * annotations.
 * 
 * @author ganeshan
 */
public class TagToAnnotationMapper {
    
    private static Object objLock = new Object();
    /**
     * Returns the annotation map for the given class name.
     * 
     * @param file
     *            The java file.
     * @return Map The map of class names to line numbers.
     */
    public static Map getAnnotationLinesForFile(IFile file) {
       Map _v = null;
    
        synchronized (objLock) {
        final List _lst = SECommons.getClassesInFile(file);
        _v = new HashMap();
        if (_lst != null && _lst.size() > 0) {
            for (int _i = 0; _i < _lst.size(); _i++) {
                final String _className = (String) _lst.get(_i);
                SootClass _sootclass = null;
                try {
                    _sootclass = Scene.v().getSootClass(_className);
                } catch (RuntimeException _rme) {
                    KaveriErrorLog
                            .logException("Error loading sootclass", _rme);
                }
                if (_sootclass != null) {
                    processClassForLines(_v, _sootclass);

                }
            }
        }
        }
        return _v;
    }

    /**
     * Returns the mapping information for the given soot class.
     * 
     * @param classMap
     *            The Main map
     * @param sootclass
     *  
     */
    private static void processClassForLines(final Map classMap,
            SootClass sootclass) {
        final List _list = sootclass.getMethods();
        final Map _mMap = new HashMap();

        for (int _nCtr = 0; _nCtr < _list.size(); _nCtr++) {
            final SootMethod _method = (SootMethod) _list.get(_nCtr);

            // Don't bother with abstract or non-concrete methods
            // This doesn't filter everything but works well enough
            if (_method.isAbstract() || !_method.isConcrete()) {
                continue;
            }

            Body _body = null;

            if (_method.hasActiveBody()) {
                // An active body is necessary to get the tags properly.
                // retrieving
                // destroys the tags
                _body = _method.getActiveBody();
            }  else {
                _body = _method.retrieveActiveBody();
            }
            
            

            final List _lst = new LinkedList();
            final String _classname = sootclass.getName();
            final String _methodname = SECommons.getSearchPattern(_method);
            final Chain _unitchain = _body.getUnits();
           // processUnit(_unitchain, _lst, _classname, _methodname);
            processUnit2(_unitchain, _lst, _classname, _methodname);

            if (_lst.size() > 0) {
                _mMap.put(_methodname, _lst);
            }
        }

        final String _className = sootclass.getName();

        //final List _linelist = (List) _v.get(_className);
        if (_mMap.size() > 0) {
            classMap.put(_className, _mMap);
        }

    }

    
    /**
     * Processes the list of jimple units in a soot method to get the
     * corresponding java line numbers to annotate.
     *  (Alternative version - bugs in the previous version).
     * @param unitchain
     *            The set of Jimple statements.
     * @param lst
     *            The list to add the line numbers
     * @param classname
     *            The name of the class in which the units exist.
     * @param methodname
     *            The name of the method in which the units exist.
     */
    
    private static void processUnit2(final Chain unitchain, final List lst,
            final String classname, final String methodname) {
        final Iterator _iterator = unitchain.snapshotIterator();
        Collection _c = new Vector();
        int _nLine = -1;
        int _currLine = -1;
        
        while (_iterator.hasNext()) {
            final Stmt _stmt = (Stmt) _iterator.next();
            _nLine = getLineNumberFromUnit(_stmt);
            if (_nLine == -1) {
                continue;
            } else if (_currLine == -1) {
                _currLine = _nLine;
                _c.add(_stmt);
                continue;
            }
            if (_currLine == _nLine) {
                _c.add(_stmt);                
            } else {
                if (_c.size() > 0) {
                    boolean _complete = true;
                    boolean _isSlicePresent = false;
                    for (Iterator iter = _c.iterator(); iter.hasNext();) {
                      final Stmt _setstmt = (Stmt) iter.next();
                      boolean _tagPresent = isSliceTagPresent(_setstmt);
                      _complete = _complete & _tagPresent;
                      _isSlicePresent = _isSlicePresent | _tagPresent;                        
                    }
                    if (_isSlicePresent) {
                        final AnnotationData _data = new AnnotationData();
                        _data.setClassName(classname);
                        _data.setMethodName(methodname);
                        _data.setNLineNumber(_currLine);
                        _data.setComplete(_complete);
                        if (!lst.contains(_data)) {
                            lst.add(_data);
                        } else {
                            final AnnotationData _tdata = (AnnotationData) lst.get(lst.indexOf(_data));
                            _tdata.setComplete(_tdata.isComplete() & _data.isComplete());                            
                        }
                    }                    
                }
                _currLine = _nLine;
                _c.clear();
                _c.add(_stmt);
            }
        }
        if (_currLine != -1 && _c.size() > 0) {
            boolean _complete = true;
            boolean _isSlicePresent = false;
            for (Iterator iter = _c.iterator(); iter.hasNext();) {
              final Stmt _setstmt = (Stmt) iter.next();
              boolean _tagPresent = isSliceTagPresent(_setstmt);
              _complete = _complete & _tagPresent;
              _isSlicePresent = _isSlicePresent | _tagPresent;                        
            }
            if (_isSlicePresent) {
                final AnnotationData _data = new AnnotationData();
                _data.setClassName(classname);
                _data.setMethodName(methodname);
                _data.setNLineNumber(_currLine);
                _data.setComplete(_complete);
                if (!lst.contains(_data)) {
                    lst.add(_data);
                } else {
                    final AnnotationData _tdata = (AnnotationData) lst.get(lst.indexOf(_data));
                    _tdata.setComplete(_tdata.isComplete() & _data.isComplete());                            
                }
            }
        }
        
    }
    
    /**
     * Processes the list of jimple units in a soot method to get the
     * corresponding java line numbers to annotate.
     * 
     * @param unitchain
     *            The set of Jimple statements.
     * @param lst
     *            The list to add the line numbers
     * @param classname
     *            The name of the class in which the units exist.
     * @param methodname
     *            The name of the method in which the units exist.
     */
    private static void processUnit(final Chain unitchain, final List lst,
            final String classname, final String methodname) {
        final Iterator _unititerator = unitchain.snapshotIterator();
        int _currLine = -1;
        boolean _wasComplete = true;
        boolean _atleastSlicePresent = false;

        while (_unititerator.hasNext()) {
            final Stmt _stmt = (Stmt) _unititerator.next();
            int _nLine = getLineNumberFromUnit(_stmt);

            if (_nLine == -1) {
                continue;
            } else if (_currLine == -1) {
                _currLine = _nLine;
                _atleastSlicePresent = isSliceTagPresent(_stmt);
                if (!_atleastSlicePresent) {
                    _wasComplete = false;
                }
                continue;
            }

            if (_currLine == _nLine) {
                if (!isSliceTagPresent(_stmt)) {
                    _wasComplete = false;
                    continue;
                } 
                    _atleastSlicePresent = true;                
            } else {
                if (_atleastSlicePresent) {
                    final AnnotationData _data = new AnnotationData();
                    _data.setNLineNumber(_currLine);
                    _data.setComplete(_wasComplete);
                    _data.setClassName(classname);
                    _data.setMethodName(methodname);

                    if (!lst.contains(_data)) {
                        lst.add(_data);
                    } else {
                        final AnnotationData _tdata = (AnnotationData) lst
                                .get(lst.indexOf(_data));
                        _tdata.setComplete(_data.isComplete()
                                | _tdata.isComplete());
                    }
                }
                _currLine = _nLine;
                _wasComplete = isSliceTagPresent(_stmt); /* Changed */
                _atleastSlicePresent = isSliceTagPresent(_stmt);
            }
        }

        if (_currLine != -1 && _atleastSlicePresent) {
            final AnnotationData _data = new AnnotationData();
            _data.setNLineNumber(_currLine);
            _data.setComplete(_wasComplete);
            _data.setClassName(classname);
            _data.setMethodName(methodname);

            if (!lst.contains(_data)) {
                lst.add(_data);
            } else {
                final AnnotationData _tdata = (AnnotationData) lst.get(lst
                        .indexOf(_data));
                _tdata.setComplete(_data.isComplete() | _tdata.isComplete());
            }
        }
    }

    /**
     * Gets the Java line number for a Jimple Stmt.
     * 
     * @param unit
     *            The Jimple Statement from which the line number is to be
     *            extracted Postcondition: result == -1 if no useful tags
     *            present, result = linenumber otherwise
     * 
     * @return int Line Number
     */
    private static int getLineNumberFromUnit(final Stmt unit) {
        int _nLine = -1;
        final LineNumberTag _lntag = (LineNumberTag) unit.getTag(Messages
                .getString("EclipseIndusDriver.4")); //$NON-NLS-1$
        final SourceLnPosTag _stag = (SourceLnPosTag) unit.getTag(Messages
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
     * Decides if the jimple statememt has a slice tag associated with it or
     * not.
     * 
     * @param stmt
     *            The Jimple Stmt for which the presence of tag is to be
     *            checked.
     * 
     * @return boolean true if the slice tag is present
     */
    private static boolean isSliceTagPresent(final Stmt stmt) {
        boolean _btagpresent = false;
        final String nameOfSliceTag = KaveriPlugin.getDefault()
                .getIndusConfiguration().getEclipseIndusDriver()
                .getNameOfSliceTag();
        final NamedTag _sTag = (NamedTag) stmt.getTag(nameOfSliceTag);

        if (_sTag != null) {
            _btagpresent = true;
        }
        return _btagpresent;
    }
}