
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;

import java.util.Collection;


/**
 * The super interface to be implemented by all the flow graph node objects used in FA framework.  It provides the basic
 * methods to add a node into the flow graph and to add values the node.  Although the methods provide to add values, it
 * upto the implementation to process the values as it sees fit.  So, <code>addValue</code> means that a value arrived at
 * this node, and an implementation can create a store the incoming value or derive another value and store the derived
 * value.
 * 
 * <p>
 * The main purpose of this class in FA framework is to represent the summary set, and hence, it provides mostly basic set
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
   Revision 1.5  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.2  2003/08/12 18:39:56  venku
   Ripple effect of moving IPrototype to Indus.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.1  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
