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

import org.apache.commons.collections.Transformer;

/**
 * This interface extends directed graph interface to query for an object associated a node of the graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <N> the type of the nodes of this graph.
 * @param <O1> the type of the objects stored in the nodes of this graph.
 */
public interface IObjectDirectedGraph<N extends IObjectNode<N, O1>, O1>
		extends IDirectedGraph<N> {

	/**
	 * This transformer can be used with Collection Utils to extract the objects from a collection of SimpleNodes. If the
	 * collection has objects of other type then the transformation will insert null into the collection being operated.
	 */
	Transformer OBJECT_EXTRACTOR = new Transformer() {

		/**
		 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
		 */
		public Object transform(final Object input) {
			return ((IObjectNode) input).getObject();
		}
	};

	/**
	 * Returns a node that represents <code>o</code> in this graph.
	 * 
	 * @param o is the object being represented by a node in this graph.
	 * @return the node representing <code>o</code>.
	 * @pre o != null
	 */
	N queryNode(O1 o);
}

// End of File
