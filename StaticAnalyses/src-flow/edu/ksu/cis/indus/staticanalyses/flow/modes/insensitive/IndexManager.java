
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;


/**
 * <p>
 * This class implements insensitive index manager.  In simple words, it generates indices such that entities can be
 * differentiated solely on their credentials and not on any other auxiliary information such as program point or call
 * stack.
 * </p>
 * Created: Fri Jan 25 13:11:19 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class IndexManager
  extends AbstractIndexManager {
	/**
	 * <p>
	 * This class represents an index that identifies an entity independent of any context information..
	 * </p>
	 */
	class DummyIndex
	  implements IIndex {
		/**
		 * <p>
		 * The entity that this index identifies.
		 * </p>
		 */
		Object object;

		/**
		 * <p>
		 * Creates a new <code>DummyIndex</code> instance.
		 * </p>
		 *
		 * @param object the entity being identified by this index.
		 */
		DummyIndex(Object o) {
			this.object = o;
		}

		/**
		 * <p>
		 * Compares if the given object is the same as this object.
		 * </p>
		 *
		 * @param object the object to be compared with.
		 *
		 * @return <code>true</code> if <code>object</code> is the same as this object; <code>false</code> otherwise.
		 */
		public boolean equals(Object o) {
			return this.object.hashCode() == o.hashCode();
		}

		/**
		 * <p>
		 * Returns the hash code for this object.
		 * </p>
		 *
		 * @return returns the hash code for this object.
		 */
		public int hashCode() {
			return object.hashCode();
		}

		/**
		 * <p>
		 * Returns the stringized representation of this object.
		 * </p>
		 *
		 * @return the stringized representation of this object.
		 */
		public String toString() {
			return object.toString();
		}
	}

	/**
	 * <p>
	 * Returns a new instance of this class.
	 * </p>
	 *
	 * @return a new instance of this class.
	 */
	public Object getClone() {
		return new IndexManager();
	}

	/**
	 * <p>
	 * This method throws an <code>UnsupportedOperationException</code> exception.
	 * </p>
	 *
	 * @param object (This parameter is ignored.)
	 *
	 * @return (This method throws an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported.
	 */
	public Object getClone(Object o) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Single parameter prototype() is not supported.");
	}

	/**
	 * <p>
	 * Returns an index corresponding to the given entity.
	 * </p>
	 *
	 * @param object the entity for which the index in required.
	 * @param c this parameter is ignored.  This can be <code>null</code>.
	 *
	 * @return the index that uniquely identifies <code>object</code>.
	 */
	protected IIndex getIndex(Object o, Context c) {
		return new DummyIndex(o);
	}
}

/*****
 ChangeLog:

$Log$
Revision 0.10  2003/05/22 22:18:32  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
