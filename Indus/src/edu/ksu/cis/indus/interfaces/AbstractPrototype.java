
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

package edu.ksu.cis.indus.interfaces;

/**
 * This class is the abstract implementation of <code>IPrototype</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractPrototype
  implements IPrototype {
	/**
	 * This implementation throws <code>UnsupportedOperationException</code>.
	 *
	 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone()
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("getClone(Object) is not supported.");
	}

	/**
	 * This implementation throws <code>UnsupportedOperationException</code>.
	 *
	 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone(java.lang.Object)
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("getClone(Object) is not supported.");
	}
}

/*
   ChangeLog:
   $Log$
 */
