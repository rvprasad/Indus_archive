
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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;

import soot.ArrayType;
import soot.G;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.DirectedAndSimpleNodeGraphTest;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CallGraphTester
  extends TestCase {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	ICallGraphInfo cgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private OFAnalyzer ofa;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Scene scene;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String classes;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final String tagName = "CallGraphTester:FA";

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(final String[] args) {
		StringBuffer sb = new StringBuffer();

		for (int i = args.length - 1; i >= 0; i--) {
			sb.append(args[i] + " ");
		}

		CallGraphTester tester = new CallGraphTester();
		tester.classes = sb.toString();

		TestRunner runner = new TestRunner();
		runner.setLoading(false);
		runner.start(new String[0]);
		runner.startTest(tester);
		runner.runSuite();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph");

		//$JUnit-BEGIN$
		suite.addTestSuite(CallGraphTester.class);
		//$JUnit-END$
		return suite;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @throws Throwable DOCUMENT ME!
	 */
	public final void testGetCallGraph()
	  throws Throwable {
		assertTrue(cgi.getCallGraph() != null);

		DirectedAndSimpleNodeGraphTest graphTest =
			new DirectedAndSimpleNodeGraphTest() {
				protected void setUp() {
					dg = cgi.getCallGraph();
				}
			};
		graphTest.run();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetCalleesInvokeExprContext() {
		Context context = new Context();
		Collection calleeMethods = new ArrayList();

		for (Iterator i = cgi.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod caller = (SootMethod) i.next();
			Collection callees = cgi.getCallees(caller);
			calleeMethods.clear();

			for (Iterator j = callees.iterator(); j.hasNext();) {
				CallTriple ctrp = (CallTriple) j.next();
				calleeMethods.add(ctrp.getMethod());
			}

			for (Iterator j = callees.iterator(); j.hasNext();) {
				CallTriple ctrp1 = (CallTriple) j.next();
				context.setStmt(ctrp1.getStmt());
				context.setRootMethod(caller);

				Collection newCallees = cgi.getCallees(ctrp1.getExpr(), context);

				for (Iterator k = callees.iterator(); k.hasNext();) {
					CallTriple ctrp2 = (CallTriple) k.next();

					if (ctrp2.getExpr().equals(ctrp1.getExpr())) {
						assertTrue(newCallees.contains(ctrp2.getMethod()));
					}
				}
				calleeMethods.removeAll(newCallees);
			}
			assertTrue(calleeMethods.isEmpty());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetCallersAndGetCallees() {
		Collection heads = cgi.getHeads();
		Collection tails = cgi.getCallGraph().getTails();

		for (Iterator i = cgi.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod callee = (SootMethod) i.next();
			Collection callers = cgi.getCallers(callee);
			assertTrue(callers != null);

			if (callers.isEmpty()) {
				assertTrue(heads.contains(callee));
			} else {
				for (Iterator j = callers.iterator(); j.hasNext();) {
					CallTriple ctrp1 = (CallTriple) j.next();
					SootMethod caller = ctrp1.getMethod();
					assertTrue(cgi.isReachable(caller));

					Collection callees = cgi.getCallees(caller);

					if (callees.isEmpty()) {
						assertTrue(tails.contains(caller));
					} else {
						boolean t = false;

						for (Iterator k = callees.iterator(); k.hasNext();) {
							CallTriple ctrp2 = (CallTriple) k.next();

							if (ctrp2.getMethod().equals(callee)) {
								t = true;
								break;
							}
						}
						assertTrue(t);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetHeads() {
		Collection heads = cgi.getHeads();
		assertTrue(heads != null);

		for (Iterator i = heads.iterator(); i.hasNext();) {
			SootMethod sm = (SootMethod) i.next();
			assertTrue(cgi.getCallers(sm).isEmpty());
		}
		assertTrue(cgi.getReachableMethods().containsAll(heads));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetReachableMethods() {
		Collection reachables = cgi.getReachableMethods();
		assertTrue(reachables != null);

		for (Iterator i = reachables.iterator(); i.hasNext();) {
			assertTrue(i.next() instanceof SootMethod);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetSCCs() {
		Collection sccs = cgi.getSCCs();
		assertTrue(sccs != null);

		for (Iterator i = sccs.iterator(); i.hasNext();) {
			Collection scc1 = (Collection) i.next();
			assertTrue(scc1 != null);

			for (Iterator j = sccs.iterator(); j.hasNext();) {
				Collection scc2 = (Collection) j.next();

				if (scc1 != scc2) {
					assertTrue(CollectionUtils.intersection(scc1, scc2).isEmpty());
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testIsReachable() {
		Collection reachables = cgi.getReachableMethods();
		Collection heads = cgi.getHeads();
		SimpleNodeGraph cg = cgi.getCallGraph();

		for (Iterator i = scene.getClasses().iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			for (Iterator j = sc.getMethods().iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				assertTrue(cgi.isReachable(sm) == reachables.contains(sm));

				if (cgi.isReachable(sm)) {
					boolean t = false;

					for (Iterator k = heads.iterator(); k.hasNext();) {
						t |= cg.isReachable(cg.getNode(k.next()), cg.getNode(sm), true);
					}
					assertTrue(t == cgi.isReachable(sm) || heads.contains(sm));
				}
			}
		}
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected final void setUp()
	  throws Exception {
		ofa = OFAnalyzer.getFSOSAnalyzer(tagName);
		scene = Scene.v();

		if (classes == null) {
			classes = System.getProperty("callgraphtester.classes");
		}

		if (classes == null || classes.length() == 0) {
			throw new RuntimeException("callgraphtester.classes property was empty.  Aborting.");
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
		cgi = getCGImpl();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected ICallGraphInfo getCGImpl() {
		CallGraph cgiImpl = new CallGraph();

		ValueAnalyzerBasedProcessingController pc = new ValueAnalyzerBasedProcessingController();
		pc.setAnalyzer(ofa);
		pc.setEnvironment(ofa.getEnvironment());
		pc.setProcessingFilter(new TagBasedProcessingFilter(tagName));
		cgiImpl.hookup(pc);
		pc.process();
		cgiImpl.unhook(pc);
		return cgiImpl;
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected final void tearDown()
	  throws Exception {
		ofa.reset();
		ofa = null;
		resetCGImpl();
		cgi = null;
		scene = null;
		G.reset();
	}

	/**
	 * DOCUMENT ME!
	 */
	protected void resetCGImpl() {
		((CallGraph) cgi).reset();
	}
}

/*
   ChangeLog:
   $Log$
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
