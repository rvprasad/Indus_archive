
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

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNode;


/**
 * This class tests directed graph and simple node graph implementations with a different graph instance.  All the local
 * tests are overloaded suitably.
 * 
 * <p>
 * This represents the CFG as generated by <b>jikes</b> for the following snippet.
 * </p>
 * <pre>
 *  public static void main(String[] s) {
 *       int i = 0;
 *       do {
 *           while(i > 0) {
 *               i--;
 *           };
 *           i++;
 *       } while (i < 10);
 *       System.out.println("Hi");
 *   }
 * </pre>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class JikesBasedDirectedAndSimpleNodeGraphTest
  extends SimpleNodeGraphTest {
	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#testAddEdgeFromTo()
	 */
	public void localtestAddEdgeFromTo() {
		// we do nothing here as we do not want to change the graph.
	}

	/**
	 * We construct a graph with cycles enclosed in cycles.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		sng = new SimpleNodeGraph();
		name2node.put("a", sng.getNode("a"));
		name2node.put("b", sng.getNode("b"));
		name2node.put("c", sng.getNode("c"));
		name2node.put("d", sng.getNode("d"));
		name2node.put("e", sng.getNode("e"));
		name2node.put("f", sng.getNode("f"));
		sng.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("b"));
		sng.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("c"));
		sng.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("d"));
		sng.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("e"));
		sng.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("f"));
		// add loop edges
		sng.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("c"));
		sng.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("b"));
        dg = sng;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetHeads()
	 */
	protected void localtestGetHeads() {
		assertFalse(sng.getHeads().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGraphGetTails()
	 */
	protected void localtestGraphGetTails() {
		assertFalse(sng.getTails().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsAncestorOf()
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(sng.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("a")));
		assertTrue(sng.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("f")));
		assertFalse(sng.isAncestorOf((INode) name2node.get("e"), (INode) name2node.get("b")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsReachable()
	 */
	protected void localtestIsReachable() {
		assertTrue(sng.isReachable((INode) name2node.get("b"), (INode) name2node.get("d"), false));
		assertTrue(sng.isReachable((INode) name2node.get("b"), (INode) name2node.get("d"), true));
		assertTrue(sng.isReachable((INode) name2node.get("a"), (INode) name2node.get("f"), true));
		assertTrue(sng.isReachable((INode) name2node.get("f"), (INode) name2node.get("a"), false));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.3  2003/12/02 09:42:34  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/11/10 03:40:50  venku
   - renamed DirectedAndSimpleNodeGraphTest1 to
     AbstractDirectedGraphTest.
   Revision 1.1  2003/11/10 03:39:53  venku
   - renamed test2 and test3 to JikesBased and JavacBased tests.
   Revision 1.4  2003/09/28 23:19:36  venku
 *** empty log message ***
     Revision 1.3  2003/09/11 12:31:00  venku
     - made ancestral relationship antisymmetric
     - added testcases to test the relationship.
     Revision 1.2  2003/09/11 02:37:12  venku
     - added a test case for javac compilation of Divergent04 test.
     - created test suite to test directed and simple node graph.
     Revision 1.1  2003/09/11 01:52:07  venku
     - prenum, postnum, and back edges support has been added.
     - added test case to test the above addition.
     - corrected subtle bugs in test1
     - refactored test1 so that setup local testing can be added by subclasses.
 */
