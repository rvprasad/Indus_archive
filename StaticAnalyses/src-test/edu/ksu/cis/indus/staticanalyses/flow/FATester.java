
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

import edu.ksu.cis.indus.common.graph.FIFOWorkBag;
import edu.ksu.cis.indus.common.graph.IWorkBag;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.VoidType;


/**
 * This test flow analysis framework instance.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FATester
  extends TestCase {
	/**
	 * This is a white space separated list of class names that are the application classes in the system.
	 */
	static String classes;

	/**
	 * The flow analysis framework instance to test.
	 */
	static FA fa;

	/**
	 * The tag used by the flow analysis instance.
	 */
	private static final String TAG_NAME = "FATester:TestTag";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FATester.class);

	/**
	 * This is the setup in which various tests are run.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class FATestSetup
	  extends TestSetup {
		/**
		 * Creates a new FATestSetup object.
		 *
		 * @param test is the test to run in this setup.
		 *
		 * @pre test != null
		 */
		FATestSetup(final Test test) {
			super(test);
		}

		/**
		 * @see TestCase#setUp()
		 */
		protected void setUp() {
			final AbstractAnalyzer _ofa = OFAnalyzer.getFSOSAnalyzer(TAG_NAME);
			final Scene _scene = Scene.v();

			if (classes == null) {
				classes = System.getProperty("fatester.classes");
			}

			if (classes == null || classes.length() == 0) {
				throw new RuntimeException("fatester.classes property was empty.  Aborting.");
			}

			final StringBuffer _sb = new StringBuffer(classes);
			final String[] _j = _sb.toString().split(" ");
			final Collection _rootMethods = new ArrayList();

			for (int _i = _j.length - 1; _i >= 0; _i--) {
				final SootClass _sc = _scene.loadClassAndSupport(_j[_i]);

				if (_sc.declaresMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
						  VoidType.v())) {
					final SootMethod _sm =
						_sc.getMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
							VoidType.v());

					if (_sm.isPublic() && _sm.isConcrete()) {
						_rootMethods.add(_sm);
					}
				}
			}

			_ofa.analyze(_scene, _rootMethods);
			fa = _ofa.fa;
		}
	}

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are the command line arguments
	 *
	 * @pre args != null
	 */
	public static void main(final String[] args) {
		final StringBuffer _sb = new StringBuffer();

		for (int _i = args.length - 1; _i >= 0; _i--) {
			_sb.append(args[_i] + " ");
		}

		classes = _sb.toString();

		final TestRunner _runner = new TestRunner();
		_runner.setLoading(false);
		_runner.start(new String[0]);
		_runner.startTest(suite());
		_runner.runSuite();
	}

	/**
	 * Retrieves the test suite of the tests in this class.
	 *
	 * @return a test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.FA");

		//$JUnit-BEGIN$
		_suite.addTestSuite(FATester.class);
		//$JUnit-END$
		return new FATestSetup(_suite);
	}

	/**
	 * Tests the tagging based on the containment of various entities in the system.
	 */
	public void testContainment() {
		checkContainmentOnTaggedEntity();

		checkContainmentOnUnTaggedEntity();
	}

	/**
	 * Tests the tagging based on inheritance.
	 */
	public void testInheritance() {
		final IWorkBag _wb = new FIFOWorkBag();

		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (_sc.hasTag(TAG_NAME)) {
				if (_sc.hasSuperclass()) {
					_wb.addWorkNoDuplicates(_sc.getSuperclass());
				}
				_wb.addAllWorkNoDuplicates(_sc.getInterfaces());
			}
		}

		final Collection _processedClasses = fa.getClasses();

		while (_wb.hasWork()) {
			final SootClass _sc = (SootClass) _wb.getWork();
			assertTrue(_sc.hasTag(TAG_NAME));
			assertTrue(_processedClasses.contains(_sc));

			if (_sc.hasTag(TAG_NAME)) {
				if (_sc.hasSuperclass()) {
					_wb.addWorkNoDuplicates(_sc.getSuperclass());
				}
				_wb.addAllWorkNoDuplicates(_sc.getInterfaces());
			}
		}
	}

	/**
	 * Tests the processing of classes by the analysis instance.
	 */
	public void testProcessing() {
		final Collection _processedClasses = fa.getClasses();

		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (_sc.hasTag(TAG_NAME)) {
				assertTrue(_processedClasses.contains(_sc));
			} else {
				assertFalse(_processedClasses.contains(_sc));
			}
		}
	}

	/**
	 * Check tagging based on containment for tagged entities.
	 */
	private void checkContainmentOnTaggedEntity() {
		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();
			boolean _flag = false;

			if (LOGGER.isDebugEnabled() && _sc.hasTag(TAG_NAME)) {
				LOGGER.debug("TAGGED Class: " + _sc);
			}

			for (final Iterator _j = _sc.getFields().iterator(); _j.hasNext();) {
				final SootField _sf = (SootField) _j.next();

				if (_sf.hasTag(TAG_NAME)) {
					_flag = true;

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("TAGGED Field: " + _sf);
					}
				}
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.hasTag(TAG_NAME)) {
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
				assertTrue(_sc.hasTag(TAG_NAME));
			}
		}
	}

	/**
	 * Check tagging based on containment but for untagged entities.
	 */
	private void checkContainmentOnUnTaggedEntity() {
		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (!_sc.hasTag(TAG_NAME)) {
				for (final Iterator _j = _sc.getFields().iterator(); _j.hasNext();) {
					assertFalse(((SootField) _j.next()).hasTag(TAG_NAME));
				}

				for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
					assertFalse(((SootMethod) _j.next()).hasTag(TAG_NAME));
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
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
   - made FATester command-line compatible.
   - made use of AbstractDirectedGraphTest in
     CallGraphTester to test the constructed call graphs.
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
