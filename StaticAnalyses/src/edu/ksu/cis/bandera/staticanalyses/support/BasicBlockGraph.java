
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.support;

import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtGraph;
import ca.mcgill.sable.soot.jimple.StmtList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents the basic block graph for a given method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class BasicBlockGraph
  extends DirectedGraph {
	/**
	 * A constant array of zero-length.
	 */
	private static final BasicBlock ZERO_LENGTH_ARRAY[] = new BasicBlock[0];

	/**
	 * The 3-address format body of the method represented by this graph.
	 */
	protected final JimpleBody jimpleBody;

	/**
	 * The control flow graph of the method represented by this graph.
	 */
	protected final StmtGraph stmtGraph;

	/**
	 * The collection of nodes(<code>BasicBlock</code>) in this graph.
	 */
	private final List blocks;

	/**
	 * An array of <code>BasicBlock</code> objects.
	 */
	private final BasicBlock stmt2BlockMap[];

	/**
	 * Creates a new BasicBlockGraph object.
	 *
	 * @param stmtGraph is the control flow graph being represented by this graph.
	 */
	protected BasicBlockGraph(StmtGraph stmtGraph) {
		this.stmtGraph = stmtGraph;
		jimpleBody = (JimpleBody) stmtGraph.getBody();

		StmtList stmtList = jimpleBody.getStmtList();
		int numOfStmt = stmtList.size();

		if (numOfStmt == 0) {
			blocks = Collections.EMPTY_LIST;
			stmt2BlockMap = ZERO_LENGTH_ARRAY;
			return;
		} else {
			blocks = new ArrayList();
			stmt2BlockMap = new BasicBlock[numOfStmt];
		}

		int leader = 0;
		int trailer = numOfStmt;
		Collection processed = new HashSet();
		List stmts = new ArrayList();
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		wb.addWork(stmtList.get(0));

		while (!wb.isEmpty()) {
			stmts.clear();

			Stmt stmt = (Stmt) wb.getWork();

			if (processed.contains(stmt)) {
				continue;
			}
			processed.add(stmt);
			leader = stmtList.indexOf(stmt);

			Stmt pred = stmt;

			while (true) {
				ca.mcgill.sable.util.Collection preds = stmtGraph.getPredsOf(stmt);
				ca.mcgill.sable.util.Collection succs = stmtGraph.getSuccsOf(stmt);
				int succsSize = succs.size();

				if (preds.size() > 1 && pred != stmt) {
					trailer = stmtList.indexOf(pred);
					wb.addWorkNoDuplicates(stmt);
					break;
				}
				stmts.add(stmt);

				if (succsSize > 1 || succsSize == 0) {
					trailer = stmtList.indexOf(stmt);

					if (succsSize > 1) {
						wb.addAllWorkNoDuplicates(Util.convert("java.util.ArrayList", succs));
					}
					break;
				}
				pred = stmt;
				stmt = (Stmt) stmtGraph.getSuccsOf(pred).get(0);
			}

			BasicBlock bblock = new BasicBlock(leader, trailer, stmts);

			for (Iterator i = stmts.iterator(); i.hasNext();) {
				stmt2BlockMap[stmtList.indexOf(i.next())] = bblock;
			}
			blocks.add(bblock);
		}

		// Connect the nodes of the graph.
		for (Iterator i = blocks.iterator(); i.hasNext();) {
			BasicBlock block = (BasicBlock) i.next();
			Stmt stmt = (Stmt) stmtList.get(block._TRAILER);

			for (ca.mcgill.sable.util.Iterator j = stmtGraph.getSuccsOf(stmt).iterator(); j.hasNext();) {
				Stmt nStmt = (Stmt) j.next();
				BasicBlock nBlock = getEnclosingBlock(nStmt);
				block.addSuccessors(nBlock);
				nBlock.addPredecessors(block);
			}
		}

		// Setup the head of the graph.
		heads.add(getEnclosingBlock((Stmt) stmtGraph.getHeads().get(0)));

		// Setup the tails of the graph.
		for (ca.mcgill.sable.util.Iterator i = stmtGraph.getTails().iterator(); i.hasNext();) {
			tails.add(getEnclosingBlock((Stmt) i.next()));
		}
	}

	/**
	 * This class represents a basic block in a method.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public class BasicBlock
	  extends SimpleNodeGraph.SimpleNode {
		/**
		 * An index into the statement list of the method.  It is the index of the leader statement of this block.
		 */
		public final int _LEADER;

		/**
		 * An index into the statement list of the method.  It is the index of the trailer statement of this block.
		 */
		public final int _TRAILER;

		/**
		 * The list of statements represented by this block.
		 */
		private final List stmts;

		/**
		 * Creates a new BasicBlock object.
		 *
		 * @param leader is the index of the leader statement of this block in the statement list of the method.
		 * @param trailer is the index of the trailer statement of this block in the statement list of the method.
		 * @param stmts is the list of statements being represented by this block.
		 *
		 * @pre leader >= 0 && leader < graph.numOfStmt && trailer >= leader && trailer < graph.numOfStmt;
		 */
		protected BasicBlock(int leader, int trailer, List stmts) {
			super(null);
			this._LEADER = leader;
			this._TRAILER = trailer;
			this.stmts = new ArrayList(stmts);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code>.
		 *
		 * @param start is the index starting from which the statements are requested.  The index is relative to the
		 *           statement list of the method and not the statement list of this block.
		 *
		 * @return a list of <code>Stmt</code>s.
		 *
		 * @post (start < leader or start >= trailer) implies (result.size() = 0)
		 */
		public final List getStmtFrom(int start) {
			return getStmtFromTo(start, _TRAILER);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code> till <code>end</code>. Both indices are
		 * relative to the statement list of the method and not the statement list of this block.
		 *
		 * @param start is the starting index of the requested statement list.
		 * @param end is the ending index of the requested statement list.
		 *
		 * @return a list of <code>Stmt</code>s.
		 *
		 * @post ((start < leader or end > trailer or start >= end)) implies (result.size() = 0)
		 */
		public final List getStmtFromTo(int start, int end) {
			List result = Collections.EMPTY_LIST;

			StmtList sl = stmtGraph.getBody().getStmtList();
			Stmt begStmt = (Stmt) sl.get(start);
			Stmt endStmt = (Stmt) sl.get(end);

			if (stmts.contains(begStmt) && stmts.contains(endStmt)) {
				result = new ArrayList();

				Iterator i = stmts.iterator();

				for (; i.hasNext();) {
					if (i.next().equals(begStmt)) {
						break;
					}
				}
				result.add(begStmt);

				for (; i.hasNext();) {
					Object o = i.next();

					if (o.equals(endStmt)) {
						break;
					}
					result.add(o);
				}
			}
			return result;
		}

		/**
		 * Retrieves the statements in this block .
		 *
		 * @return a list of <code>Stmt</code>s.
		 */
		public final List getStmtsOf() {
			return Collections.unmodifiableList(stmts);
		}
	}

	/**
	 * Retrieve the basic block enclosing the statement at the given index.  The index is relative to the statemet list of
	 * the method.
	 *
	 * @param stmtIndex is the index of the statement of interest.
	 *
	 * @return the basic block enclosing the statement at the given index.
	 *
	 * @post stmt2BlockMap.length = 0 implies result = null
	 */
	public final BasicBlock getEnclosingBlock(int stmtIndex) {
		BasicBlock result = null;

		if (stmt2BlockMap.length != 0) {
			result = stmt2BlockMap[stmtIndex];
		}
		return result;
	}

	/**
	 * Retrieve the basic block enclosing the given statement.
	 *
	 * @param stmt is the statement of interest.
	 *
	 * @return the basic block enclosing the statement.
	 */
	public final BasicBlock getEnclosingBlock(Stmt stmt) {
		return getEnclosingBlock(jimpleBody.getStmtList().indexOf(stmt));
	}

	/**
	 * Returns the nodes in the graph.
	 *
	 * @return the list of <code>BasicBlocks</code> that make up the nodes in the graph.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.support.DirectedGraph#getNodes()
	 */
	public List getNodes() {
		return Collections.unmodifiableList(blocks);
	}

	/**
	 * Returns the number of nodes in the graph.
	 *
	 * @return the number of nodes in the graph.
	 */
	public int size() {
		return blocks.size();
	}
}

/*****
 ChangeLog:

$Log$

*****/
