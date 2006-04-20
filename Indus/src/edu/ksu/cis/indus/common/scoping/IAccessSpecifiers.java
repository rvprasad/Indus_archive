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

package edu.ksu.cis.indus.common.scoping;

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
	boolean isDefaultAccess();

	/**
	 * Checks if private access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isPrivateAccess();

	/**
	 * Checks if protected access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isProtectedAccess();

	/**
	 * Checks if public access level is enabled.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isPublicAccess();

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
