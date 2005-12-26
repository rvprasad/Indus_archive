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

import edu.ksu.cis.indus.common.collections.IPredicate;

/**
 * This implementation checks if there is a path from the node representing the given object to the node representing an
 * object in the graph in the direction specified at initialization time. <code>evaluate</code> method will return
 * <code>true</code> only if both objects are represented in the node.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of nodes in the graph processed by this predicate.
 * @param <O> the type of object being represented by the object graph.
 */
public final class GraphReachabilityPredicate<N extends IObjectNode<N, O>, O>
		implements IPredicate<O> {

	/**
	 * The node to which the path leads to.
	 */
	private final N destNode;

	/**
	 * The direction of the path.
	 */
	private final boolean forward;

	/**
	 * The graph in which reachability is calculated.
	 */
	private final IObjectDirectedGraph<N, O> graph;

	/**
	 * Creates a new GraphReachabilityPredicate object.
	 *
	 * @param theDestObject is the object that is represented as a node in <code>theGraph</code>.
	 * @param forwardDir <code>true</code> indicates forward direction (following the edges); <code>false</code> indicates
	 *            backward direction (following the edges in the reverse direction).
	 * @param theGraph of interest.
	 * @pre theGraph != null
	 */
	public GraphReachabilityPredicate(final N theDestObject, final boolean forwardDir,
			final IObjectDirectedGraph<N, O> theGraph) {
		destNode = theDestObject;
		forward = forwardDir;
		graph = theGraph;
	}

	/**
	 * @see edu.ksu.cis.indus.common.collections.IPredicate#evaluate(Object)
	 */
	public <V extends O> boolean evaluate(final V srcObject) {
		final N _srcNode = graph.queryNode(srcObject);
		return destNode != null && _srcNode != null && graph.isReachable(_srcNode, destNode, forward);
	}
}

// End of File
