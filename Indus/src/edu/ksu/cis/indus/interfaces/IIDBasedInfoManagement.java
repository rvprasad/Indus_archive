
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
	Object setInfoFor(final Comparable<?> infoID, final Object info);

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
	Object removeInfo(final Comparable<?> infoID);
}

// End of File
