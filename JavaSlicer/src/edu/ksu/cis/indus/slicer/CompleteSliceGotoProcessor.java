
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
 * This class can be used goto post-process while generating a complete slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompleteSliceGotoProcessor
  implements IGotoProcessor {
	/**
	 * The processor to consider backward effect on goto inclusions.
	 */
	IGotoProcessor backwardProcessor;

	/**
	 * The processor to consider forward effect on goto inclusions.
	 */
	IGotoProcessor forwardProcessor;

	/**
	 * Creates a new CompleteSliceGotoProcessor object.
	 *
	 * @param collector is the slice collector which annotated the system whose gotos needs to be processed.
	 *
	 * @pre collector != null
	 */
	CompleteSliceGotoProcessor(final TaggingBasedSliceCollector collector) {
		backwardProcessor = new SliceGotoProcessor(collector, true);
		forwardProcessor = new SliceGotoProcessor(collector, false);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#postprocess()
	 */
	public void postprocess() {
		backwardProcessor.postprocess();
		forwardProcessor.postprocess();
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#preprocess(soot.SootMethod)
	 */
	public void preprocess(final SootMethod method) {
		backwardProcessor.preprocess(method);
		forwardProcessor.preprocess(method);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#process(BasicBlock)
	 */
	public void process(final BasicBlock bbg) {
		backwardProcessor.process(bbg);
		forwardProcessor.process(bbg);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.4  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.3  2003/12/04 12:10:12  venku
   - changes that take a stab at interprocedural slicing.
   Revision 1.2  2003/12/02 09:42:17  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.1  2003/11/25 00:00:45  venku
   - added support to include gotos in the slice.
   - added logic to include all tail points in the slice after slicing
     and only in case of backward executable slice.
   - added logic to include exceptions in a limited way.
 */
