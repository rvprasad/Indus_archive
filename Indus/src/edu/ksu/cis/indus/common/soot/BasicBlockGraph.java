
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.AbstractMutableDirectedGraph;
import edu.ksu.cis.indus.common.graph.INode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

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
		 * Retrieves the statements in this block starting from <code>start</code> till <code>end</code>. 
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
		public List getStmtFromTo(final Stmt start, final Stmt end) {
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
			return getStmtFromTo(start, trailerStmt);
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
	 * @see AbstractMutableDirectedGraph#containsNodes(edu.ksu.cis.indus.common.graph.INode)
	 */
	protected boolean containsNodes(final INode node) {
		return blocks.contains(node);
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

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/07/16 05:37:17  venku
   - changed index based queries to statement based queries.

   Revision 1.4  2004/07/07 06:25:08  venku
   - the way statement sub list was constructed in the basic block was incorrect.  FIXED.
   - ripple effect.
   Revision 1.3  2004/07/04 11:52:42  venku
   - renamed getStmtFrom() to getStmtsFrom().
   Revision 1.2  2004/07/04 11:09:01  venku
   - headless and multiple headed methods cause issue with statement graphs and basic blocks.  FIXED.
   Revision 1.1  2004/05/31 21:38:12  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.16  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.15  2004/02/24 22:25:56  venku
   - documentation
   Revision 1.14  2004/02/23 09:09:02  venku
   - the unit graph may not connect all units occurring in the graph.
     Hence, care is taken while constructing the graph structure.
   Revision 1.13  2004/01/25 09:00:58  venku
   - coding convention.
   Revision 1.12  2004/01/25 03:20:52  venku
   - getLeaderStmt()/getTrailerStmt() will return null in case there
     are not leader/trailter statements.
   Revision 1.11  2004/01/22 09:00:54  venku
   - getHead assumed that the basic block graph had some nodes.
     This is not true for native methods. FIXED.
   Revision 1.10  2004/01/19 13:30:06  venku
   - simplified the logic in getStmtsFromTo().
   Revision 1.9  2004/01/19 13:06:12  venku
   - in getStmtsFrom() it retrieved the next statement if start == end. FIXED.
   Revision 1.8  2004/01/17 00:38:13  venku
   - documentation.
   Revision 1.7  2004/01/06 00:53:36  venku
   - coding conventions.
   Revision 1.6  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.5  2003/12/31 10:43:08  venku
   - size() was unused in IDirectedGraph, hence, removed it.
     Ripple effect.
   Revision 1.4  2003/12/28 01:02:38  venku
   - removed field handlerBlocks as it was only used in one method.
     The blocks are generated on the fly.
   Revision 1.3  2003/12/15 06:55:06  venku
   - formatting
   - error while building basic block graph.  FIXED.
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.16  2003/12/04 08:35:22  venku
   - formatting.
   Revision 1.15  2003/12/04 08:34:52  venku
   - as methods in language such as Java have one entry point,
     it makes sense to have a getHead() method in basic block graph.
   Revision 1.14  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.13  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.12  2003/11/05 09:28:10  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.11  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.10  2003/09/12 08:09:37  venku
   - documentation.
   Revision 1.9  2003/09/11 12:18:35  venku
   - added support to retrieve basic blocks in which
     exception handlers begin.
   - added support to detect ancestral relationship between nodes.
   Revision 1.8  2003/09/10 10:51:07  venku
   - documentation.
   - removed unnecessary typecast.
   Revision 1.7  2003/09/02 11:50:54  venku
   - start==end in getStmtFromTo() returned a list with 2 instances of the
    statement. FIXED.
   Revision 1.6  2003/09/02 07:39:54  venku
   - getStmtFromTo() was off by one at the end.  Also, it relied on Stmt list to calculate this info.
     Now it uses the unit graph to calculate this info.
   - added getLeaderStmt() method.
   Revision 1.5  2003/08/24 08:13:11  venku
   Major refactoring.
    - The methods to modify the graphs were exposed.
    - The above anamoly was fixed by supporting a new class AbstractMutableDirectedGraph.
    - Each Mutable graph extends this graph and exposes itself via
      suitable interface to restrict access.
    - Ripple effect of the above changes.
   Revision 1.4  2003/08/15 08:24:19  venku
   Added a convenience method to retrieve trailer statement of a basic block.
   Revision 1.3  2003/08/11 06:40:54  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
 */
