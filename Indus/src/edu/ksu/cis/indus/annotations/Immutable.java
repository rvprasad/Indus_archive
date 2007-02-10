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

package edu.ksu.cis.indus.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation indicates immutability of entities via published (public, protected, package-private) interfaces.
 * Immutability is that the data reachable from the entities will not be mutated.
 * <ul>
 * <li>With fields and local variables, this annotation indicates requirement.
 * <li>With parameters, it indicates guarantee provided by the enclosed method.
 * <li>With instance methods, it indicates guarantee provided by the method about the receiver object.
 * <li>With static methods, it indicates guarantee provided by the method about class entities.
 * </ul>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD }) @Documented @Empty public @interface Immutable {
	// empty
}

// End of File
