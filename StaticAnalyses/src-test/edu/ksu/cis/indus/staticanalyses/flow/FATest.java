
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.IndusTestCase;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;


/**
 * This tests a flow analysis framework instance.  This test cannot be run by itself.  It needs to be run via a decorator.
 * This approach lets any future tests that use flow analysis framework can be extended easily to drive this test too. (More
 * test the  merrier.)   <code>FATestSetup</code> is the decorator provided for this purpose.  Refer to
 * <code>FATestSetup</code> for more details.  For running the tests, please use <code>FARegressionTestSuite</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FATest
  extends IndusTestCase
  implements IFATest {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FATest.class);

	/** 
	 * The flow analysis framework instance to test.
	 */
	private FA fa;

	/** 
	 * The name of that tag used to identify parts of the system that was touched by the analysis.
	 */
	private String faTagName;

	/**
	 * @see IFATest#setAnalyzer(edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer valueAnalyzer) {
	}

    /**
     * Sets the flow analyzer instance to be tested.
     *
     * @param theFA the flow analyzer instance.
     *
     * @pre theFA != null
     */
    public void setFA(final FA theFA) {
		fa = theFA;
	}

	/**
	 * @see IFATest#setFATagName(String)
	 */
	public void setFATagName(final String tagName) {
		faTagName = tagName;
	}

	/**
	 * Tests the tagging based on the containment of various entities in the system.
	 */
	public void testContainment() {
		checkContainmentOnTaggedEntity();
		checkContainmentOnUnTaggedEntity();
	}

	/**
	 * Tests the processing of classes by the analysis instance.
	 */
	public void testProcessing() {
		final Collection _processedClasses = fa.getClasses();

		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (_sc.hasTag(faTagName)) {
				assertTrue(_processedClasses.contains(_sc));
			} else {
				assertFalse(_processedClasses.contains(_sc));
			}
		}
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		if (fa == null || faTagName == null) {
			throw new IllegalStateException("Please call setFA() and setFATagName() before using this test.");
		}
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		fa = null;
		faTagName = null;
	}

	/**
	 * Check tagging based on containment for tagged entities.
	 */
	private void checkContainmentOnTaggedEntity() {
		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();
			boolean _flag = false;

			if (LOGGER.isDebugEnabled() && _sc.hasTag(faTagName)) {
				LOGGER.debug("TAGGED Class: " + _sc);
			}

			for (final Iterator _j = _sc.getFields().iterator(); _j.hasNext();) {
				final SootField _sf = (SootField) _j.next();

				if (_sf.hasTag(faTagName)) {
					_flag = true;

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("TAGGED Field: " + _sf);
					}
				}
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.hasTag(faTagName)) {
					assertNotNull(fa.queryMethodVariant(_sm));
					_flag = true;

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("TAGGED Method: " + _sm);
					}
				} else {
					assertNull(fa.queryMethodVariant(_sm));
				}
			}

			if (_flag) {
				assertTrue(_sc.hasTag(faTagName));
			}
		}
	}

	/**
	 * Check tagging based on containment but for untagged entities.
	 */
	private void checkContainmentOnUnTaggedEntity() {
		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (!_sc.hasTag(faTagName)) {
				for (final Iterator _j = _sc.getFields().iterator(); _j.hasNext();) {
					assertFalse(((SootField) _j.next()).hasTag(faTagName));
				}

				for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
					assertFalse(((SootMethod) _j.next()).hasTag(faTagName));
				}
			}
		}
	}
}

// End of File
