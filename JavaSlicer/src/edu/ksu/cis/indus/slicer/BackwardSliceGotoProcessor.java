
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.jimple.GotoStmt;
import soot.jimple.Stmt;


/**
 * This implementation handles statements of the basic block as required for goto processing of backward slices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class BackwardSliceGotoProcessor
  extends AbstractSliceGotoProcessor {
	/**
	 * Creates a new BackwardSliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 *
	 * @pre collector != null
	 */
	public BackwardSliceGotoProcessor(final SliceCollector collector) {
		super(collector);
	}

	/**
	 * @see AbstractSliceGotoProcessor#getStmtsOfForProcessing(BasicBlock)
	 */
	protected List getStmtsOfForProcessing(final BasicBlock bb) {
		final List _result = new ArrayList(bb.getStmtsOf());
		Collections.reverse(_result);
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	protected final void processForIntraBasicBlockGotos(final BasicBlockGraph bbg) {
		for (final Iterator _j = bbg.getNodes().iterator(); _j.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _j.next();

			boolean _tagged = false;
			final String _tagName = sliceCollector.getTagName();
			final List _list = getStmtsOfForProcessing(_bb);

			for (final Iterator _i = _list.iterator(); _i.hasNext();) {
				final Stmt _stmt = (Stmt) _i.next();

				if (_stmt.getTag(_tagName) != null) {
					_tagged = true;
					workBag.addWork(_bb);
				} else if (_stmt instanceof GotoStmt && _tagged) {
					sliceCollector.includeInSlice(_stmt);
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
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
