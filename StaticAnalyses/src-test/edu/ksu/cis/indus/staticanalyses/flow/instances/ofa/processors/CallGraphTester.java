
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraphTest;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNode;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;

import org.apache.commons.collections.CollectionUtils;

import soot.ArrayType;
import soot.G;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;


/**
 * This class tests information calculated by
 * <code>edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CallGraphTester
  extends TestCase {
	/**
	 * The call graph to be tested.
	 */
	static CallGraph cgi;

	/**
	 * The object flow analysis to be used in conjunction with the call graph construction.
	 */
	static OFAnalyzer ofa;

	/**
	 * The system that provides the call graph.
	 */
	static Scene scene;

	/**
	 * The whitespace seperated list of names of the classes that form the system.
	 */
	static String classes;

	/**
	 * The tag name used by the object flow analysis.
	 */
	static final String TAG_NAME = "CallGraphTester:FA";

	/**
	 * This class wraps the graph test case to be applicable to the call graph.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static final class GraphTest
	  extends SimpleNodeGraphTest {
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
		 * Tests the size() method of the graph associated with the call graph.
		 */
		public void testSize() {
			assertTrue(cgi.getReachableMethods().size() == dg.getNodes().size());
		}

		/**
		 * Sets up the test.
		 */
		protected void setUp() {
			dg = (SimpleNodeGraph) cgi.getCallGraph();
		}

		/**
		 * Does nothing.
		 */
		protected void localtestAddEdgeFromTo() {
			// we do nothing as we are dealing with an immutable graph
		}

		/**
		 * Test  <code>getHeads()</code> method of the graph associated with the call graph.
		 */
		protected void localtestGetHeads() {
			assertTrue(dg.getHeads().containsAll(cgi.getHeads()));
			assertTrue(cgi.getHeads().containsAll(dg.getHeads()));
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
					assertTrue(sng.getNode(_sm).getSuccsOf().isEmpty());
				}
			}
		}

		/**
		 * Does nothing.
		 */
		protected void localtestIsAncestorOf() {
			// we cannot know in advance what is the ancestor relationship in a graph.  Do nothing.
		}

		/**
		 * Does nothing.
		 */
		protected void localtestIsReachable() {
			// we cannot know in advance what is the calle relationship in a graph.  Do nothing.
		}
	}


	/**
	 * This class sets up the call graph once before various tests are run on the call graph.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class CallGraphTestSetup
	  extends TestSetup {
		/**
		 * Creates a new CallGraphTestSetup object.
		 *
		 * @param test to be run in this set up.
		 */
		CallGraphTestSetup(final Test test) {
			super(test);
		}

		/**
		 * @see TestCase#setUp()
		 */
		protected void setUp()
		  throws Exception {
			ofa = OFAnalyzer.getFSOSAnalyzer(TAG_NAME);
			scene = Scene.v();

			if (classes == null) {
				classes = System.getProperty("callgraphtester.classes");
			}

			if (classes == null || classes.length() == 0) {
				throw new RuntimeException("callgraphtester.classes property was empty.  Aborting.");
			}

			final StringBuffer _sb = new StringBuffer(classes);
			final String[] _j = _sb.toString().split(" ");
			final Collection _rootMethods = new ArrayList();

			for (int _i = _j.length - 1; _i >= 0; _i--) {
				final SootClass _sc = scene.loadClassAndSupport(_j[_i]);

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

			ofa.analyze(scene, _rootMethods);

			final CallGraph _cgiImpl = new CallGraph();

			final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
			_pc.setAnalyzer(ofa);
			_pc.setEnvironment(ofa.getEnvironment());
			_pc.setProcessingFilter(new TagBasedProcessingFilter(TAG_NAME));
			_cgiImpl.hookup(_pc);
			_pc.process();
			_cgiImpl.unhook(_pc);
			cgi = _cgiImpl;
			System.out.println(_cgiImpl.dumpGraph());
		}

		/**
		 * @see TestCase#tearDown()
		 */
		protected void tearDown()
		  throws Exception {
			ofa.reset();
			ofa = null;
			cgi = null;
			scene = null;
			G.reset();
		}
	}

	/**
	 * This is the entry point via the command-line.
	 *
	 * @param args is the command line arguments.
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
	 * Retrieves the test suite that encapsulates the tests defined in this class.
	 *
	 * @return a test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		final TestSuite _suite =
			new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph");

		//$JUnit-BEGIN$
		_suite.addTestSuite(CallGraphTester.class);
		_suite.addTestSuite(GraphTest.class);
		//$JUnit-END$
		return new CallGraphTestSetup(_suite);
	}

	/**
	 * Tests <code>getCallGraph()</code>.
	 */
	public final void testGetCallGraph() {
		assertNotNull(cgi.getCallGraph());
	}

	/**
	 * Tests <code>getCalles(soot.jimple.InvokeExpr, edu.ksu.cis.indus.processing.Context)</code>.
	 */
	public final void testGetCalleesInvokeExprContext() {
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
	public final void testGetCallersAndGetCallees() {
		final Collection _heads = cgi.getHeads();
		final Collection _tails = cgi.getCallGraph().getTails();

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
	 * Tests <code>getHeads()</code>.
	 */
	public final void testGetHeads() {
		final Collection _heads = cgi.getHeads();
		assertNotNull(_heads);

		for (final Iterator _i = _heads.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			assertTrue(cgi.getCallers(_sm).isEmpty());
		}
		assertTrue(cgi.getReachableMethods().containsAll(_heads));
	}

	/**
	 * Tests <code>getReachableMethods()</code>.
	 */
	public final void testGetReachableMethods() {
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			assertTrue(_i.next() instanceof SootMethod);
		}
	}

	/**
	 * Tests <code>getSCCs()</code>.
	 */
	public final void testGetSCCs() {
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
	 * Tests <code>isReachable</code>.
	 */
	public final void testIsReachable() {
		final Collection _reachables = cgi.getReachableMethods();
		final Collection _heads = cgi.getHeads();
		final SimpleNodeGraph _cg = (SimpleNodeGraph) cgi.getCallGraph();

		for (final Iterator _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				assertEquals(cgi.isReachable(_sm), _reachables.contains(_sm));

				if (cgi.isReachable(_sm)) {
					boolean _t = false;

					for (final Iterator _k = _heads.iterator(); _k.hasNext();) {
						_t |= _cg.isReachable(_cg.getNode(_k.next()), _cg.getNode(_sm), true);
					}
					assertTrue(_t || _heads.contains(_sm));
				}
			}
		}
	}

	/**
	 * Tests the tags on the reachable methods based on tags used during object flow analysis.
	 */
	public final void testTagsOnReachableMethods() {
		final Context _ctxt = new Context();
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			final SootMethod _o = (SootMethod) _i.next();
			assertTrue(_o.hasTag(TAG_NAME));
			assertTrue(_o.getDeclaringClass().hasTag(TAG_NAME));

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
				assertFalse(_sm.hasTag(TAG_NAME));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
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
   - made FATester command-line compatible.
   - made use of AbstractDirectedGraphTest in
     CallGraphTester to test the constructed call graphs.
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
