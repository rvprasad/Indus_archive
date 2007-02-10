/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.MutableDirectedGraph;
import edu.ksu.cis.indus.common.graph.MutableNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Trap;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

/**
 * This class represents the basic block graph for a given method.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class BasicBlockGraph
		extends MutableDirectedGraph<BasicBlockGraph.BasicBlock> {

	/**
	 * This class represents a basic block in a method.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public final class BasicBlock
			extends MutableNode<BasicBlock> {

		/**
		 * This indicates if this block is an exit block.
		 */
		private final boolean isExitBlock;

		/**
		 * This is the leader statement in the block.
		 */
		@NonNull private final Stmt leaderStmt;

		/**
		 * The list of statements represented by this block.
		 */
		@NonNull @NonNullContainer private final List<Stmt> stmts;

		/**
		 * This is the trailer statement in the block.
		 */
		@NonNull private final Stmt trailerStmt;

		/**
		 * Creates a new BasicBlock object.
		 * 
		 * @param stmtsParam is the list of statements being represented by this block.
		 * @param isAnExitBlock indicates if the trailer statement of this basic block may cause an exception that will result
		 *            in the control exiting the graph.
		 * @pre getStmtGraph().getBody().getUnits().containsAll(stmtsParam)
		 */
		BasicBlock(@NonNull @NonNullContainer @Immutable final List<Stmt> stmtsParam, final boolean isAnExitBlock) {
			super(new HashSet<BasicBlock>(), new HashSet<BasicBlock>());
			stmts = new ArrayList<Stmt>(stmtsParam);
			leaderStmt = stmts.get(0);
			trailerStmt = stmts.get(stmts.size() - 1);
			isExitBlock = isAnExitBlock;
		}

		/**
		 * Retrieves the statement at the leader position.
		 * 
		 * @return the leader statement.
		 */
		@NonNull public Stmt getLeaderStmt() {
			return leaderStmt;
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code>.
		 * 
		 * @param start is the statement starting from which the statements are requested.
		 * @return a modifiable list of <code>Stmt</code>s.
		 * @post (getStmtGraph().getBody().getUnits().indexOf(start) &lt; leader or
		 *       getStmtGraph().getBody().getUnits().indexOf(start) > trailer) implies (result.size() = 0)
		 */
		@Functional @NonNull @NonNullContainer public List<Stmt> getStmtsFrom(final Stmt start) {
			return getStmtsFromTo(start, trailerStmt);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code> till <code>end</code> (inclusive).
		 * 
		 * @param start is the starting statement of the requested statement list.
		 * @param end is the ending statement of the requested statement list.
		 * @return a modifiable list of <code>Stmt</code>s.
		 * @post (getStmtGraph().getBody().getUnits().indexOf(start) &lt; leader or
		 *       getStmtGraph().getBody().getUnits().indexOf(end) > trailer or
		 *       getStmtGraph().getBody().getUnits().indexOf(start) > sgetStmtGraph().getBody().getUnits().indexOf(end))
		 *       implies (result.size() = 0)
		 */
		@Functional @NonNullContainer @NonNull public List<Stmt> getStmtsFromTo(final Stmt start, final Stmt end) {
			final List<Stmt> _result;
			final int _startIndex = stmtList.indexOf(start);
			final int _endIndex = stmtList.indexOf(end);
			final int _leaderIndex = stmtList.indexOf(leaderStmt);
			final int _trailerIndex = stmtList.indexOf(trailerStmt);

			if (_startIndex >= _leaderIndex && _endIndex <= _trailerIndex && _startIndex <= _endIndex) {
				_result = new ArrayList<Stmt>(stmtList.subList(_startIndex, _endIndex + 1));
				_result.retainAll(stmts);
			} else {
				_result = Collections.emptyList();
			}
			return _result;
		}

		/**
		 * Retrieves the statements in this block .
		 * 
		 * @return an unmodifiable list of statements.
		 */
		@Functional @NonNullContainer @NonNull public List<Stmt> getStmtsOf() {
			return Collections.unmodifiableList(stmts);
		}

		/**
		 * Returns the trailer statement of this basic block.
		 * 
		 * @return the trailer statement.
		 */
		@NonNull @NonNullContainer @Functional public Stmt getTrailerStmt() {
			return trailerStmt;
		}

		/**
		 * Checks if the trailer statement of this basic block may cause the control to exit the graph.
		 * 
		 * @return <code>true</code> if the trailer statement of this basic block may cause the control to exit the graph;
		 *         <code>false</code>, otherwise.
		 */
		public boolean isAnExitBlock() {
			return isExitBlock;
		}

		/**
		 * {@inheritDoc}
		 */
		@Functional @NonNull @Override public String toString() {
			return new ToStringBuilder(this).append("stmts", this.stmts).toString();
		}
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(BasicBlockGraph.class);

	/**
	 * The list of statements in the method being represented by this graph.
	 */
	@NonNull @NonNullContainer final List<Stmt> stmtList;

	/**
	 * An array of <code>BasicBlock</code> objects.
	 */
	@NonNull @NonNullContainer private final Map<Stmt, BasicBlock> stmt2BlockMap;

	/**
	 * The control flow graph of the method represented by this graph.
	 */
	@NonNull private final UnitGraph stmtGraph;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theStmtGraph that will be represented by this basic block graph.
	 * @param method that is being represented by this graph. <i>This is required only if exception flow based basic block
	 *            graph is required.</i>
	 * @param analysis to be used for exception based basic block splitting. <i>This is required only if exception flow based
	 *            basic block graph is required.</i>
	 */
	public BasicBlockGraph(@NonNull @Immutable final UnitGraph theStmtGraph, @NonNull @Immutable final SootMethod method,
			@Immutable final IExceptionRaisingInfo analysis) {
		this.stmtGraph = theStmtGraph;

		@SuppressWarnings("unchecked") final List<Stmt> _units = new ArrayList<Stmt>(stmtGraph.getBody().getUnits());
		stmtList = Collections.unmodifiableList(_units);

		final int _numOfStmt = stmtList.size();

		if (_numOfStmt == 0) {
			stmt2BlockMap = Collections.emptyMap();
			return;
		}

		final List<Stmt> _stmts = new ArrayList<Stmt>();
		final IWorkBag<Stmt> _wb = new HistoryAwareLIFOWorkBag<Stmt>(new HashSet<Stmt>());
		_wb.addWork(stmtList.get(0));
		stmt2BlockMap = new HashMap<Stmt, BasicBlock>(_numOfStmt);

		while (_wb.hasWork()) {
			_stmts.clear();

			final Stmt _stmt = _wb.getWork();
			final boolean _isExitBlock = getBasicBlockStmts(_stmt, _wb, _stmts, analysis, method);
			final BasicBlock _bblock = new BasicBlock(_stmts, _isExitBlock);

			for (final Iterator<Stmt> _i = _stmts.iterator(); _i.hasNext();) {
				stmt2BlockMap.put(_i.next(), _bblock);
			}
			addNode(_bblock);
		}
		setupGraph();
	}

	/**
	 * Retreives the statements occurring in the given basic blocks.
	 * 
	 * @param basicBlocks of interest.
	 * @return a collection of statements
	 */
	@NonNull @NonNullContainer public List<Stmt> getEnclosedStmts(
			@NonNull @NonNullContainer @Immutable final Collection<BasicBlock> basicBlocks) {
		final List<Stmt> _result = new ArrayList<Stmt>();
		final Iterator<BasicBlock> _i = basicBlocks.iterator();
		final int _iEnd = basicBlocks.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = _i.next();
			_result.addAll(_bb.getStmtsOf());
		}
		return _result;
	}

	/**
	 * Retreives the basic blocks in which the given statements occur.
	 * 
	 * @param stmts of interest.
	 * @return a collection of basic blocks
	 */
	@NonNull @NonNullContainer public List<BasicBlock> getEnclosingBasicBlocks(
			@NonNull @NonNullContainer @Immutable final Collection<Stmt> stmts) {
		final List<BasicBlock> _result = new ArrayList<BasicBlock>();
		final Iterator<Stmt> _i = stmts.iterator();
		final int _iEnd = stmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _stmt = _i.next();
			final BasicBlock _enclosingBlock = getEnclosingBlock(_stmt);

			if (_enclosingBlock != null) {
				_result.add(_enclosingBlock);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("getEnclosedBasicBlocks() - One of the given statement is not represented by any"
							+ "block in the graph" + _stmt);
				}
			}
		}
		return _result;
	}

	/**
	 * Retrieve the basic block enclosing the given statement.
	 * 
	 * @param stmt is the statement of interest.
	 * @return the basic block enclosing the statement.
	 */
	@Functional public BasicBlock getEnclosingBlock(final Stmt stmt) {
		return stmt2BlockMap.get(stmt);
	}

	/**
	 * Retrieves the basic blocks at which the exception handlers begin.
	 * 
	 * @return the exception handler basic blocks.
	 */
	@NonNull @NonNullContainer @Functional public Collection<BasicBlock> getHandlerBlocks() {
		Collection<BasicBlock> _handlerBlocks;
		@SuppressWarnings("unchecked") final Collection<Trap> _traps = stmtGraph.getBody().getTraps();

		if (!_traps.isEmpty()) {
			_handlerBlocks = new HashSet<BasicBlock>();

			for (final Iterator<Trap> _i = _traps.iterator(); _i.hasNext();) {
				final BasicBlock _block = getEnclosingBlock((Stmt) (_i.next()).getHandlerUnit());

				if (_block != null) {
					_handlerBlocks.add(_block);
				}
			}
		} else {
			_handlerBlocks = Collections.emptySet();
		}

		return _handlerBlocks;
	}

	/**
	 * Return the head node of this graph. Basic Block graphs in general have a single head and this contains the first
	 * statement reachable in the CFG of the method. Hence, this method returns the basic block enclosing the first statement
	 * of the method if the underlying CFG contain the first statement. If the method is native, <code>null</code> is
	 * returned.
	 * <p>
	 * Note that the head returned by this method may not be the head of the graph. An example is a method that starts with a
	 * <code>while</code> loop whose index variable is a field of the enclosing class. In this case, the first statement in
	 * the method body does have a predecessor, hence, there can be no "head" as defined for graphs.
	 * </p>
	 * 
	 * @return the head node
	 */
	@Functional public BasicBlock getHead() {
		final Collection<BasicBlock> _heads = getSources();
		BasicBlock _result = null;

		if (_heads.size() == 1) {
			_result = _heads.iterator().next();
		} else {
			final Stmt _stmt = (Stmt) stmtGraph.getBody().getUnits().getFirst();
			@SuppressWarnings("unchecked") final Iterator<Stmt> _i = stmtGraph.iterator();
			for (; _i.hasNext();) {
				final Stmt _t = _i.next();

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
	 */
	@NonNull @Functional public UnitGraph getStmtGraph() {
		return stmtGraph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @Override public String toString() {
		return new ToStringBuilder(this).append("blocks", getNodes()).toString();
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
	 * @return <code>true</code> if the trailer statement of the detected basic block may cause the control to exit the
	 *         graph; <code>false</code>, otherwise.
	 */
	private boolean getBasicBlockStmts(@NonNull @NonNullContainer @Immutable final Stmt leaderStmt,
			@NonNull final IWorkBag<Stmt> wb, @NonNull @NonNullContainer final List<Stmt> stmts,
			@Immutable final IExceptionRaisingInfo analysis, @NonNull @Immutable final SootMethod method) {
		stmts.add(leaderStmt);

		@SuppressWarnings("unchecked") final Collection<Stmt> _t = stmtGraph.getSuccsOf(leaderStmt);
		int _size = _t.size();
		boolean _throwsUncaughtException = analysis != null && analysis.doesStmtThrowUncaughtException(leaderStmt, method);

		if (_size == 1 && !_throwsUncaughtException) {
			Stmt _pred = leaderStmt;
			Stmt _stmt = _t.iterator().next();

			while (true) {
				@SuppressWarnings("unchecked") final Collection<Stmt> _preds = stmtGraph.getPredsOf(_stmt);

				// if this statement has multiple predecessor then it marks the boundary of a basic block.
				if (_preds.size() > 1) {
					wb.addWorkNoDuplicates(_stmt);
					break;
				}

				@SuppressWarnings("unchecked") final Collection<Stmt> _succs = stmtGraph.getSuccsOf(_stmt);
				_size = _succs.size();
				_throwsUncaughtException = analysis != null && analysis.doesStmtThrowUncaughtException(_stmt, method);

				if (_size == 1 && !_throwsUncaughtException) {
					if (stmts.contains(_stmt)) {
						// if we did come around a self-loop then the basic block cannot be extended further
						break;
					}
					stmts.add(_stmt);
					_pred = _stmt;
					_stmt = (Stmt) stmtGraph.getSuccsOf(_pred).get(0);
				} else {
					stmts.add(_stmt);

					// if there are multiple successors then it marks the boundary of a basic block.
					if (_size > 1 || _throwsUncaughtException) {
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
		for (final Iterator<BasicBlock> _i = getNodes().iterator(); _i.hasNext();) {
			final BasicBlock _block = _i.next();
			final Stmt _stmt = _block.getTrailerStmt();

			for (@SuppressWarnings("unchecked") final Iterator<Stmt> _j = stmtGraph.getSuccsOf(_stmt).iterator(); _j
					.hasNext();) {
				final Stmt _succ = _j.next();
				final BasicBlock _succBlock = getEnclosingBlock(_succ);

				if (_succBlock != null) {
					addEdgeFromTo(_block, _succBlock);
				}
			}
		}
		shapeChanged();
	}
}

// End of File
