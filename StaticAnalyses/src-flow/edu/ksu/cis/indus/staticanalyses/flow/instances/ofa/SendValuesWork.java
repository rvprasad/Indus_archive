
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

import edu.ksu.cis.indus.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This class represents a peice of work to inject a set of values into a flow graph node.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SendValuesWork
  extends AbstractWork {
	/**
	 * This is the work pool of work peices that will be reused upon request..
	 */
	private static final List POOL = new ArrayList();

	/**
	 * Injects the values into the associated node.
	 */
	public final void execute() {
		node.addValues(this.values);
	}

	/**
	 * Puts back this work into the work pool to be reused.
	 */
	protected void processed() {
		POOL.add(this);
	}

	/**
	 * Creates a new <code>SendValuesWork</code> instance.
	 *
	 * @param toNode the node into which the values need to be injected.
	 * @param valueToBeSent the value to be injected.
	 *
	 * @return a work peice with the given data embedded in it.
	 *
	 * @post result != null
	 * @pre toNode != null and valueToBeSent != null
	 */
	static final SendValuesWork getWork(final IFGNode toNode, final Object valueToBeSent) {
		SendValuesWork result;

		if (POOL.isEmpty()) {
			result = new SendValuesWork();
		} else {
			result = (SendValuesWork) POOL.remove(0);
		}
		result.setFGNode(toNode);
		result.addValue(valueToBeSent);
		return result;
	}

	/**
	 * Creates a new <code>SendValuesWork</code> instance.
	 *
	 * @param toNode the node into which the values need to be injected.
	 * @param valuesToBeSent a collection containing the values to be injected.
	 *
	 * @return a work peice with the given data embedded in it.
	 *
	 * @post result != null
	 * @pre toNode != null and valuesToBeSent != null
	 */
	static final SendValuesWork getWork(final IFGNode toNode, final Collection valuesToBeSent) {
		SendValuesWork result;

		if (POOL.isEmpty()) {
			result = new SendValuesWork();
		} else {
			result = (SendValuesWork) POOL.remove(0);
		}
		result.setFGNode(toNode);
		result.addValues(valuesToBeSent);
		return result;
	}
}

/*
   ChangeLog:
   
   $Log$
   Revision 1.2  2003/08/18 01:01:18  venku
   Trying to fix CVS's erratic behavior.

   
   Revision 1.1  2003/08/17 11:19:13  venku
   Placed the simple SendValuesWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractWork and WorkList to enable work pool support.

 */
