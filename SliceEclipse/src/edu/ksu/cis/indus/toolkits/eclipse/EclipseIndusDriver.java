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
package edu.ksu.cis.indus.toolkits.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Path;

import soot.Body;
import soot.G;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;
import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.transformations.TagBasedDestructiveSliceResidualizer;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * Drives indus from eclipse.
 *
 * @author Ganeshan
 */
public class EclipseIndusDriver extends SootBasedDriver {
	/**
	 * <p> Logger to log the activities of the run</p>.
	 * 
	 */
	private static final Log LOGGER = LogFactory
			.getLog(EclipseIndusDriver.class);

	/**
	 * <p>The set of criteria</p>.
	 * 
	 */
	private Collection criteria = Collections.EMPTY_LIST;

	/**
	 * <p>The slicer tool</p>.
	 * 
	 */
	private SlicerTool slicer;

	/**
	 * The slicecriteria factory.
	 */
	private SliceCriteriaFactory factory;
	
	/**
	 * <p>Location of the default configuration, relative to plugin directory </p>.
	 * 
	 *
	 */
	private String defaultConfigFilename = Messages
			.getString("EclipseIndusDriver.0"); //$NON-NLS-1$

	/**
	 * <p>The tag applied to the slice</p>.
	 * 
	 * 
	 */
	private final String nameOfSliceTag = Messages
			.getString("EclipseIndusDriver.1"); //$NON-NLS-1$

	/**
	 * Creates a new EclipseIndusDriver object.
	 */
	public EclipseIndusDriver() {
		G.reset();
		slicer = new SlicerTool(TokenUtil.getTokenManager(),
				new ExceptionFlowSensitiveStmtGraphFactory());
		factory = SliceCriteriaFactory.getFactory();
	}

	/** 
	 * 
	 * <p>Sets the classes on which indus will operate</p>.
	 *
	 * @param classes The collection of class names
	 */
	public void setClasses(final Collection classes) {
		super.setClassNames(classes);
	}

	/**
	 * Resets the slicer.
	 *
	 */
	public void reset() {
		G.reset();
		slicer.reset();
	}

	/**
	 * <p>Sets the configuration to the given configuration </p>.
	 *
	 * @param configuration URL pointing to the configuration file
	 */
	public final void setConfiguration(final URL configuration) {
		String _defaultConfiguration;
		//if (slicer == null) {
		//	return;
		//}

		if (configuration == null) {
			_defaultConfiguration = getDefaultConfiguration();
			slicer.destringizeConfiguration(_defaultConfiguration);
		} else {
			slicer.destringizeConfiguration(parseConfiguration(configuration));
		}
	}



	/**
	 * <i> Have to use it with a java <-> jimple translator.</i>
	 * <p>Sets the criteria used for slicing</p>
	 *
	 * @param sootMethod The soot method inside which the criteria lies
	 * @param stmt The jimple statement criteria
	 * @param considerVal The value at that statement should be considered or not
	 */
	public void setCriteria(final SootMethod sootMethod, final Stmt stmt, final boolean considerVal) {		
		final Collection _coll = factory.getCriterion(sootMethod, stmt, true, considerVal);
		if (criteria != Collections.EMPTY_LIST) {
			_coll.addAll(criteria);
		}
		criteria = _coll;
	}

	/**
	 * <i> Have to use it with a java <-> jimple translator.</i>
	 * 
	 * <p>Sets the criteria used for slicing</p>
	 *
	 * @param sootMethod The soot method inside which the criteria lies
	 * @param stmt The jimple statement criteria
	 * @param box The Box criteria
	 */
	public void setCriteria(final SootMethod sootMethod, final Stmt stmt,
			final ValueBox box) {		
		final Collection _coll = factory.getCriterion(sootMethod, stmt, box, true);
		if (criteria != Collections.EMPTY_LIST) {
			_coll.addAll(criteria);
		}
		criteria = _coll;
	}

	/**
	 *
	 * <p>Adds the given path to the soot class path.</p>	 
	 *
	 * @param path The path to add to the soot class path
	 */
	public void addToPath(final String path) {
		addToSootClassPath(path);
	}

	/**
	 * <p>Executes the slicer.</p>
	 */
	public void execute() {

		slicer.setTagName(nameOfSliceTag);
		slicer.setSystem(scene);
		slicer.setRootMethods(rootMethods);
		slicer.setCriteria(criteria);
		slicer.run(Phase.STARTING_PHASE, true);
	}

	/**
	 * 
	 * <p>Initialize the soot driver.</p>
	 */
	public void initializeSlicer() {
		Options.v().set_keep_line_number(true);
		Options.v().set_src_prec(Options.src_prec_java);
		super.initialize();
	}

	/**
	 * Parses and returns the text corressponding to the default indus configuration.
	 *
	 * @return String The parsed contents of the default configuration
	 */
	private String getDefaultConfiguration() {
		final StringBuffer _defaultConfiguration = new StringBuffer();

		try {
			final InputStream _is = SliceEclipsePlugin.getDefault().openStream(
					new Path(defaultConfigFilename));
			final BufferedReader _configReader = new BufferedReader(
					new InputStreamReader(_is));

			while (_configReader.ready()) {
				_defaultConfiguration.append(_configReader.readLine());
			}
			_is.close();
		} catch (IOException _ioe) {
			LOGGER.fatal(Messages.getString("EclipseIndusDriver.2")); //$NON-NLS-1$
		}
		return _defaultConfiguration.toString();
	}

	/**
	 * Parses the configuration file and returns a string containing the parsed text.
	 *
	 * @param configuration The URL to the configuration file
	 *
	 * @return String The string of the contents of the configuration file
	 */
	private String parseConfiguration(final URL configuration) {
		final StringBuffer _userConfiguration = new StringBuffer();

		try {
			final BufferedReader _configReader = new BufferedReader(
					new InputStreamReader(configuration.openStream()));

			while (_configReader.ready()) {
				_userConfiguration.append(_configReader.readLine());
			}
		} catch (IOException _ioe) {
			LOGGER.fatal(Messages.getString("EclipseIndusDriver.3")); //$NON-NLS-1$
		}
		return _userConfiguration.toString();
	}

	/**
	 * Returns a vector of java line numbers associated with jimple slice tags.
	 * @return Vector Vector of line numbers
	 */
	public HashMap getAnnotationLineNumbers() {
		final HashMap _v = new HashMap();
		final Chain _classlist =  Scene.v().getApplicationClasses();
		final Iterator _iterator = _classlist.snapshotIterator();

		while (_iterator.hasNext()) {
			final SootClass _sootclass = (SootClass) _iterator.next();
			final List _list = _sootclass.getMethods();
			final List _lst = new LinkedList();
			for (int _nCtr = 0; _nCtr < _list.size(); _nCtr++) {
				final SootMethod _method = (SootMethod) _list.get(_nCtr);
				if (_method.isAbstract() || !_method.isConcrete()) {
					continue;
				}
				Body _body = null;
				if (_method.hasActiveBody()) {
					_body = _method.getActiveBody();
				} else {
					_body = _method.retrieveActiveBody();
				}

				final int _line = getLineNumberFromMethod(_method);
				if (_line > -1) {
					final Integer _itg = new Integer(_line);
					if (!_lst.contains(_itg)) {
						_lst.add(_itg);
					}
				}
				final Chain _unitchain = _body.getUnits();
				processUnit(_unitchain, _lst);
			}
			final String _className = _sootclass.getName();
			//final List _linelist = (List) _v.get(_className);
			if (_lst.size() > 0) {
				_v.put(_className, _lst);
			}

		}

		return _v;
	}

	/**
	 * Processes the unit to get the line number if the unit is tagged with the slice.
	 * @param unitchain The set of Jimple statements.
	 * @param lst The list to add the line numbers
	 */
	private void processUnit(final Chain unitchain, final List lst) {
		final Iterator _unititerator = unitchain.snapshotIterator();

		while (_unititerator.hasNext()) {
			final Stmt _stmt = (Stmt) _unititerator.next();
			final int _nLine = getLineNumberFromUnit(_stmt);
			if (_nLine > -1) {
				final Integer _intg = new Integer(_nLine);
				if (!lst.contains(_intg)) {
					lst.add(_intg);
				}
			}
		}
	}

	/**
	 * Gets the line number for a method if the method has the slice tag.
	 * @param method Tje soot method.
	 * @return int The line number coressponding to the method.
	 */
	private int getLineNumberFromMethod(final SootMethod method) {
		int _nLine = -1;
		final NamedTag _sTag = (NamedTag) method.getTag(nameOfSliceTag);
		if (_sTag != null) {
			final LineNumberTag _lntag = (LineNumberTag) method.getTag(Messages
					.getString("EclipseIndusDriver.4")); //$NON-NLS-1$
			final SourceLnPosTag _stag = (SourceLnPosTag) method
					.getTag(Messages.getString("EclipseIndusDriver.5")); //$NON-NLS-1$

			if (_stag != null) {
				_nLine = _stag.startLn();
			} else {
				if (_lntag != null) {
					//_nLine = Integer.parseInt(_lntag.toString()); // To be used with unpatched soot versions
					_nLine = _lntag.getLineNumber();
				}
			}

		}
		return _nLine;
	}

	/**
	 * Gets the line number for a Stmt if it is tagged with the slice.
	 * @param unit The Statement in which to find the tags
	 * @return int Line Number
	 */
	private int getLineNumberFromUnit(final Stmt unit) {
		int _nLine = -1;
		final NamedTag _sTag = (NamedTag) unit.getTag(nameOfSliceTag);
		if (_sTag != null) {
			final LineNumberTag _lntag = (LineNumberTag) unit.getTag(Messages
					.getString("EclipseIndusDriver.4")); //$NON-NLS-1$
			final SourceLnPosTag _stag = (SourceLnPosTag) unit.getTag(Messages
					.getString("EclipseIndusDriver.5")); //$NON-NLS-1$

			if (_stag != null) {
				_nLine = _stag.startLn();
			} else {
				if (_lntag != null) {
					//_nLine = Integer.parseInt(_lntag.toString()); // To be used with unpatched soot versions
					_nLine = _lntag.getLineNumber();
				}
			}

		}
		return _nLine;
	}

	/**
	 * Returns the default configuration.
	 * @return Returns the defaultConfigFilename.
	 */
	public String getDefaultConfigFilename() {
		return defaultConfigFilename;
	}

	/**
	 * Sets the default configuration.
	 * @param defaultConfig The defaultConfigFilename to set.
	 */
	public void setDefaultConfigFilename(final String defaultConfig) {
		this.defaultConfigFilename = defaultConfig;
	}

	/**
	 * Sets the configuration to config.
	 * @param config The configuration
	 */
	public void setConfiguration(final String config) {		
		slicer.destringizeConfiguration(config);

	}

	/**
	 * Returns the slicertool instance.
	 * @return Returns the slicer.
	 */
	public SlicerTool getSlicer() {
		return slicer;
	}

	/**
	 * Dumps the jimple.
	 * @author Ganeshan
	 * @param outputDirectory The output directory for the Jimple files.
	 */
	public void dumpJimple(final String outputDirectory) {

		final Printer _printer = Printer.v();

		for (final Iterator _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Dumping jimple for " + _sc);
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.isConcrete()) {
					_sm.retrieveActiveBody();
				}
			}

			PrintWriter _writer = null;

			try {
				final File _file = new File(outputDirectory + File.separator
						+ _sc.getName() + ".jimple");
				_writer = new PrintWriter(new FileWriter(_file));
				// write .jimple file
				_printer.printTo(_sc, _writer);				
			} catch (final IOException _e) {
				LOGGER.error("Error while writing " + _sc, _e);
			} finally {
				if (_writer != null) {
					_writer.flush();
					_writer.close();
				}
			}
		}
	}

	/**
	 * Update the jimple destructively.
	 */
	private void destructivelyUpdateJimple() {
		final TagBasedDestructiveSliceResidualizer _residualizer = new TagBasedDestructiveSliceResidualizer();
		_residualizer.setTagToResidualize(nameOfSliceTag);
		_residualizer.residualizeSystem(scene);
	}
}