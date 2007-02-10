
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
	 *
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

// End of File
