
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

import edu.ksu.cis.indus.common.graph.IDirectedGraph.INode;

import org.apache.commons.collections.Predicate;


/**
 * This implementation checks if there is a path from the node representing the given object to the node representing an
 * object in the graph in the direction specified at initialization time.  <code>evaluate</code> method will return
 * <code>true</code> only if both objects are represented in the node.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class GraphReachabilityPredicate
  implements Predicate {
	/** 
	 * The node to which the path leads to.
	 */
	private final INode destNode;

	/** 
	 * The graph in which reachability is calculated.
	 */
	private final IObjectDirectedGraph graph;

	/** 
	 * The direction of the path.
	 */
	private final boolean forward;

	/**
	 * Creates a new GraphReachabilityPredicate object.
	 *
	 * @param theDestObject is the object that is represented as a node in <code>theGraph</code>.
	 * @param forwardDir <code>true</code> indicates forward direction (following the edges); <code>false</code> indicates
	 * 		  backward direction (following the edges in the reverse direction).
	 * @param theGraph of interest.
	 *
	 * @pre theGraph != null
	 */
	public GraphReachabilityPredicate(final Object theDestObject, final boolean forwardDir,
		final IObjectDirectedGraph theGraph) {
		destNode = theGraph.queryNode(theDestObject);
		forward = forwardDir;
		graph = theGraph;
	}

	/**
	 * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
	 */
	public boolean evaluate(final Object srcObject) {
		final INode _srcNode = graph.queryNode(srcObject);
		return destNode != null && _srcNode != null && graph.isReachable(_srcNode, destNode, forward);
	}
}

// End of File
