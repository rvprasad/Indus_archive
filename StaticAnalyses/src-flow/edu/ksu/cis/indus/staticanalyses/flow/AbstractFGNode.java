
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

package edu.ksu.cis.indus.staticanalyses.flow;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
	 * An instance of <code>Logger</code> used for logging purpose.
	 */
	private static final Logger LOGGER = LogManager.getLogger(AbstractFGNode.class);

	/**
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this node.
	 * The elements in the set are of type <code>IFGNode</code>.
	 */
	protected final Set succs = new HashSet();

	/**
	 * The set of values contained in this node.  The elements in the set are of type <code>Object</code>.
	 */
	protected final Set values = new HashSet();

	/**
	 * A filter that controls the outflow of values from this node.
	 */
	protected ValueFilter filter;

	/**
	 * The worklist associated with the enclosing instance of the framework.  This is required if subclasses will want to
	 * generate new work depending on the new values or new successors that may occur.
	 */
	protected final WorkList worklist;

	/**
	 * Creates a new <code>AbstractFGNode</code> instance.
	 *
	 * @param worklistToUse The worklist associated with the enclosing instance of the framework.
	 */
	protected AbstractFGNode(WorkList worklistToUse) {
		this.worklist = worklistToUse;
		filter = null;
	}

	/**
	 * Sets the filter on this node.
	 *
	 * @param filterToUse to be used by this node.
	 */
	public void setFilter(ValueFilter filterToUse) {
		this.filter = filterToUse;
	}

	/**
	 * Adds a successor node to this node.
	 *
	 * @param node the node to be added as successor to this node.
	 */
	public void addSucc(IFGNode node) {
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
	 */
	public void addSuccs(Collection successors) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + successors + " as the successors to " + this);
		}
		this.succs.addAll(succs);
		onNewSuccs(succs);
	}

	/**
	 * Injects a value into the set of values associated with this node.
	 *
	 * @param value the value to be injected in to this node.
	 */
	public void addValue(Object value) {
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
	 */
	public void addValues(Collection valuesToInject) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Injecting " + valuesToInject + " into " + this);
		}
		this.values.addAll(valuesToInject);
		onNewValues(valuesToInject);
	}

	/**
	 * Checks if the given values exists in the set of values associated with this node.
	 *
	 * @param o the value to be checked for existence.
	 *
	 * @return <code>true</code> if <code>o</code> exists in the set of values associated with this node; <code>false</code>
	 *            otherwise.
	 */
	public boolean containsValue(Object o) {
		return values.contains(o);
	}

	/**
	 * Returns the set of values that exist in this node and not in <code>src</code> node.
	 *
	 * @param src the subtrahend in set difference operation.
	 *
	 * @return a <code>Collection</code> containing the values resulting from the set difference between the set of values
	 *            associated with this node and <code>src</code>.
	 */
	public final Collection diffValues(edu.ksu.cis.indus.staticanalyses.flow.IFGNode src) {
		Set temp = new HashSet();

		for (Iterator i = values.iterator(); i.hasNext();) {
			Object t = i.next();

			if (!src.getValues().contains(t)) {
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
	 * @param successors the set of <code>IFGNode</code>s being added as successors to this node.
	 */
	public void onNewSuccs(Collection successors) {
		for (Iterator i = successors.iterator(); i.hasNext();) {
			onNewSucc((IFGNode) i.next());
		}
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @param param1 (This is ignored.)
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 *            subclasses.
	 */
	public Object getClone(Object param1) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("prototype(param1) method is not supported.");
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 *            subclasses.
	 */
	public Object getClone() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Parameterless prototype() method is not supported.");
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 */
	public String toString() {
		return "IFGNode:" + hashCode();
	}
}

/*****
 ChangeLog:

$Log$
Revision 0.10  2003/05/22 22:18:32  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
