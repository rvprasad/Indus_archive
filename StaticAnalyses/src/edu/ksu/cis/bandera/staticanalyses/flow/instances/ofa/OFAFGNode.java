
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.WorkList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This class represents the flow graph node that accumulates objects as their entities would refer to objects at run-time.
 * This is an Object-flow analysis specific implementation.  Created: Thu Jan 31 00:42:34 2002
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
	 */
	public OFAFGNode(WorkList wl) {
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
		 * @param node the node into which the values need to be injected.
		 * @param values a collection containing the values to be injected.
		 */
		SendValuesWork(FGNode node, Collection values) {
			super(node, values);
		}

		/**
		 * Creates a new <code>SendValuesWork</code> instance.
		 *
		 * @param node the node into which the values need to be injected.
		 * @param value the value to be injected.
		 */
		SendValuesWork(FGNode node, Object value) {
			super(node, new HashSet());
			addValue(value);
		}

		/**
		 * Injects the values into the associated node.
		 */
		public final void execute() {
			node.addValues(this.values);
		}
	}

	/**
	 * Adds a new work to the worklist to propogate the values in this node to <code>succ</code>.  Only the difference values
	 * are propogated.
	 *
	 * @param succ the successor node that was added to this node.
	 */
	public void onNewSucc(FGNode succ) {
		Collection temp = diffValues(succ);

		if(!temp.isEmpty()) {
			worklist.addWork(new SendValuesWork(succ, temp));
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>value</code> in this node to it's successor nodes.
	 *
	 * @param value the value to be propogated to the successor node.
	 */
	public void onNewValue(Object value) {
		for(Iterator i = succs.iterator(); i.hasNext();) {
			FGNode succ = (FGNode) i.next();

			if(!succ.getValues().contains(value)) {
				worklist.addWork(new SendValuesWork(succ, value));
			}
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>values</code> in this node to it's successor nodes.
	 *
	 * @param values the values to be propogated to the successor node.  The collection contains object of type
	 * 		  <code>Object</code>.
	 */
	public void onNewValues(Collection values) {
		for(Iterator i = succs.iterator(); i.hasNext();) {
			FGNode succ = (FGNode) i.next();

			if(!diffValues(succ).isEmpty()) {
				worklist.addWork(new SendValuesWork(succ, values));
			}
		}
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the <code>WorkList</code> to be passed to the constructor of this class.
	 *
	 * @return a new instance of this class parameterized by <code>o</code>.
	 */
	public Object prototype(Object o) {
		return new OFAFGNode((WorkList) o);
	}
}

/*****
 ChangeLog:

$Log$

*****/
