
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
 * Tests to tests graphs with no cycle.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleNodeGraphNoCycleTest
  extends SimpleNodeGraphTest {
	/**
	 * @see JikesBasedDirectedAndSimpleNodeGraphTest#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final SimpleNodeGraph _sng = new SimpleNodeGraph();
		name2node.put("a", _sng.getNode("a"));
		name2node.put("b", _sng.getNode("b"));
		name2node.put("c", _sng.getNode("c"));
		name2node.put("d", _sng.getNode("d"));
		name2node.put("e", _sng.getNode("e"));

		_sng.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("b"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("c"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("d"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("e"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("c"));
		setSNG(_sng);
	}

	/**
	 * Does nothing.
	 */
	protected final void localtestAddEdgeFromTo() {
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetCycles()
	 */
	protected void localtestGetCycles() {
		assertTrue(dg.getCycles().isEmpty());
	}

	/**
	 * @see TestCase#tearDown
	 */
	protected final void tearDown()
	  throws Exception {
		super.tearDown();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetHeads()
	 */
	protected void localtestGetHeads() {
		assertFalse(sng.getHeads().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetPseudoTails()
	 */
	protected void localtestGetPseudoTails() {
		assertTrue(dg.getPseudoTails().isEmpty());
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
		assertTrue(sng.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("e")));
		assertFalse(sng.isAncestorOf((INode) name2node.get("e"), (INode) name2node.get("b")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsReachable()
	 */
	protected void localtestIsReachable() {
		assertTrue(sng.isReachable((INode) name2node.get("a"), (INode) name2node.get("e"), true));
		assertTrue(sng.isReachable((INode) name2node.get("e"), (INode) name2node.get("a"), false));
		assertTrue(sng.isReachable((INode) name2node.get("b"), (INode) name2node.get("e"), true));
		assertFalse(sng.isReachable((INode) name2node.get("d"), (INode) name2node.get("c"), false));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/02/08 01:04:12  venku
   - renamed TestSuite classes to NoArgTestSuite classes.
   Revision 1.6  2004/01/22 08:18:55  venku
   - added test methods to handle getPseudoTails().
   Revision 1.5  2004/01/06 01:51:06  venku
   - renamed DirectedGraphTestSuite to GraphNoArgTestSuite.
   Revision 1.4  2003/12/31 10:02:02  venku
 *** empty log message ***
         Revision 1.3  2003/12/31 08:29:58  venku
         - changed the graph.
         - added localtestAddEdgeFromTo() to override that
           defined in the super class.
         Revision 1.2  2003/12/30 10:04:25  venku
         - sng in SimpleNodeGraphTest should track dg or the otherway
           round to make the hierarchy of test work.  This has
           been fixed by adding setSNG().
         Revision 1.1  2003/12/30 09:24:59  venku
         - Refactored DirectedAndSimpleNodeGraphTest into
            - AbstractDirectedGraphTest
            - SimpleNodeGraphTest
         - Introduced SimpleNodeGraphNoCycleTest
         - Java/Jikes based graph test inherit from SimpleNodeGraphTest.
         - Renamed DirectedAndSiimpleNodeGraphTestSuite to
           GraphNoArgTestSuite.
         - added checks to test exceptional behavior as well.
 */
