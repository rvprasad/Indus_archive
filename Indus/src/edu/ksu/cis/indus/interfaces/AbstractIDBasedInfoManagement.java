
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

import java.util.HashMap;
import java.util.Map;


/**
 * This is an abstract implementation of <code>IIDBasedInfoManagement</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractIDBasedInfoManagement
  implements IIDBasedInfoManagement {
	/** 
	 * This maps IDs to implementations that provides information.
	 */
	private final Map<Comparable, Object> infoMap = new HashMap<Comparable, Object>();

	/**
	 * @see IIDBasedInfoManagement#setInfoFor(Comparable, Object)
	 */
	public final Object setInfoFor(final Comparable infoID, final Object info) {
		return infoMap.put(infoID, info);
	}

	/**
	 * @see IIDBasedInfoManagement#clearInfo()
	 */
	public final void clearInfo() {
		infoMap.clear();
	}

	/**
	 * @see IIDBasedInfoManagement#removeInfo(Comparable)
	 */
	public final Object removeInfo(final Comparable infoID) {
		return infoMap.remove(infoID);
	}

	/**
	 * Retrieves the implementation that provides the info for the given ID.
	 *
	 * @param infoID for which the info is being requested.
	 *
	 * @return the implementation that provide the info.
	 *
	 * @pre infoID != null
	 */
	protected final Object getInfoFor(final Comparable infoID) {
		return infoMap.get(infoID);
	}
}

// End of File
