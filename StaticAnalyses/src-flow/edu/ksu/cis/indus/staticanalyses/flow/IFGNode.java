
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;

import java.util.Collection;


/**
 * The super interface to be implemented by all the flow graph node objects used in BFA framework.  It provides the basic
 * methods to add a node into the flow graph and to add values the node.  Although the methods provide to add values, it
 * upto the implementation to process the values as it sees fit.  So, <code>addValue</code> means that a value arrived at
 * this node, and an implementation can create a store the incoming value or derive another value and store the derived
 * value.
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
	 * Sets a filter object which will filter the values flowing out of this graph.
	 *
	 * @param filter object to be used.
	 *
	 * @throws UnsupportedOperationException if the implementation does not provide this feature.
	 *
	 * @pre filter != null
	 */
	void setFilter(IValueFilter filter);

	/**
	 * Returns the values in this node.
	 *
	 * @return the values in this node.
	 */
	Collection getValues();

	/**
	 * Adds a successor node to this node.
	 *
	 * @param node the node to be added as a successor node.
	 */
	void addSucc(IFGNode node);

	/**
	 * Adds a set of successor nodes to this node.
	 *
	 * @param succs the set of nodes to be added as successor nodes.
	 */
	void addSuccs(Collection succs);

	/**
	 * Adds a value to this node.  This indicates a value arrived at this node.
	 *
	 * @param value the value that arrived at this node.
	 *
	 * @pre values != null
	 */
	void addValue(Object value);

	/**
	 * Adds a set of values to this node.  This indicates a set of values arrived at this node.
	 *
	 * @param values the set of values that arrived at this node.
	 *
	 * @pre values != null
	 */
	void addValues(Collection values);

	/**
	 * Checks if the given value exists in this node.
	 *
	 * @param o the value whose presence in this node is to be tested.
	 *
	 * @return <code>true</code> if the node contains the value; <code>false</code> otherwise.
	 *
	 * @pre o != null
	 */
	boolean containsValue(Object o);

	/**
	 * Returns a collection containing the set difference between values in this node and the given node.  The values in this
	 * node provide A in A \ B whereas B is provided by the <code>src</code>.
	 *
	 * @param src the node containing the values of set B in A \ B.
	 *
	 * @return a collection of values in that exist in this node and not in <code>src</code>.
	 *
	 * @pre src != null
	 */
	Collection diffValues(IFGNode src);
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.2  2003/08/12 18:39:56  venku
   Ripple effect of moving IPrototype to Indus.
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 1.1  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
