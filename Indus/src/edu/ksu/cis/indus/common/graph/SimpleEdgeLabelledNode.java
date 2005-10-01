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

import java.util.HashSet;

/**
 * This is an implementation of an object node to be used in mutable edge-labelled graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <O> the object stored in this node.
 */
public class SimpleEdgeLabelledNode<O>
		extends MutableEdgeLabelledNode<SimpleEdgeLabelledNode<O>>
		implements IObjectNode<SimpleEdgeLabelledNode<O>, O> {

	/**
	 * The object being represetned by this node.
	 */
	final O object;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param o is the object to be represented by the node.
	 */
	SimpleEdgeLabelledNode(final O o) {
		super(new HashSet<SimpleEdgeLabelledNode<O>>(), new HashSet<SimpleEdgeLabelledNode<O>>());
		object = o;
	}

	/**
	 * Retrieves the associated object.
	 * 
	 * @return the associated object.
	 */
	public O getObject() {
		return object;
	}

	/**
	 * Returns the stringized representation of this object.
	 * 
	 * @return stringized representation.
	 * @post result != null
	 */
	@Override public String toString() {
		return object + "";
	}
}
