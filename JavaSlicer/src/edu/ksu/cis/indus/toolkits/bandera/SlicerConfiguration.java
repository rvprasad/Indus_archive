
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
	 */
	List<String> retentionList;

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
