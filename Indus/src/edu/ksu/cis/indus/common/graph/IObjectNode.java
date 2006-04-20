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

/**
 * This interface facilitates the access to the object in a node.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the subtype of this type.
 * @param <O> the type of the objects stored in the nodes of this graph.
 */
public interface IObjectNode<T extends IObjectNode<T, O>, O>
		extends INode<T> {

	/**
	 * Retrieves the object associated with the node.
	 * 
	 * @return the node.
	 */
	@Functional O getObject();
}
