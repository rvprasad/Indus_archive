package ase05;

import ase05.InfluenceChecker.PairNode;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.IEdgeLabel;
import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView;
import edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This provides a graph view of the control/dependence dependence information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class DependenceGraphView
  implements IEdgeLabelledDirectedGraphView {
	/**
     * 
     */
    private final InfluenceChecker checker;

    /**
     * Creates an instance of this class.
     * 
     * @param checker
     */
    DependenceGraphView(InfluenceChecker checker) {
        this.checker = checker;
    }

    /**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getIncomingEdgeLabels(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
	 */
	public Collection getIncomingEdgeLabels(final INode node) {
		throw new UnsupportedOperationException("This operation is unsupported.");
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getOutgoingEdgeLabels(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
	 */
	public Collection getOutgoingEdgeLabels(final INode node) {
		final Collection _result = new HashSet();
		final Stmt _s = (Stmt) ((PairNode) node).first;
		final SootMethod _sm = (SootMethod) ((PairNode) node).second;
		final Iterator _i = this.checker.cdas.iterator();
		final int _iEnd = this.checker.cdas.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
			final Collection _dents = _da.getDependents(_s, _sm);

			if (!_dents.isEmpty()) {
				_result.add(InfluenceChecker.CD);
				break;
			}
		}

		final Iterator _j = this.checker.ddas.iterator();
		final int _jEnd = this.checker.ddas.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _j.next();
			final Collection _dents = _da.getDependents(_s, _sm);

			if (!_dents.isEmpty()) {
				_result.add(InfluenceChecker.DD);
				break;
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getPredsOf(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
	 */
	public Collection getPredsOf(final INode node) {
		throw new UnsupportedOperationException("This operation is not supported.");
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getPredsViaEdgesLabelled(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode,
	 * 		edu.ksu.cis.indus.common.graph.IEdgeLabel)
	 */
	public Collection getPredsViaEdgesLabelled(final INode node, final IEdgeLabel label) {
		throw new UnsupportedOperationException("This operation is not supported.");
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getSuccsOf(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
	 */
	public Collection getSuccsOf(final INode node) {
		throw new UnsupportedOperationException("This operation is not supported.");
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getSuccsViaEdgesLabelled(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode,
	 * 		edu.ksu.cis.indus.common.graph.IEdgeLabel)
	 */
	public Collection getSuccsViaEdgesLabelled(final INode node, final IEdgeLabel label) {
		final Stmt _s = (Stmt) ((PairNode) node).first;
		final SootMethod _sm = (SootMethod) ((PairNode) node).second;
		final Collection _result = new HashSet();
		_result.addAll(getSuccs(_s, _sm, this.checker.cdas));
		_result.addAll(getSuccs(_s, _sm, this.checker.ddas));
		return _result;
	}

	/**
	 * Retrieves the successors of the given
	 *
	 * @param s is the statement of interest.
	 * @param sm is the method containing <code>s</code>.
	 * @param das is the collection of dependence analysis from which the graph-based information should be retrieved.
	 *
	 * @return a collection of successor nodes based on the dependence information available in <code>das</code>.
	 *
	 * @pre s != null and sm != null and das.oclIsKindOf(Collection(IDependencyAnalysis))
	 */
	private Collection getSuccs(final Stmt s, final SootMethod sm, final Collection das) {
		final Collection _result = new HashSet();
		final Iterator _i = das.iterator();
		final int _iEnd = das.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
			final Collection _dents = _da.getDependents(s, sm);
			final Iterator _j = _dents.iterator();
			final int _jEnd = _dents.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Object _o = _j.next();

				if (_o instanceof Pair) {
					final Pair _p = (Pair) _o;
					_result.add(new InfluenceChecker.PairNode(_p.getFirst(), _p.getSecond()));
				} else if (_o instanceof Stmt) {
					_result.add(new InfluenceChecker.PairNode(_o, sm));
				}
			}
		}
		return _result;
	}
}