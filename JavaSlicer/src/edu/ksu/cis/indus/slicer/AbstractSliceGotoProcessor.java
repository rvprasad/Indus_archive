
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
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;

import soot.SootMethod;
import soot.Trap;

import soot.jimple.GotoStmt;
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
public abstract class AbstractSliceGotoProcessor
  implements ISliceGotoProcessor {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	public static final Predicate GOTO_STMT_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object object) {
				return object instanceof GotoStmt;
			}
		};

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
	 * @see ISliceGotoProcessor#process(Collection, BasicBlockGraphMgr)
	 */
	public final void process(final Collection methods, final BasicBlockGraphMgr bbgMgr) {
		// include all gotos required to recreate the control flow of the system.
		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(_sm);

			if (_bbg != null) {
				process(_sm, _bbg);
			}
		}
	}

	/**
	 * Process the basic block to consider intra basic block gotos to reconstruct the control flow.
	 *
	 * @param bb is the basic block to be processed.
	 *
	 * @pre bb != null
	 */
	protected abstract void processForIntraBasicBlockGotos(final BasicBlock bb);

	/**
	 * Process the current method's body for goto-based control flow retention.
	 *
	 * @param theMethod to be processed.
	 * @param bbg is the basic block graph of <code>theMethod</code>.
	 *
	 * @pre theMethod != null
	 * @pre bbg != null
	 */
	private void process(final SootMethod theMethod, final BasicBlockGraph bbg) {
		method = theMethod;
		workBag.clear();

		processForIntraBasicBlockGotos(bbg);

		final UnitGraph _unitGraph = bbg.getStmtGraph();
		final List _units = IteratorUtils.toList(_unitGraph.iterator());
		final Collection _handlerStmts = new ArrayList();

		// collect the handler statements in the traps
		for (final Iterator _i = _unitGraph.getBody().getTraps().iterator(); _i.hasNext();) {
			final Trap _trap = (Trap) _i.next();
			_handlerStmts.add(_trap.getHandlerUnit());
		}

		final Collection _temp = new HashSet();

		while (workBag.hasWork()) {
			final BasicBlock _bb = (BasicBlock) workBag.getWork();
			final Stmt _leader = _bb.getLeaderStmt();
			final int _lind = _units.indexOf(_leader);
			_temp.add(_bb);

			if (_lind > 0) {
				final Stmt _predStmtOfLeader = (Stmt) _units.get(_lind - 1);
				final List _succsOfPred = _unitGraph.getSuccsOf(_predStmtOfLeader);

				/*
				 * let pred be the predecessor of the leader in the byte code sequence.
				 * if
				 *   - the leader is not a successor of pred
				 * or
				 *   - the leader is a trap handler
				 * or
				 *   - the leader is a successor of pred and pred is a goto statement
				 * then include pred in the slice and add the basic block of pred to the workbag.
				 */
				if (!_succsOfPred.contains(_leader)
					  || _handlerStmts.contains(_leader)
					  || (_succsOfPred.contains(_leader) && _predStmtOfLeader instanceof GotoStmt)) {
					sliceCollector.includeInSlice(_predStmtOfLeader);
				}
			}
		}
	}

	/**
	 * Process the basic block graph to consider intra basic block gotos to reconstruct the control flow.
	 *
	 * @param bbg is the basic block graph containing the basic blocks to be processed.
	 *
	 * @pre bbg != null
	 */
	private void processForIntraBasicBlockGotos(final BasicBlockGraph bbg) {
		for (final Iterator _j = bbg.getNodes().iterator(); _j.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _j.next();
			processForIntraBasicBlockGotos(_bb);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.15  2004/06/16 07:59:35  venku
   - goto processing was skewed. FIXED.
   - note that we should just use specialization in executable case.
   - refactoring.
   Revision 1.14  2004/06/15 10:36:51  venku
   - used units from the graph rather than the body to determine the reachable units.
   Revision 1.13  2004/06/12 06:47:28  venku
   - documentation.
   - refactoring.
   - coding conventions.
   - catered feature request 384, 385, and 386.
   Revision 1.12  2004/05/31 21:38:11  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.11  2004/02/27 12:33:31  venku
   - subtle error when including statement before handler statement. FIXED.
   Revision 1.10  2004/02/27 00:52:52  venku
   - documentation and coding convention.
   Revision 1.9  2004/01/27 01:48:24  venku
   - statements preceeding exception handlers need to be included
     not considering the jump based on exception.  FIXED.
   Revision 1.8  2004/01/27 00:41:34  venku
   - coding convention.
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
