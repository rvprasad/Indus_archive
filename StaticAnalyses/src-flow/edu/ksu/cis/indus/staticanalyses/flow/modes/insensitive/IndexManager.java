
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;


/**
 * This class implements insensitive index manager.  In simple words, it generates indices such that entities can be
 * differentiated solely on their credentials and not on any other auxiliary information such as program point or call
 * stack.
 * 
 * <p>
 * Created: Fri Jan 25 13:11:19 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class IndexManager
  extends AbstractIndexManager {
	/**
	 * This class represents an index that identifies an entity independent of any context information..
	 */
	class DummyIndex
	  implements IIndex {
		/**
		 * The entity that this index identifies.
		 */
		Object object;

		/**
		 * Creates a new <code>DummyIndex</code> instance.
		 *
		 * @param o the entity being identified by this index.
		 */
		DummyIndex(final Object o) {
			this.object = o;
		}

		/**
		 * Compares if the given object is the same as this object.
		 *
		 * @param o the object to be compared with.
		 *
		 * @return <code>true</code> if <code>object</code> is the same as this object; <code>false</code> otherwise.
		 */
		public boolean equals(final Object o) {
			boolean result = false;

			if (o != null && o instanceof DummyIndex) {
				DummyIndex di = (DummyIndex) o;

				if (object != null) {
					result = object.equals(di.object);
				} else {
					result = object == di.object;
				}
			}
			return result;
		}

		/**
		 * Returns the hash code for this object.
		 *
		 * @return returns the hash code for this object.
		 */
		public int hashCode() {
			int result = 17;

			if (object != null) {
				result = 37 * result * object.hashCode();
			}
			return result;
		}

		/**
		 * Returns the stringized representation of this object.
		 *
		 * @return the stringized representation of this object.
		 */
		public String toString() {
			return object.toString();
		}
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @return a new instance of this class.
	 */
	public Object getClone() {
		return new IndexManager();
	}

	/**
	 * This method throws an <code>UnsupportedOperationException</code> exception.
	 *
	 * @param o <i>ignored</i>.
	 *
	 * @return (This method throws an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported.
	 */
	public Object getClone(final Object o)
	  throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Single parameter prototype() is not supported.");
	}

	/**
	 * Returns an index corresponding to the given entity.
	 *
	 * @param o the entity for which the index in required.
	 * @param c <i>ignored</i>..
	 *
	 * @return the index that uniquely identifies <code>o</code>.
	 */
	protected IIndex getIndex(final Object o, final Context c) {
		return new DummyIndex(o);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
