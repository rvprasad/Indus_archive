
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
	private final Map infoMap = new HashMap();

	/**
	 * @see IIDBasedInfoManagement#setInfoFor(Object, Object)
	 */
	public final Object setInfoFor(final Object infoID, final Object info) {
		return infoMap.put(infoID, info);
	}

	/**
	 * @see IIDBasedInfoManagement#clearInfo()
	 */
	public final void clearInfo() {
		infoMap.clear();
	}

	/**
	 * @see IIDBasedInfoManagement#removeInfo(Object)
	 */
	public final Object removeInfo(final Object infoID) {
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
	protected final Object getInfoFor(final Object infoID) {
		return infoMap.get(infoID);
	}
}

// End of File
