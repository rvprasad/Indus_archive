
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.GotoStmt;
import soot.jimple.Stmt;


/**
 * This class provides the logic required to process the given slice in order to include goto statements such that it
 * realizes the control as in the original program but as required in the slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SliceGotoProcessor {
	/** 
	 * This filter out statements that are not of type <code>GotoStmt</code>.
	 */
	public static final Predicate GOTO_STMT_PREDICATE = PredicateUtils.instanceofPredicate(GotoStmt.class);

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceGotoProcessor.class);

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
	public SliceGotoProcessor(final SliceCollector collector) {
		sliceCollector = collector;
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

			if (_bbg != null) {
				process(_sm, _bbg);
			}
		}
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
	private void process(final SootMethod theMethod, final BasicBlockGraph bbg) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("process(SootMethod theMethod = " + theMethod + ") - BEGIN");
		}

		method = theMethod;

		// process basic blocks to include all gotos in basic blocks with slice statements.
		final Collection _bbInSliceInOrig = processForIntraBasicBlockGotos(bbg);
		final IObjectDirectedGraph _dag = bbg.getDAG();
		final Collection _bbInSliceInDAG = new ArrayList();
		final Iterator _i = _bbInSliceInOrig.iterator();
		final int _iEnd = _bbInSliceInOrig.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			_bbInSliceInDAG.add(_dag.queryNode(_bb));
		}

		// find basic blocks between slice basic blocks to include the gotos in them into the slice.
		final Collection _bbToBeIncludedInSlice = _dag.getNodesOnPathBetween(_bbInSliceInDAG);

		// find basic blocks that are part of cycles (partially or completely) in the slice.
		final Collection _backedges = bbg.getBackEdges();
		final Iterator _k = _backedges.iterator();
		final int _kEnd = _backedges.size();

		for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
			final Pair _edge = (Pair) _k.next();
			_bbInSliceInDAG.clear();
			_bbInSliceInDAG.add(_dag.queryNode(_edge.getFirst()));
			_bbInSliceInDAG.add(_dag.queryNode(_edge.getSecond()));

			final Collection _nodes = _dag.getNodesOnPathBetween(_bbInSliceInDAG);

			if (CollectionUtils.containsAny(_nodes, _bbInSliceInDAG)) {
				_bbToBeIncludedInSlice.addAll(_nodes);
			}
		}

		CollectionUtils.transform(_bbToBeIncludedInSlice, IObjectDirectedGraph.OBJECT_EXTRACTOR);

		// include the gotos in the found basic blocks in the slice.
		final Iterator _j = _bbToBeIncludedInSlice.iterator();
		final int _jEnd = _bbToBeIncludedInSlice.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final BasicBlock _bb = (BasicBlock) _j.next();
			final List _stmtsOf = new ArrayList(_bb.getStmtsOf());
			CollectionUtils.filter(_stmtsOf, GOTO_STMT_PREDICATE);
			sliceCollector.includeInSlice(_stmtsOf);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("process() - END");
		}
	}

	/**
	 * Process the basic block to consider intra basic block gotos to reconstruct the control flow.
	 *
	 * @param bb is the basic block to be processed.
	 * @param bbInSlice is the collection of basic blocks containing atleast one statement in the slice. This is an out
	 * 		  param.
	 *
	 * @pre bb != null and bbInSlice != null
	 * @post bbInSlice.containsAll(bbInSlice$pre)
	 */
	private void processForIntraBasicBlockGotos(final BasicBlock bb, final Collection bbInSlice) {
		for (final Iterator _i = bb.getStmtsOf().iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();

			if (sliceCollector.hasBeenCollected(_stmt)) {
				bbInSlice.add(bb);

				final List _stmtsOf = new ArrayList(bb.getStmtsOf());
				CollectionUtils.filter(_stmtsOf, GOTO_STMT_PREDICATE);
				sliceCollector.includeInSlice(_stmtsOf);
				break;
			}
		}
	}

	/**
	 * Process the basic block graph to consider intra basic block gotos to reconstruct the control flow.
	 *
	 * @param bbg is the basic block graph containing the basic blocks to be processed.
	 *
	 * @return the basic blocks containing atleast one statement in the slice.
	 *
	 * @pre bbg != null
	 * @post result != null and result.oclIsKindOf(Collection(BasicBloc))
	 * @post bbg.getNodes().containsAll(result)
	 */
	private Collection processForIntraBasicBlockGotos(final BasicBlockGraph bbg) {
		final Collection _result = new HashSet();

		for (final Iterator _j = bbg.getNodes().iterator(); _j.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _j.next();
			processForIntraBasicBlockGotos(_bb, _result);
		}
		return _result;
	}
}

// End of File
