
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

package edu.ksu.cis.indus.slicer.processing;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.slicer.SliceCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This implementation handles statements of the basic block as required for goto processing of complete slices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class CompleteSliceGotoProcessor
  extends AbstractSliceGotoProcessor {
	/**
	 * Creates a new CompleteSliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 *
	 * @pre collector != null
	 */
	protected CompleteSliceGotoProcessor(SliceCollector collector) {
		super(collector);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.processing.AbstractSliceGotoProcessor#postProcessBasicBlock(BasicBlock)
	 */
	protected Collection getLastStmtAndSuccsOfBasicBlock(final BasicBlock bb) {
		final Collection _stmts = bb.getStmtsOf();
		final Collection _result = new ArrayList();
		_result.add(new Pair(bb.getTrailerStmt(), bb.getSuccsOf()));

		if (_stmts.size() > 1) {
			_result.add(new Pair(bb.getLeaderStmt(), bb.getPredsOf()));
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.processing.AbstractSliceGotoProcessor#getStmtsOfForProcessing(BasicBlock)
	 */
	protected List getStmtsOfForProcessing(BasicBlock bb) {
		return bb.getStmtsOf();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/01/11 03:44:25  venku
   - Deleted IGotoProcessor and SliceGotoProcessor.
   - Moved the logic of SliceGotoProcessor into
     AbstractSliceGotoProcessor.
   - Different slices are handled by different processor classes.

   Revision 1.6  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
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
