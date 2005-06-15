
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

package edu.ksu.cis.indus.toolkits.bandera;

import java.util.List;


/**
 * This is a mere data class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class SlicerConfiguration {
	/** 
	 * The list of FQNs of classes that need to be retained when class erasure optimization is applied.
	 *
	 * @invariant retentionList.oclIsKindOf(Collection(String))
	 */
	List retentionList;

	/** 
	 * This is the string containing the slicer configuration.
	 */
	String slicerConfigurationStr;

	/** 
	 * This indicates if class erasure optimization should be applied or otherwise.
	 */
	boolean eraseUnnecessaryClasses;

	/**
	 * Creates an instance of this class.
	 */
	public SlicerConfiguration() {
		super();
	}
}

// End of File
