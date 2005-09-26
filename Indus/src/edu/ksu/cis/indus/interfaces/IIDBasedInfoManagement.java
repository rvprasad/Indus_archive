
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

package edu.ksu.cis.indus.interfaces;

/**
 * This interface is used to manage information based on ID.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IIDBasedInfoManagement {
	/**
	 * Sets the information for the given id.
	 *
	 * @param infoID is the id.
	 * @param info is the implemenation that provides the info.
	 *
	 * @return the previous implementation that provided the info.
	 *
	 * @pre infoID != null and info != null
	 */
	Object setInfoFor(final Comparable infoID, final Object info);

	/**
	 * Clears all ID to info mapping.
	 */
	void clearInfo();

	/**
	 * Removes the ID to info mapping.
	 *
	 * @param infoID for which the mapping is to be removed.
	 *
	 * @return the implementation that provide the info.
	 *
	 * @pre infoID != null
	 */
	Object removeInfo(final Comparable infoID);
}

// End of File
