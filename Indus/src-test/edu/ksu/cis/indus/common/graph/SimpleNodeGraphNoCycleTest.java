
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
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#testAddEdgeFromTo()
	 */
	public void localtestAddEdgeFromTo() {
		// we do nothing here as we do not want to change the graph.
	}

	/**
	 * Tests <code>getCycles()</code> method.
	 */
	public final void testGetCycles() {
		assertTrue(sng.getCycles().isEmpty());
	}

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
        setSNG(_sng);
	}

    /**
     * @see TestCase#tearDown
     */
    protected final void tearDown() throws Exception {
        super.tearDown();
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
   Revision 1.1  2003/12/30 09:24:59  venku
   - Refactored DirectedAndSimpleNodeGraphTest into
      - AbstractDirectedGraphTest
      - SimpleNodeGraphTest
   - Introduced SimpleNodeGraphNoCycleTest
   - Java/Jikes based graph test inherit from SimpleNodeGraphTest.
   - Renamed DirectedAndSiimpleNodeGraphTestSuite to
     DirectedGraphTestSuite.
   - added checks to test exceptional behavior as well.

 */
