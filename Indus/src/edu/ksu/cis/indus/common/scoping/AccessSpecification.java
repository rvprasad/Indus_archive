
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
 * This class represents access control specification for the purpose of serialization and deserialization in the realm of
 * scope definition.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class AccessSpecification {
	/** 
	 * This contains the access specification encoded in terms of XXX_ACCESS constants defined in
	 * <code>IAccessSpecifiers</code>.
	 */
	private byte access;

	/**
	 * Checks if the given access control specifier confirms to this specification.
	 *
	 * @param specifier to be checked for conformance.
	 *
	 * @return <code>true</code> if <code>specifier</code> confirms to this spec; <code>false</code>, otherwise.
	 *
	 * @pre specifier != null
	 */
	public boolean conformant(final IAccessSpecifiers specifier) {
		return isDefaultAccess() == specifier.isDefaultAccess() || isProtectedAccess() == specifier.isProtectedAccess()
		  || isPublicAccess() == specifier.isPublicAccess() || isPrivateAccess() == specifier.isPrivateAccess();
	}

	/**
	 * Sets the default access control (package-private) level.
	 *
	 * @param value to be set.
	 */
	void setDefaultAccess(final boolean value) {
		if (value) {
			access |= IAccessSpecifiers.DEFAULT_ACCESS;
		} else {
			access &= ~IAccessSpecifiers.DEFAULT_ACCESS;
		}
	}

	/**
	 * Retrieves default access control level.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isDefaultAccess() {
		return (access & IAccessSpecifiers.DEFAULT_ACCESS) != 0;
	}

	/**
	 * Sets the private access control level.
	 *
	 * @param value to be set.
	 */
	void setPrivateAccess(final boolean value) {
		if (value) {
			access |= IAccessSpecifiers.PRIVATE_ACCESS;
		} else {
			access &= ~IAccessSpecifiers.PRIVATE_ACCESS;
		}
	}

	/**
	 * Retrieves private access control level.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isPrivateAccess() {
		return (access & IAccessSpecifiers.PRIVATE_ACCESS) != 0;
	}

	/**
	 * Sets the protected access control level.
	 *
	 * @param value to be set.
	 */
	void setProtectedAccess(final boolean value) {
		if (value) {
			access |= IAccessSpecifiers.PROTECTED_ACCESS;
		} else {
			access &= ~IAccessSpecifiers.PROTECTED_ACCESS;
		}
	}

	/**
	 * Retrieves protected access control level.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isProtectedAccess() {
		return (access & IAccessSpecifiers.PROTECTED_ACCESS) != 0;
	}

	/**
	 * Sets the public access control level.
	 *
	 * @param value to be set.
	 */
	void setPublicAccess(final boolean value) {
		if (value) {
			access |= IAccessSpecifiers.PUBLIC_ACCESS;
		} else {
			access &= ~IAccessSpecifiers.PUBLIC_ACCESS;
		}
	}

	/**
	 * Retrieves public access control level.
	 *
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	boolean isPublicAccess() {
		return (access & IAccessSpecifiers.PUBLIC_ACCESS) != 0;
	}
}

// End of File
