
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.AbstractXMLBasedTestSetup;
import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.SootValueTypeManager;

import edu.ksu.cis.indus.xmlizer.JimpleXMLizerCLI;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestSuite;

import soot.G;


/**
 * This is the setup in which various tests of flow analyses are run.  The classes to be processed during the test can be
 * configured via the command line or via specifying <code>FARegressionTestSuite.FA_TEST_PROPERTIES_FILE</code> system
 * property. The syntax for both these options is a space separated list of class names.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FATestSetup
  extends AbstractXMLBasedTestSetup {
	/** 
	 * The tag used by the flow analysis instance.
	 */
	public static final String TAG_NAME = "indus.staticanalyses.flow.FATestSetup:FA";

	/** 
	 * The value analyzer used during testing.
	 */
	protected IValueAnalyzer valueAnalyzer;

	/** 
	 * The names of the class to analyze.
	 */
	protected final String classNames;

	/** 
	 * The class path to use with soot in this setup.
	 */
	private final String sootClassPath;

	/**
	 * Creates a new FATestSetup object.
	 *
	 * @param test is the test to run in this setup.
	 * @param theNameOfClasses is the list of classes.
	 * @param classpath to be used to find the classes.
	 *
	 * @pre test != null and theNameOfClasses != null
	 */
	protected FATestSetup(final TestSuite test, final String theNameOfClasses, final String classpath) {
		super(test);
		valueAnalyzer = OFAnalyzer.getFSOIAnalyzer(FATestSetup.TAG_NAME, new BitSetTokenManager(new SootValueTypeManager()));
		sootClassPath = classpath;
		classNames = theNameOfClasses;
	}

	/**
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();

		final SootBasedDriver _driver = new SootBasedDriver();
		_driver.addToSootClassPath(sootClassPath);
		_driver.setClassNames(Arrays.asList(classNames.split(" ")));
		_driver.initialize();
		valueAnalyzer.analyze(new Environment(_driver.getScene()), _driver.getRootMethods());

		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IFATest.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final IFATest _test = (IFATest) _i.next();
			_test.setFA(((AbstractAnalyzer) valueAnalyzer).fa);
			_test.setAnalyzer(valueAnalyzer);
			_test.setFATagName(TAG_NAME);
		}

		if (dumpLocation != null) {
			JimpleXMLizerCLI.writeJimpleAsXML(_driver.getScene(), dumpLocation, null, idGenerator,
				new TagBasedProcessingFilter(TAG_NAME));
		}
	}

	/**
	 * @see junit.extensions.TestSetup#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		G.reset();
		valueAnalyzer.reset();
		valueAnalyzer = null;
		idGenerator.reset();
		super.tearDown();
	}
}

// End of File
