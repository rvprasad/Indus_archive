
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
import java.util.Iterator;
import java.util.List;

/**
 * This class represents the basic block graph for a given method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class BasicBlockGraph extends DirectedGraph {
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
		blocks = new ArrayList();

		StmtList stmtList = jimpleBody.getStmtList();
		int numOfStmt = stmtList.size();
		stmt2BlockMap = new BasicBlock[numOfStmt];

		int leader = 0, trailer = numOfStmt;

		// Create basic blocks in the graph.
		for (int i = 0; i < numOfStmt; i++) {
			Stmt stmt = (Stmt) stmtList.get(i);
			trailer = i;

			if (stmtGraph.getPredsOf(stmt).size() > 1) {
				BasicBlock bblock = new BasicBlock(leader, trailer - 1, this);

				for (int j = leader; j < trailer; j++) {
					stmt2BlockMap[j] = bblock;
				}
				leader = trailer;
				blocks.add(bblock);
			}
			if (stmtGraph.getSuccsOf(stmt).size() > 1) {
				BasicBlock bblock = new BasicBlock(leader, trailer, this);

				for (int j = leader; j <= trailer; j++) {
					stmt2BlockMap[j] = bblock;
				}
				leader = ++trailer;
				blocks.add(bblock);
			} 
		}
		// fix up the last set of statements into a block
		BasicBlock bblock = new BasicBlock(leader, trailer, this);

		for (int j = leader; j <= trailer; j++) {
			stmt2BlockMap[j] = bblock;
		}
		blocks.add(bblock);

		// Connect the nodes of the graph.
		for (Iterator i = blocks.iterator(); i.hasNext();) {
			BasicBlock block = (BasicBlock) i.next();
			Stmt stmt = (Stmt) stmtList.get(block.trailer);

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
	public class BasicBlock extends SimpleNodeGraph.SimpleNode {
		/**
		 * The graph in which this node occurs.
		 */
		public final BasicBlockGraph graph;

		/**
		 * An index into the statement list of the method.  It is the index of the leader statement of this block.
		 */
		public final int leader;

		/**
		 * An index into the statement list of the method.  It is the index of the trailer statement of this block.
		 */
		public final int trailer;

		/**
		 * Creates a new BasicBlock object.
		 *
		 * @param leader is the index of the leader statement of this block in the statement list of the method.
		 * @param trailer is the index of the trailer statement of this block in the statement list of the method.
		 * @param graph is the graph in which block occurs.
		 *
		 * @pre leader >= 0 && leader &lt; graph.numOfStmt && trailer >= leader && trailer &lt; graph.numOfStmt;
		 */
		protected BasicBlock(int leader, int trailer, BasicBlockGraph graph) {
			super(null);
			this.leader = leader;
			this.trailer = trailer;
			this.graph = graph;
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code>.
		 *
		 * @param start is the index starting from which the statements are requested.  The index is relative to the
		 * 		  statement list of the method and not the statement list of this block.
		 *
		 * @return a list of <code>Stmt</code>s.
		 *
		 * @post (start &lt; leader or start >= trailer) implies (result.size() = 0)
		 */
		public final List getStmtFrom(int start) {
			List result = Collections.EMPTY_LIST;

			if (start >= leader && start < trailer) {
				result = new ArrayList();

				StmtList sl = graph.jimpleBody.getStmtList();

				for (int i = start; i <= trailer; i++) {
					result.add(sl.get(i));
				}
			}
			return result;
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code> till <code>end</code>. Both indices are
		 * relative to the statement list of the method and not the statement list of this block.
		 *
		 * @param start is the index starting from which the statements are requested. (inclusive)
		 * @param end is the index till which the statements are requested. (inclusive)
		 *
		 * @return a list of <code>Stmt</code>s.
		 *
		 * @post ((start &lt; leader or end > trailer or start >= end)) implies (result.size() = 0)
		 */
		public final Collection getStmtFromTo(int start, int end) {
			Collection result = Collections.EMPTY_LIST;

			if (start >= leader && end <= trailer && start < end) {
				result = new ArrayList();

				StmtList sl = graph.jimpleBody.getStmtList();

				for (int i = start; i < end; i++) {
					result.add(sl.get(i));
				}
			}
			return result;
		}

		/**
		 * Retrieves the statements in this block .
		 *
		 * @return a list of <code>Stmt</code>s.
		 */
		public final Collection getStmtsOf() {
			StmtList sl = graph.jimpleBody.getStmtList();
			Collection result = new ArrayList();

			for (int i = leader; i < -trailer; i++) {
				result.add(sl.get(i));
			}
			return result;
		}
	}

	/**
	 * Retrieve the basic block enclosing the statement at the given index.  The index is relative to the statemet list of
	 * the method.
	 *
	 * @param stmtIndex is the index of the statement of interest.
	 *
	 * @return the basic block enclosing the statement at the given index.
	 */
	public final BasicBlock getEnclosingBlock(int stmtIndex) {
		return stmt2BlockMap[stmtIndex];
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
