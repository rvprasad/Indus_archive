
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
import edu.ksu.cis.indus.common.graph.AbstractMutableDirectedGraph;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNodeGraphBuilder;

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
  extends AbstractMutableDirectedGraph {
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
	 * The collection of basic block nodes in this graph.
	 *
	 * @invariant blocks.oclIsKindOf(Sequence(BasicBlock))
	 */
	private final List blocks;

	/** 
	 * An array of <code>BasicBlock</code> objects.
	 */
	private final Map stmt2BlockMap;

	/** 
	 * The control flow graph of the method represented by this graph.
	 */
	private final UnitGraph stmtGraph;

	/**
	 * Creates a new BasicBlockGraph object.
	 *
	 * @param theStmtGraph is the control flow graph being represented by this graph.
	 *
	 * @pre theStmtGraph != null
	 */
	BasicBlockGraph(final UnitGraph theStmtGraph) {
		this.stmtGraph = theStmtGraph;
		stmtList = Collections.unmodifiableList(new ArrayList(stmtGraph.getBody().getUnits()));

		final int _numOfStmt = stmtList.size();

		if (_numOfStmt == 0) {
			blocks = Collections.EMPTY_LIST;
			stmt2BlockMap = Collections.EMPTY_MAP;
			return;
		}

		final List _stmts = new ArrayList();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addWork(stmtList.get(0));
		blocks = new ArrayList();
		stmt2BlockMap = new HashMap(_numOfStmt);

		while (_wb.hasWork()) {
			final Stmt _stmt = (Stmt) _wb.getWork();
			_stmts.clear();
			getBasicBlockStmtsInto(_stmt, _wb, _stmts);

			final BasicBlock _bblock = new BasicBlock(_stmts);

			for (final Iterator _i = _stmts.iterator(); _i.hasNext();) {
				stmt2BlockMap.put(_i.next(), _bblock);
			}
			blocks.add(_bblock);
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
	  extends AbstractMutableDirectedGraph.AbstractMutableNode {
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
		 * Creates a new BasicBlock object.
		 *
		 * @param stmtsParam is the list of statements being represented by this block.
		 *
		 * @pre stmtsParam != null and stmsParam.oclIsKindOf(Sequence(Stmt))
		 * @pre getStmtGraph().getBody().getUnits().containsAll(stmtsParam)
		 */
		BasicBlock(final List stmtsParam) {
			super(new HashSet(), new HashSet());
			stmts = new ArrayList(stmtsParam);
			leaderStmt = (Stmt) stmts.get(0);
			trailerStmt = (Stmt) stmts.get(stmts.size() - 1);
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
	 * Returns the nodes in the graph.
	 *
	 * @return an unmodifiable list of <code>BasicBlocks</code> that make up the nodes in the graph.
	 *
	 * @post result != null
	 *
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraph#getNodes()
	 */
	public List getNodes() {
		return Collections.unmodifiableList(blocks);
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
		return new ToStringBuilder(this).append("blocks", this.blocks).toString();
	}

	/**
	 * @see AbstractMutableDirectedGraph#containsNode(edu.ksu.cis.indus.common.graph.INode)
	 */
	protected boolean containsNode(final INode node) {
		return blocks.contains(node);
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
	 *
	 * @pre leaderStmt != null and wb != null and stmts != null
	 * @pre stmts.oclIsKindOf(Collection(Stmt))
	 */
	private void getBasicBlockStmtsInto(final Stmt leaderStmt, final IWorkBag wb, final List stmts) {
		Stmt _stmt = leaderStmt;
		Stmt _pred = leaderStmt;

		while (true) {
			final Collection _preds = stmtGraph.getPredsOf(_stmt);
			final Collection _succs = stmtGraph.getSuccsOf(_stmt);
			final int _succsSize = _succs.size();

			if (_preds.size() > 1 && _pred != _stmt) {
				wb.addWorkNoDuplicates(_stmt);
				break;
			}
			stmts.add(_stmt);

			if (_succsSize > 1 || _succsSize == 0) {
				if (_succsSize > 1) {
					wb.addAllWorkNoDuplicates(_succs);
				}
				break;
			}
			_pred = _stmt;
			_stmt = (Stmt) stmtGraph.getSuccsOf(_pred).get(0);
		}
	}

	/**
	 * Sets up the blocks into a graph.
	 */
	private void setupGraph() {
		// Connect the nodes of the graph.
		for (final Iterator _i = blocks.iterator(); _i.hasNext();) {
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

		// Setup the tails of the graph.
		for (final Iterator _i = stmtGraph.getTails().iterator(); _i.hasNext();) {
			final BasicBlock _block = getEnclosingBlock((Stmt) _i.next());

			if (_block != null) {
				tails.add(_block);
			}
		}
	}
}

// End of File
