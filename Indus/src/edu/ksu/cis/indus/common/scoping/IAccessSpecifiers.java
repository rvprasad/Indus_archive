
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
 * This interface provides information about access control for entities such as class, methods, and fields.  This  interface
 * will eventually be moved to Espina.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
interface IAccessSpecifiers {
	/** 
	 * This constant indicates default (package-private) access.
	 */
	byte DEFAULT_ACCESS = 4;

	/** 
	 * This constant indicates private access.
	 */
	byte PRIVATE_ACCESS = 8;

	/** 
	 * This constant indicates protected access.
	 */
	byte PROTECTED_ACCESS = 2;

	/** 
	 * This constant indicates public access.
	 */
	byte PUBLIC_ACCESS = 1;

	/**
	 * Enables/disables default access.
	 *
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setDefaultAccess(boolean value);

	/**
	 * Checks if default access level is enabled.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isDefaultAccess();

	/**
	 * Enables/disables private access.
	 *
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setPrivateAccess(boolean value);

	/**
	 * Checks if private access level is enabled.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isPrivateAccess();

	/**
	 * Enables/disables protected access.
	 *
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setProtectedAccess(boolean value);

	/**
	 * Checks if protected access level is enabled.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isProtectedAccess();

	/**
	 * Enables/disables public access.
	 *
	 * @param value <code>true</code> enables; <code>false</code> disables.
	 */
	void setPublicAccess(boolean value);

	/**
	 * Checks if public access level is enabled.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isPublicAccess();
}

// End of File
