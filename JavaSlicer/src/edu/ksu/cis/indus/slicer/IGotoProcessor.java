
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;

import soot.SootMethod;


/**
 * This is the interface via which gotos in the system are selected for inclusion into the slice.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
interface IGotoProcessor {
	/**
	 * Perform post processing after processing the goto statements.
	 */
	void postprocess();

	/**
	 * Perform pre processing before the processing of goto statements occurring in <code>method</code>.
	 *
	 * @param method that contains the gotos that will be processed from here on.
	 *
	 * @pre method != null
	 */
	void preprocess(SootMethod method);

	/**
	 * Process the basic block for goto inclusions in slices.
	 *
	 * @param bbg of the method whose gotos need to be included in the slices.
	 *
	 * @pre bbg != null
	 */
	void process(BasicBlock bbg);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.3  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.2  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.1  2003/11/25 00:00:45  venku
   - added support to include gotos in the slice.
   - added logic to include all tail points in the slice after slicing
     and only in case of backward executable slice.
   - added logic to include exceptions in a limited way.
 */
