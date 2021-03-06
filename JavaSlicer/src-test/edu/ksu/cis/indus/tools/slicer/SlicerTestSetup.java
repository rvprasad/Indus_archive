
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.AbstractXMLBasedTestSetup;
import edu.ksu.cis.indus.TestHelper;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.XMLBasedDependencyAnalysisTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.G;


/**
 * This is the setup in which various tests of the slicer are run.  The classes to be processed during the test can be
 * configured via the command line or via specifying <code>SlicerRegressionTestSuite.SLICER_TEST_PROPERTIES_FILE</code>
 * system property. The syntax for both these options is a space separated list of class names.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerTestSetup
  extends AbstractXMLBasedTestSetup {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SlicerTestSetup.class);

	/** 
	 * The slicer driver.
	 */
	SliceXMLizerCLI driver;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param suite is the slicer driver.
	 * @param classNames is the classes that provide the basis for the system to be sliced.
	 * @param xmlTestDirectory is the directory to store the test data.
	 * @param xmlControlDirectory is the directory containing the test control data.
	 * @param sootClasspath is the soot class path used during slicing.
	 *
	 * @pre suite != null and classNames != null and xmlTestDirectory != null and xmlControlDirectory != null and
	 * 		sootClasspath != null
	 */
	public SlicerTestSetup(final TestSuite suite, final String[] classNames, final String xmlTestDirectory,
		final String xmlControlDirectory, final String sootClasspath) {
		super(suite);

		driver = new SliceXMLizerCLI();
		driver.setOutputDirectory(xmlTestDirectory);

		if (sootClasspath != null) {
			driver.addToSootClassPath(sootClasspath);
		}
		driver.setClassNames(Arrays.asList(classNames));
		setXMLControlDir(xmlControlDirectory);
		setXMLTestDir(xmlTestDirectory);
	}

	/**
	 * Initializes and drives the fixture.
	 *
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp()
	  throws Exception {
		// drive the slicer
		driver.setIDGenerator(idGenerator);
		driver.initialize();
		driver.slicer.destringizeConfiguration(stringizeConfig());
		driver.executeForSingleSlice();

		// setup testcases for dependency analysis
		final TestSuite _suite = (TestSuite) getTest();
		final DependencyXMLizer _xmlizer = new DependencyXMLizer();
		final ICallGraphInfo _cgiImpl = driver.slicer.getCallGraph();
		final IEnvironment _environment = driver.slicer.getSystem();

		for (final Iterator _i = driver.slicer.getDAs().iterator(); _i.hasNext();) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
			final XMLBasedDependencyAnalysisTest _test = new XMLBasedDependencyAnalysisTest(_da, _xmlizer);
			_test.setCallGraph(_cgiImpl);
			_test.setEnvironment(_environment);
			_suite.addTest(_test);
		}
		setStmtGraphFactory(driver.slicer.getStmtGraphFactory());
		super.setUp();

		TestHelper.appendSuiteNameToTestsIn(_suite, true);

		// We do not generate xmlized slice as it is responsibility of the XMLBasedTest to 
		// generate the test data before testing.
	}

	/**
	 * Resets the underlying Soot framework and associated fixture.
	 *
	 * @see junit.extensions.TestSetup#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		// residualize the system
		driver.destructivelyUpdateJimple();

		// write XMLized Jimple data
		if (dumpLocation != null) {
			driver.jimpleXMLDumpDir = dumpLocation;
			driver.dumpJimpleAsXML("");
		}

		G.reset();
		driver.slicer.reset();

		for (final Iterator _iter = driver.slicer.getDAs().iterator(); _iter.hasNext();) {
			((IDependencyAnalysis) _iter.next()).reset();
		}
		driver = null;
		super.tearDown();
	}

	/**
	 * Retrieves the default slicer configuration.
	 *
	 * @return the configuration as a string.
	 *
	 * @throws RuntimeException when an error occurs when operating on the default configuration file.
	 */
	private String stringizeConfig() {
		Reader _reader = null;
		final URL _defaultConfigFileName =
			ClassLoader.getSystemResource("edu/ksu/cis/indus/tools/slicer/default_slicer_configuration.xml");

		try {
			_reader = new InputStreamReader(_defaultConfigFileName.openStream());
		} catch (FileNotFoundException _e1) {
			LOGGER.error("Even default configuration file could not be found.  Aborting", _e1);
			throw new RuntimeException(_e1);
		} catch (IOException _e2) {
			LOGGER.error("Could not retrieve a handle to default configuration file.  Aborting.", _e2);
			throw new RuntimeException(_e2);
		}

		String _result = null;

		try {
			final BufferedReader _br = new BufferedReader(_reader);
			final StringBuffer _buffer = new StringBuffer();

			while (_br.ready()) {
				_buffer.append(_br.readLine());
			}
			_result = _buffer.toString();
		} catch (IOException _e) {
			LOGGER.error("IO error while reading configuration file.  Aborting", _e);
			throw new RuntimeException(_e);
		}
		return _result;
	}
}

// End of File
