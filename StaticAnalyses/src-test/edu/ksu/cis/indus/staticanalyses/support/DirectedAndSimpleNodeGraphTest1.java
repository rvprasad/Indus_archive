
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.ksu.cis.indus.staticanalyses.support.MutableDirectedGraph.MutableNode;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class tests <code>DirectedGraph</code> and <code>SimpleNodeGraph</code> classes that exists in the same package.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DirectedAndSimpleNodeGraphTest1
  extends TestCase {
	/**
	 * This maps node names to nodes.
	 *
	 * @invariant name2node.oclIsKindOf(Map(String, INode))
	 */
	private final Map name2node = new HashMap();

	/**
	 * This is the graph that will be tested.
	 */
	private SimpleNodeGraph dg;

	/**
	 * Constructor for DirectedAndSimpleNodeGraphTest1.
	 *
	 * @param name of the test
	 */
	public DirectedAndSimpleNodeGraphTest1(final String name) {
		super(name);
	}

	/**
	 * Returns a suite of tests defined in this class.
	 *
	 * @return the suite of tests.
	 */
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("suite of tests in DirectedAndSimpleNodeGraphTest1");
		suite.addTestSuite(DirectedAndSimpleNodeGraphTest1.class);
		return suite;
	}

	/**
	 * Tests <code>addEdgeFromTo</code> method.
	 */
	public final void testAddEdgeFromTo() {
		BitSet[] preds1 = dg.getAllPredsAsBitSet();
		BitSet[] succs1 = dg.getAllSuccsAsBitSet();

		// Add edge from c to h
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("h"));
		// Add edge from a to f
		dg.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("f"));

		BitSet[] preds2 = dg.getAllPredsAsBitSet();
		BitSet[] succs2 = dg.getAllSuccsAsBitSet();

		for (int i = 0; i < succs2.length; i++) {
			succs2[i].xor(succs1[i]);

			if (i == dg.getNodes().indexOf(name2node.get("a")) || i == dg.getNodes().indexOf(name2node.get("c"))) {
				assertTrue(succs2[i].cardinality() == 1);
			} else {
				assertTrue(succs2[i].cardinality() == 0);
			}
			preds2[i].xor(preds1[i]);

			if (i == dg.getNodes().indexOf(name2node.get("h")) || i == dg.getNodes().indexOf(name2node.get("f"))) {
				assertTrue(preds2[i].cardinality() == 1);
			} else {
				assertTrue(preds2[i].cardinality() == 0);
			}
		}
	}

	/**
	 * Tests <code>getAllPredsAsBitSet()</code> method.
	 */
	public final void testGetAllPredsAsBitSet() {
		BitSet[] preds = dg.getAllPredsAsBitSet();
		List nodes = dg.getNodes();
		Collection temp = new HashSet();

		for (int i = 0; i < preds.length; i++) {
			temp.clear();

			int pos = preds[i].nextSetBit(0);

			while (pos != -1) {
				temp.add(nodes.get(pos));
				pos = preds[i].nextSetBit(pos + 1);
			}

			INode node = (INode) nodes.get(i);
			Collection p = node.getPredsOf();

			/*
			 * check if preds from the above call is a non-empty set.
			 * check if preds from the getPredsOf() call is a non-empty set.
			 * check if the returned preds are the same.
			 */
			assertTrue(!temp.isEmpty() && !p.isEmpty() && p.containsAll(temp) && temp.containsAll(p));
		}
	}

	/**
	 * Tests <code>getAllSuccsAsBitSet()</code> method.
	 */
	public final void testGetAllSuccsAsBitSet() {
		BitSet[] succs = dg.getAllSuccsAsBitSet();
		List nodes = dg.getNodes();
		Collection temp = new HashSet();

		for (int i = 0; i < succs.length; i++) {
			temp.clear();

			int pos = succs[i].nextSetBit(0);

			while (pos != -1) {
				temp.add(nodes.get(pos));
				pos = succs[i].nextSetBit(pos + 1);
			}

			INode node = (INode) nodes.get(i);
			Collection p = node.getSuccsOf();

			/*
			 * check if succs from the above call is a non-empty set.
			 * check if succs from the getPredsOf() call is a non-empty set.
			 * check if the returned succs are the same.
			 */
			assertTrue(!temp.isEmpty() && !p.isEmpty() && p.containsAll(temp) && temp.containsAll(p));
		}
	}

	/**
	 * Tests <code>getCycles()</code> method.
	 */
	public final void testGetCycles() {
		Collection cycles = dg.getCycles();
		assertFalse(cycles.isEmpty());

		for (Iterator i = cycles.iterator(); i.hasNext();) {
			Collection cycle = (Collection) i.next();
			INode node = (INode) cycle.iterator().next();
			assertTrue(dg.isReachable(node, node, true));
		}
	}

	/**
	 * Tests <code>getForwardSuccsOf()</code> method.
	 */
	public final void testGetForwardSuccsOf() {
		for (Iterator i = name2node.values().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			Collection succs = dg.getForwardSuccsOf(node);

			for (Iterator j = succs.iterator(); j.hasNext();) {
				MutableNode succ = (MutableNode) j.next();
				assertTrue(succ.getPredsOf().contains(node));
			}
		}
	}

	/**
	 * Tests <code>getHeads()</code> method.
	 */
	public final void testGetHeads() {
		assertTrue(dg.getHeads().isEmpty());

		for (Iterator i = dg.getHeads().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(node.getPredsOf().isEmpty());
		}
	}

	/**
	 * Tests <code>getNode()</code> method.
	 */
	public final void testGetNode() {
		for (Iterator i = name2node.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			INode node = dg.getNode(entry.getKey());
			assertTrue(node.equals(entry.getValue()));
		}
	}

	/**
	 * Tests <code>getNodes()</code> method.
	 */
	public final void testGetNodes() {
		List nodes1 = dg.getNodes();

		// check for bidirectional containment to establish equality.
		assertTrue(name2node.values().containsAll(dg.getNodes()));
		assertTrue(dg.getNodes().containsAll(name2node.values()));

		List nodes2 = dg.getNodes();

		// check ordering across calls is same..
		assertTrue(nodes1.equals(nodes2));
	}

	/**
	 * Tests <code>getSCCs()</code> method.
	 */
	public final void testGetSCCs() {
		Collection sccs = dg.getSCCs(true);
		assertFalse(sccs.isEmpty());

		List nodes = dg.getNodes();

		for (Iterator i = sccs.iterator(); i.hasNext();) {
			Collection scc = (Collection) i.next();

			for (Iterator j = scc.iterator(); j.hasNext();) {
				INode srcNode = (INode) j.next();

				for (Iterator k = nodes.iterator(); k.hasNext();) {
					INode destNode = (INode) k.next();

					if (scc.contains(destNode)) {
						assertTrue(dg.isReachable(srcNode, destNode, true) && dg.isReachable(destNode, srcNode, true));
					} else {
						assertTrue(dg.isReachable(srcNode, destNode, true) ^ dg.isReachable(destNode, srcNode, true));
					}
				}
			}
		}
	}

	/**
	 * Tests <code>getSpanningSuccs()</code> method.
	 */
	public final void testGetSpanningSuccs() {
		Map spanningSuccs = dg.getSpanningSuccs();
		List temp = new ArrayList();

		for (Iterator i = spanningSuccs.values().iterator(); i.hasNext();) {
			Collection succs = (Collection) i.next();
			temp.addAll(succs);
		}

		// ensure all tree nodes are among the nodes of this graph.
		assertTrue(dg.getNodes().containsAll(spanningSuccs.keySet()));
		assertTrue(dg.getNodes().containsAll(temp));

		// ensure no node appears as child of two different parents in the spanning tree.
		for (Iterator i = temp.iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(CollectionUtils.cardinality(node, temp) == 1);
		}
	}

	/**
	 * Tests <code>getTails()</code> method.
	 */
	public final void testGetTails() {
		assertTrue(dg.getTails().isEmpty());

		for (Iterator i = dg.getTails().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(node.getSuccsOf().isEmpty());
		}
	}

	/**
	 * Tests <code>isReachable()</code> method.
	 */
	public final void testIsReachable() {
		assertTrue(dg.isReachable((INode) name2node.get("a"), (INode) name2node.get("e"), true));
		assertFalse(dg.isReachable((INode) name2node.get("f"), (INode) name2node.get("c"), true));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("a"), false));
		assertFalse(dg.isReachable((INode) name2node.get("c"), (INode) name2node.get("h"), false));
	}

	/**
	 * Tests <code>performTopologicalSort()</code> method.
	 */
	public final void testPerformTopologicalSort() {
		List sorted = dg.performTopologicalSort(true);
		List nodes = dg.getNodes();
		assertTrue(nodes.containsAll(sorted) && sorted.containsAll(nodes));

		if (dg.getCycles().isEmpty()) {
			for (Iterator i = sorted.iterator(); i.hasNext();) {
				INode src = (INode) i.next();
				Collection t = sorted.subList(sorted.indexOf(src), sorted.size() - 1);

				for (Iterator j = t.iterator(); j.hasNext();) {
					INode dest = (INode) j.next();
					assertFalse(dg.isReachable(dest, src, true));
				}
			}
		}
	}

	/**
	 * Tests <code>size()</code> method.
	 */
	public final void testSize() {
		assertTrue(name2node.values().size() == dg.getNodes().size());
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
		dg = new SimpleNodeGraph();
		// create graph as given on pp 553 of "Introduction to Algorithms" book.
		name2node.put("a", dg.getNode("a"));
		name2node.put("b", dg.getNode("b"));
		name2node.put("c", dg.getNode("c"));
		name2node.put("d", dg.getNode("d"));
		name2node.put("e", dg.getNode("e"));
		name2node.put("f", dg.getNode("f"));
		name2node.put("g", dg.getNode("g"));
		name2node.put("h", dg.getNode("h"));
		// connect them now
		dg.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("b"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("c"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("d"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("c"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("h"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("g"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("g"), (SimpleNode) name2node.get("h"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("h"), (SimpleNode) name2node.get("h"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("g"), (SimpleNode) name2node.get("f"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("f"), (SimpleNode) name2node.get("g"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("f"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("f"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("e"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("a"));
	}
}

/*
   ChangeLog:
   $Log$
 */
