
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
	 * The list of statements in the method being represented by this graph.
	 *
	 * @invariant stmtList.oclIsKindOf(Sequence(Stmt))
	 */
	final List stmtList;

	/**
	 * The control flow graph of the method represented by this graph.
	 */
	final UnitGraph stmtGraph;

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
	 * Creates a new BasicBlockGraph object.
	 *
	 * @param stmtGraphParam is the control flow graph being represented by this graph.
	 *
	 * @pre stmtGraphParam != null
	 */
	BasicBlockGraph(final UnitGraph stmtGraphParam) {
		this.stmtGraph = stmtGraphParam;
		stmtList = Collections.unmodifiableList(new ArrayList(stmtGraph.getBody().getUnits()));

		int numOfStmt = stmtList.size();

		if (numOfStmt == 0) {
			blocks = Collections.EMPTY_LIST;
			stmt2BlockMap = Collections.EMPTY_MAP;
			return;
		} else {
			blocks = new ArrayList();
			stmt2BlockMap = new HashMap(numOfStmt);
		}

		int leader = 0;
		int trailer = numOfStmt;
		Collection processed = new HashSet();
		List stmts = new ArrayList();
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		wb.addWork(stmtList.get(0));

		while (wb.hasWork()) {
			stmts.clear();

			Stmt stmt = (Stmt) wb.getWork();

			if (processed.contains(stmt)) {
				continue;
			}
			processed.add(stmt);
			leader = stmtList.indexOf(stmt);

			Stmt pred = stmt;

			while (true) {
				Collection preds = stmtGraph.getPredsOf(stmt);
				Collection succs = stmtGraph.getSuccsOf(stmt);
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
						wb.addAllWorkNoDuplicates(succs);
					}
					break;
				}
				pred = stmt;
				stmt = (Stmt) stmtGraph.getSuccsOf(pred).get(0);
			}

			BasicBlock bblock = new BasicBlock(leader, trailer, stmts);

			for (Iterator i = stmts.iterator(); i.hasNext();) {
				stmt2BlockMap.put(i.next(), bblock);
			}
			blocks.add(bblock);
		}

		// Connect the nodes of the graph.
		for (Iterator i = blocks.iterator(); i.hasNext();) {
			BasicBlock block = (BasicBlock) i.next();
			Stmt stmt = (Stmt) stmtList.get(block._trailer);

			for (Iterator j = stmtGraph.getSuccsOf(stmt).iterator(); j.hasNext();) {
				Stmt nStmt = (Stmt) j.next();
				BasicBlock nBlock = getEnclosingBlock(nStmt);
				block.addSuccessors(nBlock);
				nBlock.addPredecessors(block);
			}
		}

		// Setup the head of the graph.
		heads.add(getEnclosingBlock((Stmt) stmtGraph.getHeads().get(0)));

		// Setup the tails of the graph.
		for (Iterator i = stmtGraph.getTails().iterator(); i.hasNext();) {
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
		public final int _leader;

		/**
		 * An index into the statement list of the method.  It is the index of the trailer statement of this block.
		 */
		public final int _trailer;

		/**
		 * The list of statements represented by this block.
		 *
		 * @invariant stmts.oclIsKindOf(Sequence(Stmt))
		 */
		private final List stmts;

		/**
		 * Creates a new BasicBlock object.
		 *
		 * @param leader is the index of the leader statement of this block in the statement list of the method.
		 * @param trailer is the index of the trailer statement of this block in the statement list of the method.
		 * @param stmtsParam is the list of statements being represented by this block.
		 *
		 * @pre leader >= 0 && leader &lt; stmtList.size() && trailer >= leader && trailer &lt; stmtList.size();
		 */
		BasicBlock(final int leader, final int trailer, final List stmtsParam) {
			super(null);
			this._leader = leader;
			this._trailer = trailer;
			this.stmts = new ArrayList(stmtsParam);
		}

		/**
		 * Retrieves the statements in this block starting from <code>start</code>.
		 *
		 * @param start is the index starting from which the statements are requested.  The index is relative to the
		 * 		  statement list of the method and not the statement list of this block.
		 *
		 * @return a list of <code>Stmt</code>s.
		 *
		 * @post result != null
		 * @post (start &lt; leader or start >= trailer) implies (result.size() = 0)
		 */
		public final List getStmtFrom(final int start) {
			return getStmtFromTo(start, _trailer);
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
		 * @post result != null
		 * @post ((start &lt; leader or end > trailer or start >= end)) implies (result.size() = 0)
		 */
		public final List getStmtFromTo(final int start, final int end) {
			List result = Collections.EMPTY_LIST;
			Stmt begStmt = getStmtAt(start);
			Stmt endStmt = getStmtAt(end);

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
		 * @return a list of statements.
		 *
		 * @post result.oclIsKindOf(Sequence(Stmt))
		 */
		public final List getStmtsOf() {
			return Collections.unmodifiableList(stmts);
		}

		/**
		 * Returns the trailer statement of this basic block.
		 *
		 * @return the trailer statement
		 *
		 * @post result != null
		 */
		public final Stmt getTrailer() {
			return getStmtAt(_trailer);
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
	public final BasicBlock getEnclosingBlock(final Stmt stmt) {
		return (BasicBlock) stmt2BlockMap.get(stmt);
	}

	/**
	 * Returns the nodes in the graph.
	 *
	 * @return the list of <code>BasicBlocks</code> that make up the nodes in the graph.
	 *
	 * @post result != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedGraph#getNodes()
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
}

/*
   ChangeLog:
   $Log$
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
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
