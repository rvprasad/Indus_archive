
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
import edu.ksu.cis.indus.common.graph.IWorkBag;
import edu.ksu.cis.indus.common.graph.LIFOWorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import soot.SootMethod;

import soot.jimple.GotoStmt;
import soot.jimple.Stmt;


/**
 * This class processes the given slice  to include goto statements such that it realizes the control as in the original
 * program but as required in the slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SliceGotoProcessor
  implements IGotoProcessor {
	/**
	 * The slice collector.
	 */
	private final TaggingBasedSliceCollector sliceCollector;

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
	 * This indicates if the slice is a backward slice or a forward slice. <code>true</code> indicates backward slice and
	 * <code>false</code> indicates forward slice.
	 */
	private final boolean backwardSlice;

	/**
	 * Creates a new SliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 * @param backward <code>true</code> indicates the slice is backward; <code>false</code>, otherwise.
	 *
	 * @pre collector != null
	 */
	public SliceGotoProcessor(final TaggingBasedSliceCollector collector, final boolean backward) {
		sliceCollector = collector;
		backwardSlice = backward;
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#postprocess()
	 */
	public final void postprocess() {
		final String _tagName = sliceCollector.getTagName();
		final Collection _processed = new HashSet();

		while (workBag.hasWork()) {
			final BasicBlock _bb = (BasicBlock) workBag.getWork();
			_processed.add(_bb);

			Stmt _trailer;
			Collection _succs;

			if (backwardSlice) {
				_trailer = _bb.getTrailerStmt();
				_succs = _bb.getSuccsOf();
			} else {
				_trailer = _bb.getLeaderStmt();
				_succs = _bb.getPredsOf();
			}

			if (!CollectionUtils.intersection(taggedBB, _succs).isEmpty()
				  && _trailer.getTag(_tagName) == null
				  && _trailer instanceof GotoStmt) {
				sliceCollector.includeInSlice(_trailer);
				sliceCollector.includeInSlice(method);
				process(_bb);

				if (!_processed.contains(_bb)) {
					workBag.addWorkNoDuplicates(_bb);
				}
			}
		}
		workBag.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#process(soot.SootMethod)
	 */
	public final void preprocess(final SootMethod theMethod) {
		method = theMethod;
		taggedBB.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#process(BasicBlock)
	 */
	public final void process(final BasicBlock bb) {
		boolean _tagged = false;
		final String _tagName = sliceCollector.getTagName();
		final List _list = new ArrayList(bb.getStmtsOf());

		if (backwardSlice) {
			Collections.reverse(_list);
		}

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
		workBag.addWork(bb);
	}
}

/*
   ChangeLog:
   $Log$
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
