
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.IndusTestCase;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;


/**
 * This tests implementations of <code>IUnitGraphFactory</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class UnitGraphFactoryTest
  extends IndusTestCase {
	/**
	 * The scene.
	 */
	private Scene scene;

	/**
	 * Tests <code>getUnitGraph</code>.
	 */
	public final void testGetUnitGraph() {
		final SootClass _sc = scene.loadClassAndSupport("java.lang.Object");
		final SootMethod _notify = _sc.getMethodByName("notify");
		localTestGetUnitGraph(_notify);

		final SootMethod _wait = _sc.getMethodByName("equals");
		localTestGetUnitGraph(_wait);
	}

	/**
	 * Tests <code>reset</code>.
	 */
	public final void testReset() {
		final SootClass _sc = scene.loadClassAndSupport("java.lang.Object");
		final SootMethod _notify = _sc.getMethodByName("notify");

		final IUnitGraphFactory _ugf1 = new CompleteUnitGraphFactory();
		final UnitGraph _cmpltg1 = _ugf1.getUnitGraph(_notify);
		_ugf1.reset();

		final UnitGraph _cmpltg3 = _ugf1.getUnitGraph(_notify);
		assertNotSame(_cmpltg1, _cmpltg3);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		scene = Scene.v();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		scene = null;
	}

	/**
	 * Local helper method to test <code>getUnitGraph</code>.
	 *
	 * @param method to be tested with
	 *
	 * @pre method != null
	 */
	private void localTestGetUnitGraph(final SootMethod method) {
		final IUnitGraphFactory _ugf1 = new CompleteUnitGraphFactory();
		final UnitGraph _cmpltg1 = _ugf1.getUnitGraph(method);
		assertNotNull(_cmpltg1);
		assertTrue(_cmpltg1 instanceof CompleteUnitGraph);

		final IUnitGraphFactory _ugf2 = new TrapUnitGraphFactory();
		final UnitGraph _cmpltg2 = _ugf2.getUnitGraph(method);
		assertNotNull(_cmpltg2);
		assertTrue(_cmpltg2 instanceof TrapUnitGraph);

		final IUnitGraphFactory _ugf3 = new ExceptionFlowSensitiveUnitGraphFactory();
		final UnitGraph _cmpltg3 = _ugf3.getUnitGraph(method);
		assertNotNull(_cmpltg3);
		assertTrue(_cmpltg3 instanceof ExceptionFlowSensitiveUnitGraph);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/02/17 05:48:45  venku
   - added test for ExceptionFlowSensitiveUnitGraph.

   Revision 1.2  2004/02/09 00:28:33  venku
   - added a new class, IndusTestCase, that extends TestCase
     to differentiate between the test method name and the
     test instance name.
   - all test cases in indus extends IndusTestCase.
   - added a new method TestHelper to append container's name
     to the test cases.
   Revision 1.1  2004/01/28 22:45:07  venku
   - added new test cases for testing classes in soot package.
 */
