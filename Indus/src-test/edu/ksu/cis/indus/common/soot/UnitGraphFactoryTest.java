
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.G;
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
	 * A map from statement graph factory objects to the class of graphs returned from these factories.
	 * @invariant factories.oclIsKindOf(Map(IStmtGraphFactory, Class))
	 */
	private Map factories;

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
		final SootMethod _equals = _sc.getMethodByName("equals");

		for (final Iterator _i = factories.keySet().iterator(); _i.hasNext();) {
			final IStmtGraphFactory _factory = (IStmtGraphFactory) _i.next();
			final UnitGraph _graph1 = _factory.getStmtGraph(_notify);
			assertNotNull(_graph1);
			assertTrue(((Class) factories.get(_factory)).isInstance(_graph1));

			final UnitGraph _graph2 = _factory.getStmtGraph(_equals);
			assertNotNull(_graph2);
			assertTrue(((Class) factories.get(_factory)).isInstance(_graph2));
		}
	}

	/**
	 * Tests <code>reset</code>.
	 */
	public final void testReset() {
		final SootClass _sc = scene.loadClassAndSupport("java.lang.Object");
		final SootMethod _notify = _sc.getMethodByName("notify");

		for (final Iterator _i = factories.keySet().iterator(); _i.hasNext();) {
			final IStmtGraphFactory _factory = (IStmtGraphFactory) _i.next();
			final UnitGraph _graph1 = _factory.getStmtGraph(_notify);
			_factory.reset();

			final UnitGraph _graph2 = _factory.getStmtGraph(_notify);
			assertNotSame(_graph1, _graph2);
		}
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() {
		scene = Scene.v();
		factories = new HashMap();
		factories.put(new CompleteStmtGraphFactory(), CompleteUnitGraph.class);
		factories.put(new TrapStmtGraphFactory(), TrapUnitGraph.class);
		factories.put(new ExceptionFlowSensitiveStmtGraphFactory(
				ExceptionFlowSensitiveStmtGraphFactory.SYNC_RELATED_EXCEPTIONS,
				true), ExceptionFlowSensitiveStmtGraph.class);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() {
	    factories.clear();
		scene = null;
		G.reset();
		
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/04/21 02:24:04  venku
   - test clean up code was added.
   Revision 1.6  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.5  2004/03/26 00:24:07  venku
   - ripple effect of refactoring soot package in Indus.
   Revision 1.4  2004/02/17 05:59:15  venku
   - renamed ExceptionFlowSensitiveStmtGraphXXXX to
     ExceptionFlowSensitiveUnitGraph.
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
