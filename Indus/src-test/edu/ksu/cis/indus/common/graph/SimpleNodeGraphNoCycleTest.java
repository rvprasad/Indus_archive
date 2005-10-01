
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
 * Tests to tests graphs with no cycle.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleNodeGraphNoCycleTest
  extends SimpleNodeGraphTest {
	/**
	 * @see edu.ksu.cis.indus.common.graph.SimpleNodeGraphTest#testGetNodesInPathBetween()
	 */
	public void testGetNodesInPathBetween() {
		final Collection _t = new HashSet();
		_t.add(sng.getNode("b"));
		_t.add(sng.getNode("d"));

		final Collection _nodes = dg.getNodesOnPathBetween(_t);
		assertTrue(_nodes.containsAll(_t));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#testGetTails()
	 */
	public void testGetTails() {
		assertFalse(dg.getTails().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.SimpleNodeGraphTest#testlocalGetSinks()
	 */
	public void testlocalGetSinks() {
		assertFalse(dg.getSinks().isEmpty());
	}

	/**
	 * Tests <code>getSinks()</code> locally.
	 */
	public void testlocalGraphGetSinks() {
		assertFalse(sng.getSinks().isEmpty());
	}

	/**
	 * @see SimpleNodeGraphTest#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final SimpleNodeGraph<String> _sng = new SimpleNodeGraph<String>();
		name2node.put("a", _sng.getNode("a"));
		name2node.put("b", _sng.getNode("b"));
		name2node.put("c", _sng.getNode("c"));
		name2node.put("d", _sng.getNode("d"));
		name2node.put("e", _sng.getNode("e"));

		_sng.addEdgeFromTo( name2node.get("a"),  name2node.get("b"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("c"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("d"));
		_sng.addEdgeFromTo( name2node.get("d"),  name2node.get("e"));
		_sng.addEdgeFromTo( name2node.get("e"),  name2node.get("c"));
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
	 * @see SimpleNodeGraphTest#tearDown()
	 */
	protected final void tearDown()
	  throws Exception {
		super.tearDown();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsAncestorOf()
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(sng.isAncestorOf( name2node.get("a"),  name2node.get("a")));
		assertTrue(sng.isAncestorOf( name2node.get("a"),  name2node.get("e")));
		assertFalse(sng.isAncestorOf( name2node.get("e"),  name2node.get("b")));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestIsReachable()
	 */
	protected void localtestIsReachable() {
		assertFalse(sng.isReachable( name2node.get("a"),  name2node.get("a"), true));
		assertTrue(sng.isReachable( name2node.get("a"),  name2node.get("e"), true));
		assertTrue(sng.isReachable( name2node.get("e"),  name2node.get("a"), false));
		assertTrue(sng.isReachable( name2node.get("b"),  name2node.get("e"), true));
		assertFalse(sng.isReachable( name2node.get("d"),  name2node.get("c"), false));
	}
}

// End of File
