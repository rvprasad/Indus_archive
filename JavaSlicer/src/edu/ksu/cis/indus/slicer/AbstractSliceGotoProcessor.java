
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.SootMethod;

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides most of the logic required to process the given slice in order to include goto statements such that it
 * realizes the control as in the original program but as required in the slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractSliceGotoProcessor {
	/**
	 * A workbag.
	 */
	protected final IWorkBag workBag = new LIFOWorkBag();

	/**
	 * The slice collector.
	 */
	protected final SliceCollector sliceCollector;

	/**
	 * The method being processed.
	 */
	protected SootMethod method;

	/**
	 * Creates a new AbstractSliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 *
	 * @pre collector != null
	 */
	protected AbstractSliceGotoProcessor(final SliceCollector collector) {
		sliceCollector = collector;
	}

	/**
	 * Process the current method's body for goto-based control flow retention.
	 *
	 * @param theMethod to be processed.
	 * @param bbg is the basic block graph of <code>theMethod</code>.
	 *
	 * @pre theMethod != null
	 * @pre bbg != null
	 */
	public final void process(final SootMethod theMethod, final BasicBlockGraph bbg) {
		method = theMethod;
		workBag.clear();

		processForIntraBasicBlockGotos(bbg);

		final Collection _processed = new HashSet();
		final UnitGraph _unitGraph = bbg.getStmtGraph();
		final List _units = new ArrayList(_unitGraph.getBody().getUnits());

		while (workBag.hasWork()) {
			final BasicBlock _bb = (BasicBlock) workBag.getWork();
			_processed.add(_bb);

			final Stmt _leader = _bb.getLeaderStmt();
			final int _lind = _units.indexOf(_leader);

			if (_lind > 0) {
				final Stmt _predStmtOfLeader = (Stmt) _units.get(_lind - 1);
				final List _succsOfPred = _unitGraph.getSuccsOf(_predStmtOfLeader);

				if (!_succsOfPred.contains(_leader)) {
					sliceCollector.includeInSlice(_predStmtOfLeader);
				}
			}
		}
	}

	/**
	 * Process the given methods.
	 *
	 * @param methods to be processed.
	 * @param bbgMgr provides the basic block required to process the methods.
	 *
	 * @pre methods != null and bbgMgr != null
	 * @pre methods.oclIsKindOf(Collection(SootMethod))
	 */
	public final void process(final Collection methods, final BasicBlockGraphMgr bbgMgr) {
		// include all gotos required to recreate the control flow of the system.
		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(_sm);

			if (_bbg == null) {
				continue;
			}
			process(_sm, _bbg);
		}
	}

	/**
	 * Process the basic blocks to consider intra basic block gotos to reconstruct the control flow.
	 *
	 * @param bbg is the basic block graph containing the basic blocks to be processed.
	 *
	 * @pre bbg != null
	 */
	protected abstract void processForIntraBasicBlockGotos(final BasicBlockGraph bbg);
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/01/26 23:54:13  venku
   - coding convention.

   Revision 1.6  2004/01/22 01:01:40  venku
   - coding convention.

   Revision 1.5  2004/01/14 11:18:17  venku
   - subtle bug in which local variable overrode the field.  FIXED.

   Revision 1.4  2004/01/13 23:34:54  venku
   - fixed the processing of intra basicblock jumps and
     inter basic block jumps.

   Revision 1.3  2004/01/13 08:39:07  venku
   - moved the GotoProcessors back into the slicer core as these
     classes home the logic required for slice creation.
   Revision 1.3  2004/01/13 07:53:51  venku
   - as post processing beyond retention of semantics of slice is
     particular to the application or the tool.  Hence, moved the
     post processors into processing package under slicer tool.
   - added a new method to AbstractSliceGotoProcessor to
     process a collection of methods given a basic block graph
     manager.
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
   Revision 1.11  2004/01/11 00:01:23  venku
   - formatting and coding convention.
   Revision 1.10  2004/01/06 00:17:05  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.9  2003/12/13 19:46:33  venku
   - documentation of SliceCollector.
   - renamed collect() to includeInSlice().
   Revision 1.8  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.7  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.6  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.5  2003/12/04 12:10:12  venku
   - changes that take a stab at interprocedural slicing.
   Revision 1.4  2003/12/02 19:20:50  venku
   - coding convention and formatting.
   Revision 1.3  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/12/01 12:21:25  venku
   - methods in collector underwent a lot of change to minimize them.
   - ripple effect.
   Revision 1.1  2003/11/25 00:00:45  venku
   - added support to include gotos in the slice.
   - added logic to include all tail points in the slice after slicing
     and only in case of backward executable slice.
   - added logic to include exceptions in a limited way.
 */
