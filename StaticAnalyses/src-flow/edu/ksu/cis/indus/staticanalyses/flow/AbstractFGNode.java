
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Flow graph node associated with value associated variants.  This class provides the basic behavior required by the nodes
 * in the flow graph.  It is required that the nodes be able to keep track of the successor nodes and the set of values.
 * However, an implementation may transform the existing values as new values arrive, or change successors as new successors
 * are added.  Hence, all imlementing classes are required to implement <code>IFGNode.onNewSucc, IFGNode.onNewSuccs,
 * IFGNode.onNewValue,</code> and <code>IFGNode.onNewValues</code> methods. Created: Tue Jan 22 02:57:07 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractFGNode
  implements IFGNode {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractFGNode.class);

	/**
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this node.
	 * The elements in the set are of type <code>IFGNode</code>.
	 *
	 * @invariant succs != null
	 */
	protected final Set succs = new HashSet();

	/**
	 * The set of values contained in this node.  The elements in the set are of type <code>Object</code>.
	 *
	 * @invariant values != null
	 */
	protected final Set values = new HashSet();

	/**
	 * A filter that controls the outflow of values from this node.
	 */
	protected IValueFilter filter;

	/**
	 * The worklist associated with the enclosing instance of the framework.  This is required if subclasses will want to
	 * generate new work depending on the new values or new successors that may occur.
	 *
	 * @invariant worklist != null
	 */
	protected final WorkList worklist;

	/**
	 * Creates a new <code>AbstractFGNode</code> instance.
	 *
	 * @param worklistToUse The worklist associated with the enclosing instance of the framework.
	 *
	 * @pre worklistToUse != null
	 */
	protected AbstractFGNode(final WorkList worklistToUse) {
		this.worklist = worklistToUse;
		filter = null;
	}

	/**
	 * Sets the filter on this node.
	 *
	 * @param filterToUse to be used by this node.
	 */
	public void setFilter(final IValueFilter filterToUse) {
		this.filter = filterToUse;
	}

	/**
	 * Adds a successor node to this node.
	 *
	 * @param node the node to be added as successor to this node.
	 *
	 * @pre node != null
	 */
	public void addSucc(final IFGNode node) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + node + " as the successor to " + this);
		}
		succs.add(node);
		onNewSucc(node);
	}

	/**
	 * Adds a set of successors to this node.
	 *
	 * @param successors the collection of <code>IFGNode</code>s to be added as successors to this node.
	 *
	 * @pre successors != null
	 */
	public void addSuccs(final Collection successors) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + successors + " as the successors to " + this);
		}
		succs.addAll(successors);
		onNewSuccs(successors);
	}

	/**
	 * Injects a value into the set of values associated with this node.
	 *
	 * @param value the value to be injected in to this node.
	 *
	 * @pre value != null
	 */
	public void addValue(final Object value) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Injecting " + value + " into " + this);
		}
		values.add(value);
		onNewValue(value);
	}

	/**
	 * Injects a set of values into the set of values associated with this node.
	 *
	 * @param valuesToInject the collection of <code>Object</code>s to be added as successors to this node.
	 *
	 * @pre valuesToInject != null
	 */
	public void addValues(final Collection valuesToInject) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Injecting " + valuesToInject + " into " + this);
		}
		values.addAll(valuesToInject);
		onNewValues(valuesToInject);
	}

	/**
	 * Checks if the given values exists in the set of values associated with this node.
	 *
	 * @param o the value to be checked for existence.
	 *
	 * @return <code>true</code> if <code>o</code> exists in the set of values associated with this node; <code>false</code>
	 * 		   otherwise.
	 *
	 * @pre o != null
	 */
	public boolean containsValue(final Object o) {
		return values.contains(o);
	}

	/**
	 * Returns the set of values that exist in this node and not in <code>src</code> node.
	 *
	 * @param src the subtrahend in set difference operation.
	 *
	 * @return a <code>Collection</code> containing the values resulting from the set difference between the set of values
	 * 		   associated with this node and <code>src</code>.
	 *
	 * @pre src != null
	 * @post result != null
	 */
	public final Collection diffValues(final IFGNode src) {
		Set temp = new HashSet();
		Collection srcValues = src.getValues();

		for (Iterator i = values.iterator(); i.hasNext();) {
			Object t = i.next();

			if (!srcValues.contains(t)) {
				temp.add(t);
			}
		}

		return temp.isEmpty() ? Collections.EMPTY_SET
							  : temp;
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 * 		   subclasses.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype() method is not supported.");
	}

	/**
	 * Returns the values associated with this node.
	 *
	 * @return a collection of values associated (injected) into this node.
	 *
	 * @post result != null
	 */
	public Collection getValues() {
		return Collections.unmodifiableCollection(values);
	}

	/**
	 * Performs a specific action when a successor node is added to this node.  This is a template method to be provided by
	 * subclasses.
	 *
	 * @param succ the node being added as the successor to this node.
	 *
	 * @pre succ != null
	 */
	public abstract void onNewSucc(final IFGNode succ);

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @param param <i>ignored</i>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 * 		   subclasses.
	 */
	public Object getClone(final Object param) {
		throw new UnsupportedOperationException("prototype(param1) method is not supported.");
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 *
	 * @post result != null
	 */
	public String toString() {
		return "IFGNode:" + hashCode();
	}

	/**
	 * Performs specific operation when new successor nodes are added to this node.  This is a template method that can be
	 * overridden by subclasses.
	 *
	 * @param successors the set of <code>IFGNode</code>s being added as successors to this node.
	 *
	 * @pre successors != null
	 */
	protected void onNewSuccs(final Collection successors) {
		for (Iterator i = successors.iterator(); i.hasNext();) {
			onNewSucc((IFGNode) i.next());
		}
	}

	/**
	 * Performs a specific action when a value is added to this node.  This is a template method to be provided by
	 * subclasses.
	 *
	 * @param value the value being added to this node.
	 *
	 * @pre value != null
	 */
	protected abstract void onNewValue(final Object value);

	/**
	 * Performs a specific action when a set of values is added to this node.  This is a template method to be provided by
	 * subclasses.
	 *
	 * @param newValues the collection of values being added to this node.
	 *
	 * @pre newValues != null
	 */
	protected abstract void onNewValues(final Collection newValues);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/26 16:53:34  venku
   diffValues() used to get values() on src inside the loop.  However,
   this was loop invariant, hence, has been hoisted outside the loop.
   Revision 1.3  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
