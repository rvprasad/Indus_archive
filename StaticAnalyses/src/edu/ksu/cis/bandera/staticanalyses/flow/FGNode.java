
package edu.ksu.cis.bandera.staticanalyses.flow;

import java.util.Collection;


//FGNode.java

/**
 * <p>The super interface to be implemented by all the flow graph node objects used in BFA framework.  It provides the basic
 * methods to add a node into the flow graph and to add values the node.  Although the methods provide to add values, it upto
 * the implementation to process the values as it sees fit.  So, <code>addValue</code> means that a value arrived at this
 * node, and an implementation can create a store the incoming value or derive another value and store the derived value.</p>
 *
 * <p>The main purpose of this class in BFA framework is to represent the summary set, and hence, it provides mostly basic set
 * operations.  However, it is possible to derive complex operations from these basic operations.  There is no support for
 * removing of nodes or values as it is designed to be used in an additive environment.  Moreover, removing of either nodes or
 * values will require other specific processing which is unknown at this level of abstraction.</p>
 *
 * <p>Created: Sun Feb 24 08:36:51 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface FGNode
  extends Prototype {
	/**
	 * <p>Returns the values in this node.</p>
	 *
	 * @return the values in this node.
	 */
	public Collection getValues();

	/**
	 * <p>Adds a successor node to this node.</p>
	 *
	 * @param node the node to be added as a successor node.
	 */
	public void addSucc(FGNode node);

	/**
	 * <p>Adds a set of successor nodes to this node. </p>
	 *
	 * @param succs the set of nodes to be added as successor nodes.
	 */
	public void addSuccs(Collection succs);

	/**
	 * <p>Adds a value to this node.  This indicates a value arrived at this node.</p>
	 *
	 * @param value the value that arrived at this node.
	 */
	public void addValue(Object value);

	/**
	 * <p>Adds a set of values to this node.  This indicates a set of values arrived at this node.</p>
	 *
	 * @param values the set of values that arrived at this node.
	 */
	public void addValues(Collection values);

	/**
	 * <p>Checks if the given value exists in this node.</p>
	 *
	 * @param o the value whose presence in this node is to be tested.
	 * @return <code>true</code> if the node contains the value; <code>false</code> otherwise.
	 */
	public boolean containsValue(Object o);

	/**
	 * <p>Returns a collection containing the set difference between values in this node and the given node.  The values in
	 * this node provide A in A \ B whereas B is provided by the <code>src</code>.</p>
	 *
	 * @param src the node containing the values of set B in A \ B.
	 * @return a collection of values in that exist in this node and not in <code>src</code>.
	 */
	public Collection diffValues(FGNode src);

	/**
	 * <p>Performs a specific action when a successor node is added to this node.  This is a hook method provided to for
	 * convenience of implementation.</p>
	 *
	 * @param succ the node being added as the successor to this node.
	 */
	public void onNewSucc(FGNode succ);

	/**
	 * <p>Performs a specific action when a set of successor nodes is added to this node.  This is a hook method provided to
	 * for convenience of implementation.</p>
	 *
	 * @param succs the collection of nodes being added as the successor to this node.
	 */
	public void onNewSuccs(Collection succs);

	/**
	 * <p>Performs a specific action when a value is added to this node.  This is a hook method provided to for convenience of
	 * implementation.</p>
	 *
	 * @param value the value being added to this node.
	 */
	public void onNewValue(Object value);

	/**
	 * <p>Performs a specific action when a set of values is added to this node.  This is a hook method provided to for
	 * convenience of implementation.</p>
	 *
	 * @param values the collection of values being added to this node.
	 */
	public void onNewValues(Collection values);
} // FGNode
