
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

import java.util.HashSet;
import java.util.Set;


/**
 * <p>
 * This class encapsulates the index creation logic.  It is abstract and it provides an interface through which new indices
 * can be obtained.  The sub classes should provide the logic for the actual creation of the indices.
 * </p>
 * 
 * <p>
 * Created: Tue Jan 22 04:54:38 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractIndexManager
  implements Prototype {
	/**
	 * <p>
	 * The collection of indices managed by this object.
	 * </p>
	 */
	protected Set indices = new HashSet();

	/**
	 * <p>
	 * This operation is unsupported.
	 * </p>
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * <p>
	 * This operation is unsupported.
	 * </p>
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object prototype(Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * <p>
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, a new index
	 * is created and returned.
	 * </p>
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context.
	 */
	protected abstract Index getIndex(Object o, Context c);

	/**
	 * <p>
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, it returns
	 * <code>null</code>.
	 * </p>
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context, if one exists; <code>null</code> otherwise.
	 */
	final Index queryIndex(Object o, Context c) {
		Index temp = getIndex(o, c);

		if(!indices.contains(temp)) {
			indices.add(temp);
		}

		// end of if (sm2indices.containsKey(sm)) else
		return temp;
	}

	/**
	 * <p>
	 * Reset the manager.  Flush all the internal data structures to enable a new session.
	 * </p>
	 */
	void reset() {
		indices.clear();
	}
}

/*****
 ChangeLog:

$Log$

*****/
