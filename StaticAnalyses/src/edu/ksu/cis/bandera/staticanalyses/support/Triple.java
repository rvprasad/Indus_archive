
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

/**
 * This class represents a triplet of objects.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Triple
  implements Cloneable {
	/**
	 * The first object of this triple.
	 */
	private Object first;

	/**
	 * The second object of this triple.
	 */
	private Object second;

	/**
	 * The third object of this triple.
	 */
	private Object third;

	/**
	 * Creates a new Triple object.
	 *
	 * @param first the first object of this triple.
	 * @param second the second object of this triple.
	 * @param third the third object of this triple
	 */
	public Triple(Object first, Object second, Object third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * Returns the first object in the triple.
	 *
	 * @return the first object in the triple.
	 */
	public Object getFirst() {
		return first;
	}

	/**
	 * Returns the second object in the triple.
	 *
	 * @return the second object in the triple.
	 */
	public Object getSecond() {
		return second;
	}

	/**
	 * Returns the third object in the triple.
	 *
	 * @return the third object in the triple.
	 */
	public Object getThird() {
		return third;
	}

	/**
	 * Clones this triple.
	 *
	 * @return a cloned copy of this triple.
	 *
	 * @throws CloneNotSupportedException will not be thrown.
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		return (Triple) super.clone();
	}

	/**
	 * Checks if the given object is equal to this triple.
	 *
	 * @param o is the object to be tested for equality with this triple.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this triple; <code>false</code>, otherwise.
	 */
	public boolean equals(Object o) {
		boolean result = false;

		if(o instanceof Triple) {
			Triple temp = (Triple) o;

			if(first != null) {
				result = first.equals(first);
			} else {
				result = first == temp.first;
			}

			if(second != null) {
				result = result && second.equals(second);
			} else {
				result = result && second == temp.second;
			}

			if(third != null) {
				result = result && third.equals(third);
			} else {
				result = result && third == temp.third;
			}
		}
		return result;
	}

	/**
	 * Returns the hash code for this triple.
	 *
	 * @return the hash code of this triple.  It is derived from the objects that constitute this triple.
	 */
	public int hashCode() {
		String temp = "";

		if(first != null) {
			temp = temp + first;
		}

		if(second != null) {
			temp = temp + second;
		}

		if(third != null) {
			temp = temp + third;
		}
		return temp.hashCode();
	}
}

/*****
 ChangeLog:

$Log$

*****/
