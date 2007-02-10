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

package ase05;

import ase05.InfluenceChecker.PairNode;
import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.graph.IDirectedGraphView;
import edu.ksu.cis.indus.common.graph.IEdgeLabel;
import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This provides a graph view of the call graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class CallGraphView
		implements IEdgeLabelledDirectedGraphView<IDirectedGraphView.INode> {

	/**
	 * DOCUMENT ME!
	 */
	private final InfluenceChecker checker;

	/**
	 * DOCUMENT ME!
	 */
	private final boolean invoked;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param checker DOCUMENT ME!
	 * @param includeInvoked DOCUMENT ME!
	 */
	CallGraphView(final InfluenceChecker checker, final boolean includeInvoked) {
		this.checker = checker;
		invoked = includeInvoked;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getIncomingEdgeLabels(IDirectedGraphView.INode)
	 */
	public Collection<IEdgeLabel> getIncomingEdgeLabels(@SuppressWarnings("unused") final IDirectedGraphView.INode node) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getOutgoingEdgeLabels(IDirectedGraphView.INode)
	 */
	public Collection<IEdgeLabel> getOutgoingEdgeLabels(final IDirectedGraphView.INode node) {
		final Collection<IEdgeLabel> _result = new HashSet<IEdgeLabel>();

		final SootMethod _method = (SootMethod) ((PairNode) node).second;
		if (!this.checker.cgi.getCallees(_method).isEmpty()) {
			_result.add(InfluenceChecker.CALLS);
		}

		if (invoked && _method.hasActiveBody()) {
			final List<Stmt> _l = new ArrayList<Stmt>(_method.getActiveBody().getUnits());
			if (CollectionUtils.exists(_l, SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE)) {
				_result.add(InfluenceChecker.CALLS);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getPredsOf(IDirectedGraphView.INode)
	 */
	public Collection<IDirectedGraphView.INode> getPredsOf(final IDirectedGraphView.INode node) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getPredsViaEdgesLabelled(IDirectedGraphView.INode,
	 *      IEdgeLabel)
	 */
	public Collection<IDirectedGraphView.INode> getPredsViaEdgesLabelled(
			@SuppressWarnings("unused") final IDirectedGraphView.INode node,
			@SuppressWarnings("unused") final IEdgeLabel label) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see IDirectedGraphView#getSuccsOf(IDirectedGraphView.INode)
	 */
	public Collection<INode> getSuccsOf(final IDirectedGraphView.INode node) {
		final Collection<INode> _result = new HashSet<INode>();
		final SootMethod _method = (SootMethod) ((PairNode) node).second;
		final Collection<CallTriple> _callees = this.checker.cgi.getCallees(_method);
		final boolean _b = _method.hasActiveBody();
		if (_b) {
			final List<Stmt> _l = new ArrayList<Stmt>(_method.getActiveBody().getUnits());
			CollectionUtils.filter(_l, SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);
			final Iterator<CallTriple> _i = _callees.iterator();
			final int _iEnd = _callees.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final CallTriple _triple = _i.next();
				final Stmt _stmt = _triple.getSecond();
				_result.add(new InfluenceChecker.PairNode(_stmt, _triple.getMethod()));
				if (invoked) {
					_result.add(new InfluenceChecker.PairNode(_stmt, _stmt.getInvokeExpr().getMethod()));
				}
				_l.remove(_stmt);
			}

			if (invoked) {
				final Iterator<Stmt> _j = _l.iterator();
				final int _jEnd = _l.size();
				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final Stmt _s = _j.next();
					_result.add(new InfluenceChecker.PairNode(null, _s.getInvokeExpr().getMethod()));
				}
			}
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getSuccsViaEdgesLabelled(IDirectedGraphView.INode,
	 *      IEdgeLabel)
	 */
	public Collection<IDirectedGraphView.INode> getSuccsViaEdgesLabelled(final IDirectedGraphView.INode node,
			final IEdgeLabel label) {
		return label.equals(InfluenceChecker.CALLS) ? getSuccsOf(node) : Collections.EMPTY_SET;
	}
}
