/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
