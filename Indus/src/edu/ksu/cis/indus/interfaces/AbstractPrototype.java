/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
 * @param <T> is the type of the prototype object.
 */
public abstract class AbstractPrototype<T>
		implements IPrototype<T> {

	/**
	 * This implementation throws <code>UnsupportedOperationException</code>.
	 * 
	 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone(java.lang.Object[])
	 */
	public T getClone(@SuppressWarnings("unused") final Object... o) {
		throw new UnsupportedOperationException("getClone(Object) is not supported.");
	}
}

// End of File
