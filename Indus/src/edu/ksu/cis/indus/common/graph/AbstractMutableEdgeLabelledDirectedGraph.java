
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;


/**
 * This is abstract implementation of mutable edge labelled directed graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AbstractMutableEdgeLabelledDirectedGraph
  extends AbstractEdgeLabelledDirectedGraph {
	/**
	 * Adds an edge between the given nodes with the given label.
	 *
	 * @param src is the source node of the edge to be added.
	 * @param label is the edge label.
	 * @param dest is the destination node of the edge to be added.
	 *
	 * @pre src != null and label != null and dest != null
	 */
	public final void addEdgeFromTo(final INode src, final ILabel label, final INode dest) {
		super.addEdgeFromTo(src, dest);
		CollectionsUtilities.putIntoSetInMap(CollectionsUtilities.getMapFromMap(node2outEdges, src), label, dest);
		CollectionsUtilities.putIntoSetInMap(CollectionsUtilities.getMapFromMap(node2inEdges, dest), label, src);
	}
}

// End of File
