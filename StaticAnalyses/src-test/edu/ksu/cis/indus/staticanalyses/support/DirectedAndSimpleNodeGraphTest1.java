
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

package edu.ksu.cis.indus.staticanalyses.support;

import junit.framework.TestCase;

import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class tests <code>DirectedGraph</code> and <code>SimpleNodeGraph</code> classes that exists in the same package.
 * Methods which conduct tests specific to the setup data are named as <code>localtestXXX()</code> and these will be called
 * from <code>testXXX()</code> methods.  So, subclasses which setup diffferent graph should override this method suitably.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 *
 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedGraph
 * @see edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph
 */
public class DirectedAndSimpleNodeGraphTest1
  extends TestCase {
	/**
	 * This maps node names to nodes.
	 *
	 * @invariant name2node.oclIsKindOf(Map(String, INode))
	 */
	protected final Map name2node = new HashMap();

	/**
	 * This is the graph that will be tested.
	 */
	protected SimpleNodeGraph dg;

	/**
	 * Tests <code>addEdgeFromTo</code> method.
	 */
	public final void testAddEdgeFromTo() {
		localtestAddEdgeFromTo();
	}

	/**
	 * Tests <code>getBackEdges()</code> method.
	 */
	public final void testGetBackEdges() {
		Collection cycles = dg.getCycles();
		Collection backedges = dg.getBackEdges();
		Collection nodes = new HashSet();

		for (Iterator i = backedges.iterator(); i.hasNext();) {
			Pair edge = (Pair) i.next();
			nodes.clear();
			nodes.add(edge.getFirst());
			nodes.add(edge.getSecond());
			// ensure that all nodes in the edge do belong to the graph.
			assertTrue(dg.getNodes().containsAll(nodes));

			boolean flag = false;

			// ensure that the edge belongs to atlease one cycle.
			for (Iterator j = cycles.iterator(); j.hasNext();) {
				Collection cycle = (Collection) j.next();
				flag |= cycle.containsAll(nodes);
			}
			assertTrue(flag);
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
	 * Tests <code>getDAG()</code> method.
	 */
	public final void testGetDAG() {
		Map dag = dg.getDAG();
		Collection backedges = dg.getBackEdges();

		for (Iterator i = dag.keySet().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			Collection succs = (Collection) ((Pair) dag.get(node)).getSecond();

			for (Iterator j = succs.iterator(); j.hasNext();) {
				assertFalse(backedges.contains(new Pair(node, j.next())));
			}
		}
	}

	/**
	 * Tests <code>getHeads()</code> method.
	 */
	public final void testGetHeads() {
		Collection heads = dg.getHeads();

		// ensure there are no predecessors for the head nodes
		for (Iterator i = heads.iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(node.getPredsOf().isEmpty());
		}

		// ensure none of the nodes have a head node as their successor
		for (Iterator i = dg.getNodes().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(CollectionUtils.intersection(node.getSuccsOf(), heads).isEmpty());
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
		Collection sccsTrue = dg.getSCCs(true);
		assertFalse(sccsTrue.isEmpty());

		Collection rest = new ArrayList();
		Collection nodes = new ArrayList();

		for (Iterator i = sccsTrue.iterator(); i.hasNext();) {
			Collection scc = (Collection) i.next();
			nodes.addAll(scc);

			if (scc.size() > 1) {
				for (Iterator j = scc.iterator(); j.hasNext();) {
					INode srcNode = (INode) j.next();
					rest.clear();
					rest.addAll(scc);
					rest.remove(srcNode);

					for (Iterator k = rest.iterator(); k.hasNext();) {
						INode destNode = (INode) k.next();
						assertTrue(dg.isReachable(srcNode, destNode, true) && dg.isReachable(srcNode, destNode, false));
					}
				}
			}
		}

		Collection sccsFalse = dg.getSCCs(false);
		assertFalse(sccsFalse.isEmpty());

		for (Iterator i = sccsFalse.iterator(); i.hasNext();) {
			Collection scc = (Collection) i.next();

			if (scc.size() > 1) {
				for (Iterator j = scc.iterator(); j.hasNext();) {
					INode srcNode = (INode) j.next();
					rest.clear();
					rest.addAll(scc);
					rest.remove(srcNode);

					for (Iterator k = rest.iterator(); k.hasNext();) {
						INode destNode = (INode) k.next();
						assertTrue(dg.isReachable(srcNode, destNode, true) && dg.isReachable(srcNode, destNode, false));
					}
				}
			}
		}
		assertTrue(sccsTrue.containsAll(sccsFalse) && sccsFalse.containsAll(sccsTrue) && dg.getNodes().containsAll(nodes));
	}

	/**
	 * Tests <code>getSpanningSuccs()</code> method.
	 */
	public final void testGetSpanningSuccs() {
		Map spanningSuccs = dg.getSpanningSuccs();
		Set temp = new HashSet();

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

		// ensure that all nodes occur in the spanning tree
		temp.addAll(spanningSuccs.keySet());
		assertTrue(temp.containsAll(dg.getNodes()));
	}

	/**
	 * Tests <code>getTails()</code> method.
	 */
	public final void testGetTails() {
		Collection tails = dg.getTails();

		// ensure none of the tails have a successor
		for (Iterator i = tails.iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(node.getSuccsOf().isEmpty());
		}

		// ensure none of the nodes have a tail node as a predecessor
		for (Iterator i = dg.getNodes().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			assertTrue(CollectionUtils.intersection(node.getPredsOf(), tails).isEmpty());
		}
		localtestGraphGetTails();
	}

	/**
	 * Tests <code>isAncestorOf()</code> method.
	 */
	public final void testIsAncestorOf() {
		localtestIsAncestorOf();
	}

	/**
	 * Tests <code>isReachable()</code> method.
	 */
	public final void testIsReachable() {
		localtestIsReachable();
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
	 * We construct the graph given in the book "Introduction to Algorithms" on page 553.
	 *
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
		dg = new SimpleNodeGraph();

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

	/**
	 * Extracts the predecessors and successors of the given graph into the given maps.
	 * 
	 * @param graph from which to extract information.
	 * @param preds will contain a node to predecessor mapping on return.
	 * @param succs will contain a node to successor mapping on return.
     * @pre graph != null and preds != null and succs != null
     * @post preds.oclIsKindOf(INode, Collection(INode)) and succs.oclIsKindOf(INode, Collection(INode))
	 */
	protected void extractPredSuccCopy(final DirectedGraph graph, final Map preds, final Map succs) {
		for (Iterator i = graph.getNodes().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			preds.put(node, new ArrayList(node.getPredsOf()));
			succs.put(node, new ArrayList(node.getSuccsOf()));
		}
	}

	/**
	 * Tests <code>addEdgeFromTo</code> method on test local graph instance.
	 */
	protected void localtestAddEdgeFromTo() {
		Map preds1 = new HashMap();
		Map succs1 = new HashMap();
		extractPredSuccCopy(dg, preds1, succs1);

		// Add edge from c to h
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("h"));

		assertTrue(((INode) name2node.get("c")).getSuccsOf().contains(name2node.get("h")));
		assertTrue(((INode) name2node.get("h")).getPredsOf().contains(name2node.get("c")));

		Map preds2 = new HashMap();
		Map succs2 = new HashMap();
		extractPredSuccCopy(dg, preds2, succs2);

		for (Iterator i = dg.getNodes().iterator(); i.hasNext();) {
			INode node = (INode) i.next();

			if (name2node.get("h") != node) {
				assertTrue(preds1.get(node).equals(preds2.get(node)));
			}

			if (name2node.get("c") != node) {
				assertTrue(succs1.get(node).equals(succs2.get(node)));
			}
		}

		// Add edge from a to f
		dg.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("f"));

		preds1.clear();
		succs1.clear();
		extractPredSuccCopy(dg, preds1, succs1);

		assertTrue(((INode) name2node.get("a")).getSuccsOf().contains(name2node.get("f")));
		assertTrue(((INode) name2node.get("f")).getPredsOf().contains(name2node.get("a")));

		for (Iterator i = dg.getNodes().iterator(); i.hasNext();) {
			INode node = (INode) i.next();

			if (name2node.get("f") != node) {
				assertTrue(preds1.get(node).equals(preds2.get(node)));
			}

			if (name2node.get("a") != node) {
				assertTrue(succs1.get(node).equals(succs2.get(node)));
			}
		}
	}

	/**
	 * Checks <code>getHeads()</code> on the local graph instance.
	 */
	protected void localtestGetHeads() {
		assertTrue(dg.getHeads().isEmpty());
	}

	/**
	 * Checks <code>getTails()</code> on the local graph instance.
	 */
	protected void localtestGraphGetTails() {
		assertTrue(dg.getTails().isEmpty());
	}

	/**
	 * Tests <code>isAncestorOf()</code> method.
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(dg.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("a")));
		assertTrue(dg.isAncestorOf((INode) name2node.get("h"), (INode) name2node.get("h")));
	}

	/**
	 * Checks <code>isReachable()</code> on the local graph instance.
	 */
	protected void localtestIsReachable() {
		assertTrue(dg.isReachable((INode) name2node.get("a"), (INode) name2node.get("e"), true));
		assertFalse(dg.isReachable((INode) name2node.get("f"), (INode) name2node.get("c"), true));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("a"), false));
		assertFalse(dg.isReachable((INode) name2node.get("c"), (INode) name2node.get("h"), false));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("h"), false));
		assertTrue(dg.isReachable((INode) name2node.get("h"), (INode) name2node.get("h"), true));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2003/09/12 08:07:26  venku
   - documentation.
   Revision 1.9  2003/09/11 12:31:00  venku
   - made ancestral relationship antisymmetric
   - added testcases to test the relationship.
   Revision 1.8  2003/09/11 02:37:30  venku
   - formatting.
   Revision 1.7  2003/09/11 02:37:12  venku
   - added a test case for javac compilation of Divergent04 test.
   - created test suite to test directed and simple node graph.
   Revision 1.6  2003/09/11 01:52:07  venku
   - prenum, postnum, and back edges support has been added.
   - added test case to test the above addition.
   - corrected subtle bugs in test1
   - refactored test1 so that setup local testing can be added by subclasses.
   Revision 1.5  2003/09/09 01:14:29  venku
   - spruced up getSCCs() test for both true and false arguments.
   Revision 1.4  2003/09/02 02:46:39  venku
   - Removed unwanted import.
   Revision 1.3  2003/09/01 20:57:12  venku
   - Deleted getForwardSuccsOf().
   Revision 1.2  2003/08/24 12:35:47  venku
   Documentation changes.
   Revision 1.1  2003/08/24 12:05:34  venku
   Well added unit tests based on JUnit to the StaticAnalyses part of Indus.
 */
