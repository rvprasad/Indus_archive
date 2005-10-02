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

import java.util.EnumSet;


import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class represents access control specification for the purpose of serialization and deserialization in the realm of
 * scope definition.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class AccessSpecification {

	/**
	 * This contains the access specification encoded in terms of XXX_ACCESS constants defined in
	 * <code>IAccessSpecifiers.AccessEnum</code>.
	 */
	private final EnumSet<AccessSpecificationEnum> access = EnumSet.noneOf(AccessSpecificationEnum.class);

	/**
	 * Creates a new AccessSpecification object with default access set to public and protected.
	 */
	public AccessSpecification() {
		access.add(AccessSpecificationEnum.PROTECTED_ACCESS);
		access.add(AccessSpecificationEnum.PUBLIC_ACCESS);
	}

	/**
	 * Checks if the given access control specifier confirms to this specification.
	 * 
	 * @param specifier to be checked for conformance.
	 * @return <code>true</code> if <code>specifier</code> confirms to this spec; <code>false</code>, otherwise.
	 * @pre specifier != null
	 */
	public boolean conformant(final IAccessSpecifiers specifier) {
		return isDefaultAccess() == specifier.isDefaultAccess() || isProtectedAccess() == specifier.isProtectedAccess()
				|| isPublicAccess() == specifier.isPublicAccess() || isPrivateAccess() == specifier.isPrivateAccess();
	}

	/**
	 * Retrieves default access control level.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	public boolean isDefaultAccess() {
		return access.contains(AccessSpecificationEnum.DEFAULT_ACCESS);
	}

	/**
	 * Retrieves private access control level.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	public boolean isPrivateAccess() {
		return access.contains(AccessSpecificationEnum.PRIVATE_ACCESS);
	}

	/**
	 * Retrieves protected access control level.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	public boolean isProtectedAccess() {
		return access.contains(AccessSpecificationEnum.PROTECTED_ACCESS);
	}

	/**
	 * Retrieves public access control level.
	 * 
	 * @return <code>true</code> if enabled; <code>false</code>, otherwise.
	 */
	public boolean isPublicAccess() {
		return access.contains(AccessSpecificationEnum.PUBLIC_ACCESS);
	}

	/**
	 * Sets the default access control (package-private) level.
	 * 
	 * @param value to be set.
	 */
	public void setDefaultAccess(final boolean value) {
		if (value) {
			access.add(AccessSpecificationEnum.DEFAULT_ACCESS);
		} else {
			access.remove(AccessSpecificationEnum.DEFAULT_ACCESS);
		}
	}

	/**
	 * Sets the private access control level.
	 * 
	 * @param value to be set.
	 */
	public void setPrivateAccess(final boolean value) {
		if (value) {
			access.add(AccessSpecificationEnum.PRIVATE_ACCESS);
		} else {
			access.remove(AccessSpecificationEnum.PRIVATE_ACCESS);
		}
	}

	/**
	 * Sets the protected access control level.
	 * 
	 * @param value to be set.
	 */
	public void setProtectedAccess(final boolean value) {
		if (value) {
			access.add(AccessSpecificationEnum.PROTECTED_ACCESS);
		} else {
			access.remove(AccessSpecificationEnum.PROTECTED_ACCESS);
		}
	}

	/**
	 * Sets the public access control level.
	 * 
	 * @param value to be set.
	 */
	public void setPublicAccess(final boolean value) {
		if (value) {
			access.add(AccessSpecificationEnum.PUBLIC_ACCESS);
		} else {
			access.remove(AccessSpecificationEnum.PUBLIC_ACCESS);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		final StringBuffer _access = new StringBuffer(access + " ");

		if (isPrivateAccess()) {
			_access.append("Private | ");
		}

		if (isDefaultAccess()) {
			_access.append("Package Private | ");
		}

		if (isProtectedAccess()) {
			_access.append("Protected | ");
		}

		if (isPublicAccess()) {
			_access.append("Public | ");
		}
		return new ToStringBuilder(this).appendSuper(super.toString()).append("access", _access).toString();
	}
}

// End of File
