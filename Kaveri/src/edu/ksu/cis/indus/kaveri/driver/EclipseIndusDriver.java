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
 * Created on Apr 1, 2004
 *
 * Indus Driver for eclipse
 */
package edu.ksu.cis.indus.kaveri.driver;

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.presentation.AnnotationData;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.transformations.TagBasedDestructiveSliceResidualizer;

import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;
import edu.ksu.cis.indus.tools.slicer.contextualizers.StaticSliceCriteriaCallStackContextualizer;
import edu.ksu.cis.indus.tools.slicer.criteria.generators.StaticSliceCriteriaGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.Path;
import org.jibx.runtime.JiBXException;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

import soot.options.Options;

import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;

import soot.util.Chain;

/**
 * The main interface between Kaveri and the Indus slicer. This drives the
 * slicer on behalf of Kaveri.
 * 
 * @author Ganeshan
 */
public class EclipseIndusDriver extends SootBasedDriver {
    /**
     * <p>
     * Logger to log the activities of the run.
     * </p>
     */
    private static final Log LOGGER = LogFactory
            .getLog(EclipseIndusDriver.class);

    /**
     * The set of criteria.
     */
    private Collection criteria = Collections.EMPTY_LIST;

    /**
     * The set of contexts.
     */
    private Collection contextCollection = Collections.EMPTY_LIST;

    /**
     * The slicecriteria factory instance.
     */
    private SliceCriteriaFactory factory;

    /**
     * <p>
     * The slicer tool.
     * </p>
     */
    private SlicerTool slicer;

    /**
     * <p>
     * Location of the default configuration, relative to the plugin directory.
     * </p>
     */
    private String defaultConfigFilename = Messages
            .getString("EclipseIndusDriver.0"); //$NON-NLS-1$

    /**
     * <p>
     * The slice tag name.
     * </p>
     */
    private String nameOfSliceTag = ""; // Messages.getString("EclipseIndusDriver.1");
                                        // //$NON-NLS-1$

    /**
     * Creates a new EclipseIndusDriver object.
     */
    public EclipseIndusDriver() {
        factory = SliceCriteriaFactory.getFactory();
        contextCollection = new ArrayList();
    }

    /**
     * Sets the given list of classes to application classes.
     * 
     * @param classes
     *            The set of class names to set to application classes
     */
    public void setApplicationClasses(List classes) {
        for (int _i = 0; _i < classes.size(); _i++) {
            final String _classname = (String) classes.get(_i);
            try {
                final SootClass _sootclass = scene.getSootClass(_classname);
                if (_sootclass != null) {
                    _sootclass.setApplicationClass();
                }

            } catch (RuntimeException _rme) {
                //KaveriErrorLog.logException("Soot Class not present", _rme);
                // No need to log this. Expected if the class was not loaded by
                // soot.
            }
        }
    }

    /**
     * This function is used to extract line numbers of Java statements from the
     * corresponding Jimple statements which have a slice tag assiciated with
     * them. Returns a mapping of class names with their associated line
     * numbers. <b>Postcondition: </b> result.oclIsKindOf(HashMap) &&
     * result.keys.oclIsKindOf(String : classname) &&
     * result.values.oclIsKindOf(Map) and
     * result.values.values.oclIsKindOf(Collection(AnnotationData))
     * 
     * @return Map The map of classnames to line numbers
     */
    public Map getAnnotationLineNumbers() {
        final Map _v = new HashMap();
        final Chain _classlist = Scene.v().getApplicationClasses();
        final Iterator _iterator = _classlist.snapshotIterator();

        while (_iterator.hasNext()) {
            final SootClass _sootclass = (SootClass) _iterator.next();
            final List _list = _sootclass.getMethods();
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
                } else {
                    _body = _method.retrieveActiveBody();
                }

                final List _lst = new LinkedList();

                /*
                 * final int _line = getLineNumberFromMethod(_method); if (_line >
                 * -1) { final AnnotationData _data = new AnnotationData();
                 * _data.setComplete(true); _data.setNLineNumber(_line); if
                 * (!_lst.contains(_data)) { _lst.add(_data); } }
                 */
                final String _classname = _sootclass.getName();
                final String _methodname = SECommons.getSearchPattern(_method);
                final Chain _unitchain = _body.getUnits();
                processUnit(_unitchain, _lst, _classname, _methodname);

                if (_lst.size() > 0) {
                    _mMap.put(_methodname, _lst);
                }
            }

            final String _className = _sootclass.getName();

            //final List _linelist = (List) _v.get(_className);
            if (_mMap.size() > 0) {
                _v.put(_className, _mMap);
            }
        }

        return _v;
    }

    /**
     * <p>
     * Sets the classes on which indus will operate.
     * </p>
     * Precondition: classes != null and classes.oclIsKindOf(Collection(String))
     * 
     * @param classes
     *            The collection of class names
     * 
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     * @throws IllegalArgumentException
     *             Throws IllegalArgumentException if invalid parameters are
     *             passed.
     */
    public void setClasses(final Collection classes)
            throws NullPointerException, IllegalArgumentException {
        if (SECommons.checkForNull(classes)) {
            throw new NullPointerException("setClasses got null for classname");
        }

        if (classes.size() == 0) {
            throw new IllegalArgumentException("setClasses got no classes");
        }

        if (!SECommons.checkForClassEquality(classes.iterator().next(),
                String.class)) {
            throw new IllegalArgumentException(
                    "setClasses expects Strings as classnames");
        }
        super.setClassNames(classes);
    }

    /**
     * <p>
     * Sets the current configuration to the configuration present in the given
     * file.
     * </p>
     * Precondition: configuration != null
     * 
     * @param configuration
     *            URL pointing to the configuration file
     * 
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     * @throws IllegalArgumentException
     *             Throws IllegalArgumentException if invalid parameters are
     *             passed.
     * @throws IOException
     *             Throws IOException if unable to parse the configuration
     */
    public final void setConfiguration(final URL configuration)
            throws NullPointerException, IllegalArgumentException, IOException {
        String _defaultConfiguration;

        if (SECommons.checkForNull(configuration)) {
            throw new NullPointerException(
                    "URL set to null in setConfiguration");
        }

        boolean _isOk = false;

        if (configuration == null) {
            _defaultConfiguration = getDefaultConfiguration();
            _isOk = slicer.destringizeConfiguration(_defaultConfiguration);

            if (!_isOk) {
                throw new IllegalArgumentException(
                        "setConfiguration sent an invalid configuration");
            }
        } else {
            _isOk = slicer
                    .destringizeConfiguration(parseConfiguration(configuration));

            if (!_isOk) {
                throw new IllegalArgumentException(
                        "setConfiguration sent an invalid configuration");
            }
        }
    }

    /**
     * Sets the configuration to the configuration specified by config.
     * <b>Precondition: </b> config != null
     * 
     * @param config
     *            The configuration
     * 
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     * @throws IllegalArgumentException
     *             Throws IllegalArgumentException if invalid parameters are
     *             passed.
     */
    public void setConfiguration(final String config)
            throws NullPointerException, IllegalArgumentException {
        if (SECommons.checkForNull(config)) {
            throw new NullPointerException(
                    "setConfiguration expects non-null parameter");
        }

        final boolean _isOk = slicer.destringizeConfiguration(config);

        if (!_isOk) {
            throw new IllegalArgumentException(
                    "setConfiguration sent invalid configuration");
        }
    }

    /**
     * <p>
     * Sets the criteria used for slicing.
     * </p>
     * Precondition : sootMethod , stmt != null
     * 
     * @param sootMethod
     *            The soot method inside which the criteria lies
     * @param stmt
     *            The jimple statement criteria
     * @param considerVal
     *            The value at that statement should be considered or not
     * 
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     *  
     */
    public void setCriteria(final SootMethod sootMethod, final Stmt stmt,
            final boolean considerVal) throws NullPointerException {
        if (SECommons.checkForNull(sootMethod) || SECommons.checkForNull(stmt)) {
            throw new NullPointerException(
                    "setCriteria expects non-null parameters");
        }

        final Collection _coll = factory.getCriteria(sootMethod, stmt, true,
                considerVal);

        if (criteria != Collections.EMPTY_LIST) {
            _coll.addAll(criteria);
        }
        criteria = _coll;

    }

    /**
     * Residualize the Scence.
     *
     */
    public void residualize() {
        final TagBasedDestructiveSliceResidualizer _residualizer = new TagBasedDestructiveSliceResidualizer();
		_residualizer.setTagToResidualize(nameOfSliceTag);
		_residualizer.setBasicBlockGraphMgr(slicer.getBasicBlockGraphManager());
		_residualizer.residualizeSystem(slicer.getSystem());
    }
    
    
    /**
     * <p>
     * Sets the criteria used for slicing.
     * </p>
     * precondition: sootMethod, stmt, box != null
     * 
     * @param sootMethod
     *            The soot method inside which the criteria lies
     * @param stmt
     *            The jimple statement criteria
     * @param box
     *            The ValueBox criteria
     * 
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     *  
     */
    public void setCriteria(final SootMethod sootMethod, final Stmt stmt,
            final ValueBox box) throws NullPointerException {
        if (SECommons.checkForNull(sootMethod) || SECommons.checkForNull(stmt)
                || SECommons.checkForNull(box)) {
            throw new NullPointerException(
                    "setCriteria expects non-null parameters");
        }

        final Collection _coll = factory.getCriteria(sootMethod, stmt, box,
                true);

        if (criteria != Collections.EMPTY_LIST) {
            _coll.addAll(criteria);
        }
        criteria = _coll;
    }

    /**
     * Returns the default configuration filename.
     * 
     * @return Returns the defaultConfigFilename.
     */
    public String getDefaultConfigFilename() {
        return defaultConfigFilename;
    }

    /**
     * Returns the slicer tool instance.
     * 
     * @return SliceTool Returns the slicer tool.
     */
    public SlicerTool getSlicer() {
        return slicer;
    }

    /**
     * <p>
     * Adds the given path to the soot class path.
     * </p>
     * Precondition: path != null
     * 
     * @param path
     *            The path to add to the soot class path
     * 
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     *  
     */
    public void addToPath(final String path) throws NullPointerException {
        if (SECommons.checkForNull(path)) {
            throw new NullPointerException(
                    "addToPath expects non-null parameters");
        }
        addToSootClassPath(path);
    }

    /**
     * <p>
     * Executes the slicer.
     * </p>
     */
    public void execute() {
        slicer.setTagName(nameOfSliceTag);
        slicer.setSystem(new Environment(scene));
        slicer.setRootMethods(rootMethods);
        final String _scopeStr = KaveriPlugin.getDefault()
                .getIndusConfiguration().getScopeSpecification();
        try {
            if (!(_scopeStr == null || _scopeStr.equals(""))) {
                slicer
                        .setSliceScopeDefinition(SpecificationBasedScopeDefinition
                                .deserialize(_scopeStr));
            }
        } catch (JiBXException _jbe) {
            SECommons.handleException(_jbe);
            KaveriErrorLog.logException(
                    "Error while deserializing scope specification", _jbe);
        }

        StaticSliceCriteriaGenerator _sscg = null;
         if (contextCollection.size() > 0) { 
             _sscg = new StaticSliceCriteriaGenerator(criteria);
             _sscg.setCriteriaContextualizer(new StaticSliceCriteriaCallStackContextualizer(contextCollection));    
             slicer.addCriteriaGenerator(_sscg); 
          }
                  

        slicer.setCriteria(criteria);
        slicer.run(Phase.STARTING_PHASE, true); // changed from true
        if (_sscg != null) {
            slicer.removeCriteriaGenerator(_sscg);
        }       
    }

    /**
     * <p>
     * Initialize the soot driver.
     * </p>
     */
    public void initializeSlicer() {
        Options.v().set_keep_line_number(true);
        // Fix for soot.CompilationDeathException.
        //Options.v().set_src_prec(Options.src_prec_java);
        super.initialize();
    }

    /**
     * Resets soot and the slicer.
     */
    public void reset() {
        //G.reset();
        criteria.clear();
        contextCollection.clear();
        super.reset();        
        if (slicer != null) {
            slicer.reset();
            slicer.setSliceScopeDefinition(null);
        }
    }

    /**
     * Parses the default configuration file and returns the contents.
     * 
     * @return String The parsed contents of the default configuration
     * 
     * @throws IOException
     *             Throws IOException if an IOError occurs.
     */
    private String getDefaultConfiguration() throws IOException {
        final StringBuffer _defaultConfiguration = new StringBuffer();

        try {
            final InputStream _is = KaveriPlugin.getDefault().openStream(
                    new Path(defaultConfigFilename));
            final BufferedReader _configReader = new BufferedReader(
                    new InputStreamReader(_is));

            while (_configReader.ready()) {
                _defaultConfiguration.append(_configReader.readLine());
            }
            _configReader.close();
        } catch (IOException _ioe) {
            KaveriErrorLog.logException(
                    "Error rading the default configuration", _ioe);
            throw new IOException(
                    "IO error, could not get defaultconfiguration");
        }
        return _defaultConfiguration.toString();
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
    private int getLineNumberFromUnit(final Stmt unit) {
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
    private boolean isSliceTagPresent(final Stmt stmt) {
        boolean _btagpresent = false;
        final NamedTag _sTag = (NamedTag) stmt.getTag(nameOfSliceTag);

        if (_sTag != null) {
            _btagpresent = true;
        }
        return _btagpresent;
    }

    /**
     * Parses the configuration file and returns a string containing the parsed
     * text.
     * 
     * @param configuration
     *            The URL to the configuration file
     * 
     * @return String The string of the contents of the configuration file
     * @throws NullPointerException
     *             Throws KaveriException if null is passed as parameter.
     * @throws IOException
     *             Throws IOException if an IOError occurs / configuration is
     *             null.
     */
    private String parseConfiguration(final URL configuration)
            throws NullPointerException, IOException {
        if (SECommons.checkForNull(configuration)) {
            throw new NullPointerException(
                    "parseConfiguration expects a non-null configuration");
        }

        final StringBuffer _userConfiguration = new StringBuffer();

        try {
            final BufferedReader _configReader = new BufferedReader(
                    new InputStreamReader(configuration.openStream()));

            while (_configReader.ready()) {
                _userConfiguration.append(_configReader.readLine());
            }
            _configReader.close();
        } catch (IOException _ioe) {
            LOGGER.fatal(Messages.getString("EclipseIndusDriver.3")); //$NON-NLS-1$
            KaveriErrorLog.logException("Error reading configuration", _ioe);
            throw new IOException("IO error, unable to parse configuration");
        }
        return _userConfiguration.toString();
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
    private void processUnit(final Chain unitchain, final List lst,
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
                _wasComplete = true;
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
     * Sets the slicer tool.
     * 
     * @param sliceTool
     *            The slicer tool instance.
     */
    public void setSlicer(SlicerTool sliceTool) {
        slicer = sliceTool;
        cfgProvider = slicer.getStmtGraphFactory();
    }

    /**
     * @return Returns the nameOfSliceTag.
     */
    public String getNameOfSliceTag() {
        return nameOfSliceTag;
    }

    /**
     * @param nameOfSliceTag
     *            The nameOfSliceTag to set.
     */
    public void setNameOfSliceTag(String nameOfSliceTag) {
        this.nameOfSliceTag = nameOfSliceTag;
    }

    /**
     * Adds the stack to the context.
     *  
     */
    public void addToContext(final Stack stkContext) {
        contextCollection.add(stkContext);
    }
}