
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Tests exception cases in <code>SimpleNodeGraph</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleNodeGraphTest
  extends AbstractDirectedGraphTest {
	/**
	 * This maps node names to nodes.
	 *
	 * @invariant name2node.oclIsKindOf(Map(String, INode))
	 */
	protected final Map name2node = new HashMap();

	/**
	 * The reference to a simple node graph used for testing.  This should be the same as dg, so set this via
	 * <code>setSNG</code>.
	 *
	 * @invariant dg = sng
	 */
	protected SimpleNodeGraph sng;

	/**
	 * Tests <code>getNode</code> method.
	 */
	public final void testGetNode() {
		for (final Iterator _i = name2node.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final INode _node = sng.getNode(_entry.getKey());
			assertTrue(_node.equals(_entry.getValue()));
			assertTrue(((SimpleNode) _node).getObject().equals(_entry.getKey()));
		}

		try {
			sng.getNode(null);
			fail("Should have raised an exception.");
		} catch (NullPointerException _e) {
			;
		}
	}

	/**
	 * @see AbstractDirectedGraphTest#testGetNodes
	 */
	public void testGetNodes() {
		final List _nodes1 = dg.getNodes();

		// check for bidirectional containment to establish equality.
		assertTrue(name2node.values().containsAll(dg.getNodes()));
		assertTrue(dg.getNodes().containsAll(name2node.values()));

		final List _nodes2 = dg.getNodes();

		// check ordering across calls is same..
		assertTrue(_nodes1.equals(_nodes2));
	}

	/**
	 * Set the graph to be tested.
	 *
	 * @param graph is the graph to be tested.
	 *
	 * @pre graph != null
	 * @post sng = graph and dg = graph
	 */
	protected void setSNG(final SimpleNodeGraph graph) {
		sng = graph;
		dg = sng;
	}

	/**
	 * We construct the graph given in the book "Introduction to Algorithms" on page 553.
	 *
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final SimpleNodeGraph _sng = new SimpleNodeGraph();

		name2node.put("a", _sng.getNode("a"));
		name2node.put("b", _sng.getNode("b"));
		name2node.put("c", _sng.getNode("c"));
		name2node.put("d", _sng.getNode("d"));
		name2node.put("e", _sng.getNode("e"));
		name2node.put("f", _sng.getNode("f"));
		name2node.put("g", _sng.getNode("g"));
		name2node.put("h", _sng.getNode("h"));
		// connect them now
		_sng.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("b"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("c"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("f"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("e"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("d"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("g"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("c"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("h"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("f"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("a"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("f"), (SimpleNode) name2node.get("g"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("g"), (SimpleNode) name2node.get("h"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("g"), (SimpleNode) name2node.get("f"));
		_sng.addEdgeFromTo((SimpleNode) name2node.get("h"), (SimpleNode) name2node.get("h"));
		setSNG(_sng);
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestAddEdgeFromTo
	 */
	protected void localtestAddEdgeFromTo() {
		final Map _preds1 = new HashMap();
		final Map _succs1 = new HashMap();
		extractPredSuccCopy(dg, _preds1, _succs1);

		// Add edge from c to h
		sng.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("h"));

		assertTrue(((INode) name2node.get("c")).getSuccsOf().contains(name2node.get("h")));
		assertTrue(((INode) name2node.get("h")).getPredsOf().contains(name2node.get("c")));

		final Map _preds2 = new HashMap();
		final Map _succs2 = new HashMap();
		extractPredSuccCopy(dg, _preds2, _succs2);

		for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();

			if (name2node.get("h") != _node) {
				assertTrue(_preds1.get(_node).equals(_preds2.get(_node)));
			}

			if (name2node.get("c") != _node) {
				assertTrue(_succs1.get(_node).equals(_succs2.get(_node)));
			}
		}

		// Add edge from a to f
		sng.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("f"));

		_preds1.clear();
		_succs1.clear();
		extractPredSuccCopy(dg, _preds1, _succs1);

		assertTrue(((INode) name2node.get("a")).getSuccsOf().contains(name2node.get("f")));
		assertTrue(((INode) name2node.get("f")).getPredsOf().contains(name2node.get("a")));

		for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();

			if (name2node.get("f") != _node) {
				assertTrue(_preds1.get(_node).equals(_preds2.get(_node)));
			}

			if (name2node.get("a") != _node) {
				assertTrue(_succs1.get(_node).equals(_succs2.get(_node)));
			}
		}

		// Adding edges between non-existent nodes
		assertFalse(sng.addEdgeFromTo(sng.new SimpleNode("t1"), sng.new SimpleNode("t2")));

		// Create Spanning tree, add a new node, and check if the a new spanning tree will be built.
		final Map _temp = new HashMap(dg.getSpanningSuccs());
		assertTrue(sng.addEdgeFromTo(sng.getNode("i"), sng.getNode("b")));
		assertFalse(_temp.equals(dg.getSpanningSuccs()));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetCycles()
	 */
	protected void localtestGetCycles() {
		assertFalse(dg.getCycles().isEmpty());
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestGetHeads
	 */
	protected void localtestGetHeads() {
		assertTrue(dg.getHeads().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetPseudoTails()
	 */
	protected void localtestGetPseudoTails() {
		final Collection _dtails = dg.getPseudoTails();
		assertTrue(_dtails.size() == 1);
		assertTrue(_dtails.contains(name2node.get("h")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetSCCs()
	 */
	protected void localtestGetSCCs() {
		final Collection _sccsTrue = dg.getSCCs(true);
		final Collection _sccsFalse = dg.getSCCs(false);
		assertFalse(_sccsTrue.isEmpty());
		assertFalse(_sccsFalse.isEmpty());
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestGraphGetTails
	 */
	protected void localtestGraphGetTails() {
		assertTrue(dg.getTails().isEmpty());
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestIsAncestorOf
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(dg.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("a")));
		assertTrue(dg.isAncestorOf((INode) name2node.get("h"), (INode) name2node.get("h")));
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestIsReachable
	 */
	protected void localtestIsReachable() {
		assertTrue(dg.isReachable((INode) name2node.get("a"), (INode) name2node.get("e"), true));
		assertFalse(dg.isReachable((INode) name2node.get("f"), (INode) name2node.get("c"), true));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("a"), false));
		assertFalse(dg.isReachable((INode) name2node.get("c"), (INode) name2node.get("h"), false));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("h"), false));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("h"), true));
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		dg = null;
		sng = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2004/02/08 01:04:12  venku
   - renamed TestSuite classes to NoArgTestSuite classes.
   Revision 1.9  2004/02/05 18:17:29  venku
   - getPseudoTails() is incorrect when the pseudo tails are mutually
     reachable.  FIXED.
   Revision 1.8  2004/02/05 16:12:36  venku
   - added a new test case for testing pseudoTails.
   Revision 1.7  2004/01/22 08:18:55  venku
   - added test methods to handle getPseudoTails().
   Revision 1.6  2004/01/22 05:19:29  venku
   - coding convention.
   Revision 1.5  2004/01/06 01:51:06  venku
   - renamed DirectedGraphTestSuite to GraphNoArgTestSuite.
   Revision 1.4  2004/01/03 19:02:38  venku
   - formatting and coding conventions.
   Revision 1.3  2003/12/31 10:43:08  venku
   - size() was unused in IDirectedGraph, hence, removed it.
     Ripple effect.
   Revision 1.2  2003/12/30 10:04:24  venku
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
