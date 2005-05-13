
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.MutableDirectedGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNodeGraphBuilder;

import edu.ksu.cis.indus.interfaces.IExceptionRaisingInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Trap;

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This class represents the basic block graph for a given method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class BasicBlockGraph
  extends MutableDirectedGraph {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(BasicBlockGraph.class);

	/** 
	 * The list of statements in the method being represented by this graph.
	 *
	 * @invariant stmtList.oclIsKindOf(Sequence(Stmt))
	 */
	final List stmtList;

	/** 
	 * An array of <code>BasicBlock</code> objects.
	 */
	private final Map stmt2BlockMap;

	/** 
	 * The control flow graph of the method represented by this graph.
	 */
	private final UnitGraph stmtGraph;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theStmtGraph that will be represented by this basic block graph.
	 * @param method that is being represented by this graph.  <i>This is required only if exception flow based basic block
	 * 		  graph is required.</i>
	 * @param analysis to be used for exception based basic block splitting.  <i>This is required only if exception flow
	 * 		  based basic block graph is required.</i>
	 *
	 * @pre theStmtGraph != null
	 */
	public BasicBlockGraph(final UnitGraph theStmtGraph, final SootMethod method, final IExceptionRaisingInfo analysis) {
		this.stmtGraph = theStmtGraph;
		stmtList = Collections.unmodifiableList(new ArrayList(stmtGraph.getBody().getUnits()));

		final int _numOfStmt = stmtList.size();

		if (_numOfStmt == 0) {
			stmt2BlockMap = Collections.EMPTY_MAP;
			return;
		}

		final List _stmts = new ArrayList();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addWork(stmtList.get(0));
		stmt2BlockMap = new HashMap(_numOfStmt);

		while (_wb.hasWork()) {
			_stmts.clear();

			final Stmt _stmt = (Stmt) _wb.getWork();
			final boolean _isExitBlock = getBasicBlockStmtsInto(_stmt, _wb, _stmts, analysis, method);
			final BasicBlock _bblock = new BasicBlock(_stmts, _isExitBlock);

			for (final Iterator _i = _stmts.iterator(); _i.hasNext();) {
				stmt2BlockMap.put(_i.next(), _bblock);
			}
			addNode(_bblock);
		}
		setupGraph();
	}

	/**
	 * This class represents a basic block in a method.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public final class BasicBlock
	  extends MutableDirectedGraph.MutableNode {
		/** 
		 * The list of statements represented by this block.
		 *
		 * @invariant stmts.oclIsKindOf(Sequence(Stmt))
		 */
		private final List stmts;

		/** 
		 * This is the leader statement in the block.
		 */
		private final Stmt leaderStmt;

		/** 
		 * This is the trailer statement in the block.
		 */
		private final Stmt trailerStmt;

		/** 
		 * This indicates if this block is an exit block.
		 */
		private final boolean isExitBlock;

		/**
		 * Creates a new BasicBlock object.
		 *
		 * @param stmtsParam is the list of statements being represented by this block.
		 * @param isAnExitBlock indicates if the trailer statement of this basic block may cause an exception that will
		 * 		  result in the control exiting the graph.
		 *
		 * @pre stmtsParam != null and stmsParam.oclIsKindOf(Sequence(Stmt))
		 * @pre getStmtGraph().getBody().getUnits().containsAll(stmtsParam)
		 */
		BasicBlock(final List stmtsParam, final boolean isAnExitBlock) {
			super(new HashSet(), new HashSet());
			stmts = new ArrayList(stmtsParam);
			leaderStmt = (Stmt) stmts.get(0);
			trailerStmt = (Stmt) stmts.get(stmts.size() - 1);
			isExitBlock = isAnExitBlock;
		}

		/**
		 * Checks if the trailer statement of this basic block may cause the control to exit the graph.
		 *
		 * @return <code>true</code> if the trailer statement of this basic block may cause the control to exit the graph;
		 * 		   <code>false</code>, otherwise.
		 */
		public boolean isAnExitBlock() {
			return isExitBlock;
		}

		/**
		 * Retrieves the statement at the leader position.
		 *
		 * @return the leader statement.  <code>null</code> if no leader statement exists.
		 */
		public Stmt getLeaderStmt() {
			return leaderStmt;
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code>.
		 *
		 * @param start is the statement starting from which the statements are requested.
		 *
		 * @return a modifiable list of <code>Stmt</code>s.
		 *
		 * @post result != null
		 * @post (getStmtGraph().getBody().getUnits().indexOf(start) &lt; leader or
		 * 		 getStmtGraph().getBody().getUnits().indexOf(start) > trailer) implies (result.size() = 0)
		 */
		public List getStmtsFrom(final Stmt start) {
			return getStmtsFromTo(start, trailerStmt);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code> till <code>end</code> (inclusive).
		 *
		 * @param start is the starting statement of the requested statement list.
		 * @param end is the ending statement of the requested statement list.
		 *
		 * @return a modifiable list of <code>Stmt</code>s.
		 *
		 * @post result != null and result.oclIsKindOf(Sequence(Stmt))
		 * @post (getStmtGraph().getBody().getUnits().indexOf(start) &lt; leader or
		 * 		 getStmtGraph().getBody().getUnits().indexOf(end) > trailer or
		 * 		 getStmtGraph().getBody().getUnits().indexOf(start) > sgetStmtGraph().getBody().getUnits().indexOf(end))
		 * 		 implies (result.size() = 0)
		 */
		public List getStmtsFromTo(final Stmt start, final Stmt end) {
			final List _result;
			final int _startIndex = stmtList.indexOf(start);
			final int _endIndex = stmtList.indexOf(end);
			final int _leaderIndex = stmtList.indexOf(leaderStmt);
			final int _trailerIndex = stmtList.indexOf(trailerStmt);

			if (_startIndex >= _leaderIndex && _endIndex <= _trailerIndex && _startIndex <= _endIndex) {
				_result = new ArrayList(stmtList.subList(_startIndex, _endIndex + 1));
				_result.retainAll(stmts);
			} else {
				_result = Collections.EMPTY_LIST;
			}
			return _result;
		}

		/**
		 * Retrieves the statements in this block .
		 *
		 * @return an unmodifiable list of statements.
		 *
		 * @post result.oclIsKindOf(Sequence(Stmt))
		 */
		public List getStmtsOf() {
			return Collections.unmodifiableList(stmts);
		}

		/**
		 * Returns the trailer statement of this basic block.
		 *
		 * @return the trailer statement. <code>null</code> if no leader statement exists.
		 */
		public Stmt getTrailerStmt() {
			return trailerStmt;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return new ToStringBuilder(this).append("stmts", this.stmts).toString();
		}
	}

	/**
	 * Retreives the statements occurring in the given basic blocks.
	 *
	 * @param basicBlocks of interest.
	 *
	 * @return a collection of statements
	 *
	 * @pre basicBlocks != null and basicBlocks.oclIsKindOf(Collection(BasicBlock))
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	public List getEnclosedStmts(final Collection basicBlocks) {
		final List _result = new ArrayList();
		final Iterator _i = basicBlocks.iterator();
		final int _iEnd = basicBlocks.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			_result.addAll(_bb.getStmtsOf());
		}
		return _result;
	}

	/**
	 * Retreives the basic blocks in which the given statements occur.
	 *
	 * @param stmts of interest.
	 *
	 * @return a collection of basic blocks
	 *
	 * @pre stmts != null and stmts.oclIsKindOf(Collection(Stmt))
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 */
	public List getEnclosingBasicBlocks(final Collection stmts) {
		final List _result = new ArrayList();
		final Iterator _i = stmts.iterator();
		final int _iEnd = stmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _stmt = (Stmt) _i.next();
			final BasicBlock _enclosingBlock = getEnclosingBlock(_stmt);

			if (_enclosingBlock != null) {
				_result.add(_enclosingBlock);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(
						"getEnclosedBasicBlocks() - One of the given statement is not represented by any block in the graph"
						+ _stmt);
				}
			}
		}
		return _result;
	}

	/**
	 * Retrieve the basic block enclosing the given statement.
	 *
	 * @param stmt is the statement of interest.
	 *
	 * @return the basic block enclosing the statement.
	 *
	 * @pre stmt != null
	 */
	public BasicBlock getEnclosingBlock(final Stmt stmt) {
		return (BasicBlock) stmt2BlockMap.get(stmt);
	}

	/**
	 * Retrieves the basic blocks at which the exception handlers begin.
	 *
	 * @return the exception handler basic blocks.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 */
	public Collection getHandlerBlocks() {
		Collection _handlerBlocks;

		final Collection _traps = stmtGraph.getBody().getTraps();

		if (!_traps.isEmpty()) {
			_handlerBlocks = new HashSet();

			for (final Iterator _i = _traps.iterator(); _i.hasNext();) {
				final BasicBlock _block = getEnclosingBlock((Stmt) ((Trap) _i.next()).getHandlerUnit());

				if (_block != null) {
					_handlerBlocks.add(_block);
				}
			}
		} else {
			_handlerBlocks = Collections.EMPTY_LIST;
		}

		return _handlerBlocks;
	}

	/**
	 * Return the head node of this graph.  Basic Block graphs in general have a single head and this contains the first
	 * statement reachable in the CFG of the method.  Hence, this method returns the basic block enclosing the first
	 * statement of the method if the underlying CFG contain the first statement. Otherwise, it returns <code>null</code>.
	 * If the method is native, <code>null</code> is returned.
	 * 
	 * <p>
	 * Note that the head returned by this method may not be the head of the graph. An example is  a method that starts with
	 * a <code>while</code> loop whose index variable is a field of the enclosing class. In this case, the first statement
	 * in the method body does have a predecessor, hence, there can be no "head" as defined for graphs.
	 * </p>
	 *
	 * @return the head node
	 */
	public BasicBlock getHead() {
		final Collection _heads = getHeads();
		BasicBlock _result = null;

		if (_heads.size() == 1) {
			_result = (BasicBlock) _heads.iterator().next();
		} else {
			final Stmt _stmt = (Stmt) stmtGraph.getBody().getUnits().getFirst();

			for (final Iterator _i = stmtGraph.iterator(); _i.hasNext();) {
				final Stmt _t = (Stmt) _i.next();

				if (_stmt.equals(_t)) {
					_result = getEnclosingBlock(_stmt);
					break;
				}
			}
		}
		return _result;
	}

	/**
	 * Retrieves the statement graph represented by this basic block graph.
	 *
	 * @return the statement graph.
	 *
	 * @post result != null
	 */
	public UnitGraph getStmtGraph() {
		return stmtGraph;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("blocks", getNodes()).toString();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.AbstractDirectedGraph#setupGraphBuilder()
	 */
	protected void setupGraphBuilder() {
		builder = new SimpleNodeGraphBuilder();
	}

	/**
	 * Retrieves the statements of the basic block being processed. <code>stmts</code> is filled with the statements that
	 * form the current basic block graph.
	 *
	 * @param leaderStmt from which the rest of the basic block body should be discovered.
	 * @param wb is the workbag into which new leader statement is added.
	 * @param stmts will contain the statements that make up the current basic block graph.
	 * @param analysis that is used to detect exception flow based control point.
	 * @param method for which the basic block graph is being constructed.
	 *
	 * @return <code>true</code> if the trailer statement of the detected basic block may cause the control to exit the
	 * 		   graph; <code>false</code>, otherwise.
	 *
	 * @pre leaderStmt != null and wb != null and stmts != null
	 * @pre stmts.oclIsKindOf(Collection(Stmt))
	 */
	private boolean getBasicBlockStmtsInto(final Stmt leaderStmt, final IWorkBag wb, final List stmts,
		final IExceptionRaisingInfo analysis, final SootMethod method) {
		stmts.add(leaderStmt);

		final Collection _t = stmtGraph.getSuccsOf(leaderStmt);
		final int _size = _t.size();
		boolean _throwsUncaughtException = analysis != null && analysis.doesStmtThrowUncaughtException(leaderStmt, method);

		if (_size == 1 && !_throwsUncaughtException) {
			Stmt _pred = leaderStmt;
			Stmt _stmt = (Stmt) _t.iterator().next();

			while (true) {
				final Collection _preds = stmtGraph.getPredsOf(_stmt);

				// if this statement has multiple predecessor then it marks the boundary of a basic block.
				if (_preds.size() > 1) {
					wb.addWorkNoDuplicates(_stmt);
					break;
				}

				final Collection _succs = stmtGraph.getSuccsOf(_stmt);
				final int _succsSize = _succs.size();
				_throwsUncaughtException = analysis != null && analysis.doesStmtThrowUncaughtException(_stmt, method);

				if (_succsSize == 1 && !_throwsUncaughtException) {
					// check if we did not come around basic block involved in a self-loop (a->a)
					if (!stmts.contains(_stmt)) {
						stmts.add(_stmt);
						_pred = _stmt;
						_stmt = (Stmt) stmtGraph.getSuccsOf(_pred).get(0);
					} else {
						// if we did come around a self-loop then the basic block cannot be extended further
						break;
					}
				} else {
					stmts.add(_stmt);

					// if there are multiple successors then it marks the boundary of a basic block.
					if (_succsSize > 1 || _throwsUncaughtException) {
						wb.addAllWorkNoDuplicates(_succs);
					}
					break;
				}
			}
		} else if (_size > 1 || _throwsUncaughtException) {
			wb.addAllWorkNoDuplicates(_t);
		}
		return _throwsUncaughtException || _size == 0;
	}

	/**
	 * Sets up the blocks into a graph.
	 */
	private void setupGraph() {
		// Connect the nodes of the graph.
		for (final Iterator _i = getNodes().iterator(); _i.hasNext();) {
			final BasicBlock _block = (BasicBlock) _i.next();
			final Stmt _stmt = _block.getTrailerStmt();

			for (final Iterator _j = stmtGraph.getSuccsOf(_stmt).iterator(); _j.hasNext();) {
				final Stmt _nStmt = (Stmt) _j.next();
				final BasicBlock _nBlock = getEnclosingBlock(_nStmt);

				if (_nBlock != null) {
					addEdgeFromTo(_block, _nBlock);
				}
			}
		}

		// Setup the head of the graph.
		final List _hds = stmtGraph.getHeads();

		if (!_hds.isEmpty()) {
			final BasicBlock _enclosingBlock = getEnclosingBlock((Stmt) _hds.get(0));

			if (_enclosingBlock != null) {
				heads.add(_enclosingBlock);
			}
		}

		shapeChanged();
	}
}

// End of File
