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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.common.collections.ITransformer;

/**
 * This interface extends directed graph interface to query for an object associated a node of the graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of the nodes of this graph.
 * @param <O> the type of the objects stored in the nodes of this graph.
 */
public interface IObjectDirectedGraph<N extends IObjectNode<N, O>, O>
		extends IDirectedGraph<N> {

	/**
	 * Provides the object extractor that can be used to extract objects embedded in the nodes of this graph.
	 * 
	 * @return an object extractor.
	 */
	@NonNull ITransformer<N, O> getObjectExtractor();

	/**
	 * Returns a node that represents <code>o</code> in this graph.
	 * 
	 * @param o is the object being represented by a node in this graph.
	 * @return the node representing <code>o</code>.
	 */
	@Functional N queryNode(@NonNull O o);
}

// End of File
