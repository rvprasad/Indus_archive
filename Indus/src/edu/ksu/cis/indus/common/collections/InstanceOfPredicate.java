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
package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This predicate performs <code>instanceof</code> check.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type in the instanceof check.
 * @param <V> is the type of value that participates in the instanceof check.
 */
public class InstanceOfPredicate<T, V>
		implements IPredicate<V> {

	/**
	 * The class of the type that participates in the instanceof check.
	 */
	private Class<T> clazz;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param instanceClass is the class of the type involved in the instanceof check.
	 */
	public InstanceOfPredicate(@NonNull final Class<T> instanceClass) {
		super();
		clazz = instanceClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public  boolean evaluate(final V t) {
		return clazz.isInstance(t);
	}

}

// End of File
