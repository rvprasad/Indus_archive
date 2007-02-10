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

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This is the interface used to filter the basis of criteria during automatic criteria generation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> is the type of input object to the predicate.
 */
public interface ISliceCriteriaPredicate<T>
		extends IPredicate<T> {

	/**
	 * Sets the slicer tool in conjunction of which this filter is being used.
	 * 
	 * @param slicer is the tool to be used.
	 */
	void setSlicerTool(SlicerTool<?> slicer);
}

// End of File
