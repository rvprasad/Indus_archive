
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class provides intra-thread aliased use-def information which is based on types, points-to information, and call
 * graph. If the use is reachable from the def via the control flow graph or via the CFG and the call graph, then def and
 * use site are related by use-def relation.  This does not consider any intervening definition when calculating the def-use
 * relation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 *
 * @see AliasedUseDefInfo
 */
public final class AliasedUseDefInfov2
  extends AliasedUseDefInfo {
	/** 
	 * This provides the call graph of the system.
	 */
	protected final ICallGraphInfo cgi;

	/** 
	 * This provides the thread graph of the system.
	 */
	protected final IThreadGraphInfo tgi;

	/** 
	 * This is a cache of the collection of method invocation statements in methods.
	 *
	 * @invariant method2EnclosingInvokingStmtsCache.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 * @invariant method2EnclosingInvokingStmtsCache.oclIsKindOf.values()->forall(o | o->forall(p | p.containsInvokeExpr()))
	 */
	private Map method2EnclosingInvokingStmtsCache;

	/**
	 * {@inheritDoc}
	 *
	 * @param cg is the call graph to use.
	 * @param tg is the thread graph to use.  If this parameter is <code>null</code> then it is assumed all methods execute
	 * in the same thread. 
	 *
	 * @pre iva != null and cg != null and bbgManager != null and pairManager != null
	 */
	public AliasedUseDefInfov2(final IValueAnalyzer iva, final ICallGraphInfo cg, final IThreadGraphInfo tg,
		final BasicBlockGraphMgr bbgManager, final PairManager pairManager) {
		super(iva, bbgManager, pairManager);
		cgi = cg;
		tgi = tg;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		method2EnclosingInvokingStmtsCache = new HashMap();
		super.consolidate();
		method2EnclosingInvokingStmtsCache = null;
	}

	/**
	 * {@inheritDoc} 
	 * 
	 * <p>This implementation actually checks if the use site is reachable from the def site via control flow.
	 * However, it does not check for the existence of any overriding definitions alike strong updates.</p>
	 *
	 * @pre defMethod != null and defStmt != null and useMethod != null and useStmt != null
	 */
	protected boolean isReachableViaInterProceduralControlFlow(final SootMethod defMethod, final Stmt defStmt,
		final SootMethod useMethod, final Stmt useStmt) {
		boolean _result = occurInSameThread(defMethod, useMethod);

		if (_result) {
			/*
			 * Check if the use method is reachable from the def method. If so, check the ordering within the def method.
			 */
			if (cgi.getMethodsReachableFrom(defMethod, true).contains(useMethod)) {
				_result = doesControlFlowPathExistsBetween(defMethod, defStmt, useMethod, true);
			}

			/*
			 * Check if the def method is reachable from the use method. If so, check the ordering within the use method.
			 */
			if (!_result && cgi.getMethodsReachableFrom(useMethod, true).contains(defMethod)) {
				_result = doesControlFlowPathExistsBetween(useMethod, useStmt, defMethod, false);
			}

			/*
			 * Check if the control can reach from the def method to the use method across common call-graph ancestor.
			 */
			if (!_result) {
				_result = doesControlPathExistsFromTo(defMethod, useMethod);
			}
		}
		return _result;
	}

	/**
	 * Retrieves the iterator on the collection of statements in the method that invoke methods.
	 *
	 * @param method of interest.
	 *
	 * @return an iterator.
	 *
	 * @pre method != null
	 * @post result != null
	 * @post all returned values are of type Stmt and contain an invoke expression.
	 */
	private Iterator getInvokingStmtIteratorFor(final SootMethod method) {
		final Collection _temp = (Collection) method2EnclosingInvokingStmtsCache.get(method);
		final Iterator _result;

		if (_temp == null) {
			final Predicate _invokeExprFilter =
				new Predicate() {
					public boolean evaluate(final Object o) {
						return ((Stmt) o).containsInvokeExpr();
					}
				};

			final Iterator _stmts = bbgMgr.getBasicBlockGraph(method).getStmtGraph().iterator();
			_result = IteratorUtils.filteredIterator(_stmts, _invokeExprFilter);
			method2EnclosingInvokingStmtsCache.put(method, IteratorUtils.toList(_result));
		} else {
			_result = _temp.iterator();
		}
		return _result;
	}

	/**
	 * Checks if there is path for control between <code>targetmethod</code> and <code>stmt</code> in <code>method</code> via
	 * intra-procedural control flow and call chains after executing.
	 *
	 * @param method in which control starts.
	 * @param stmt at which control starts.
	 * @param targetMethod which the control should reach.
	 * @param forward <code>true</code> indicates to check if the the control reaches <code>targetmethod</code> after
	 * 		  executing <code>stmt</code> in <code>method</code>; <code>false</code>, if the the control reaches
	 * 		  <code>targetmethod</code> before  executing <code>stmt</code> in <code>method</code>
	 *
	 * @return <code>true</code> if there is control flow path between the given points; <code>false</code>, otherwise.
	 */
	private boolean doesControlFlowPathExistsBetween(final SootMethod method, final Stmt stmt, final SootMethod targetMethod,
		final boolean forward) {
		boolean _result = false;
		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);
		final BasicBlock _bbDef = _bbg.getEnclosingBlock(stmt);
		final Collection _reachableBBs = new HashSet(_bbg.getReachablesFrom(_bbDef, forward));
		final Collection _bbDefStmts;

		if (forward) {
			_bbDefStmts = _bbDef.getStmtsFrom(stmt);
		} else {
			_bbDefStmts = _bbDef.getStmtFromTo(_bbDef.getLeaderStmt(), stmt);
		}

		for (final Iterator _j = _bbDefStmts.iterator(); _j.hasNext() && !_result;) {
			final Stmt _stmt = (Stmt) _j.next();

			if (_stmt.containsInvokeExpr()) {
				_result = cgi.getMethodsReachableFrom(_stmt, method).contains(targetMethod);
			}
		}

		for (final Iterator _i = _reachableBBs.iterator(); _i.hasNext() && !_result;) {
			final BasicBlock _bb = (BasicBlock) _i.next();

			for (final Iterator _j = _bb.getStmtsOf().iterator(); _j.hasNext() && !_result;) {
				final Stmt _stmt = (Stmt) _j.next();

				if (_stmt.containsInvokeExpr()) {
					_result = cgi.getMethodsReachableFrom(_stmt, method).contains(targetMethod);
				}
			}
		}
		return _result;
	}

	/**
	 * Checks if there is a control path between the <code>defMethod</code> and <code>useMethod</code>.
	 *
	 * @param defMethod is the def-site containing method.
	 * @param useMethod is the use-site containing method.
	 *
	 * @return <code>true</code> if there is a control path; <code>false</code>, otherwise.
	 *
	 * @pre defMethod != null and useMethod != null
	 */
	private boolean doesControlPathExistsFromTo(final SootMethod defMethod, final SootMethod useMethod) {
		boolean _result = false;
		final Collection _commonAncestors =
			CollectionUtils.intersection(cgi.getMethodsReachableFrom(defMethod, false),
				cgi.getMethodsReachableFrom(useMethod, false));

		for (final Iterator _i = _commonAncestors.iterator(); _i.hasNext() && !_result;) {
			final SootMethod _sm = (SootMethod) _i.next();
			final Iterator _invokingStmtIterator = getInvokingStmtIteratorFor(_sm);

			for (final Iterator _j = _invokingStmtIterator; _j.hasNext() && !_result;) {
				final Stmt _stmt = (Stmt) _j.next();
				final Collection _callees = cgi.getMethodsReachableFrom(_stmt, _sm);

				if (_callees.contains(defMethod) && !_callees.contains(useMethod)) {
					_result = doesControlFlowPathExistsBetween(_sm, _stmt, useMethod, true);
				}
			}
		}
		return _result;
	}

	/**
	 * Checks if the data defining method and the method in which the data is used occurs in the same thread.
	 *
	 * @param defMethod obviously contains the definition.
	 * @param useMethod obviously contains the use.
	 *
	 * @return <code>true</code> if the given methods occur in the same thread; <code>false</code>, otherwise.
	 *
	 * @pre defMethod != null and useMethod != null
	 */
	private boolean occurInSameThread(final SootMethod defMethod, final SootMethod useMethod) {
		final boolean _result;

		if (tgi != null) {
			_result = CollectionUtils.containsAny(tgi.getExecutionThreads(defMethod), tgi.getExecutionThreads(useMethod));
		} else {
			_result = true;
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2004/08/06 07:37:33  venku
   - thread-graph based optimization.
   Revision 1.3  2004/08/02 07:33:45  venku
   - small but significant change to the pair manager.
   - ripple effect.
   Revision 1.2  2004/07/28 09:09:27  venku
   - changed aliased use def analysis to consider thread.
   - also fixed a bug in the same analysis.
   - ripple effect.
   - deleted entry control dependence and renamed direct entry control da as
     entry control da.
   Revision 1.1  2004/07/16 06:38:47  venku
   - added  a more precise implementation of aliased use-def information.
   - ripple effect.
 */
