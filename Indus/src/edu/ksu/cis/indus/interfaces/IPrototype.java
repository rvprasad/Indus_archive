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

import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This interface helps realize the <i>IPrototype</i> design pattern as defined in the Gang of Four book. It provides the
 * methods via which concrete object can be created from a prototype object. The default implementation for these methods
 * should raise <code>UnsupportedOperationException</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T> is the type of the prototype.
 */
public interface IPrototype<T> {

	/**
	 * Creates a concrete object from this prototype object. The concrete object can be parameterized by the information in
	 * <code>o</code>.
	 * 
	 * @param o object containing the information to parameterize the concrete object.
	 * @return concrete object based on this prototype object.
	 */
	T getClone(@NonNull Object... o);
}

// End of File
