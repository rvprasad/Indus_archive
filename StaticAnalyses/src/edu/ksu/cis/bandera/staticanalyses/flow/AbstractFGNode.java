
package edu.ksu.cis.bandera.staticanalyses.flow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Flow graph node associated with value associated variants.  This class provides the basic behavior required by the nodes
 * in the flow graph.  It is required that the nodes be able to keep track of the successor nodes and the set of values.
 * However, an implementation may transform the existing values as new values arrive, or change successors as new successors
 * are added.  Hence, all imlementing classes are required to implement <code>FGNode.onNewSucc, FGNode.onNewSuccs,
 * FGNode.onNewValue,</code> and <code>FGNode.onNewValues</code> methods.
 *
 * Created: Tue Jan 22 02:57:07 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractFGNode
  implements FGNode {
	/**
	 * An instance of <code>Logger</code> used for logging purpose.
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractFGNode.class);

	/**
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this
	 * node.  The elements in the set are of type <code>FGNode</code>.
	 *
	 */
	protected final Set succs = new HashSet();

	/**
	 * The set of values contained in this node.  The elements in the set are of type <code>Object</code>.
	 *
	 */
	protected final Set values = new HashSet();

	/**
	 * The worklist associated with the enclosing instance of the framework.  This is required if subclasses will want to
	 * generate new work depending on the new values or new successors that may occur.
	 *
	 */
	protected final WorkList worklist;

	/**
	 * Creates a new <code>AbstractFGNode</code> instance.
	 *
	 * @param worklist The worklist associated with the enclosing instance of the framework.
	 */
	protected AbstractFGNode(WorkList worklist) {
		this.worklist = worklist;
	}

	/**
	 * Adds a successor node to this node.
	 *
	 * @param node the node to be added as successor to this node.
	 */
	public void addSucc(FGNode node) {
		logger.debug("Adding " + node + " as the successor to " + this);
		succs.add(node);
		onNewSucc(node);
	}

	/**
	 * Adds a set of successors to this node.
	 *
	 * @param succs the collection of <code>FGNode</code>s to be added as successors to this node.
	 */
	public void addSuccs(Collection succs) {
		logger.debug("Adding " + succs + " as the successors to " + this);
		this.succs.addAll(succs);
		onNewSuccs(succs);
	}

	/**
	 * Injects a value into the set of values associated with this node.
	 *
	 * @param value the value to be injected in to this node.
	 */
	public void addValue(Object value) {
		logger.debug("Injecting [" + value + "] into " + this);
		values.add(value);
		onNewValue(value);
	}

	/**
	 * Injects a set of values into the set of values associated with this node.
	 *
	 * @param values the collection of <code>Object</code>s to be added as successors to this node.
	 */
	public void addValues(Collection values) {
		logger.debug("Injecting [" + values + "] into " + this);
		this.values.addAll(values);
		onNewValues(values);
	}

	/**
	 * Checks if the given values exists in the set of values associated with this node.
	 *
	 * @param o the value to be checked for existence.
	 * @return <code>true</code> if <code>o</code> exists in the set of values associated with this node; <code>false</code>
	 * otherwise.
	 */
	public boolean containsValue(Object o) {
		return values.contains(o);
	}

	/**
	 * Returns the set of values that exist in this node and not in <code>src</code> node.
	 *
	 * @param src the subtrahend in set difference operation.
	 * @return a <code>Collection</code> containing the values resulting from the set difference between the set of values
	 * associated with this node and <code>src</code>.
	 */
	public final Collection diffValues(edu.ksu.cis.bandera.staticanalyses.flow.FGNode src) {
		Set temp = new HashSet();

		for(Iterator i = values.iterator(); i.hasNext();) {
			Object t = i.next();

			if(!src.getValues().contains(t)) {
				temp.add(t);
			} 
		} 

		return temp;
	}

	/**
	 * Returns the values associated with this node.
	 *
	 * @return a collection of values associated (injected) into this node.
	 */
	public Collection getValues() {
		return Collections.unmodifiableCollection(values);
	}

	/**
	 * Performs specific operation when new successor nodes are added to this node.  It internally calls
	 * <code>onNewSucc</code> for each of the successor.
	 *
	 * @param succs the set of <code>FGNode</code>s being added as successors to this node.
	 */
	public void onNewSuccs(Collection succs) {
		for(Iterator i = succs.iterator(); i.hasNext();) {
			onNewSucc((FGNode)i.next());
		} 
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @param param1 (This is ignored.)
	 * @return (This method raises an exception.)
	 */
	public Object prototype(Object param1) {
		throw new UnsupportedOperationException("prototype(param1) method is not supported.");
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @return (This method raises an exception.)
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype() method is not supported.");
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 */
	public String toString() {
		return "FGNode:" + hashCode() + "\n";
	}
}
