
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;

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
	private Map method2EnclosingInvokingStmtsCache = new HashMap();

	/**
	 * {@inheritDoc}
	 *
	 * @param iva is the value analyzer to use.
	 * @param cg is the call graph to use.
	 * @param tg is the thread graph to use.  If this parameter is <code>null</code> then it is assumed all methods execute
	 * 		  in the same thread.
	 * @param bbgManager is the basic block graph manager to use.
	 * @param pairManager is the pair object manager to use.
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
		method2EnclosingInvokingStmtsCache.clear();
		super.consolidate();
		method2EnclosingInvokingStmtsCache.clear();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation actually checks if the use site is reachable from the def site via control flow. However, it does
	 * not check for the existence of any overriding definitions alike strong updates.
	 * </p>
	 *
	 * @pre defMethod != null and defStmt != null and useMethod != null and useStmt != null
	 */
	protected boolean isReachableViaInterProceduralControlFlow(final SootMethod defMethod, final Stmt defStmt,
		final SootMethod useMethod, final Stmt useStmt) {
		boolean _result = !tgi.mustOccurInDifferentThread(defMethod, useMethod);

		if (_result) {
			_result = false;

			/*
			 * Check if the use method is reachable from the def method. If so, check the use method is reachable via
			 * any of the invocation statement that succeed defStmt in defMethod.
			 */
			if (cgi.isCalleeReachableFromCaller(useMethod, defMethod)) {
				_result = doesControlFlowPathExistsBetween(defMethod, defStmt, useMethod, true);
			}

			/*
			 * Check if the def method is reachable from the use method. If so, check the def method is reachable via
			 * any of the invocation statements that precede useStmt in useMethod.
			 */
			if (!_result && cgi.isCalleeReachableFromCaller(defMethod, useMethod)) {
				_result = doesControlFlowPathExistsBetween(useMethod, useStmt, defMethod, false);
			}

			/*
			 * Check if the control can reach from the def method to the use method via some common ancestor in the
			 * call-graph.  We cannot assume that the previous two conditions need to be false to evaluate this block.  The
			 * reason being that there may be a call chain from the defMethod to the useMethod but the invocation site in
			 * defMethod may occur prior to the defStmt.  The same holds for condition two.
			 */
			if (!_result) {
				_result = doesControlPathExistsFromTo(defMethod, useMethod);
			}
		} else {
			_result = tgi.containsClassInitThread(tgi.getExecutionThreads(defMethod));
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
			final Iterator _stmts = bbgMgr.getBasicBlockGraph(method).getStmtGraph().iterator();
			_result = IteratorUtils.filteredIterator(_stmts, SootPredicatesAndTransformers.INVOKE_EXPR_PREDICATE);
			method2EnclosingInvokingStmtsCache.put(method, IteratorUtils.toList(_result));
		} else {
			_result = _temp.iterator();
		}
		return _result;
	}

	/**
	 * Checks if there is path for control between <code>targetmethod</code> and <code>stmt</code> in <code>method</code> via
	 * intra-procedural control flow and call chains.
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
			_bbDefStmts = _bbDef.getStmtsFromTo(_bbDef.getLeaderStmt(), stmt);
		}

		for (final Iterator _j =
				IteratorUtils.filteredIterator(_bbDefStmts.iterator(), SootPredicatesAndTransformers.INVOKE_EXPR_PREDICATE);
			  _j.hasNext() && !_result;) {
			final Stmt _stmt = (Stmt) _j.next();
			_result = cgi.isCalleeReachableFromCallSite(targetMethod, _stmt, method);
		}

		for (final Iterator _i = _reachableBBs.iterator(); _i.hasNext() && !_result;) {
			final BasicBlock _bb = (BasicBlock) _i.next();

			for (final Iterator _j =
					IteratorUtils.filteredIterator(_bb.getStmtsOf().iterator(),
						SootPredicatesAndTransformers.INVOKE_EXPR_PREDICATE); _j.hasNext() && !_result;) {
				final Stmt _stmt = (Stmt) _j.next();
				_result = cgi.isCalleeReachableFromCallSite(targetMethod, _stmt, method);
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
		final boolean _flag = cgi.isCalleeReachableFromCaller(useMethod, defMethod);

		for (final Iterator _i = _commonAncestors.iterator(); _i.hasNext() && !_result;) {
			final SootMethod _sm = (SootMethod) _i.next();
			final Iterator _invokingStmtIterator = getInvokingStmtIteratorFor(_sm);

			for (final Iterator _j = _invokingStmtIterator; _j.hasNext() && !_result;) {
				final Stmt _stmt = (Stmt) _j.next();
				final Collection _callees = cgi.getMethodsReachableFrom(_stmt, _sm);

				/*
				 * We cannot just require !_callees.contains(useMethod) as it is possible that useMethod is reachable from
				 * defMethod but the defStmt may not be (see comments in isReachableViaInterProceduralControlFlow).  However,
				 * we can strenghten the condition to avoid unnecessary explorations by requiring either the useMethod be not
				 * reachable from the invocation site or it be reachable via the defMethod also.
				 */
				if (_callees.contains(defMethod) && (!_callees.contains(useMethod) ^ _flag)) {
					_result = doesControlFlowPathExistsBetween(_sm, _stmt, useMethod, true);
				}
			}
		}
		return _result;
	}
}

// End of File
