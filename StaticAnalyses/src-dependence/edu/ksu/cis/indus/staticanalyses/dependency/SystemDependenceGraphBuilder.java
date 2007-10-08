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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.graph.SimpleEdgeGraph;
import edu.ksu.cis.indus.common.graph.SimpleEdgeGraphBuilder;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	public static final Object INTER_PROCEDURAL_DATA_DEPENDENCE = "Inter-procedural data dependence";

	/**
	 * The collection of classes that define the scope in which dependence nodes have both incoming and outgoing edges.
	 */
	private final Collection<SootClass> coreClasses;

	/**
	 * This is collection of dependence analyses from which dependences need to be captured.
	 */
	private final Collection<IDependencyAnalysis> deps;

	/**
	 * This is the call graph. If this is <code>null</code> then dependences such as control dependence that are based on
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
	private final SimpleEdgeGraphBuilder<Pair<Stmt, SootMethod>> segb;

	/**
	 * Creates an instance of this class.
	 *
	 * @param dependences is collection of dependence analyses from which dependences need to be captured.
	 * @param cgi is the call graph. If this is <code>null</code> then dependences such as control dependence that are based
	 *            on call-site will not be captured.
	 * @param classes to be considered while building the graph.
	 * @pre dependences != null and cgi != null and classes != null
	 */
	private SystemDependenceGraphBuilder(final Collection<IDependencyAnalysis> dependences,
			final ICallGraphInfo cgi, final Collection<SootClass> classes) {
		pairMgr = new PairManager(true, true);
		segb = new SimpleEdgeGraphBuilder<Pair<Stmt, SootMethod>>();
		deps = dependences;
		callgraph = cgi;
		coreClasses = classes;
	}

	/**
	 * Creates a system dependence graph.
	 *
	 * @param dependences is collection of dependence analyses from which dependences need to be captured.
	 * @param cgi is the call graph. If this is <code>null</code> then dependences such as control dependence that are based
	 *            on call-site will not be captured.
	 * @param classes is the collection of classes that define the scope in which dependence nodes have both incoming and
	 *            outgoing edges.
	 * @return a dependence graph
	 * @pre dependences != null
	 * @pre classes != null
	 * @post result != null
	 * @post result.getNodes()->forall(o | o.getIncomingEdgeLabels()->forall(p | dependences->exists(q |
	 *       q.getIds().contains(p)) or p.equals(INTER_PROCEDURAL_DATA_DEPENDENCE)))
	 */
	public static SimpleEdgeGraph<Pair<Stmt, SootMethod>> getSystemDependenceGraph(
			final Collection<IDependencyAnalysis> dependences, final ICallGraphInfo cgi,
			final Collection<SootClass> classes) {
		final SystemDependenceGraphBuilder _builder = new SystemDependenceGraphBuilder(dependences, cgi, classes);
		return _builder.createGraph();
	}

	/**
	 * Adds edges to the given graph builder that capture the dependences edges available from the provided analyses onto the
	 * given statment in the given method.
	 *
	 * @param stmt that serves as the destination.
	 * @param method that contains <code>stmt</code>.
	 * @pre stmt != null and method != null
	 */
	private void addEdgesFor(final Stmt stmt, final SootMethod method) {
		final Pair<Stmt, SootMethod> _dest = pairMgr.getPair(stmt, method);
		final Collection<Pair<Stmt, SootMethod>> _sources = new ArrayList<Pair<Stmt, SootMethod>>();
		final Iterator<IDependencyAnalysis> _i = deps.iterator();
		final int _iEnd = deps.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IDependencyAnalysis _da = _i.next();
			final Collection<? extends Comparable<?>> _ids = _da.getIds();
			final Collection<?> _dees;
			
			// This whole block is a HACK!!! stemming from the use of Generics.
			if (_da instanceof IdentifierBasedDataDA) {
				_dees = ((IdentifierBasedDataDA) _da).getDependees(stmt, method);
			} else if (_da instanceof IdentifierBasedDataDAv2) {
				_dees = ((IdentifierBasedDataDAv2) _da).getDependees(stmt, method);
			} else if ((_da instanceof ReferenceBasedDataDA || _da instanceof InterferenceDAv1)) {
				if (stmt instanceof AssignStmt) { 
					_dees = _da.getDependees(stmt, method);
				} else {
					_dees = Collections.emptyList();
				}
			} else {
				_dees = _da.getDependees(stmt, method);
			}
			
			final Iterator<?> _j = _dees.iterator();
			final int _jEnd = _dees.size();
			_sources.clear();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Object _o = _j.next();

				if (_o instanceof Pair) {
					_sources.add((Pair) _o);
				} else {
					_sources.add(pairMgr.getPair((Stmt) _o, method));
				}
			}

			if (_ids.contains(IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.DependenceSort.READY_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.DependenceSort.READY_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.DependenceSort.INTERFERENCE_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.DependenceSort.CONTROL_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.DependenceSort.CONTROL_DA, _dest);
			}

			if (_ids.contains(IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA)) {
				segb.addEdgeFromTo(_sources, IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA, _dest);
			}
		}
		
		if (callgraph != null) {
			final Collection<CallTriple> _callers = callgraph.getCallers(method);
			final Iterator<CallTriple> _k = _callers.iterator();
			final int _kEnd = _callers.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final CallTriple _ctrp = _k.next();
				_sources.add(pairMgr.getPair(_ctrp.getStmt(), _ctrp.getMethod()));
			}
		}

		processForInterProceduralEdges(_dest);
	}

	/**
	 * Creates the system dependence graph.
	 *
	 * @return the SDG
	 * @post result != null
	 */
	private SimpleEdgeGraph<Pair<Stmt, SootMethod>> createGraph() {
		segb.createGraph();

		final Iterator<SootClass> _j = coreClasses.iterator();
		final int _jEnd = coreClasses.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final SootClass _sc = _j.next();
			final Collection<SootMethod> _methods = _sc.getMethods();
			final Iterator<SootMethod> _k = _methods.iterator();
			final int _kEnd = _methods.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final SootMethod _sm = _k.next();

				if (_sm.hasActiveBody()) {
					final Iterator<Stmt> _l = _sm.getActiveBody().getUnits().iterator();
					final int _lEnd = _sm.getActiveBody().getUnits().size();

					for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
						final Stmt _stmt = _l.next();
						addEdgesFor(_stmt, _sm);
					}
				}
			}
		}

		segb.finishBuilding();
		return segb.getBuiltGraph();
	}

	/**
	 * Adds data dependence edges across procedure boundaries.
	 *
	 * @param node that needs to be processed.
	 * @pre node != null
	 */
	private void processForInterProceduralEdges(final Pair<Stmt, SootMethod> node) {
		final Stmt _stmt = node.getFirst();
		final SootMethod _sm = node.getSecond();

		if (_stmt instanceof ReturnStmt) {
			final Iterator<CallTriple> _i = callgraph.getCallers(_sm).iterator();
			final int _iEnd = callgraph.getCallers(_sm).size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final CallTriple _ctrp = _i.next();
				final Stmt _dest = _ctrp.getStmt();

				if (_dest instanceof AssignStmt) {
					segb.addEdgeFromTo(node, INTER_PROCEDURAL_DATA_DEPENDENCE, pairMgr.getPair(_dest, _ctrp.getMethod()));
				}
			}
		} else if (_stmt instanceof IdentityStmt) {
			final IdentityStmt _s = (IdentityStmt) _stmt;

			if (_s.getRightOp() instanceof ParameterRef) {
				final Iterator<CallTriple> _i = callgraph.getCallers(_sm).iterator();
				final int _iEnd = callgraph.getCallers(_sm).size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final CallTriple _ctrp = _i.next();
					segb.addEdgeFromTo(pairMgr.getPair(_ctrp.getStmt(), _ctrp.getMethod()), INTER_PROCEDURAL_DATA_DEPENDENCE,
							node);
				}
			}
		}
	}
}

// End of File
