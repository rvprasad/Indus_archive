
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

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.CollectionUtils;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This class tests the basic block graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class BasicBlockGraphTest
  extends AbstractDirectedGraphTest {
	/**
	 * The factory used to extract statement graphs.
	 */
	IStmtGraphFactory factory;

	/**
	 * The scene to extract the method bodies from.
	 */
	Scene scene;

	/**
	 * The basic block graph to test with.
	 */
	private BasicBlockGraph bbGraph;

	/**
	 * The name of the class containing the method to be used for testing.
	 */
	private String className;

	/**
	 * The name of the method used for testing.
	 */
	private String methodName;

	/**
	 * Tests if the basic block graph is minimal.
	 */
	public final void testBasicBlockGraphAgainstStmtGraph() {
		final UnitGraph _unitGraph = bbGraph.getStmtGraph();
		final Collection _unitsInGraph = new ArrayList();
		final Collection _unitsInBB = new ArrayList();

		CollectionUtils.addAll(_unitsInGraph, _unitGraph.iterator());

		for (final Iterator _i = bbGraph.getNodes().iterator(); _i.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			_unitsInBB.addAll(_bb.getStmtsOf());
		}

		assertTrue(_unitGraph.getClass().getName() + ":" + _unitGraph.getBody().getMethod() + " - "
			+ CollectionUtils.subtract(_unitsInBB, _unitsInGraph).toString(), _unitsInGraph.containsAll(_unitsInBB));
		assertTrue(_unitGraph.getClass().getName() + ":" + _unitGraph.getBody().getMethod() + " - "
			+ CollectionUtils.subtract(_unitsInGraph, _unitsInBB).toString(), _unitsInBB.containsAll(_unitsInGraph));
	}

	/**
	 * Tests the structure of basic block graph.
	 */
	public final void testBasicBlockGraphStructure() {
		final UnitGraph _unitGraph = bbGraph.getStmtGraph();
		final Collection _units = new ArrayList();

		for (final Iterator _i = bbGraph.getNodes().iterator(); _i.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			final Stmt _leader = _bb.getLeaderStmt();
			final Collection _preds = _unitGraph.getPredsOf(_leader);

			if (!_preds.isEmpty()) {
				boolean _flag = _preds.size() > 1;

				if (!_flag) {
					for (final Iterator _j = _preds.iterator(); _j.hasNext();) {
						final Stmt _pred = (Stmt) _j.next();
						_flag |= _unitGraph.getSuccsOf(_pred).size() > 1;
					}
				}
				assertTrue(_leader.toString() + _unitGraph.getBody().getMethod(), _flag);
			}

			final Stmt _trailer = _bb.getTrailerStmt();
			final Collection _succs = _unitGraph.getSuccsOf(_trailer);

			if (!_succs.isEmpty()) {
				boolean _flag = _succs.size() > 1;

				if (!_flag) {
					for (final Iterator _j = _succs.iterator(); _j.hasNext();) {
						final Stmt _succ = (Stmt) _j.next();
						_flag |= _unitGraph.getPredsOf(_succ).size() > 1;
					}
				}
				assertTrue(_trailer.toString() + _unitGraph.getBody().getMethod(), _flag);
			}

			_units.clear();
			_units.addAll(_bb.getStmtsOf());
			_units.remove(_leader);
			_units.remove(_trailer);

			for (final Iterator _iter = _units.iterator(); _iter.hasNext();) {
				final Stmt _unit = (Stmt) _iter.next();
				assertTrue(_unitGraph.getPredsOf(_unit).size() == 1);
				assertTrue(_unitGraph.getSuccsOf(_unit).size() == 1);
			}
		}
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() {
		final BasicBlockGraphMgr _mgr = new BasicBlockGraphMgr();
		_mgr.setUnitGraphFactory(factory);

		final SootClass _sc = scene.getSootClass(className);
		final SootMethod _sm = _sc.getMethodByName(methodName);
		bbGraph = _mgr.getBasicBlockGraph(_sm);
		dg = bbGraph;
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		factory.reset();
		methodName = null;
		className = null;
		factory = null;
		bbGraph = null;
		dg = null;
		scene = null;
	}

	/**
	 * Retrieves the basic block graphs tests that test the basic block graph implementation against various method bodies.
	 * For test purposes, use this method and do not populate a test suite by using the main class.
	 *
	 * @return the test that tests basic block graph implementation.
	 *
	 * @post result != null
	 */
	static Test getTests() {
		final TestSuite _result = new TestSuite();
		_result.setName("BasicBlockGraphTest");

		final IStmtGraphFactory[] _factories =
			new IStmtGraphFactory[] {
				new CompleteStmtGraphFactory(), new TrapStmtGraphFactory(), new ExceptionFlowSensitiveStmtGraphFactory(),
			};
		final String[] _methodNames = { "notify", "equals", "loadOneMoreProvider" };
		final String[] _classNames = { "java.lang.Object", "java.lang.Object", "java.security.Security" };

		for (int _j = 0; _j < _methodNames.length; _j++) {
			for (int _i = 0; _i < _factories.length; _i++) {
				final IStmtGraphFactory _factory = _factories[_i];
				final TestSuite _temp = new TestSuite(BasicBlockGraphTest.class);
				final String _prefix = _factory.getClass().getName() + ":BasicBlockGraphTest";

				for (final Enumeration _enum = _temp.tests(); _enum.hasMoreElements();) {
					final BasicBlockGraphTest _test = (BasicBlockGraphTest) _enum.nextElement();
					_test.setTestName(_prefix + ":" + _methodNames[_j] + ":" + _classNames[_j] + ":"
						+ _test.getTestMethodName());
					_test.factory = _factory;
					_test.methodName = _methodNames[_j];
					_test.className = _classNames[_j];
				}
				_result.addTest(_temp);
			}
		}

		return new TestSetup(_result) {
				private Scene scene;

				public void setUp() {
					scene = Scene.v();

					for (int _i = 0; _i < _classNames.length; _i++) {
						scene.loadClassAndSupport(_classNames[_i]).getMethodByName(_methodNames[_i]);
					}

					for (final Iterator _i =
							TestHelper.getTestCasesReachableFromSuite(_result, BasicBlockGraphTest.class).iterator();
						  _i.hasNext();) {
						final BasicBlockGraphTest _element = (BasicBlockGraphTest) _i.next();
						_element.scene = scene;
					}
				}

				public void tearDown() {
					scene = null;
					G.reset();
				}
			};
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/06/14 08:39:29  venku
   - added a property to SootBasedDriver to control the type of statement graph
     factory to be used.
   - removed getDefaultFactory() from ExceptionFlowSensitiveStmtGraphFactory.
   - ripple effect.

   Revision 1.4  2004/06/14 04:55:04  venku
   - documentation.
   - coding conventions.
   Revision 1.3  2004/06/01 08:13:02  venku
   - changed the name of the tests.
   Revision 1.2  2004/06/01 08:05:12  venku
   - changed the name of the tests.
   Revision 1.1  2004/06/01 01:12:16  venku
   - added a new testcase to test BasicBlockGraph.
   - documentation.
   - added iterator() method to ExceptionFlowSensitiveStmtGraph to
     return only statement captured in the graph.
 */
