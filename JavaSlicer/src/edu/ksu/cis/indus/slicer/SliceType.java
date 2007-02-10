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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * The enumeration of types of slices.
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
	 * Provides the name of the slice type.
	 * 
	 * @param v is the slice type of interest.
	 * @return the name of the slice type.
	 */
	@NonNull @Functional public static String name(@NonNull final SliceType v) {
		return v.name();
	}
}
