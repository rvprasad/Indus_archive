
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;


/**
 * This is an abstract implementation of edge labelled directed graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractEdgeLabelledDirectedGraph
  extends SimpleNodeGraph
  implements IEdgeLabelledDirectedGraph {
	/** 
	 * This maps nodes to maps that map labels to nodes that can reachable the key node via an edge with the key label.
	 *
	 * @pre node2inEdges.oclIsKindOf(Map(INode, Map(ILabel, Collection(INode))))
	 */
	final Map node2inEdges = new HashMap();

	/** 
	 * This maps nodes to maps that map labels to nodes reachable from the key node via an edge with the key label.
	 *
	 * @pre node2inEdges.oclIsKindOf(Map(INode, Map(ILabel, Collection(INode))))
	 */
	final Map node2outEdges = new HashMap();

	/**
	 * @see IEdgeLabelledDirectedGraph#getDestOfOutgoingEdgesLabelled(edu.ksu.cis.indus.common.graph.INode, ILabel)
	 */
	public final Collection getDestOfOutgoingEdgesLabelled(final INode node, final ILabel label) {
		return (Collection) MapUtils.getObject((Map) MapUtils.getObject(node2outEdges, node, Collections.EMPTY_MAP), label,
			Collections.EMPTY_SET);
	}

	/**
	 * Retrieve the predecessors of the given node.
	 *
	 * @param node of interest.
	 *
	 * @return a map of labels and the nodes that can reach this node via edges labelled by the key label.
	 *
	 * @pre node != null
	 * @post result != null and result.oclIsKindOf(Map(ILabel, Collection(INode)))
	 */
	public Map getPredsOf(final INode node) {
		return Collections.unmodifiableMap((Map) MapUtils.getObject(node2inEdges, node, Collections.EMPTY_MAP));
	}

	/**
	 * Retrieve the successors of the given node.
	 *
	 * @param node of interest.
	 *
	 * @return a map of labels and the nodes that can be reached from this node via edges labelled by the key label.
	 *
	 * @pre node != null
	 * @post result != null and result.oclIsKindOf(Map(ILabel, Collection(INode)))
	 */
	public Map getSuccsOf(final INode node) {
		return Collections.unmodifiableMap((Map) MapUtils.getObject(node2outEdges, node, Collections.EMPTY_MAP));
	}

	/**
	 * @see IEdgeLabelledDirectedGraph#hasOutgoingEdgeLabelled(edu.ksu.cis.indus.common.graph.INode, ILabel)
	 */
	public final boolean hasOutgoingEdgeLabelled(final INode node, final ILabel label) {
		return ((Map) MapUtils.getObject(node2outEdges, node, Collections.EMPTY_MAP)).containsKey(label);
	}
}

// End of File
