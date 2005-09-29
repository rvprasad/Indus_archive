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
