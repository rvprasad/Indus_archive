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

import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.InstanceOfPredicate;
import edu.ksu.cis.indus.common.collections.ListUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * <p>
 * This class provides intraprocedural forward control dependence information. The "direct-ness" and the "non-termination
 * sensitivity" of the analysis depends on the flavor backward control dependence used to calculate forward control dependence
 * (provided via <code>setup</code>).
 * </p>
 * <p>
 * For more information about the dependence calculated in this implementation, please refer to <a href="FILL ME">FILL ME</a>.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExitControlDA
		extends AbstractControlDA {

	/**
	 * This predicate can be used to check if an object of this class type.
	 */
	public static final IPredicate<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> INSTANCEOF_PREDICATE = new InstanceOfPredicate<ExitControlDA, IDependencyAnalysis<?, ?, ?, ?, ?, ?>>(
			ExitControlDA.class);

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExitControlDA.class);

	/**
	 * The instance of analysis that provides backward control dependence information.
	 */
	private IDependencyAnalysis<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> entryControlDA;

	/**
	 * Creates an instance of this class.
	 */
	public ExitControlDA() {
		super(Direction.FORWARD_DIRECTION);
	}

	/**
	 * Calculates the control dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public final void analyze() {
		analyze(callgraph.getReachableMethods());
	}

	/**
	 * Calculates the control dependency information for the provided methods. The use of this method does not require a prior
	 * call to <code>setup</code>.
	 *
	 * @param methods to be analyzed.
	 * @pre methods != null and not method->includes(null)
	 */
	public final void analyze(final Collection<SootMethod> methods) {
		unstable();

		if (entryControlDA.isStable()) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: Exit Control Dependence processing");
			}

			for (final Iterator<SootMethod> _i = methods.iterator(); _i.hasNext();) {
				final SootMethod _sm = _i.next();
				final BasicBlockGraph _bbg = getBasicBlockGraph(_sm);
				final Collection<BasicBlock> _dependeeBBs = calculateEntryControlDependeesOfSinksIn(_bbg, _sm);

				if (!_dependeeBBs.isEmpty()) {
					calculateDependenceForStmts(calculateDependenceForBBs(_bbg, _dependeeBBs), _sm);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(toString());
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: Exit Control Dependence processing");
			}

			stable();
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: Exit Control Dependence processing due to unstable entry control dependence info.");
			}
		}
	}

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 * @pre info.get(IDependencyAnalysis.CONTROL_DA) != null
	 * @pre info.get(IDependencyAnalysis.ID).oclIsTypeOf(IDependencyAnalysis)
	 * @pre info.get(IDependencyAnalysis.ID).getDirection().equals(IDependencyAnalysis.BACKWARD_DIRECTION) or
	 *      info.get(IDependencyAnalysis.ID).getDirection().equals(IDependencyAnalysis.BI_DIRECTIONAL)
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	@Override protected void setup() throws InitializationException {
		super.setup();

		final Collection<IDependencyAnalysis<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt>> _temp = (Collection) info
				.get(IDependencyAnalysis.DependenceSort.CONTROL_DA);

		if (_temp == null) {
			throw new InitializationException(IDependencyAnalysis.DependenceSort.CONTROL_DA
					+ " was not provided or none of the provided control dependences were backward in direction.");
		}

		for (final Iterator<IDependencyAnalysis<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt>> _i = _temp.iterator(); _i
				.hasNext();) {
			final IDependencyAnalysis<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> _da = _i.next();

			if (_da.getIds().contains(IDependencyAnalysis.DependenceSort.CONTROL_DA)
					&& (_da.getDirection().equals(Direction.BACKWARD_DIRECTION) || _da.getDirection().equals(
							Direction.BI_DIRECTIONAL))) {
				entryControlDA = _da;
			}
		}

		if (entryControlDA == null) {
			throw new InitializationException(IDependencyAnalysis.DependenceSort.CONTROL_DA + " with direction "
					+ Direction.BACKWARD_DIRECTION
					+ " was not provided or none of the provided control dependences were backward in direction.");
		}
	}

	/**
	 * Calculates the dependence information at the level of basic blocks.
	 *
	 * @param bbg is the basic block graph for which dependence need to be calculated.
	 * @param dependeeBBs is the set of forward control dependees in <code>bbg</code>.
	 * @return a map from dependee basic block to collection of dependents basic block.
	 * @pre bbg != null and dependeeBBs != null
	 * @post result != null
	 * @post bbg.getNodes().containsAll(result.keySet())
	 * @post result.values()->forall(o | bbg.getNodes().containsAll(o))
	 */
	private Map<BasicBlock, Collection<BasicBlock>> calculateDependenceForBBs(final BasicBlockGraph bbg,
			final Collection<BasicBlock> dependeeBBs) {
		final Map<BasicBlock, Collection<BasicBlock>> _dependence = new HashMap<BasicBlock, Collection<BasicBlock>>();
		final Iterator<BasicBlock> _i = dependeeBBs.iterator();
		final int _iEnd = dependeeBBs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _dependeeBB = _i.next();
			final Collection<BasicBlock> _dependents = bbg.getReachablesFrom(_dependeeBB, false);
			_dependence.put(_dependeeBB, _dependents);
		}
		return _dependence;
	}

	/**
	 * Calculates dependence information for statements from the dependence information for basic blocks.
	 *
	 * @param dependeeBB2dependentBBs maps dependee basic blocks to collection of dependent basic blocks.
	 * @param method for which the dependence is being recorded.
	 * @pre dependeeBB2dependentBBs != null and method != null
	 */
	private void calculateDependenceForStmts(final Map<BasicBlock, Collection<BasicBlock>> dependeeBB2dependentBBs,
			final SootMethod method) {
		final List<Collection<Stmt>> _methodLocalDee2Dent = MapUtils.getListFromMap(dependee2dependent, method);
		final List<Collection<Stmt>> _methodLocalDent2Dee = MapUtils.getListFromMap(dependent2dependee, method);
		final List<Stmt> _stmtList = getStmtList(method);
		final int _noOfStmtsInMethod = _stmtList.size();
		final List<Stmt> _dependeeBBStmts = new ArrayList<Stmt>();
		ListUtils.ensureSize(_methodLocalDee2Dent, _noOfStmtsInMethod, null);
		ListUtils.ensureSize(_methodLocalDent2Dee, _noOfStmtsInMethod, null);

		for (final Iterator<Map.Entry<BasicBlock, Collection<BasicBlock>>> _i = dependeeBB2dependentBBs.entrySet().iterator(); _i
				.hasNext();) {
			final Map.Entry<BasicBlock, Collection<BasicBlock>> _entry = _i.next();
			final BasicBlock _dependeeBB = _entry.getKey();
			final Stmt _dependee = _dependeeBB.getTrailerStmt();

			// record dependence within dependee block
			final Collection<Stmt> _dependents = ListUtils.getAtIndexFromListUsingFactory(_methodLocalDee2Dent, _stmtList
					.indexOf(_dependee), SetUtils.<Stmt> getFactory());
			_dependeeBBStmts.clear();
			_dependeeBBStmts.addAll(_dependeeBB.getStmtsOf());
			_dependeeBBStmts.remove(_dependee);
			_dependents.addAll(_dependeeBBStmts);
			recordDependence(_methodLocalDent2Dee, _stmtList, _dependee, _dependeeBBStmts);

			// record dependence for dependent blocks
			final Collection<BasicBlock> _dependentBBs = _entry.getValue();
			final Iterator<BasicBlock> _j = _dependentBBs.iterator();
			final int _jEnd = _dependentBBs.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final BasicBlock _dependentBB = _j.next();
				final List<Stmt> _dependentBBStmts = _dependentBB.getStmtsOf();
				_dependents.addAll(_dependentBBStmts);
				recordDependence(_methodLocalDent2Dee, _stmtList, _dependee, _dependentBBStmts);
			}
		}
		dependent2dependee.put(method, _methodLocalDent2Dee);
		dependee2dependent.put(method, _methodLocalDee2Dent);
	}

	/**
	 * Calculates the dependees of the control sinks.
	 *
	 * @param bbg is the basic block graph in which control sink dependees need to be calculated.
	 * @param method of <code>bbg</code>.
	 * @return the collection of basic blocks that are backward control dependees of the control sinks in <code>bbg</code>.
	 * @pre bbg != null and method != null
	 * @post result != null;
	 * @post bbg.getNodes().containsAll(result)
	 */
	private Collection<BasicBlock> calculateEntryControlDependeesOfSinksIn(final BasicBlockGraph bbg, final SootMethod method) {
		final Collection<BasicBlock> _result;
		final Collection<BasicBlock> _sinks = new ArrayList<BasicBlock>();
		_sinks.addAll(bbg.getTails());

		if (_sinks.size() > 1) {
			_result = new HashSet<BasicBlock>();

			for (final Iterator<BasicBlock> _i = _sinks.iterator(); _i.hasNext();) {
				final BasicBlock _sink = _i.next();
				final Stmt _stmt = _sink.getLeaderStmt();
				final Collection<Stmt> _dependees = entryControlDA.getDependees(_stmt, method);

				for (final Iterator<Stmt> _j = _dependees.iterator(); _j.hasNext();) {
					final Stmt _dependee = _j.next();
					final BasicBlock _dependeeBB = bbg.getEnclosingBlock(_dependee);
					_result.add(_dependeeBB);
				}
			}
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * Record dependent to dependee direction of dependence.
	 *
	 * @param methodDependent2Dependee maps dependent to collection of dependees.
	 * @param stmts is the list of statements in the method.
	 * @param dependee obviously.
	 * @param dependents obviously.
	 * @pre methodDependent2Dependee != null
	 * @pre stmts != null
	 * @pre dependee != null
	 * @pre dependents != null
	 */
	private void recordDependence(final List<Collection<Stmt>> methodDependent2Dependee, final List<Stmt> stmts,
			final Stmt dependee, final List<Stmt> dependents) {
		final Iterator<Stmt> _k = dependents.iterator();
		final int _kEnd = dependents.size();

		for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
			final Stmt _dependent = _k.next();
			final Collection<Stmt> _dependees = ListUtils.getAtIndexFromListUsingFactory(methodDependent2Dependee, stmts
					.indexOf(_dependent), SetUtils.<Stmt> getFactory());
			_dependees.add(dependee);
		}
	}
}

// End of File
