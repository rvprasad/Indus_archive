
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;

import edu.ksu.cis.indus.slicer.SliceCollector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import soot.SootMethod;

import soot.jimple.GotoStmt;
import soot.jimple.Stmt;


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
	 * The slice collector.
	 */
	private final SliceCollector sliceCollector;

	/**
	 * The collection of basic blocks that contained atleast one statement that is tagged.
	 */
	private Collection taggedBB = new HashSet();

	/**
	 * A workbag.
	 */
	private final IWorkBag workBag = new LIFOWorkBag();

	/**
	 * The method being processed.
	 */
	private SootMethod method;

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
	 * Preprocess the current method's body for goto-based control flow retention.
	 *
	 * @param theMethod to be processed.
	 * @param bbg is the basic block graph of <code>theMethod</code>.
	 *
	 * @pre theMethod != null
	 * @pre bbg != null
	 */
	public final void process(final SootMethod theMethod, final BasicBlockGraph bbg) {
		method = theMethod;
		taggedBB.clear();

		for (final Iterator _j = bbg.getNodes().iterator(); _j.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _j.next();
			processBasicBlock(_bb);
		}
		postprocess();
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
	public void process(final Collection methods, final BasicBlockGraphMgr bbgMgr) {
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
	 * Retrieve the last statements and the successors basic blocks containing the statements following these statements. The
	 * subclass determine how the statements and the successors are picked.  This information is returned as pair of
	 * <code>Stmt</code> and <code>Collection</code> of successors.
	 *
	 * @param bb is the basic block.
	 *
	 * @return a collection of pairs of statement and a collection of basic blocks.
	 *
	 * @pre bb != null
	 * @post result.oclIsKindOf(Collection(Pair(Stmt, Collection(BasicBlockGraph)))
	 * @post result->forall(o | o != null)
	 */
	protected abstract Collection getLastStmtAndSuccsOfBasicBlock(final BasicBlock bb);

	/**
	 * Retrieve the statements of the given basic block in an order.  The subclasses determine the order.
	 *
	 * @param bb is the basic block.
	 *
	 * @return a list of statements that occur in <code>bb</code> in a suitable order.
	 *
	 * @pre bb != null
	 * @post result != null and result.oclIsKindOf(Sequence(Stmt))
	 */
	protected abstract List getStmtsOfForProcessing(final BasicBlock bb);

	/**
	 * Postprocess the current method's body for goto-based control flow retention.
	 */
	private final void postprocess() {
		final String _tagName = sliceCollector.getTagName();
		final Collection _processed = new HashSet();

		while (workBag.hasWork()) {
			final BasicBlock _bb = (BasicBlock) workBag.getWork();
			_processed.add(_bb);

			final Collection _stmtSuccPairs = getLastStmtAndSuccsOfBasicBlock(_bb);

			for (Iterator _i = _stmtSuccPairs.iterator(); _i.hasNext();) {
				final Pair _pair = (Pair) _i.next();
				final Stmt _stmt = (Stmt) _pair.getFirst();
				final Collection _succs = (Collection) _pair.getSecond();

				if (!CollectionUtils.intersection(taggedBB, _succs).isEmpty()
					  && _stmt.getTag(_tagName) == null
					  && _stmt instanceof GotoStmt) {
					sliceCollector.includeInSlice(_stmt);
					sliceCollector.includeInSlice(method);
					processBasicBlock(_bb);

					if (!_processed.contains(_bb)) {
						workBag.addWorkNoDuplicates(_bb);
					}
				}
			}
		}
		workBag.clear();
	}

	/**
	 * Process the given basic block that belongs to the current method.
	 *
	 * @param bb is the basic block to be processed.
	 *
	 * @pre bb != null
	 */
	private final void processBasicBlock(final BasicBlock bb) {
		boolean _tagged = false;
		final String _tagName = sliceCollector.getTagName();
		final List _list = getStmtsOfForProcessing(bb);

		for (final Iterator _i = _list.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();

			if (_stmt.getTag(_tagName) != null) {
				_tagged = true;
				taggedBB.add(bb);
			} else if (_stmt instanceof GotoStmt && _tagged) {
				sliceCollector.includeInSlice(_stmt);
				sliceCollector.includeInSlice(method);
			}
		}

		if (_tagged) {
			workBag.addWork(bb);
		}
	}
}

/*
   ChangeLog:
   $Log$
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
