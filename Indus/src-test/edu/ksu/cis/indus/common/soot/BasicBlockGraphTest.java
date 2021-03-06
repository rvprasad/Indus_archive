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

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

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
	 * Retrieves the basic block graphs tests that test the basic block graph implementation against various method bodies.
	 * For test purposes, use this method and do not populate a test suite by using the main class.
	 * 
	 * @return the test that tests basic block graph implementation.
	 * @post result != null
	 */
	static Test getTests() {
		final TestSuite _result = new TestSuite();
		_result.setName("BasicBlockGraphTest");

		final IStmtGraphFactory[] _factories = new IStmtGraphFactory[]{new CompleteStmtGraphFactory(),
				new TrapStmtGraphFactory(), new ExceptionFlowSensitiveStmtGraphFactory(),};
		final String[] _methodNames = {"notify", "equals", "insertProviderAt"};
		final String[] _classNames = {"java.lang.Object", "java.lang.Object", "java.security.Security"};

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

				for (final Iterator _i = TestHelper.getTestCasesReachableFromSuite(_result, BasicBlockGraphTest.class)
						.iterator(); _i.hasNext();) {
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
				+ SetUtils.difference(_unitsInBB, _unitsInGraph).toString(), _unitsInGraph.containsAll(_unitsInBB));
		assertTrue(_unitGraph.getClass().getName() + ":" + _unitGraph.getBody().getMethod() + " - "
				+ SetUtils.difference(_unitsInGraph, _unitsInBB).toString(), _unitsInBB.containsAll(_unitsInGraph));
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
		_mgr.setStmtGraphFactory(factory);

		final SootClass _sc = scene.getSootClass(className);
		final SootMethod _sm = _sc.getMethodByName(methodName);
		bbGraph = _mgr.getBasicBlockGraph(_sm);
		dg = bbGraph;
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		factory.reset();
		methodName = null;
		className = null;
		factory = null;
		bbGraph = null;
		dg = null;
		scene = null;
	}
}

// End of File
