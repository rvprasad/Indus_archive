
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.jimple.GotoStmt;
import soot.jimple.Stmt;


/**
 * This implementation handles statements of the basic block as required for goto processing of complete slices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompleteSliceGotoProcessor
  extends AbstractSliceGotoProcessor {
	/**
	 * Creates a new CompleteSliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 *
	 * @pre collector != null
	 */
	public CompleteSliceGotoProcessor(final SliceCollector collector) {
		super(collector);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processForIntraBasicBlockGotos(final BasicBlockGraph bbg) {
		final Collection _gotos = new HashSet();
        final String _tagName = sliceCollector.getTagName();

		for (final Iterator _j = bbg.getNodes().iterator(); _j.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _j.next();
			boolean _tagged = false;
            _gotos.clear();

			for (final Iterator _i = _bb.getStmtsOf().iterator(); _i.hasNext();) {
				final Stmt _stmt = (Stmt) _i.next();

				if (_stmt.getTag(_tagName) != null) {
					_tagged = true;
					workBag.addWork(_bb);
				} else if (_stmt instanceof GotoStmt) {
					_gotos.add(_stmt);
				}
			}

			if (_tagged) {
				sliceCollector.includeInSlice(_gotos);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2004/01/22 01:01:40  venku
   - coding convention.

   Revision 1.11  2004/01/19 11:39:11  venku
   - added new batched includeInSlice() method to SliceCollector.
   - used new includeInSlice() method in CompleteSliceGotoProcessor.

   Revision 1.10  2004/01/13 23:34:54  venku
   - fixed the processing of intra basicblock jumps and
     inter basic block jumps.
   Revision 1.9  2004/01/13 08:39:07  venku
   - moved the GotoProcessors back into the slicer core as these
     classes home the logic required for slice creation.
   Revision 1.2  2004/01/13 04:39:29  venku
   - method and class visibility.
   Revision 1.1  2004/01/13 04:35:08  venku
   - added a new package called "processing" and it will house
     all processing done on the slice to ensure the slice satisfies
     certain properties such as executability.
   - Moved GotoProcessors into processing package.
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
