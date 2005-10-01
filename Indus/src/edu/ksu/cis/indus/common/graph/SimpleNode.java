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
 * This is a simple concrete implementation of <code>INode</code> interface.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> the type of the object stored in this node.
 */
public class SimpleNode<T>
		extends MutableNode<SimpleNode<T>>
		implements IObjectNode<SimpleNode<T>, T> {

	/**
	 * The object being represetned by this node.
	 */
	final T object;

	/**
	 * Creates a new SimpleNode object.
	 * 
	 * @param o is the object to be represented by this node.
	 */
	SimpleNode(final T o) {
		super(new HashSet<SimpleNode<T>>(), new HashSet<SimpleNode<T>>());
		this.object = o;
	}

	/**
	 * Retrieves the associated object.
	 * 
	 * @return the associated object.
	 */
	public T getObject() {
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

