
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

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.support.FIFOWorkBag;
import edu.ksu.cis.indus.support.IWorkBag;

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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FATester
  extends TestCase {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String TAG_NAME = "FATester:TestTag";

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static String classes;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static FA fa;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(FATester.class);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
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
		 * @param test DOCUMENT ME!
		 */
		FATestSetup(final Test test) {
			super(test);
		}

		/*
		 * @see TestCase#setUp()
		 */
		protected void setUp()
		  throws Exception {
			AbstractAnalyzer ofa = OFAnalyzer.getFSOSAnalyzer(TAG_NAME);
			Scene scene = Scene.v();

			if (classes == null) {
				classes = System.getProperty("fatester.classes");
			}

			if (classes == null || classes.length() == 0) {
				throw new RuntimeException("fatester.classes property was empty.  Aborting.");
			}

			StringBuffer sb = new StringBuffer(classes);
			String[] j = sb.toString().split(" ");
			Collection rootMethods = new ArrayList();

			for (int i = j.length - 1; i >= 0; i--) {
				SootClass sc = scene.loadClassAndSupport(j[i]);

				if (sc.declaresMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
						  VoidType.v())) {
					SootMethod sm =
						sc.getMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
							VoidType.v());

					if (sm.isPublic() && sm.isConcrete()) {
						rootMethods.add(sm);
					}
				}
			}

			ofa.analyze(scene, rootMethods);
			fa = ofa.fa;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();

		for (int i = args.length - 1; i >= 0; i--) {
			sb.append(args[i] + " ");
		}

		classes = sb.toString();

		TestRunner runner = new TestRunner();
		runner.setLoading(false);
		runner.start(new String[0]);
		runner.startTest(suite());
		runner.runSuite();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.FA");

		//$JUnit-BEGIN$
		suite.addTestSuite(FATester.class);
		//$JUnit-END$
		return new FATestSetup(suite);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void testContainment() {
		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();
			boolean flag = false;

			if (LOGGER.isDebugEnabled() && _sc.hasTag(TAG_NAME)) {
				LOGGER.debug("TAGGED Class: " + _sc);
			}

			for (final Iterator _j = _sc.getFields().iterator(); _j.hasNext();) {
				final SootField _sf = (SootField) _j.next();

				if (_sf.hasTag(TAG_NAME)) {
					flag = true;

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("TAGGED Field: " + _sf);
					}
				}
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.hasTag(TAG_NAME)) {
					assertNotNull(fa.queryMethodVariant(_sm));
					flag = true;

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("TAGGED Method: " + _sm);
					}
				} else {
					assertNull(fa.queryMethodVariant(_sm));
				}
			}

			if (flag) {
				assertTrue(_sc.hasTag(TAG_NAME));
			}
		}

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

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void testInheritance() {
		IWorkBag wb = new FIFOWorkBag();

		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (_sc.hasTag(TAG_NAME)) {
				if (_sc.hasSuperclass()) {
					wb.addWorkNoDuplicates(_sc.getSuperclass());
				}
				wb.addAllWorkNoDuplicates(_sc.getInterfaces());
			}
		}

		Collection processedClasses = fa.getClasses();

		while (wb.hasWork()) {
			SootClass _sc = (SootClass) wb.getWork();
			assertTrue(_sc.hasTag(TAG_NAME));
			assertTrue(processedClasses.contains(_sc));

			if (_sc.hasTag(TAG_NAME)) {
				if (_sc.hasSuperclass()) {
					wb.addWorkNoDuplicates(_sc.getSuperclass());
				}
				wb.addAllWorkNoDuplicates(_sc.getInterfaces());
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void testProcessing() {
		Collection processedClasses = fa.getClasses();

		for (final Iterator _i = fa.getScene().getClasses().iterator(); _i.hasNext();) {
			SootClass _sc = (SootClass) _i.next();

			if (_sc.hasTag(TAG_NAME)) {
				assertTrue(processedClasses.contains(_sc));
			} else {
				assertFalse(processedClasses.contains(_sc));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.5  2003/12/07 14:04:43  venku
   - made FATester command-line compatible.
   - made use of DirectedAndSimpleNodeGraphTest in
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
