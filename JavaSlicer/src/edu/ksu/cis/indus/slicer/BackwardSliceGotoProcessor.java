
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

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * This implementation handles statements of the basic block as required for goto processing of backward slices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class BackwardSliceGotoProcessor
  extends AbstractSliceGotoProcessor {
	/**
	 * Creates a new BackwardSliceGotoProcessor object.
	 *
	 * @param collector collects the slice.
	 *
	 * @pre collector != null
	 */
	protected BackwardSliceGotoProcessor(TaggingBasedSliceCollector collector) {
		super(collector);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.AbstractSliceGotoProcessor#postProcessBasicBlock(BasicBlock)
	 */
	protected Collection getLastStmtAndSuccsOfBasicBlock(final BasicBlock bb) {
		return Collections.singleton(new Pair(bb.getTrailerStmt(), bb.getSuccsOf()));
	}

	/**
	 * @see AbstractSliceGotoProcessor#getStmtsOfForProcessing(BasicBlock)
	 */
	protected List getStmtsOfForProcessing(BasicBlock bb) {
		List _result = new ArrayList(bb.getStmtsOf());
		Collections.reverse(_result);
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
 */
