package ase05;

import ase05.InfluenceChecker.PairNode;
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

import org.apache.commons.collections.CollectionUtils;

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
  implements IEdgeLabelledDirectedGraphView {
	/**
     * DOCUMENT ME!
     */
    private final InfluenceChecker checker;
    
    private final boolean invoked;

    /**
     * Creates an instance of this class.
     * 
     * @param checker DOCUMENT ME!
     */
    CallGraphView(InfluenceChecker checker, boolean includeInvoked) {
        this.checker = checker;
        invoked = includeInvoked;
    }

    /**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getIncomingEdgeLabels(IDirectedGraphView.INode)
	 */
	public Collection getIncomingEdgeLabels(final IDirectedGraphView.INode node) {
        throw new UnsupportedOperationException();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getOutgoingEdgeLabels(IDirectedGraphView.INode)
	 */
	public Collection getOutgoingEdgeLabels(final IDirectedGraphView.INode node) {
        final Collection _result = new HashSet();
        
        final SootMethod _method = (SootMethod) ((PairNode) node).second;
        if (!this.checker.cgi.getCallees(_method).isEmpty()) {
            _result.add(InfluenceChecker.CALLS);
        }
        
        if (invoked && _method.hasActiveBody()) {
            final List _l = new ArrayList(_method.getActiveBody().getUnits());
            if (CollectionUtils.exists(_l, SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE)) {
                _result.add(InfluenceChecker.CALLS);
            }
        }
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getPredsOf(IDirectedGraphView.INode)
	 */
	public Collection getPredsOf(IDirectedGraphView.INode node) {
        throw new UnsupportedOperationException();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getPredsViaEdgesLabelled(IDirectedGraphView.INode,
	 * 		IEdgeLabel)
	 */
	public Collection getPredsViaEdgesLabelled(final IDirectedGraphView.INode node, final IEdgeLabel label) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getSuccsOf(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
	 */
	public Collection getSuccsOf(final IDirectedGraphView.INode node) {
		final Collection _result = new HashSet();
		final SootMethod _method = (SootMethod) ((PairNode) node).second;
		final Collection _callees = this.checker.cgi.getCallees(_method);
        final boolean _b = _method.hasActiveBody();
        if (_b) {
            final List _l = new ArrayList(_method.getActiveBody().getUnits());
            CollectionUtils.filter(_l, SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);
            final Iterator _i = _callees.iterator();
    		final int _iEnd = _callees.size();
    		
    		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
    			final CallTriple _triple = (CallTriple) _i.next();
    			final Stmt _stmt = (Stmt) _triple.getSecond();
                _result.add(new InfluenceChecker.PairNode(_stmt, _triple.getMethod()));
                if (invoked) {
                    _result.add(new InfluenceChecker.PairNode(_stmt, _stmt.getInvokeExpr().getMethod()));
                }
                _l.remove(_stmt);
    		}
        
            if (invoked) {
        		final Iterator _j = _l.iterator();
                final int _jEnd = _l.size();
                for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
                    final Stmt _s = (Stmt) _j.next();
                    _result.add(new InfluenceChecker.PairNode(null, _s.getInvokeExpr().getMethod()));
                }
            }
        }

        return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getSuccsViaEdgesLabelled(IDirectedGraphView.INode,
	 * 		IEdgeLabel)
	 */
	public Collection getSuccsViaEdgesLabelled(final IDirectedGraphView.INode node, final IEdgeLabel label) {
        return label.equals(InfluenceChecker.CALLS) ? getSuccsOf(node)
                   : Collections.EMPTY_SET;
	}
}