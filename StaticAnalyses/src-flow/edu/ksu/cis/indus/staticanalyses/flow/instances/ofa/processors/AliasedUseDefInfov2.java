
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

import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

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
public class AliasedUseDefInfov2
  extends AliasedUseDefInfo {
	/** 
	 * This provides the call graph of the system.
	 */
	protected final ICallGraphInfo cgi;

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
	 *
	 * @pre cg != null
	 */
	public AliasedUseDefInfov2(final IValueAnalyzer iva, final ICallGraphInfo cg, final BasicBlockGraphMgr bbgManager) {
		super(iva, bbgManager);
		cgi = cg;
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
	 * {@inheritDoc} This implementation actually checks if the use site is reachable from the def site.  However, it does
	 * not check for the existence of any overriding definitions.
	 *
	 * @pre defMethod != null and defStmt != null and useMethod != null and useStmt != null
	 */
	protected boolean isReachableViaInterProceduralFlow(final SootMethod defMethod, final Stmt defStmt,
		final SootMethod useMethod, final Stmt useStmt) {
		boolean _result;
		_result = false;

		final Collection _methodsInvokedAfterDef = cgi.getMethodsReachableFrom(defMethod, true);

		if (_methodsInvokedAfterDef.contains(useMethod)) {
			_result = doesControlFlowPathExistsBetween(defMethod, defStmt, useMethod, true);
		}

		final Collection _methodsInvokedBeforeUse = cgi.getMethodsReachableFrom(useMethod, true);

		if (!_result && _methodsInvokedBeforeUse.contains(defMethod)) {
			_result = doesControlFlowPathExistsBetween(useMethod, useStmt, defMethod, false);
		}

		if (!_result) {
			_result = doesControlPathExistsFromTo(defMethod, useMethod, _methodsInvokedAfterDef, _methodsInvokedBeforeUse);
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
	 * @param methodsInvokedAfterDef is a collection of methods invoked after the defining method exits.
	 * @param methodsInvokedBeforeUse is a collection of methods invoked before the using method is entered.
	 *
	 * @return <code>true</code> if there is a control path; <code>false</code>, otherwise.
	 *
	 * @pre defMethod != null and useMethod != null and methodsInvokedAfterDef != null and methodsInvokedBeforeUse != null
	 * @pre methodsInvokedAfterDef.oclIsKindOf(Collection(SootMethod))
	 * @pre methodsInvokedAfterUse.oclIsKindOf(Collection(SootMethod))
	 */
	private boolean doesControlPathExistsFromTo(final SootMethod defMethod, final SootMethod useMethod,
		final Collection methodsInvokedAfterDef, final Collection methodsInvokedBeforeUse) {
		boolean _result = false;
		final Collection _commonAncestors = CollectionUtils.intersection(methodsInvokedBeforeUse, methodsInvokedAfterDef);

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
}

/*
   ChangeLog:
   $Log$
 */
