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

package edu.ksu.cis.indus.tools.slicer.processing;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.slicer.SliceCollector;

import java.util.Collection;

import soot.SootMethod;

/**
 * This is a generic interface that can be used to provide slice processing service.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISlicePostProcessor {

	/**
	 * Processes the slice.
	 * 
	 * @param methods are the methods in the slice.
	 * @param basicBlockMgr provides the basic block graph for the methods in the slice.
	 * @param theCollector to be used to extend the slice.
	 * @pre methods != null and basicBlockMgr != null and theCollector != null
	 */
	void process(final Collection<SootMethod> methods, final BasicBlockGraphMgr basicBlockMgr,
			final SliceCollector theCollector);
}

// End of File
