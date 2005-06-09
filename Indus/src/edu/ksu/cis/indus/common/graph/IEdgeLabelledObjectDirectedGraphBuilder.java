
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


/**
 * This interface is used to build edge-labelled directed object graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IEdgeLabelledObjectDirectedGraphBuilder {
	/**
	 * Adds a labelled edge between 2 nodes.
	 *
	 * @param src is the source node.
	 * @param label is the edge label.
	 * @param dest is the destination node.
	 *
	 * @pre src != null and dest != null
	 */
	void addEdgeFromTo(final Object src, final Object label, final Object dest);

	/**
	 * Adds a labelled edge between each of the source nodes and the destination node
	 *
	 * @param sources are the source nodes.
	 * @param label is the edge label.
	 * @param dest is the destination node.
	 *
	 * @pre src != null and dest != null
	 */
	void addEdgeFromTo(final Collection sources, final Object label, final Object dest);

	/**
	 * Adds a labelled edge between the source node and each of the destination nodes.
	 *
	 * @param src is the source node.
	 * @param label is the edge label.
	 * @param destinations are the destination nodes.
	 *
	 * @pre src != null and dest != null
	 */
	void addEdgeFromTo(final Object src, final Object label, final Collection destinations);
}

// End of File
