/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
