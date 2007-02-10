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
	@Immutable final O object;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param o is the object to be represented by the node.
	 */
	SimpleEdgeLabelledNode(@Immutable final O o) {
		super(new HashSet<SimpleEdgeLabelledNode<O>>(), new HashSet<SimpleEdgeLabelledNode<O>>());
		object = o;
	}

	/**
	 * Retrieves the associated object.
	 * 
	 * @return the associated object.
	 */
	@Functional public O getObject() {
		return object;
	}

	/**
	 * Returns the stringized representation of this object.
	 * 
	 * @return stringized representation.
	 */
	@Functional @NonNull @Override public String toString() {
		return object + "";
	}
}
