
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import java.util.Collection;
import java.util.HashSet;


/**
 * This class tests the following structure.
 * <pre>
 * a -> b
 * b -> c
 * c -> d
 * d -> b
 * b -> e
 * </pre>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleNodeGraphTest3
  extends SimpleNodeGraphTest {
	/**
	 * @see edu.ksu.cis.indus.common.graph.SimpleNodeGraphTest#testGetNodesInPathBetween()
	 */
	public void testGetNodesInPathBetween() {
		final Collection _t = new HashSet();
		_t.add(sng.getNode("a"));
		_t.add(sng.getNode("e"));

		final Collection _nodes = dg.getNodesOnPathBetween(_t);
		_t.add(sng.getNode("c"));
		_t.add(sng.getNode("b"));
		_t.add(sng.getNode("d"));
		assertTrue(_nodes.containsAll(_t) && _t.containsAll(_nodes));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final SimpleNodeGraph _sng = new SimpleNodeGraph();
		name2node.put("a", _sng.getNode("a"));
		name2node.put("b", _sng.getNode("b"));
		name2node.put("c", _sng.getNode("c"));
		name2node.put("d", _sng.getNode("d"));
		name2node.put("e", _sng.getNode("e"));

		_sng.addEdgeFromTo((INode) name2node.get("a"), (INode) name2node.get("b"));
		_sng.addEdgeFromTo((INode) name2node.get("b"), (INode) name2node.get("c"));
		_sng.addEdgeFromTo((INode) name2node.get("c"), (INode) name2node.get("d"));
		_sng.addEdgeFromTo((INode) name2node.get("d"), (INode) name2node.get("b"));
		_sng.addEdgeFromTo((INode) name2node.get("b"), (INode) name2node.get("e"));
		setSNG(_sng);
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestAddEdgeFromTo()
	 */
	protected void localtestAddEdgeFromTo() {
		// do nothing.
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetHeads()
	 */
	protected void localtestGetHeads() {
		assertTrue(dg.getHeads().size() == 1);
		assertTrue(dg.getHeads().contains(name2node.get("a")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetPseudoTails()
	 */
	protected void localtestGetPseudoTails() {
		assertTrue(dg.getPseudoTails().size() == 0);
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGraphGetTails()
	 */
	protected void localtestGraphGetTails() {
		assertTrue(dg.getTails().size() == 1);
		assertTrue(dg.getTails().contains(name2node.get("e")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsAncestorOf()
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(dg.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("d")));
		assertFalse(dg.isAncestorOf((INode) name2node.get("d"), (INode) name2node.get("a")));
		assertTrue(dg.isAncestorOf((INode) name2node.get("b"), (INode) name2node.get("c")));
		assertFalse(dg.isAncestorOf((INode) name2node.get("c"), (INode) name2node.get("b")));
		assertTrue(dg.isAncestorOf((INode) name2node.get("c"), (INode) name2node.get("d")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsReachable()
	 */
	protected void localtestIsReachable() {
		assertTrue(dg.isReachable((INode) name2node.get("b"), (INode) name2node.get("e"), true));
		assertTrue(dg.isReachable((INode) name2node.get("d"), (INode) name2node.get("a"), false));
		assertFalse(dg.isReachable((INode) name2node.get("c"), (INode) name2node.get("a"), true));
	}
}

// End of File
