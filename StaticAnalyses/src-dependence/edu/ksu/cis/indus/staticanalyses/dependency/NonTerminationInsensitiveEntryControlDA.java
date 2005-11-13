
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

import edu.ksu.cis.indus.common.collections.ListUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class provides intraprocedural non-termination sensitive backward control dependence information based on the
 * indirect version of non-termination sensitive backward control dependence.  For more information about the dependence
 * calculated in  this implementation, please refer to  <a
 * href="http://projects.cis.ksu.edu/docman/view.php/12/95/santos-tr2004-8.pdf">Santos-TR2004-8</a>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class NonTerminationInsensitiveEntryControlDA
  extends AbstractControlDA {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(NonTerminationInsensitiveEntryControlDA.class);

	/** 
	 * The instance of analysis that provides backward control dependence information.
	 */
	private final NonTerminationSensitiveEntryControlDA entryControlDA;

	/** 
	 * This indicates which version, direct or indirect, of non-termination sensitive backward dependence should be used as
	 * the basis of this analysis.
	 */
	private final boolean useIndirectBackwardDependence;

	/**
	 * Creates a new NonTerminationInsensitiveEntryControlDA object in which the indirect version of non-termination
	 * sensitive backward dependence will be used.
	 */
	public NonTerminationInsensitiveEntryControlDA() {
		this(false);
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param indirect <code>true</code> indicates that indirect version of non-termination sensitive  backward dependence
	 * 		  should be used as the basis of this analysis; <code>false</code> indicates the direct version of
	 * 		  non-termination sensitive  backward dependence should be used as the basis of this analysis.  <i>Please note
	 * 		  that this  constructor is  provided only for <b>experimentation</b> purposes.  As discussed in <a
	 * 		  href="http://projects.cis.ksu.edu/docman/view.php/12/95/santos-tr2004-8.pdf">Santos-TR2004-8</a>, only the
	 * 		  results based on the indirect non-termination sensitive backward dependence will be  complete.</i>
	 */
	NonTerminationInsensitiveEntryControlDA(final boolean indirect) {
		super(Direction.BI_DIRECTIONAL);
		entryControlDA = new NonTerminationSensitiveEntryControlDA();
		this.useIndirectBackwardDependence = indirect;
	}

	/**
	 * Calculates the control dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public void analyze() {
		analyze(callgraph.getReachableMethods());
	}



	/**
	 * Calculates the control dependency information for the provided methods.  The use of this method does not require a
	 * prior call to <code>setup</code>.
	 *
	 * @param methods to be analyzed.
	 *
	 * @pre methods != null and not method->includes(null)
	 */
	public void analyze(final Collection<SootMethod> methods) {
		unstable();

		entryControlDA.analyze(methods);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Entry Control Dependence processing");
		}

		final IDependencyAnalysis _nda;

		if (useIndirectBackwardDependence) {
			_nda = entryControlDA.getIndirectVersionOfDependence();
		} else {
			_nda = entryControlDA;
		}

		for (final Iterator<SootMethod> _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _method = _i.next();
			processMethod(_method, _nda);
		}

		if (LOGGER.isDebugEnabled()) {
			if (_nda instanceof IndirectDependenceAnalysis) {
				LOGGER.debug(((IndirectDependenceAnalysis) _nda).toString(methods));
			} else {
				LOGGER.debug(_nda.toString());
			}
			LOGGER.debug(toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Entry Control Dependence processing");
		}

		stable();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setBasicBlockGraphManager(BasicBlockGraphMgr)
	 */
	@Override public void setBasicBlockGraphManager(final BasicBlockGraphMgr bbm) {
		super.setBasicBlockGraphManager(bbm);
		entryControlDA.setBasicBlockGraphManager(bbm);
	}

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	@Override protected void setup()
	  throws InitializationException {
		super.setup();

		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}

		entryControlDA.initialize(info);
	}

	/**
	 * Retrieves the control sinks in the given graph.
	 *
	 * @param graph of interest.
	 *
	 * @return a collection of control sinks.
	 *
	 * @pre graph != null
	 * @post result != null
	 * @post result->forall(o | o->forall(p | graph.getNodes().contains(p)))
	 */
	private Collection<Collection<BasicBlock>> getControlSinksOf(final BasicBlockGraph graph) {
		final Collection<Collection<BasicBlock>> _result = new ArrayList<Collection<BasicBlock>>();
		final List<List<BasicBlock>> _sccs = graph.getSCCs(true);
		final Iterator<List<BasicBlock>> _i = _sccs.iterator();
		final int _iEnd = _sccs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			boolean _isAControlSink = true;
			final List<BasicBlock> _scc =  _i.next();
            final int _sccSize = _scc.size();

            if (_scc.size() > 1) {
            final Iterator<BasicBlock> _j = _scc.iterator();

			for (int _jIndex = 0; _jIndex < _sccSize && _isAControlSink; _jIndex++) {
				final BasicBlock _node = _j.next();
                _isAControlSink &= _scc.containsAll(_node.getSuccsOf()) && !_node.isAnExitBlock();
			}
            } else {
                final BasicBlock _node = _scc.iterator().next();
                _isAControlSink &= _node.getSuccsOf().contains(_node) || _node.isAnExitBlock();
            }

			if (_isAControlSink) {
				_result.add(_scc);
			}
		}
		return _result;
	}

	/**
	 * Retrieves the nodes belonging to the control sinks that do not contain the given node.
	 *
	 * @param node of interest.
	 * @param sinks a collection of control sinks.
	 *
	 * @return a collection of nodes.
	 *
	 * @pre node != null and sinks != null
	 * @post result != null 
	 * @post result->foreach(o | sinks->exists(p | p.contains(o) and not p.contains(node)))
	 */
	private Collection<BasicBlock> getNodesOfSinksNotContainingNode(final INode node, final Collection<Collection<BasicBlock>> sinks) {
		final Collection<BasicBlock> _result = new ArrayList<BasicBlock>();
		final Iterator<Collection<BasicBlock>> _i = sinks.iterator();
		final int _iEnd = sinks.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Collection<BasicBlock> _sinkNodes = _i.next();

			if (!_sinkNodes.contains(node)) {
				_result.addAll(_sinkNodes);
			}
		}
		return _result;
	}

	/**
	 * Analyzes the given method.
	 *
	 * @param method to be analyzed.
	 * @param da is the control dependence to be used.
	 *
	 * @pre method != null
	 */
	private void processMethod(final SootMethod method, final IDependencyAnalysis da) {
		final List<Collection<Stmt>> _methodLocalDee2Dent = MapUtils.getListFromMap(dependee2dependent, method);
		final List<Collection<Stmt>> _methodLocalDent2Dee = MapUtils.getListFromMap(dependent2dependee, method);
		final List<Stmt> _stmtList = getStmtList(method);
		final int _size = _stmtList.size();
		ListUtils.ensureSize(_methodLocalDent2Dee, _size, null);
		ListUtils.ensureSize(_methodLocalDee2Dent, _size, null);

		final BasicBlockGraph _bbg = getBasicBlockGraph(method);
		final Collection<Collection<BasicBlock>> _sinks = getControlSinksOf(_bbg);
		final Collection<Stmt> _dependees = new ArrayList<Stmt>();
		final List<BasicBlock> _nodes = _bbg.getNodes();
		final IWorkBag<Stmt> _wb = new HistoryAwareLIFOWorkBag<Stmt>(_dependees);
		final Iterator<BasicBlock> _i = _nodes.iterator();
		final int _iEnd = _nodes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _dependentBB = _i.next();
			final Collection<BasicBlock> _sinkNodes = getNodesOfSinksNotContainingNode(_dependentBB, _sinks);

			// we use a temporary copy of dependees as we will be updating the orginal collection.
			_dependees.clear();
			_wb.addAllWork(da.getDependees(_dependentBB.getLeaderStmt(), method));

			while (_wb.hasWork()) {
				final Stmt _dependeeStmt = _wb.getWork();
				final BasicBlock _dependeeBB = _bbg.getEnclosingBlock(_dependeeStmt);

				if (shouldRemoveDependenceBetween(_dependeeBB, _dependentBB, _sinkNodes)) {
					updateDependence(_dependentBB, _dependeeBB, _methodLocalDee2Dent, _methodLocalDent2Dee, _stmtList, true);
					_wb.addAllWork(da.getDependees(_dependeeBB.getTrailerStmt(), method));
				} else {
					updateDependence(_dependentBB, _dependeeBB, _methodLocalDee2Dent, _methodLocalDent2Dee, _stmtList, false);
				}
			}
		}
	}

	/**
	 * Checks if the dependence relation should be removed between statements in the given basic blocks.
	 *
	 * @param dependeeBB contains the dependee statement.
	 * @param dependentBB contains the dependent statements.
	 * @param sinkNodes is the collection of nodes that belong to control sinks of the basic block graph.
	 *
	 * @return <code>true</code> if the dependence relation should be removed; <code>false</code>, otherwise.
	 *
	 * @pre dependeeBB != null and dependentBB != null and sinkNodes != null
	 */
	private boolean shouldRemoveDependenceBetween(final BasicBlock dependeeBB, final BasicBlock dependentBB,
		final Collection<BasicBlock> sinkNodes) {
		final Collection<BasicBlock> _visited = new HashSet<BasicBlock>();
		final IWorkBag<BasicBlock> _wb = new HistoryAwareFIFOWorkBag<BasicBlock>(_visited);
		boolean _notcd = true;
		_visited.add(dependentBB);
		_wb.addWork(dependeeBB);

		while (_wb.hasWork() && _notcd) {
			final BasicBlock _bb = _wb.getWork();

			if (sinkNodes.contains(_bb)) {
				_notcd = false;
			} else {
				_wb.addAllWorkNoDuplicates(_bb.getSuccsOf());
			}
		}
		return _notcd;
	}

	/**
	 * Updates the dependence of statements in <code>dependentBB</code> on statements in <code>dependeeBB</code>. It updates
	 * the dependence information in both directions.
	 *
	 * @param dependentBB is the basic block containing the dependent statements.
	 * @param dependeeBB is the basic block containing the dependee statements.
	 * @param methodLocalDee2Dent is the map for dependee to dependent that is to be updated.
	 * @param methodLocalDent2Dee is the map for dependent to dependee that is to be updated.
	 * @param stmtList is the list of statements.
	 * @param remove <code>true</code> indicates the dependence should be removed. <code>false</code> if dependence should be
	 * 		  added.
	 *
	 * @pre dependentBB != null and dependeeBB != null and methodLocalDee2Dent != null and methodLocalDent2Dee != null and
	 * 		stmtList != null
	 */
	private void updateDependence(final BasicBlock dependentBB, final BasicBlock dependeeBB, final List<Collection<Stmt>> methodLocalDee2Dent,
		final List<Collection<Stmt>> methodLocalDent2Dee, final List<Stmt> stmtList, final boolean remove) {
		final Stmt _deeStmt = dependeeBB.getTrailerStmt();
		final Collection<Stmt> _stmtLevelDependentSet =
			ListUtils.getAtIndexFromListUsingFactory(methodLocalDee2Dent, stmtList.indexOf(_deeStmt), SetUtils.<Stmt>getFactory());
		final List<Stmt> _dents = dependentBB.getStmtsOf();

		if (remove) {
			_stmtLevelDependentSet.removeAll(_dents);
		} else {
			_stmtLevelDependentSet.addAll(_dents);
		}

		final Iterator<Stmt> _i = _dents.iterator();
		final int _iEnd = _dents.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _dentStmt = _i.next();
			final Collection<Stmt> _stmtLevelDependeeSet =
				ListUtils.getAtIndexFromListUsingFactory(methodLocalDent2Dee, stmtList.indexOf(_dentStmt), SetUtils.<Stmt>getFactory());

			if (remove) {
				_stmtLevelDependeeSet.remove(_deeStmt);
			} else {
				_stmtLevelDependeeSet.add(_deeStmt);
			}
		}
	}
}

// End of File
