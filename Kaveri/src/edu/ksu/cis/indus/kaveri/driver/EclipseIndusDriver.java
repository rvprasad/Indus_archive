/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

/*
 * Created on Apr 1, 2004
 *
 * Indus Driver for eclipse
 */
package edu.ksu.cis.indus.kaveri.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.Path;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.options.Options;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.transformations.TagBasedDestructiveSliceResidualizer;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;
import edu.ksu.cis.indus.tools.slicer.contextualizers.StaticSliceCriteriaCallStackContextualizer;
import edu.ksu.cis.indus.tools.slicer.criteria.generators.StaticSliceCriteriaGenerator;

/**
 * The main interface between Kaveri and the Indus slicer. This drives the slicer on behalf of Kaveri.
 * 
 * @author Ganeshan
 */
public class EclipseIndusDriver
		extends SootBasedDriver {

	public static final Object SOOT_UPDATED = "Soot updated event";

	/**
	 * <p>
	 * Logger to log the activities of the run.
	 * </p>
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EclipseIndusDriver.class);

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

	private static class MyObservable
			extends Observable {

		public void setChanged() {
			super.setChanged();
		}
	};

	private MyObservable subject = new MyObservable();

	/**
	 * <p>
	 * Location of the default configuration, relative to the plugin directory.
	 * </p>
	 */
	private String defaultConfigFilename = Messages.getString("EclipseIndusDriver.0"); //$NON-NLS-1$

	/**
	 * <p>
	 * The slice tag name.
	 * </p>
	 */
	private String nameOfSliceTag = "";

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
	 * @param classes The set of class names to set to application classes
	 */
	public void setApplicationClasses(List classes) {
		for (int _i = 0; _i < classes.size(); _i++) {
			final String _classname = (String) classes.get(_i);
			try {
				final SootClass _sootclass = scene.loadClassAndSupport(_classname);
				if (_sootclass != null) {
					_sootclass.setApplicationClass();
				}
			} catch (RuntimeException _rme) {
				KaveriErrorLog.logException("Soot Class not present", _rme);
			}
		}
	}

	/**
	 * <p>
	 * Sets the classes on which indus will operate.
	 * </p>
	 * Precondition: classes != null and classes.oclIsKindOf(Collection(String))
	 * 
	 * @param classes The collection of class names
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 * @throws IllegalArgumentException Throws IllegalArgumentException if invalid parameters are passed.
	 */
	public void setClasses(final Collection classes) throws NullPointerException, IllegalArgumentException {
		if (SECommons.checkForNull(classes)) {
			throw new NullPointerException("setClasses got null for classname");
		}

		if (classes.size() == 0) {
			throw new IllegalArgumentException("setClasses got no classes");
		}

		if (!SECommons.checkForClassEquality(classes.iterator().next(), String.class)) {
			throw new IllegalArgumentException("setClasses expects Strings as classnames");
		}
		super.setClassNames(classes);
	}

	/**
	 * <p>
	 * Sets the current configuration to the configuration present in the given file.
	 * </p>
	 * Precondition: configuration != null
	 * 
	 * @param configuration URL pointing to the configuration file
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 * @throws IllegalArgumentException Throws IllegalArgumentException if invalid parameters are passed.
	 * @throws IOException Throws IOException if unable to parse the configuration
	 */
	public final void setConfiguration(final URL configuration) throws NullPointerException, IllegalArgumentException,
			IOException {
		String _defaultConfiguration;

		if (SECommons.checkForNull(configuration)) {
			throw new NullPointerException("URL set to null in setConfiguration");
		}

		boolean _isOk = false;

		if (configuration == null) {
			_defaultConfiguration = getDefaultConfiguration();
			_isOk = slicer.destringizeConfiguration(_defaultConfiguration);

			if (!_isOk) {
				throw new IllegalArgumentException("setConfiguration sent an invalid configuration");
			}
		} else {
			_isOk = slicer.destringizeConfiguration(parseConfiguration(configuration));

			if (!_isOk) {
				throw new IllegalArgumentException("setConfiguration sent an invalid configuration");
			}
		}
	}

	/**
	 * Sets the configuration to the configuration specified by config. <b>Precondition: </b> config != null
	 * 
	 * @param config The configuration
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 * @throws IllegalArgumentException Throws IllegalArgumentException if invalid parameters are passed.
	 */
	public void setConfiguration(final String config) throws NullPointerException, IllegalArgumentException {
		if (SECommons.checkForNull(config)) {
			throw new NullPointerException("setConfiguration expects non-null parameter");
		}

		final boolean _isOk = slicer.destringizeConfiguration(config);

		if (!_isOk) {
			throw new IllegalArgumentException("setConfiguration sent invalid configuration");
		}
	}

	/**
	 * <p>
	 * Sets the criteria used for slicing.
	 * </p>
	 * Precondition : sootMethod , stmt != null
	 * 
	 * @param sootMethod The soot method inside which the criteria lies
	 * @param stmt The jimple statement criteria
	 * @param considerVal The value at that statement should be considered or not
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 */
	public void setCriteria(final SootMethod sootMethod, final Stmt stmt, final boolean considerVal)
			throws NullPointerException {
		if (SECommons.checkForNull(sootMethod) || SECommons.checkForNull(stmt)) {
			throw new NullPointerException("setCriteria expects non-null parameters");
		}

		final Collection _coll = factory.getCriteria(sootMethod, stmt, true, considerVal);

		if (criteria != Collections.EMPTY_LIST) {
			_coll.addAll(criteria);
		}
		criteria = _coll;

	}

	/**
	 * Residualize the Scence.
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
	 * @param sootMethod The soot method inside which the criteria lies
	 * @param stmt The jimple statement criteria
	 * @param box The ValueBox criteria
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 */
	public void setCriteria(final SootMethod sootMethod, final Stmt stmt, final ValueBox box) throws NullPointerException {
		if (SECommons.checkForNull(sootMethod) || SECommons.checkForNull(stmt) || SECommons.checkForNull(box)) {
			throw new NullPointerException("setCriteria expects non-null parameters");
		}

		final Collection _coll = factory.getCriteria(sootMethod, stmt, box, true);

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
	 * @param path The path to add to the soot class path
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 */
	public void addToPath(final String path) throws NullPointerException {
		if (SECommons.checkForNull(path)) {
			throw new NullPointerException("addToPath expects non-null parameters");
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

		final String _scopeStr = KaveriPlugin.getDefault().getIndusConfiguration().getScopeSpecification();
		try {
			if (!(_scopeStr == null || _scopeStr.equals(""))) {
				slicer.setSliceScopeDefinition(SpecificationBasedScopeDefinition.deserialize(_scopeStr));
			}
		} catch (JiBXException _jbe) {
			SECommons.handleException(_jbe);
			KaveriErrorLog.logException("Error while deserializing scope specification", _jbe);
		}

		StaticSliceCriteriaGenerator _sscg = null;
		if (contextCollection.size() > 0) {
			_sscg = new StaticSliceCriteriaGenerator(criteria);
			_sscg.setCriteriaContextualizer(new StaticSliceCriteriaCallStackContextualizer(contextCollection));
			slicer.addCriteriaGenerator(_sscg);
		} else {
			slicer.clearCriteria();
			slicer.addCriteria(criteria);
		}
		slicer.run(Phase.STARTING_PHASE, Phase.FINISHED_PHASE, true);

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
		// Options.v().set_src_prec(Options.src_prec_java);
		super.initialize();
		subject.setChanged();
		subject.notifyObservers(SOOT_UPDATED);
	}

	/**
	 * Resets soot and the slicer.
	 */
	public void reset() {
		// G.reset();
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
	 * @throws IOException Throws IOException if an IOError occurs.
	 */
	private String getDefaultConfiguration() throws IOException {
		final StringBuffer _defaultConfiguration = new StringBuffer();

		try {
			final InputStream _is = KaveriPlugin.getDefault().openStream(new Path(defaultConfigFilename));
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(_is));

			while (_configReader.ready()) {
				_defaultConfiguration.append(_configReader.readLine());
			}
			_configReader.close();
		} catch (IOException _ioe) {
			KaveriErrorLog.logException("Error rading the default configuration", _ioe);
			throw new IOException("IO error, could not get defaultconfiguration");
		}
		return _defaultConfiguration.toString();
	}

	/**
	 * Parses the configuration file and returns a string containing the parsed text.
	 * 
	 * @param configuration The URL to the configuration file
	 * @return String The string of the contents of the configuration file
	 * @throws NullPointerException Throws KaveriException if null is passed as parameter.
	 * @throws IOException Throws IOException if an IOError occurs / configuration is null.
	 */
	private String parseConfiguration(final URL configuration) throws NullPointerException, IOException {
		if (SECommons.checkForNull(configuration)) {
			throw new NullPointerException("parseConfiguration expects a non-null configuration");
		}

		final StringBuffer _userConfiguration = new StringBuffer();

		try {
			final BufferedReader _configReader = new BufferedReader(new InputStreamReader(configuration.openStream()));

			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
			_configReader.close();
		} catch (IOException _ioe) {
			LOGGER.error(Messages.getString("EclipseIndusDriver.3")); //$NON-NLS-1$
			KaveriErrorLog.logException("Error reading configuration", _ioe);
			throw new IOException("IO error, unable to parse configuration");
		}
		return _userConfiguration.toString();
	}

	/**
	 * Sets the slicer tool.
	 * 
	 * @param sliceTool The slicer tool instance.
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
	 * @param name The nameOfSliceTag to set.
	 */
	public void setNameOfSliceTag(String name) {
		this.nameOfSliceTag = name;
	}

	/**
	 * Adds the stack to the context.
	 */
	public void addToContext(final Stack stkContext) {
		contextCollection.add(stkContext);
	}

	/**
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	public void addObserver(Observer o) {
		subject.addObserver(o);
	}

	/**
	 * @see java.util.Observable#deleteObserver(java.util.Observer)
	 */
	public void deleteObserver(Observer o) {
		subject.deleteObserver(o);
	}

}
