
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

package edu.ksu.cis.indus.staticanalyses.flow;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
  extends TestCase {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FATest.class);

	/**
	 * The flow analysis framework instance to test.
	 */
	private FA fa;

	/**
	 * The name of that tag used to identify parts of the system that was touched by the analysis.
	 */
	private String faTagName;

	public void setFA(final FA theFA) {
		fa = theFA;
	}

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

/*
   ChangeLog:
   $Log$
   Revision 1.4  2004/02/08 04:53:10  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.

   Revision 1.3  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.

   Revision 1.2  2004/01/06 00:17:01  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.

   Revision 1.1  2004/01/03 19:52:54  venku
   - renamed CallGraphInfoTest to CallGraphTest
   - all tests of a kind have to be exposed via a suite like
     FATestSuite or OFAProcessorArgTestSuite.  This is to enable
     automated testing.
   - all properties should start with indus and not edu.ksu.cis.indus...

   Revision 1.11  2003/12/31 08:48:59  venku
   - Refactoring.
   - Setup classes setup each tests by data created by a common setup.
   - Tests and Setups are structured such that if test A requires
     data that can be tested by test B then testSetup B can
     be used to drive test A as well.
   Revision 1.10  2003/12/30 10:06:41  venku
    empty log message
   Revision 1.9  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.8  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.7  2003/12/08 13:31:49  venku
   - used JUnit defined assert functions.
   Revision 1.6  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.5  2003/12/07 14:04:43  venku
   - made FATest command-line compatible.
   - made use of AbstractDirectedGraphTest in
     CallGraphInfoTester to test the constructed call graphs.
   Revision 1.4  2003/12/07 08:39:23  venku
   - added more tests.
   Revision 1.3  2003/12/07 03:32:21  venku
   - added new tests.
   - formatting.
   Revision 1.2  2003/12/05 21:34:01  venku
   - formatting.
   - more tests.
   Revision 1.1  2003/12/05 15:28:12  venku
   - added test case for trivial tagging test in FA.
 */
