
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
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class AccessSpecification {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private static final byte PUBLIC_ACCESS = 1;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private static final byte PROTECTED_ACCESS = 2;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private static final byte DEFAULT_ACCESS = 4;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private static final byte PRIVATE_ACCESS = 8;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private byte access;

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param value DOCUMENT ME!
	 */
	void setDefaultAccess(final boolean value) {
		if (value) {
			access |= DEFAULT_ACCESS;
		} else {
			access &= ~DEFAULT_ACCESS;
		}
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isDefaultAccess() {
		return (access & DEFAULT_ACCESS) != 0;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param value DOCUMENT ME!
	 */
	void setPrivateAccess(final boolean value) {
		if (value) {
			access |= PRIVATE_ACCESS;
		} else {
			access &= ~PRIVATE_ACCESS;
		}
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isPrivateAccess() {
		return (access & PRIVATE_ACCESS) != 0;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param value DOCUMENT ME!
	 */
	void setProtectedAccess(final boolean value) {
		if (value) {
			access |= PROTECTED_ACCESS;
		} else {
			access &= ~PROTECTED_ACCESS;
		}
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isProtectedAccess() {
		return (access & PROTECTED_ACCESS) != 0;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param value DOCUMENT ME!
	 */
	void setPublicAccess(final boolean value) {
		if (value) {
			access |= PUBLIC_ACCESS;
		} else {
			access &= ~PUBLIC_ACCESS;
		}
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isPublicAccess() {
		return (access & PUBLIC_ACCESS) != 0;
	}
}

// End of File
