
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
import edu.ksu.cis.indus.common.structures.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;


/**
 * This class tests <code>DirectedGraph</code> and <code>SimpleNodeGraph</code> classes that exists in the same package.
 * Methods which conduct tests specific to the setup data are named as <code>localtestXXX()</code> and these will be called
 * from <code>testXXX()</code> methods.  So, subclasses which setup diffferent graph should override this method suitably.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 *
 * @see edu.ksu.cis.indus.common.graph.DirectedGraph
 * @see edu.ksu.cis.indus.common.graph.SimpleNodeGraph
 */
public class DirectedAndSimpleNodeGraphTest
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
		final Collection _cycles = dg.getCycles();
		final Collection _backedges = dg.getBackEdges();
		final Collection _nodes = new HashSet();

		for (final Iterator _i = _backedges.iterator(); _i.hasNext();) {
			final Pair _edge = (Pair) _i.next();
			_nodes.clear();
			_nodes.add(_edge.getFirst());
			_nodes.add(_edge.getSecond());
			// ensure that all nodes in the edge do belong to the graph.
			assertTrue(dg.getNodes().containsAll(_nodes));

			boolean _flag = false;

			// ensure that the edge belongs to atlease one cycle.
			for (final Iterator _j = _cycles.iterator(); _j.hasNext();) {
				final Collection _cycle = (Collection) _j.next();
				_flag |= _cycle.containsAll(_nodes);
			}
			assertTrue(_flag);
		}
	}

	/**
	 * Tests <code>getCycles()</code> method.
	 */
	public final void testGetCycles() {
		final Collection _cycles = dg.getCycles();
		assertFalse(_cycles.isEmpty());

		for (final Iterator _i = _cycles.iterator(); _i.hasNext();) {
			final Collection _cycle = (Collection) _i.next();
			final INode _node = (INode) _cycle.iterator().next();
			assertTrue(dg.isReachable(_node, _node, true));
		}
	}

	/**
	 * Tests <code>getDAG()</code> method.
	 */
	public final void testGetDAG() {
		final Map _dag = dg.getDAG();
		final Collection _backedges = dg.getBackEdges();

		for (final Iterator _i = _dag.keySet().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			final Collection _succs = (Collection) ((Pair) _dag.get(_node)).getSecond();

			for (final Iterator _j = _succs.iterator(); _j.hasNext();) {
				assertFalse(_backedges.contains(new Pair(_node, _j.next())));
			}
		}
	}

	/**
	 * Tests <code>getHeads()</code> method.
	 */
	public final void testGetHeads() {
		final Collection _heads = dg.getHeads();

		// ensure there are no predecessors for the head nodes
		for (final Iterator _i = _heads.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			assertTrue(_node.getPredsOf().isEmpty());
		}

		// ensure none of the nodes have a head node as their successor
		for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			assertTrue(CollectionUtils.intersection(_node.getSuccsOf(), _heads).isEmpty());
		}
	}

	/**
	 * Tests <code>getNode()</code> method.
	 */
	public final void testGetNode() {
		for (final Iterator _i = name2node.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final INode _node = dg.getNode(_entry.getKey());
			assertTrue(_node.equals(_entry.getValue()));
		}
	}

	/**
	 * Tests <code>getNodes()</code> method.
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
	 * Tests <code>getSCCs()</code> method.
	 */
	public final void testGetSCCs() {
		final Collection _sccsTrue = dg.getSCCs(true);
		assertFalse(_sccsTrue.isEmpty());

		checkSCCReachability(_sccsTrue);

		final Collection _sccsFalse = dg.getSCCs(false);
		assertFalse(_sccsFalse.isEmpty());

		checkSCCReachability(_sccsFalse);
		assertTrue(_sccsTrue.containsAll(_sccsFalse));
		assertTrue(_sccsFalse.containsAll(_sccsTrue));
	}

	/**
	 * Tests <code>getSpanningSuccs()</code> method.
	 */
	public final void testGetSpanningSuccs() {
		final Map _spanningSuccs = dg.getSpanningSuccs();
		final Set _temp = new HashSet();

		for (final Iterator _i = _spanningSuccs.values().iterator(); _i.hasNext();) {
			final Collection _succs = (Collection) _i.next();
			_temp.addAll(_succs);
		}

		// ensure all tree nodes are among the nodes of this graph.
		assertTrue(dg.getNodes().containsAll(_spanningSuccs.keySet()));
		assertTrue(dg.getNodes().containsAll(_temp));

		// ensure no node appears as child of two different parents in the spanning tree.
		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			assertTrue(CollectionUtils.cardinality(_node, _temp) == 1);
		}

		// ensure that all nodes occur in the spanning tree
		_temp.addAll(_spanningSuccs.keySet());
		assertTrue(_temp.containsAll(dg.getNodes()));
	}

	/**
	 * Tests <code>getTails()</code> method.
	 */
	public final void testGetTails() {
		final Collection _tails = dg.getTails();

		// ensure none of the tails have a successor
		for (final Iterator _i = _tails.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			assertTrue(_node.getSuccsOf().isEmpty());
		}

		// ensure none of the nodes have a tail node as a predecessor
		for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			assertTrue(CollectionUtils.intersection(_node.getPredsOf(), _tails).isEmpty());
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
		final List _sorted = dg.performTopologicalSort(true);
		final List _nodes = dg.getNodes();
		assertTrue(_nodes.containsAll(_sorted) && _sorted.containsAll(_nodes));

		if (dg.getCycles().isEmpty()) {
			for (final Iterator _i = _sorted.iterator(); _i.hasNext();) {
				final INode _src = (INode) _i.next();
				final Collection _t = _sorted.subList(_sorted.indexOf(_src), _sorted.size() - 1);

				for (final Iterator _j = _t.iterator(); _j.hasNext();) {
					final INode _dest = (INode) _j.next();
					assertFalse(dg.isReachable(_dest, _src, true));
				}
			}
		}
	}

	/**
	 * Tests <code>size()</code> method.
	 */
	public void testSize() {
		assertEquals(name2node.values().size(), dg.getNodes().size());
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
	 *
	 * @pre graph != null and preds != null and succs != null
	 * @post preds.oclIsKindOf(INode, Collection(INode)) and succs.oclIsKindOf(INode, Collection(INode))
	 */
	protected final void extractPredSuccCopy(final IDirectedGraph graph, final Map preds, final Map succs) {
		for (final Iterator _i = graph.getNodes().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			preds.put(_node, new ArrayList(_node.getPredsOf()));
			succs.put(_node, new ArrayList(_node.getSuccsOf()));
		}
	}

	/**
	 * Tests <code>addEdgeFromTo</code> method on test local graph instance.
	 */
	protected void localtestAddEdgeFromTo() {
		final Map _preds1 = new HashMap();
		final Map _succs1 = new HashMap();
		extractPredSuccCopy(dg, _preds1, _succs1);

		// Add edge from c to h
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("h"));

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
		dg.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("f"));

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

	/**
	 * Checks for the reachability of nodes in the SCCs.
	 *
	 * @param sccs in which reachability should be checked.
	 *
	 * @pre sccs != null
	 */
	private void checkSCCReachability(final Collection sccs) {
		final Collection _nodes = new ArrayList();
		final Collection _rest = new ArrayList();

		for (final Iterator _i = sccs.iterator(); _i.hasNext();) {
			final Collection _scc = (Collection) _i.next();
			_nodes.addAll(_scc);

			if (_scc.size() > 1) {
				for (final Iterator _j = _scc.iterator(); _j.hasNext();) {
					final INode _srcNode = (INode) _j.next();
					_rest.addAll(_scc);
					_rest.remove(_srcNode);

					for (final Iterator _k = _rest.iterator(); _k.hasNext();) {
						final INode _destNode = (INode) _k.next();
						assertTrue(dg.isReachable(_srcNode, _destNode, true) && dg.isReachable(_srcNode, _destNode, false));
					}
				}
			}
		}
		assertTrue(dg.getNodes().containsAll(_nodes));
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
   Revision 1.3  2003/12/07 04:49:11  venku
   - exposed a couple of methods as they needed to be overridden
     for call graph testing.
   Revision 1.2  2003/12/02 09:42:34  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.1  2003/11/10 03:40:50  venku
   - renamed DirectedAndSimpleNodeGraphTest1 to
     DirectedAndSimpleNodeGraphTest.
   Revision 1.12  2003/09/28 23:19:36  venku
 *** empty log message ***
           Revision 1.11  2003/09/14 23:20:48  venku
           - added support to retrieve a DAG from a graph.
           - removed support to extract preds/succs as a bitst from the graph.
           - added/removed tests for the above changes.
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
