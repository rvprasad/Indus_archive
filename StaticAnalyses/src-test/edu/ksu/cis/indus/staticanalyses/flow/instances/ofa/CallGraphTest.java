
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNode;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.IProcessor;

import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;


/**
 * This class tests information calculated by
 * <code>edu.ksu.cis.indus.staticanalyses.flow.instances.valueAnalyzer.processors.CallGraph</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CallGraphTest
  extends AbstractDirectedGraphTest
  implements IFAProcessorTest {
	/**
	 * The call graph to be tested.
	 */
	private ICallGraphInfo cgi;

	/**
	 * The object flow analysis used to construct the call graph.
	 */
	private OFAnalyzer ofa;

	/**
	 * The system that provides the call graph.
	 */
	private Scene scene;

	/**
	 * The call graph.
	 */
	private SimpleNodeGraph cg;

	/**
	 * Sets the instance of OFAnalyzer to be used during testing.
	 *
	 * @param valueAnalyzer to be used by the test.
	 *
	 * @pre valueAnalyzer != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setFA(IValueAnalyzer)
	 */
	public void setFA(final IValueAnalyzer valueAnalyzer) {
		ofa = (OFAnalyzer) valueAnalyzer;
	}

	/**
	 * Sets the call graph information instance to be used during test.
	 *
	 * @param processor provides call graph information.
	 *
	 * @pre processor != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setProcessor(IProcessor)
	 */
	public void setProcessor(final IProcessor processor) {
		final CallGraph _cg = (CallGraph) processor;
		cgi = (ICallGraphInfo) _cg;
		cg = (SimpleNodeGraph) _cg.getCallGraph();
		dg = cg;
	}

	/**
	 * Set the scene used during test.
	 *
	 * @param theScene used during test.
	 *
	 * @pre theScene != null
	 */
	public void setScene(final Scene theScene) {
		scene = theScene;
	}

	/**
	 * Tests <code>isReachable</code>.
	 */
	public void localtestIsReachable() {
		final Collection _reachables = cgi.getReachableMethods();
		final Collection _heads = cgi.getHeads();

		for (final Iterator _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				assertEquals(cgi.isReachable(_sm), _reachables.contains(_sm));

				if (cgi.isReachable(_sm)) {
					boolean _t = false;

					for (final Iterator _k = _heads.iterator(); _k.hasNext();) {
						_t |= cg.isReachable(cg.getNode(_k.next()), cg.getNode(_sm), true);
					}
					assertTrue(_t || _heads.contains(_sm));
				}
			}
		}
	}

	/**
	 * Tests <code>getCallGraph()</code>.
	 */
	public void testGetCallGraph() {
		if (cgi instanceof CallGraph) {
			assertNotNull(((CallGraph) cgi).getCallGraph());
		}
	}

	/**
	 * Tests <code>getCalles(soot.jimple.InvokeExpr, edu.ksu.cis.indus.processing.Context)</code>.
	 */
	public void testGetCalleesInvokeExprContext() {
		final Context _context = new Context();
		final Collection _calleeMethods = new ArrayList();

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _caller = (SootMethod) _i.next();
			final Collection _callees = cgi.getCallees(_caller);
			_calleeMethods.clear();

			for (final Iterator _j = _callees.iterator(); _j.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _j.next();
				_calleeMethods.add(_ctrp.getMethod());
			}

			for (final Iterator _j = _callees.iterator(); _j.hasNext();) {
				final CallTriple _ctrp1 = (CallTriple) _j.next();
				_context.setStmt(_ctrp1.getStmt());
				_context.setRootMethod(_caller);

				final Collection _newCallees = cgi.getCallees(_ctrp1.getExpr(), _context);

				for (final Iterator _k = _callees.iterator(); _k.hasNext();) {
					final CallTriple _ctrp2 = (CallTriple) _k.next();

					if (_ctrp2.getExpr().equals(_ctrp1.getExpr())) {
						assertTrue(_newCallees.contains(_ctrp2.getMethod()));
					}
				}
				_calleeMethods.removeAll(_newCallees);
			}
			assertTrue(_calleeMethods.isEmpty());
		}
	}

	/**
	 * Tests <code>getCallers(soot.Method)</code> and <code>getCallees(soot.Method)</code>.
	 */
	public void testGetCallersAndGetCallees() {
		final Collection _heads = cgi.getHeads();
		final Collection _tails = cg.getTails();

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			final Collection _callers = cgi.getCallers(_callee);
			assertNotNull(_callers);

			if (_callers.isEmpty()) {
				assertTrue(_heads.contains(_callee));
			} else {
				for (final Iterator _j = _callers.iterator(); _j.hasNext();) {
					final CallTriple _ctrp1 = (CallTriple) _j.next();
					final SootMethod _caller = _ctrp1.getMethod();
					assertTrue(cgi.isReachable(_caller));

					final Collection _callees = cgi.getCallees(_caller);

					if (_callees.isEmpty()) {
						assertTrue(_tails.contains(_caller));
					} else {
						boolean _t = false;

						for (final Iterator _k = _callees.iterator(); _k.hasNext();) {
							final CallTriple _ctrp2 = (CallTriple) _k.next();

							if (_ctrp2.getMethod().equals(_callee)) {
								_t = true;
								break;
							}
						}
						assertTrue(_t);
					}
				}
			}
		}
	}

	/**
	 * Tests getNodes() of the graph associated with the call graph.
	 */
	public void testGetNodes() {
		final Collection _reachables = cgi.getReachableMethods();
		final Collection _d = new HashSet();

		for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
			final SimpleNode _node = (SimpleNode) _i.next();
			_d.add(_node.getObject());
		}
		assertTrue(_d.containsAll(_reachables));
	}

	/**
	 * Tests <code>getReachableMethods()</code>.
	 */
	public void testGetReachableMethods() {
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			assertTrue(_i.next() instanceof SootMethod);
		}
	}

	/**
	 * Tests <code>getSCCs()</code>.
	 */
	public void testGetSCCs() {
		super.testGetSCCs();

		final Collection _sccs = cgi.getSCCs(true);
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_sccs);

		for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
			final Collection _scc1 = (Collection) _i.next();
			assertNotNull(_scc1);
			assertTrue(_reachables.containsAll(_scc1));

			for (final Iterator _j = _sccs.iterator(); _j.hasNext();) {
				final Collection _scc2 = (Collection) _j.next();

				if (_scc1 != _scc2) {
					assertTrue(CollectionUtils.intersection(_scc1, _scc2).isEmpty());
				}
			}
		}
	}

	/**
	 * Tests the size() method of the graph associated with the call graph.
	 */
	public void testSize() {
		assertTrue(cgi.getReachableMethods().size() == dg.getNodes().size());
	}

	/**
	 * Tests the tags on the reachable methods based on tags used during object flow analysis.
	 */
	public void testTagsOnReachableMethods() {
		final Context _ctxt = new Context();
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			final SootMethod _o = (SootMethod) _i.next();
			assertTrue(_o.hasTag(FATestSetup.TAG_NAME));
			assertTrue(_o.getDeclaringClass().hasTag(FATestSetup.TAG_NAME));

			if (!_o.isStatic()) {
				_ctxt.setRootMethod(_o);
				assertNotNull(ofa.getValuesForThis(_ctxt));
			}
		}

		Collection _methods = new HashSet();

		for (final Iterator _i = ofa.getEnvironment().getClasses().iterator(); _i.hasNext();) {
			_methods.addAll(((SootClass) _i.next()).getMethods());
		}
		_methods = CollectionUtils.subtract(_methods, _reachables);

		for (final Iterator _i = _methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (!_sm.isAbstract()) {
				assertFalse(_sm.hasTag(FATestSetup.TAG_NAME));
			}
		}
	}

	/**
	 * Test <code>getHeads()</code> method of the graph associated with the call graph.
	 */
	protected void localtestGetHeads() {
		Collection _heads = new HashSet();

		for (final Iterator _i = dg.getHeads().iterator(); _i.hasNext();) {
			final SimpleNode _sn = (SimpleNode) _i.next();
			_heads.add(_sn.getObject());
		}

		assertTrue(_heads.containsAll(cgi.getHeads()));
		assertTrue(cgi.getHeads().containsAll(_heads));

		_heads = cgi.getHeads();
		assertNotNull(_heads);

		for (final Iterator _i = _heads.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			assertTrue(cgi.getCallers(_sm).isEmpty());
		}
		assertTrue(cgi.getReachableMethods().containsAll(_heads));
	}

	/**
	 * Test  <code>getTails()</code> method of the graph associated with the call graph.
	 */
	protected void localtestGraphGetTails() {
		for (final Iterator _i = dg.getTails().iterator(); _i.hasNext();) {
			final SimpleNode _node = (SimpleNode) _i.next();
			assertTrue(cgi.getCallees((SootMethod) _node.getObject()).isEmpty());
		}

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (cgi.getCallees(_sm).isEmpty()) {
				assertTrue(cg.getNode(_sm).getSuccsOf().isEmpty());
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.

   Revision 1.2  2004/01/06 01:51:55  venku
   - renamed DirectedGraphTestSuite to GraphNoArgTestSuite.
   Revision 1.1  2004/01/03 19:52:54  venku
   - renamed CallGraphInfoTest to CallGraphTest
   - all tests of a kind have to be exposed via a suite like
     FATestSuite or OFAProcessorArgTestSuite.  This is to enable
     automated testing.
   - all properties should start with indus and not edu.ksu.cis.indus...
   Revision 1.1  2003/12/31 08:48:59  venku
   - Refactoring.
   - Setup classes setup each tests by data created by a common setup.
   - Tests and Setups are structured such that if test A requires
     data that can be tested by test B then testSetup B can
     be used to drive test A as well.
   Revision 1.19  2003/12/30 10:04:18  venku
   - sng in SimpleNodeGraphTest should track dg or the otherway
     round to make the hierarchy of test work.  This has
     been fixed by adding setSNG().
   Revision 1.18  2003/12/30 09:24:55  venku
   - Refactored DirectedAndSimpleNodeGraphTest into
      - AbstractDirectedGraphTest
      - SimpleNodeGraphTest
   - Introduced SimpleNodeGraphNoCycleTest
   - Java/Jikes based graph test inherit from SimpleNodeGraphTest.
   - Renamed DirectedAndSiimpleNodeGraphTestSuite to
     GraphNoArgTestSuite.
   - added checks to test exceptional behavior as well.
   Revision 1.17  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.16  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.15  2003/12/09 03:35:48  venku
   - formatting and removal of stdouts.
   Revision 1.14  2003/12/08 13:31:49  venku
   - used JUnit defined assert functions.
   Revision 1.13  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.12  2003/12/08 12:15:58  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.11  2003/12/07 14:04:43  venku
   - made FATest command-line compatible.
   - made use of AbstractDirectedGraphTest in
     CallGraphTest to test the constructed call graphs.
   Revision 1.10  2003/12/05 21:34:01  venku
   - formatting.
   - more tests.
   Revision 1.9  2003/12/05 15:28:12  venku
   - added test case for trivial tagging test in FA.
   Revision 1.8  2003/12/05 11:48:19  venku
   - added one more check while testing SCC.
   Revision 1.7  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.6  2003/11/30 01:38:52  venku
   - incorporated tag based filtering during CG construction.
   Revision 1.5  2003/11/30 01:07:57  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.4  2003/11/30 00:21:48  venku
   - coding convention.
   Revision 1.3  2003/11/29 09:48:14  venku
   - 2 SCC should be disjoint.  intersection should be used
     instead of subtract.  FIXED.
   Revision 1.2  2003/11/29 09:44:20  venku
   - changed the check for getCallees(InvokeExpr,..).
   Revision 1.1  2003/11/29 09:35:44  venku
   - added test support for processors.  CallGraph, in particular.
 */
