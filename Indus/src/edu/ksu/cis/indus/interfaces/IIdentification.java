
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
 * This interface is used identify entities in Indus.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IIdentification {
	/**
	 * Returns the ID of the implementation.  Note that it is possible that an implementation can cater many interfaces,
	 * hence, have many ids.  It is advisable that such implementations be teased apart.  If not possible, then the id
	 * should be that of the purpose that the implementation serves.
	 *
	 * @return the id of the implementation.
	 *
	 * @post result != null
	 */
	Object getId();
}

// End of File
