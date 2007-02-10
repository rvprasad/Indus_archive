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
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.HashSet;

/**
 * This is a simple concrete implementation of <code>INode</code> interface.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the type of the object stored in this node.
 */
public class SimpleNode<T>
		extends MutableNode<SimpleNode<T>>
		implements IObjectNode<SimpleNode<T>, T> {

	/**
	 * The object being represetned by this node.
	 */
	@Immutable final T object;

	/**
	 * Creates a new SimpleNode object.
	 * 
	 * @param o is the object to be represented by this node.
	 */
	SimpleNode(@Immutable final T o) {
		super(new HashSet<SimpleNode<T>>(), new HashSet<SimpleNode<T>>());
		this.object = o;
	}

	/**
	 * Retrieves the associated object.
	 * 
	 * @return the associated object.
	 */
	@Functional public T getObject() {
		return object;
	}

	/**
	 * Returns the stringized representation of this object.
	 * 
	 * @return stringized representation.
	 */
	@NonNull @Functional @Override public String toString() {
		return object + "";
	}
}
