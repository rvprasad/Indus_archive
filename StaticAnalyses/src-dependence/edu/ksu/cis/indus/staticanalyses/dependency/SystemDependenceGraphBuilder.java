
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.graph.SimpleEdgeGraph;
import edu.ksu.cis.indus.common.graph.SimpleEdgeGraphBuilder;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;


/**
 * This constructs system dependence graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SystemDependenceGraphBuilder {
	/** 
	 * This is the label of the data dependence arch across method boundaries.
	 */
	private static final Object INTER_PROCEDURAL_DATA_DEPENDENCE = "Inter-procedural data dependence";

	/** 
	 * The collection of classes that define the scope in which dependence nodes have both incoming and outgoing edges.
	 */
	private final Collection coreClasses;

	/** 
	 * This is collection of dependence analyses from which dependences need to be captured.
	 */
	private final Collection deps;

	/** 
	 * This is the call graph.  If this is <code>null</code> then dependences such as control dependence that are based on
	 * call-site will not be captured.
	 */
	private final ICallGraphInfo callgraph;

	/** 
	 * The pair manager to be used.
	 */
	private final PairManager pairMgr;

	/** 
	 * The graph builder to be used.
	 */
	private final SimpleEdgeGraphBuilder segb;

	/**
	 * Creates an instance of this class.
	 *
	 * @param dependences is collection of dependence analyses from which dependences need to be captured.
	 * @param cgi is the call graph.  If this is <code>null</code> then dependences such as control dependence that are based
	 * 		  on  call-site will not be captured.
	 * @param classes
	 */
	private SystemDependenceGraphBuilder(final Collection dependences, final ICallGraphInfo cgi, final Collection classes) {
		pairMgr = new PairManager(true, true);
		segb = new SimpleEdgeGraphBuilder();
		deps = dependences;
		callgraph = cgi;
		coreClasses = classes;
	}

	/**
	 * Creates a system dependence graph.
	 *
	 * @param dependences is collection of dependence analyses from which dependences need to be captured.
	 * @param cgi is the call graph.  If this is <code>null</code> then dependences such as control dependence that are based
	 * 		  on  call-site will not be captured.
	 * @param classes is the collection of classes that define the scope in which dependence nodes have both incoming and
	 * 		  outgoing edges.
	 *
	 * @return a dependence graph
	 *
	 * @pre dependences != null and dependences.oclIsKindOf(Collection(IDependencyAnalysis))
	 * @pre classes != null and classes.oclIsKindOf(Collection(SootClass))
	 * @post result != null
	 * @post result.getNodes()->forall(o | o.getObject().oclIsKindOf(Pair(Stmt, SootMethod)))
	 * @post result.getNodes()->forall(o | o.getIncomingEdgeLabels()->forall(p | dependences->exists(q |
	 * 		 q.getIds().contains(p)) or p.equals(INTER_PROCEDURAL_DATA_DEPENDENCE)))
	 */
	public static SimpleEdgeGraph getSystemDependenceGraph(final Collection dependences, final ICallGraphInfo cgi,
		final Collection classes) {
		final SystemDependenceGraphBuilder _builder = new SystemDependenceGraphBuilder(dependences, cgi, classes);
		return _builder.createGraph();
	}

	/**
	 * Adds edges to the given graph builder that capture the dependences edges available from the provided analyses onto the
	 * given statment in the given method
	 *
	 * @param stmt that serves as the destination.
	 * @param method that contains <code>stmt</code>.
	 *
	 * @pre stmt != null and method != null
	 */
	private void addEdgesFor(final Stmt stmt, final SootMethod method) {
		final Pair _dest = pairMgr.getPair(stmt, method);
		final Collection _sources = new ArrayList();
		final Iterator _i = deps.iterator();
		final int _iEnd = deps.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
			final Collection _ids = _da.getIds();
			final Collection _dees = _da.getDependees(stmt, method);
			final Iterator _j = _dees.iterator();
			final int _jEnd = _dees.size();
			_sources.clear();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Object _o = _j.next();

				if (_o == null) {
					if (callgraph != null) {
						final Collection _callers = callgraph.getCallers(method);
						final Iterator _k = _callers.iterator();
						final int _kEnd = _callers.size();

						for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
							final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _k.next();
							_sources.add(pairMgr.getPair(_ctrp.getStmt(), _ctrp.getMethod()));
						}
					} else {
						continue;
					}
				} else if (_o instanceof Pair) {
					_sources.add(_o);
				} else {
					_sources.add(pairMgr.getPair(_o, method));
				}
			}

			if (_ids.contains(IDependencyAnalysis.SYNCHRONIZATION_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.SYNCHRONIZATION_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.READY_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.READY_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.INTERFERENCE_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.INTERFERENCE_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.REFERENCE_BASED_DATA_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.REFERENCE_BASED_DATA_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.CONTROL_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.CONTROL_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA, _dest);
			}
		}

		processForInterProceduralEdges(_dest);
	}

	/**
	 * Creates the system dependence graph.
	 *
	 * @return the SDG
	 *
	 * @post result != null
	 */
	private SimpleEdgeGraph createGraph() {
		segb.createGraph();

		final Iterator _j = coreClasses.iterator();
		final int _jEnd = coreClasses.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final SootClass _sc = (SootClass) _j.next();
			final Collection _methods = _sc.getMethods();
			final Iterator _k = _methods.iterator();
			final int _kEnd = _methods.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final SootMethod _sm = (SootMethod) _k.next();

				if (_sm.hasActiveBody()) {
					final Iterator _l = _sm.getActiveBody().getUnits().iterator();
					final int _lEnd = _sm.getActiveBody().getUnits().size();

					for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
						final Stmt _stmt = (Stmt) _l.next();
						addEdgesFor(_stmt, _sm);
					}
				}
			}
		}

		segb.finishBuilding();
		return (SimpleEdgeGraph) segb.getBuiltGraph();
	}

	/**
	 * Adds data dependence edges across procedure boundaries.
	 *
	 * @param node that needs to be processed.
	 *
	 * @pre node != null
	 */
	private void processForInterProceduralEdges(final Pair node) {
		final Stmt _stmt = (Stmt) node.getFirst();
		final SootMethod _sm = (SootMethod) node.getSecond();

		if (_stmt instanceof ReturnStmt) {
			final Iterator _i = callgraph.getCallers(_sm).iterator();
			final int _iEnd = callgraph.getCallers(_sm).size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _i.next();
				final Stmt _dest = _ctrp.getStmt();

				if (_dest instanceof AssignStmt) {
					segb.addEdgeFromTo(node, INTER_PROCEDURAL_DATA_DEPENDENCE, pairMgr.getPair(_dest, _ctrp.getMethod()));
				}
			}
		} else if (_stmt instanceof IdentityStmt) {
			final IdentityStmt _s = (IdentityStmt) _stmt;

			if (_s.getRightOp() instanceof ParameterRef) {
				final Iterator _i = callgraph.getCallers(_sm).iterator();
				final int _iEnd = callgraph.getCallers(_sm).size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _i.next();
					segb.addEdgeFromTo(pairMgr.getPair(_ctrp.getStmt(), _ctrp.getMethod()), INTER_PROCEDURAL_DATA_DEPENDENCE,
						node);
				}
			}
		}
	}
}

// End of File
