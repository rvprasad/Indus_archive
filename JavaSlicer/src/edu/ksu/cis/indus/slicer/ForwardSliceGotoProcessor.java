
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

import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import java.util.List;


/**
 * This implementation handles statements of the basic block as required for goto processing of forward slices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ForwardSliceGotoProcessor
  extends BackwardSliceGotoProcessor {
	/**
	 * Creates a new ForwardSliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 *
	 * @pre collector != null
	 */
	public ForwardSliceGotoProcessor(final SliceCollector collector) {
		super(collector);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.BackwardSliceGotoProcessor#getStmtsOfForProcessing(BasicBlock)
	 */
	protected List getStmtsOfForProcessing(final BasicBlock bb) {
		return bb.getStmtsOf();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2004/02/25 00:09:12  venku
   - documenation.
   Revision 1.5  2004/01/22 01:01:40  venku
   - coding convention.
   Revision 1.4  2004/01/13 23:34:54  venku
   - fixed the processing of intra basicblock jumps and
     inter basic block jumps.
   Revision 1.3  2004/01/13 08:39:07  venku
   - moved the GotoProcessors back into the slicer core as these
     classes home the logic required for slice creation.
   Revision 1.2  2004/01/13 04:39:29  venku
   - method and class visibility.
   Revision 1.1  2004/01/13 04:35:08  venku
   - added a new package called "processing" and it will house
     all processing done on the slice to ensure the slice satisfies
     certain properties such as executability.
   - Moved GotoProcessors into processing package.
   Revision 1.1  2004/01/11 03:44:25  venku
   - Deleted IGotoProcessor and SliceGotoProcessor.
   - Moved the logic of SliceGotoProcessor into
     AbstractSliceGotoProcessor.
   - Different slices are handled by different processor classes.
 */
