
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

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

		int _leader;
		int _trailer;
		final Collection _processed = new HashSet();
		final List _stmts = new ArrayList();
		final IWorkBag _wb = new LIFOWorkBag();
		_wb.addWork(stmtList.get(0));
		blocks = new ArrayList();
		stmt2BlockMap = new HashMap(_numOfStmt);

		while (_wb.hasWork()) {
			_stmts.clear();

			final Stmt _stmt = (Stmt) _wb.getWork();

			if (_processed.contains(_stmt)) {
				continue;
			}
			_processed.add(_stmt);
			_leader = stmtList.indexOf(_stmt);
			_trailer = getTrailer(_stmt, _wb, _stmts);

			final BasicBlock _bblock = new BasicBlock(_leader, _trailer, _stmts);
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
		 * An index into the statement list of the method.  It is the index of the leader statement of this block.
		 */
		private final int leader;

		/**
		 * An index into the statement list of the method.  It is the index of the trailer statement of this block.
		 */
		private final int trailer;

		/**
		 * Creates a new BasicBlock object.
		 *
		 * @param theLeader is the index of the leader statement of this block in the statement list of the method.
		 * @param theTrailer is the index of the trailer statement of this block in the statement list of the method.
		 * @param stmtsParam is the list of statements being represented by this block.
		 *
		 * @pre leader >= 0 && leader &lt; stmtList.size() && trailer >= leader && trailer &lt; stmtList.size();
		 */
		BasicBlock(final int theLeader, final int theTrailer, final List stmtsParam) {
			super(new HashSet(), new HashSet());
			this.leader = theLeader;
			this.trailer = theTrailer;
			this.stmts = new ArrayList(stmtsParam);
		}

		/**
		 * Retrieves the statement at the leader position.
		 *
		 * @return the leader statement.
		 *
		 * @post result != null
		 */
		public Stmt getLeaderStmt() {
			return getStmtAt(leader);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code>.
		 *
		 * @param start is the index starting from which the statements are requested.  The index is relative to the
		 * 		  statement list of the method and not the statement list of this block.
		 *
		 * @return a modifiable list of <code>Stmt</code>s.
		 *
		 * @post result != null
		 * @post (start &lt; leader or start >= trailer) implies (result.size() = 0)
		 */
		public List getStmtFrom(final int start) {
			return getStmtFromTo(start, trailer);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code> till <code>end</code>. Both indices are
		 * relative to the statement list of the method and not the statement list of this block.
		 *
		 * @param start is the starting index of the requested statement list.
		 * @param end is the ending index of the requested statement list.
		 *
		 * @return a modifiable list of <code>Stmt</code>s.
		 *
		 * @post result != null
		 * @post ((start &lt; leader or end > trailer or start >= end)) implies (result.size() = 0)
		 */
		public List getStmtFromTo(final int start, final int end) {
			List _result = Collections.EMPTY_LIST;
			final Stmt _begStmt = getStmtAt(start);
			final Stmt _endStmt = getStmtAt(end);

			if (stmts.contains(_begStmt) && stmts.contains(_endStmt)) {
				_result = new ArrayList();
				_result.add(_begStmt);

				List _succs = getStmtGraph().getSuccsOf(_begStmt);

				while (_succs.size() == 1) {
					final Object _o = _succs.get(0);

					if (_o.equals(_endStmt)) {
						_result.add(_endStmt);
						break;
					}
					_result.add(_o);
					_succs = getStmtGraph().getSuccsOf(_o);
				}
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
		 * @return the trailer statement
		 *
		 * @post result != null
		 */
		public Stmt getTrailerStmt() {
			return getStmtAt(trailer);
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
				_handlerBlocks.add(getEnclosingBlock((Stmt) ((Trap) _i.next()).getHandlerUnit()));
			}
		} else {
			_handlerBlocks = Collections.EMPTY_LIST;
		}

		return _handlerBlocks;
	}

	/**
	 * Return the head node of this graph.  Basic Block graphs for procedures in languages without unconditional gotos can
	 * have only one entry point, hence, this method.
	 *
	 * @return the head node
	 *
	 * @post result != null
	 */
	public BasicBlock getHead() {
		return (BasicBlock) getHeads().iterator().next();
	}

	/**
	 * Returns the nodes in the graph.
	 *
	 * @return an unmodifiable list of <code>BasicBlocks</code> that make up the nodes in the graph.
	 *
	 * @post result != null
	 *
	 * @see edu.ksu.cis.indus.common.graph.DirectedGraph#getNodes()
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
	 * @see AbstractMutableDirectedGraph#containsNodes(edu.ksu.cis.indus.common.graph.INode)
	 */
	protected boolean containsNodes(final INode node) {
		return blocks.contains(node);
	}

	/**
	 * Retrieves the statement occurring at the given index in the body associated with this basic block graph.
	 *
	 * @param index at which the requested statement occurs.
	 *
	 * @return the statement occurring at the given index.
	 */
	Stmt getStmtAt(final int index) {
		return (Stmt) stmtList.get(index);
	}

	/**
	 * Retrieves the position of the trailer statement of the basic block being processed. <code>stmts</code> is filled with
	 * the statements that form the current basic block graph.
	 *
	 * @param leaderStmt from which the rest of the basic block body should be discovered.
	 * @param wb is the workbag into which new leader statement is added.
	 * @param stmts will contain the statements that make up the current basic block graph.
	 *
	 * @return the position of the trailer statement of the current basic block graph.
	 *
	 * @pre leaderStmt != null and wb != null and stmts != null
	 * @pre stmts.oclIsKindOf(Collection(Stmt))
	 * @post stmts.oclIsKindOf(Collection(Stmt))
	 */
	private int getTrailer(final Stmt leaderStmt, final IWorkBag wb, final List stmts) {
		Stmt _stmt = leaderStmt;
		Stmt _pred = leaderStmt;
		int _trailer = 0;

		while (true) {
			final Collection _preds = stmtGraph.getPredsOf(_stmt);
			final Collection _succs = stmtGraph.getSuccsOf(_stmt);
			final int _succsSize = _succs.size();

			if (_preds.size() > 1 && _pred != _stmt) {
				_trailer = stmtList.indexOf(_pred);
				wb.addWorkNoDuplicates(_stmt);
				break;
			}
			stmts.add(_stmt);

			if (_succsSize > 1 || _succsSize == 0) {
				_trailer = stmtList.indexOf(_stmt);

				if (_succsSize > 1) {
					wb.addAllWorkNoDuplicates(_succs);
				}
				break;
			}
			_pred = _stmt;
			_stmt = (Stmt) stmtGraph.getSuccsOf(_pred).get(0);
		}
		return _trailer;
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
				addEdgeFromTo(_block, _nBlock);
			}
		}

		// Setup the head of the graph.
		heads.add(getEnclosingBlock((Stmt) stmtGraph.getHeads().get(0)));

		// Setup the tails of the graph.
		for (final Iterator _i = stmtGraph.getTails().iterator(); _i.hasNext();) {
			tails.add(getEnclosingBlock((Stmt) _i.next()));
		}
	}
}

/*
   ChangeLog:
   $Log$
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
