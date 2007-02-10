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
package edu.ksu.cis.indus.common.scoping;

import edu.ksu.cis.indus.annotations.NonNull;

import java.util.EnumSet;

/**
 * The enumeration to identify various access specifiers.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public enum AccessSpecificationEnum {
	/**
	 * This identifies default access.
	 */
	DEFAULT_ACCESS,
	/**
	 * This identifies private access.
	 */
	PRIVATE_ACCESS,
	/**
	 * This identifies protected access.
	 */
	PROTECTED_ACCESS,
	/**
	 * This identifies public access.
	 */
	PUBLIC_ACCESS;

	/**
	 * Retrieves a container for this enum type.
	 * 
	 * @return the container.
	 */
	@NonNull public static EnumSet<AccessSpecificationEnum> getContainer() {
		return EnumSet.noneOf(AccessSpecificationEnum.class);
	}
}
