
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.AbstractXMLBasedTestSetup;
import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer;
import edu.ksu.cis.indus.staticanalyses.dependency.XMLBasedDependencyAnalysisTest;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;

import java.util.Iterator;

import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static final Log LOGGER = LogFactory.getLog(SlicerTestSetup.class);

	/**
	 * The slicer driver.
	 */
	SliceXMLizerCLI driver;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private String jimpleDumpDir;

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

		final IJimpleIDGenerator _idGenerator = new UniqueJimpleIDGenerator();
		driver = new SliceXMLizerCLI(_idGenerator);
		driver.setOutputDirectory(xmlTestDirectory);

		if (sootClasspath != null) {
			driver.addToSootClassPath(sootClasspath);
		}
		driver.setClassNames(classNames);
		setXMLControlDir(xmlControlDirectory);
		setXMLTestDir(xmlTestDirectory);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param dumpDir
	 */
	public void setJimpleXMLDumpLocation(final String dumpDir) {
		jimpleDumpDir = dumpDir;
	}

	/**
	 * Initializes and drives the fixture.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		// drive the slicer
		driver.initialize();
		driver.slicer.destringizeConfiguration(stringizeConfig());
		driver.execute();

		// setup testcases for dependency analysis
		final TestSuite _suite = (TestSuite) getTest();
		final DependencyXMLizer _xmlizer = new DependencyXMLizer();
		final ICallGraphInfo _cgiImpl = driver.slicer.getCallGraph();
		final IEnvironment _environment = driver.slicer.getEnvironment();

		for (final Iterator _i = driver.slicer.getDAs().iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
			final XMLBasedDependencyAnalysisTest _test = new XMLBasedDependencyAnalysisTest(_da, _xmlizer);
			_test.setCallGraph(_cgiImpl);
			_test.setEnvironment(_environment);
			_suite.addTest(_test);
		}
		setStmtGraphFactory(driver.slicer.getStmtGraphFactory());
		super.setUp();

		TestHelper.appendSuiteNameToTestsIn(_suite, true);

		// write Jimple XML
		try {
			if (jimpleDumpDir != null) {
				driver.xmlizedJimpleWriter = new FileWriter(new File(jimpleDumpDir + File.separator + "jimple.xml"));
			}
		} catch (IOException _e) {
			LOGGER.fatal("IO error while creating file to dump jimple.", _e);
		}
		driver.writeXML();
	}

	/**
	 * Resets the underlying Soot framework and associated fixture.
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		G.reset();
		driver.slicer.reset();

		for (final Iterator _iter = driver.slicer.getDAs().iterator(); _iter.hasNext();) {
			((DependencyAnalysis) _iter.next()).reset();
		}
		driver = null;
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
			LOGGER.fatal("Even default configuration file could not be found.  Aborting", _e1);
			throw new RuntimeException(_e1);
		} catch (IOException _e2) {
			LOGGER.fatal("Could not retrieve a handle to default configuration file.  Aborting.", _e2);
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
			LOGGER.fatal("IO error while reading configuration file.  Aborting", _e);
			throw new RuntimeException(_e);
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/04/21 02:24:02  venku
   - test clean up code was added.
   Revision 1.2  2004/04/20 06:53:15  venku
   - documentation.
   Revision 1.1  2004/04/18 08:59:00  venku
   - enabled test support for slicer.
 */
