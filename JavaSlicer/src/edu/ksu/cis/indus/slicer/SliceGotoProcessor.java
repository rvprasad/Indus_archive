
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

import soot.SootMethod;

import soot.jimple.GotoStmt;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.IWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.LIFOWorkBag;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SliceGotoProcessor
  implements IGotoProcessor {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final TaggingBasedSliceCollector sliceCollector;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection taggedBB = new HashSet();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private SootMethod method;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final boolean backwardSlice;

	/**
	 * Creates a new SliceGotoProcessor object.
	 *
	 * @param collector DOCUMENT ME!
	 * @param backward DOCUMENT ME!
	 */
	public SliceGotoProcessor(final TaggingBasedSliceCollector collector, final boolean backward) {
		sliceCollector = collector;
		backwardSlice = backward;
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#postprocess()
	 */
	public void postprocess() {
		String tagName = sliceCollector.getTagName();

		IWorkBag workBag = new LIFOWorkBag();
		Collection processed = new HashSet();

		while (workBag.hasWork()) {
			BasicBlock bb = (BasicBlock) workBag.getWork();
			processed.add(bb);

			Stmt trailer;
			Collection succs;

			if (backwardSlice) {
				trailer = bb.getTrailerStmt();
				succs = bb.getSuccsOf();
			} else {
				trailer = bb.getLeaderStmt();
				succs = bb.getPredsOf();
			}

			if (!CollectionUtils.intersection(taggedBB, succs).isEmpty()
				  && trailer.getTag(tagName) == null
				  && trailer instanceof GotoStmt) {
				sliceCollector.transform(trailer, method);
				process(bb);

				if (!processed.contains(bb)) {
					workBag.addWorkNoDuplicates(bb);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#process(soot.SootMethod)
	 */
	public void preprocess(final SootMethod theMethod) {
		method = theMethod;
		taggedBB.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#process(BasicBlock)
	 */
	public void process(final BasicBlock bb) {
		boolean tagged = false;
		String tagName = sliceCollector.getTagName();
		List list = new ArrayList(bb.getStmtsOf());

		if (backwardSlice) {
			Collections.reverse(list);
		}

		for (Iterator i = list.iterator(); i.hasNext();) {
			Stmt stmt = (Stmt) i.next();

			if (stmt.getTag(tagName) != null) {
				tagged = true;
				taggedBB.add(bb);
			} else if (stmt instanceof GotoStmt && tagged) {
				sliceCollector.transform(stmt, method);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
