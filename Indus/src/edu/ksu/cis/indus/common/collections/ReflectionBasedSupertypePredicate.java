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
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This class can be used to check if a given class object is the super type of a sub type.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the subtype.
 */
public class ReflectionBasedSupertypePredicate<T>
		implements IPredicate<Class<? extends T>> {

	/**
	 * The sub type against which the check will occur.
	 */
	private Class<? extends T> subtype;

	/**
	 * Creates an instance of this class.
	 */
	public ReflectionBasedSupertypePredicate() {
		super();
	}

	/**
	 * Checks if the given object is a super type of the subtype set on this object.
	 * 
	 * @param object to be checked.
	 * @return <code>true</code> if <code>object</code> is a class object and it is a super type of the subtype set on
	 *         this object; <code>false</code>, otherwise.
	 */
	@Functional public boolean evaluate(@NonNull final Class<? extends T> object) {
		return object.isAssignableFrom(subtype);
	}

	/**
	 * Sets the subtype for the check to be performed by this object.
	 * 
	 * @param clazz is the subtype.
	 */
	public void setsubType(@NonNull @Immutable final Class<? extends T> clazz) {
		subtype = clazz;
	}
}

// End of File
