
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

package edu.ksu.cis.bandera.staticanalyses.support;

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a pair of objects.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Pair
  implements Cloneable {
	/**
	 * The first object of this pair.
	 */
	protected Object first;

	/**
	 * The second object of this pair.
	 */
	protected Object second;

	/**
	 * Creates a new Pair object.
	 *
	 * @param first the first object of this pair.
	 * @param second the second object of this pair.
	 */
	public Pair(Object first, Object second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * This class manages a collection of <code>Pair</code> objects.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static class PairManager {
		/**
		 * This is the id of this service.
		 */
		public static final String ID = "Pair management service";

		/**
		 * The collection of managed pairs.
		 */
		private List pairs = new ArrayList();

		/**
		 * The scratch pad pair object to be used for does-it-manage check.
		 */
		private Pair pair = new Pair(null, null);

		/**
		 * Provides a <code>Pair</code> object wrapping 2 given objects.  However, while doing do it will check if there
		 * exists a pair which wraps the given 2 object in the given order.  If so, it will provide the existing pair.  If
		 * not, it will creat a new pair and return it.
		 *
		 * @param first element of the requested pair.
		 * @param second element of the requested pair.
		 *
		 * @return the pair wrapping <code>first</code> and <code>second</code>.
		 */
		public Pair getPair(Object first, Object second) {
			Pair result;
			pair.first = first;
			pair.second = second;

			if(pairs.contains(pair)) {
				result = (Pair) pairs.get(pairs.indexOf(pair));
			} else {
				result = new Pair(first, second);
				pairs.add(0, result);
			}
			return result;
		}

		/**
		 * Forgets about all managed pairs.
		 */
		public void reset() {
			pairs.clear();
		}
	}

	/**
	 * Returns the first object in the pair.
	 *
	 * @return the first object in the pair.
	 */
	public Object getFirst() {
		return first;
	}

	/**
	 * Returns the second object in the pair.
	 *
	 * @return the second object in the pair.
	 */
	public Object getSecond() {
		return second;
	}

	/**
	 * Clones this pair.
	 *
	 * @return a cloned copy of this pair.
	 *
	 * @throws CloneNotSupportedException will not be thrown.
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		return (Pair) super.clone();
	}

	/**
	 * Checks if the given object is equal to this pair.
	 *
	 * @param o is the object to be tested for equality with this pair.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this pair; <code>false</code>, otherwise.
	 */
	public boolean equals(Object o) {
		boolean result = false;

		if(o instanceof Pair) {
			Pair temp = (Pair) o;

			if(first != null) {
				result = first.equals(temp.first);
			} else {
				result = first == temp.first;
			}

			if(second != null) {
				result = result && second.equals(temp.second);
			} else {
				result = result && second == temp.second;
			}
		}
		return result;
	}

	/**
	 * Returns the hash code for this pair.
	 *
	 * @return the hash code of this pair.  It is derived from the objects that constitute this pair.
	 */
	public int hashCode() {
		String temp = "";

		if(first != null) {
			temp = temp + first;
		}

		if(second != null) {
			temp = temp + second;
		}
		return temp.hashCode();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}
}

/*****
 ChangeLog:

$Log$

*****/
