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
	 * @post result != null
	 */
	public static EnumSet<AccessSpecificationEnum> getContainer() {
		return EnumSet.noneOf(AccessSpecificationEnum.class);
	}
}
