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

package edu.ksu.cis.indus.slicer;

/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public enum SliceType {
	/**
	 * Backward slice request.
	 */
	BACKWARD_SLICE,

	/**
	 * Complete slice request.
	 */
	COMPLETE_SLICE,

	/**
	 * Forward slice request.
	 */
	FORWARD_SLICE;

	/**
	 * DOCUMENT ME!
	 *
	 * @param v DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String name(final SliceType v) {
		return v.name();
	}
}