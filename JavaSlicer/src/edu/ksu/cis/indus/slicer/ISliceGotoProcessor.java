
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import java.util.Collection;


/**
 * This is the interface through which goto post processing can be accomplished after slicing.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface ISliceGotoProcessor {
	/**
	 * Process the given methods.
	 *
	 * @param methods to be processed.
	 * @param bbgMgr provides the basic block required to process the methods.
	 *
	 * @pre methods != null and bbgMgr != null
	 * @pre methods.oclIsKindOf(Collection(SootMethod))
	 */
	void process(final Collection methods, final BasicBlockGraphMgr bbgMgr);
}

/*
   ChangeLog:
   $Log$
 */
