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

import edu.ksu.cis.indus.annotations.Functional;

/**
 * This interface provides information about access control for entities such as class, methods, and fields. This interface
 * will eventually be moved to Espina.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
interface IAccessSpecifiers {

	/**
	 * Checks if default access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	@Functional boolean isDefaultAccess();

	/**
	 * Checks if private access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	@Functional boolean isPrivateAccess();

	/**
	 * Checks if protected access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	@Functional boolean isProtectedAccess();

	/**
	 * Checks if public access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	@Functional boolean isPublicAccess();

	/**
	 * Enables/disables default access.
	 * 
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setDefaultAccess(boolean value);

	/**
	 * Enables/disables private access.
	 * 
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setPrivateAccess(boolean value);

	/**
	 * Enables/disables protected access.
	 * 
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setProtectedAccess(boolean value);

	/**
	 * Enables/disables public access.
	 * 
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setPublicAccess(boolean value);
}

// End of File
