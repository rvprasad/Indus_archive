
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow;

import java.util.Collection;


//IFGNode.java

/**
 * <p>
 * The super interface to be implemented by all the flow graph node objects used in BFA framework.  It provides the basic
 * methods to add a node into the flow graph and to add values the node.  Although the methods provide to add values, it
 * upto the implementation to process the values as it sees fit.  So, <code>addValue</code> means that a value arrived at
 * this node, and an implementation can create a store the incoming value or derive another value and store the derived
 * value.
 * </p>
 *
 * <p>
 * The main purpose of this class in BFA framework is to represent the summary set, and hence, it provides mostly basic set
 * operations.  However, it is possible to derive complex operations from these basic operations.  There is no support for
 * removing of nodes or values as it is designed to be used in an additive environment.  Moreover, removing of either nodes
 * or values will require other specific processing which is unknown at this level of abstraction.
 * </p>
 *
 * <p>
 * Created: Sun Feb 24 08:36:51 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface IFGNode
  extends IPrototype {
	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param filter DOCUMENT ME!
	 */
	void setFilter(ValueFilter filter);

	/**
	 * <p>
	 * Returns the values in this node.
	 * </p>
	 *
	 * @return the values in this node.
	 */
	Collection getValues();

	/**
	 * <p>
	 * Adds a successor node to this node.
	 * </p>
	 *
	 * @param node the node to be added as a successor node.
	 */
	void addSucc(IFGNode node);

	/**
	 * <p>
	 * Adds a set of successor nodes to this node.
	 * </p>
	 *
	 * @param succs the set of nodes to be added as successor nodes.
	 */
	void addSuccs(Collection succs);

	/**
	 * <p>
	 * Adds a value to this node.  This indicates a value arrived at this node.
	 * </p>
	 *
	 * @param value the value that arrived at this node.
	 */
	void addValue(Object value);

	/**
	 * <p>
	 * Adds a set of values to this node.  This indicates a set of values arrived at this node.
	 * </p>
	 *
	 * @param values the set of values that arrived at this node.
	 */
	void addValues(Collection values);

	/**
	 * <p>
	 * Checks if the given value exists in this node.
	 * </p>
	 *
	 * @param o the value whose presence in this node is to be tested.
	 *
	 * @return <code>true</code> if the node contains the value; <code>false</code> otherwise.
	 */
	boolean containsValue(Object o);

	/**
	 * <p>
	 * Returns a collection containing the set difference between values in this node and the given node.  The values in this
	 * node provide A in A \ B whereas B is provided by the <code>src</code>.
	 * </p>
	 *
	 * @param src the node containing the values of set B in A \ B.
	 *
	 * @return a collection of values in that exist in this node and not in <code>src</code>.
	 */
	Collection diffValues(IFGNode src);

	/**
	 * <p>
	 * Performs a specific action when a successor node is added to this node.  This is a hook method provided to for
	 * convenience of implementation.
	 * </p>
	 *
	 * @param succ the node being added as the successor to this node.
	 */
	void onNewSucc(IFGNode succ);

	/**
	 * <p>
	 * Performs a specific action when a set of successor nodes is added to this node.  This is a hook method provided to for
	 * convenience of implementation.
	 * </p>
	 *
	 * @param succs the collection of nodes being added as the successor to this node.
	 */
	void onNewSuccs(Collection succs);

	/**
	 * <p>
	 * Performs a specific action when a value is added to this node.  This is a hook method provided to for convenience of
	 * implementation.
	 * </p>
	 *
	 * @param value the value being added to this node.
	 */
	void onNewValue(Object value);

	/**
	 * <p>
	 * Performs a specific action when a set of values is added to this node.  This is a hook method provided to for
	 * convenience of implementation.
	 * </p>
	 *
	 * @param values the collection of values being added to this node.
	 */
	void onNewValues(Collection values);
}

/*****
 ChangeLog:

$Log$

*****/
