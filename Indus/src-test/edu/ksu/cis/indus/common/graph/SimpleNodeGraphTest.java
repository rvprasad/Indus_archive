
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

package edu.ksu.cis.indus.common.graph;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
	protected final Map<String, SimpleNode<String>> name2node = new HashMap<String, SimpleNode<String>>();

	/** 
	 * The reference to a simple node graph used for testing.  This should be the same as dg, so set this via
	 * <code>setSNG</code>.
	 *
	 * @invariant dg = sng
	 */
	protected SimpleNodeGraph<String> sng;

	/** 
	 * The number of cycles in the graph being tested.
	 */
	protected int numberOfCycles;

	/**
	 * Tests <code>getNode</code> method.
	 */
	public final void testGetNode() {
		for (final Iterator<Map.Entry<String, SimpleNode<String>>> _i = name2node.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<String, SimpleNode<String>> _entry =  _i.next();
			final SimpleNode<String> _node = sng.getNode(_entry.getKey());
			assertTrue(_node.equals(_entry.getValue()));
			assertTrue(_node.getObject().equals(_entry.getKey()));
		}

		try {
			sng.getNode((String) null);
		} catch (NullPointerException _e) {
			fail("Shouldn't have raised an exception.");
		}
	}

	/**
	 * Tests <code>getNodes</code> method.
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
	 * Tests <code>getNodesInPathBetween()</code>.
	 */
	public void testGetNodesInPathBetween() {
		final Collection _t = new HashSet();
		_t.add(sng.getNode("b"));
		_t.add(sng.getNode("e"));

		final Collection _nodes = dg.getNodesOnPathBetween(_t);
		_t.add(sng.getNode("a"));
		assertTrue(_nodes + " " + _t, _nodes.containsAll(_t) && _t.containsAll(_nodes));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#testGetTails()
	 */
	public void testGetTails() {
		final Collection _dtails = dg.getTails();
		assertTrue(_dtails.size() == 1);
		assertTrue(_dtails.contains(name2node.get("h")));
	}

	/**
	 * Performs local <code>getSinks()</code> test.
	 */
	public void testlocalGetSinks() {
		assertTrue(dg.getSinks().isEmpty());
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
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final SimpleNodeGraph<String> _sng = new SimpleNodeGraph<String>();

		name2node.put("a", _sng.getNode("a"));
		name2node.put("b", _sng.getNode("b"));
		name2node.put("c", _sng.getNode("c"));
		name2node.put("d", _sng.getNode("d"));
		name2node.put("e", _sng.getNode("e"));
		name2node.put("f", _sng.getNode("f"));
		name2node.put("g", _sng.getNode("g"));
		name2node.put("h", _sng.getNode("h"));
		// connect them now
		_sng.addEdgeFromTo( name2node.get("a"),  name2node.get("b"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("c"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("f"));
		_sng.addEdgeFromTo( name2node.get("b"),  name2node.get("e"));
		_sng.addEdgeFromTo( name2node.get("c"),  name2node.get("d"));
		_sng.addEdgeFromTo( name2node.get("c"),  name2node.get("g"));
		_sng.addEdgeFromTo( name2node.get("d"),  name2node.get("c"));
		_sng.addEdgeFromTo( name2node.get("d"),  name2node.get("h"));
		_sng.addEdgeFromTo( name2node.get("e"),  name2node.get("f"));
		_sng.addEdgeFromTo( name2node.get("e"),  name2node.get("a"));
		_sng.addEdgeFromTo( name2node.get("f"),  name2node.get("g"));
		_sng.addEdgeFromTo( name2node.get("g"),  name2node.get("h"));
		_sng.addEdgeFromTo( name2node.get("g"),  name2node.get("f"));
		_sng.addEdgeFromTo( name2node.get("h"),  name2node.get("h"));
		setSNG(_sng);

		numberOfCycles = 4;
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestAddEdgeFromTo()
	 */
	protected void localtestAddEdgeFromTo() {
		final Map _preds1 = new HashMap();
		final Map _succs1 = new HashMap();
		extractPredSuccCopy(dg, _preds1, _succs1);

		// Add edge from c to h
		sng.addEdgeFromTo( name2node.get("c"),  name2node.get("h"));

		assertTrue(name2node.get("c").getSuccsOf().contains(name2node.get("h")));
		assertTrue(name2node.get("h").getPredsOf().contains(name2node.get("c")));

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
		sng.addEdgeFromTo( name2node.get("a"),  name2node.get("f"));

		_preds1.clear();
		_succs1.clear();
		extractPredSuccCopy(dg, _preds1, _succs1);

		assertTrue(name2node.get("a").getSuccsOf().contains(name2node.get("f")));
		assertTrue(name2node.get("f").getPredsOf().contains(name2node.get("a")));

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
		try {
			assertFalse(sng.addEdgeFromTo(new SimpleNode("t1"), new SimpleNode("t2")));
			fail("This should not happen!");
		} catch (final IllegalArgumentException _e) {
			;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest#localtestGetCycles()
	 */
	protected void localtestGetCycles() {
		final Collection _cycles = dg.getCycles();
		assertTrue(_cycles.size() + "==" + numberOfCycles, _cycles.size() == numberOfCycles);
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
	 * @see AbstractDirectedGraphTest#localtestIsAncestorOf
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(dg.isAncestorOf(name2node.get("a"), name2node.get("a")));
		assertTrue(dg.isAncestorOf(name2node.get("h"), name2node.get("h")));
	}

	/**
	 * @see AbstractDirectedGraphTest#localtestIsReachable
	 */
	protected void localtestIsReachable() {
		assertTrue(dg.isReachable( name2node.get("a"), name2node.get("e"), true));
		assertFalse(dg.isReachable( name2node.get("f"), name2node.get("c"), true));
		assertTrue(dg.isReachable( name2node.get("h"), name2node.get("a"), false));
		assertFalse(dg.isReachable( name2node.get("c"), name2node.get("h"), false));
		assertTrue(dg.isReachable( name2node.get("h"), name2node.get("h"), false));
		assertTrue(dg.isReachable( name2node.get("h"), name2node.get("h"), true));
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		dg = null;
		sng = null;
		name2node.clear();
	}
}

// End of File
