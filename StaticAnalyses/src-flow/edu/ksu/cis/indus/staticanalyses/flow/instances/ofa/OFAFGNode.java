
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.WorkList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This class represents the flow graph node that accumulates objects as their entities would refer to objects at run-time.
 * This is an Object-flow analysis specific implementation.
 * 
 * <p>
 * Created: Thu Jan 31 00:42:34 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class OFAFGNode
  extends AbstractFGNode {
	/**
	 * Creates a new <code>OFAFGNode</code> instance.
	 *
	 * @param wl the worklist associated with the instance of the framework within which this node exists.
	 *
	 * @pre wl != null
	 */
	public OFAFGNode(final WorkList wl) {
		super(wl);
	}

	/**
	 * This class represents a peice of work to inject a set of values into a flow graph node.
	 */
	class SendValuesWork
	  extends AbstractWork {
		/**
		 * Creates a new <code>SendValuesWork</code> instance.
		 *
		 * @param toNode the node into which the values need to be injected.
		 * @param valuesToBeSent a collection containing the values to be injected.
		 *
		 * @pre toNode != null and valuesToBeSent != null
		 */
		SendValuesWork(final IFGNode toNode, final Collection valuesToBeSent) {
			super(toNode, valuesToBeSent);
		}

		/**
		 * Creates a new <code>SendValuesWork</code> instance.
		 *
		 * @param toNode the node into which the values need to be injected.
		 * @param valueToBeSent the value to be injected.
		 *
		 * @pre toNode != null and valueToBeSent != null
		 */
		SendValuesWork(final IFGNode toNode, final Object valueToBeSent) {
			super(toNode, new HashSet());
			addValue(valueToBeSent);
		}

		/**
		 * Injects the values into the associated node.
		 */
		public final void execute() {
			node.addValues(this.values);
		}
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the <code>WorkList</code> to be passed to the constructor of this class.
	 *
	 * @return a new instance of this class parameterized by <code>o</code>.
	 *
	 * @pre o != null
	 * @post result != null and result.oclIsKindOf(OFAFGNode)
	 */
	public Object getClone(final Object o) {
		return new OFAFGNode((WorkList) o);
	}

	/**
	 * Adds a new work to the worklist to propogate the values in this node to <code>succ</code>.  Only the difference values
	 * are propogated.
	 *
	 * @param succ the successor node that was added to this node.
	 *
	 * @pre succ != null
	 */
	public void onNewSucc(final IFGNode succ) {
		Collection temp = diffValues(succ);

		if (filter != null) {
			temp = filter.filter(temp);
		}

		if (!temp.isEmpty()) {
			worklist.addWork(new SendValuesWork(succ, temp));
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>value</code> in this node to it's successor nodes.
	 *
	 * @param value the value to be propogated to the successor node.
	 *
	 * @pre value != null
	 */
	public void onNewValue(final Object value) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			IFGNode succ = (IFGNode) i.next();

			if (!succ.getValues().contains(value) && !filter.filter(value)) {
				worklist.addWork(new SendValuesWork(succ, value));
			}
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>values</code> in this node to it's successor nodes.
	 *
	 * @param arrivingValues the values to be propogated to the successor node.  The collection contains object of type
	 * 		  <code>Object</code>.
	 *
	 * @pre arrivingValues != null
	 */
	public void onNewValues(final Collection arrivingValues) {
		Collection temp = arrivingValues;

		if (filter != null) {
			temp = filter.filter(temp);
		}

		for (Iterator i = succs.iterator(); i.hasNext();) {
			IFGNode succ = (IFGNode) i.next();

			if (!diffValues(succ).isEmpty()) {
				worklist.addWork(new SendValuesWork(succ, temp));
			}
		}
	}
}

/*
   ChangeLog:

   $Log$

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
