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

import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.InstanceOfPredicate;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

/**
 * This class provides data dependency information based on identifiers. Local variables in a method enable such dependence.
 * Given a def site, the use site is tracked based on the id being defined and used. Hence, information about field/array
 * access via primaries which are local variables is inaccurate in such a setting, hence, it is not provided by this class.
 * Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
 * <p>
 * This implementation is based on <code>soot.toolkits.scalar.SimpleLocalDefs</code> and
 * <code>soot.toolkits.scalar.SimpleLocalUses</code> classes .
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class IdentifierBasedDataDA
		extends
		AbstractDependencyAnalysis<Pair<Local, Stmt>, SootMethod, DefinitionStmt, SootMethod, List<Map<Local, Collection<DefinitionStmt>>>, DefinitionStmt, SootMethod, Pair<Local, Stmt>, SootMethod, List<Collection<Pair<Local, Stmt>>>> {

	/**
	 * This predicate can be used to check if an object of this class type.
	 */
	public static final IPredicate<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> INSTANCEOF_PREDICATE = new InstanceOfPredicate<IdentifierBasedDataDA, IDependencyAnalysis<?, ?, ?, ?, ?, ?>>(
			IdentifierBasedDataDA.class);

	/*
	 * The dependent information is stored as follows: For each method, a list of length equal to the number of statements in
	 * the methods is maintained. In case of dependent information, at each location corresponding to the statement a set of
	 * dependent statements is maintained in the list. In case of dependee information, at each location corresponding to the
	 * statement a map is maintained in the list. The map maps a value box in the statement to a collection of dependee
	 * statements. The rational for the way the information is maintained is only one local can be defined in a statement.
	 * Also, if the definition of a local reaches a statement, then all occurrences of that local at that statement must be
	 * dependent on the same reaching def.
	 */

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierBasedDataDA.class);

	/**
	 * This provides call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * Creates an instance of this class.
	 */
	public IdentifierBasedDataDA() {
		super(Direction.BI_DIRECTIONAL);
	}

	/**
	 * Returns the statements on which the locals in <code>stmt</code> depends in the given <code>method</code>.
	 * 
	 * @param stmt in which the locals occur.
	 * @param method in which <code>programPoint</code> occurs.
	 * @param da is the analysis from which to retrieve dependence information.
	 * @return a collection of statements on which <code>programPoint</code> depends.
	 */
	final static Collection<DefinitionStmt> getDependeesHelper(final Stmt stmt, final SootMethod method,
			final IDependencyAnalysis<Pair<Local, Stmt>, SootMethod, DefinitionStmt, ?, ?, ?> da) {
		Collection<DefinitionStmt> _result = Collections.emptySet();

		for (final Iterator<ValueBox> _i = stmt.getUseBoxes().iterator(); _i.hasNext();) {
			final ValueBox _vb = _i.next();
			final Value _v = _vb.getValue();

			if (_v instanceof Local) {
				final Collection<DefinitionStmt> _c = da.getDependees(new Pair<Local, Stmt>((Local) _v, stmt), method);

				if (_c != null) {
					_result = Collections.unmodifiableCollection(_c);
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("No dependence information available for " + stmt + " in " + method);
					}
				}
			}
		}

		return _result;
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Identifier Based Data Dependence processing");
		}

		for (final Iterator<SootMethod> _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _currMethod = _i.next();
			final UnitGraph _unitGraph = getUnitGraph(_currMethod);

			if (_unitGraph == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Method " + _currMethod.getSignature() + " does not have a unit graph.");
				}
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing " + _currMethod.getSignature());
			}

			calculateDAForMethod(_currMethod, _unitGraph);
		}
		stable();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END:  Identifier Based Data Dependence processing");
		}
	}

	/**
	 * Returns the statements on which <code>o</code>, depends in the given <code>method</code>.
	 * 
	 * @param programPoint is the program point at which a local occurs in the statement.
	 * @param method in which <code>programPoint</code> occurs.
	 * @return a collection of statements on which <code>programPoint</code> depends.
	 * @pre programPoint.oclTypeOf(Pair).getFirst() != null and programPoint.oclTypeOf(Pair).getSecond() != null
	 */
	public Collection<DefinitionStmt> getDependees(final Pair<Local, Stmt> programPoint, final SootMethod method) {
		Collection<DefinitionStmt> _result = Collections.emptySet();

		final Stmt _stmt = programPoint.getSecond();
		final Local _local = programPoint.getFirst();
		final List<Map<Local, Collection<DefinitionStmt>>> _dependees = dependent2dependee.get(method);
		if (_dependees != null) {
			final Map<Local, Collection<DefinitionStmt>> _local2defs = _dependees.get(getStmtList(method).indexOf(_stmt));
			final Collection<DefinitionStmt> _c = _local2defs.get(_local);

			if (_c != null) {
				_result = Collections.unmodifiableCollection(_c);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No dependence information available for " + programPoint + " in " + method);
				}
			}
		}
		return _result;
	}

	/**
	 * Returns the statements on which the locals in <code>stmt</code> depends in the given <code>method</code>.
	 * 
	 * @param stmt in which the locals occur.
	 * @param method in which <code>programPoint</code> occurs.
	 * @return a collection of statements on which <code>programPoint</code> depends.
	 */
	public Collection<DefinitionStmt> getDependees(final Stmt stmt, final SootMethod method) {
		return getDependeesHelper(stmt, method, this);
	}

	/**
	 * Returns the statement and the program point in it which depends on statement provided via <code>programPoint</code>
	 * occurring in the given <code>method</code>.
	 * 
	 * @param programPoint is the definition statement or a pair containing the definition statement as the first element.
	 *            Although, only one variable can be defined in a Jimple statement, we allow for a pair in this query to make
	 *            <code>getDependees</code> and <code>getDependents</code> symmetrical for ease of usage.
	 * @param method is the method in which the statement provided by <code>programPoint</code> occurs.
	 * @return a collection of statement and program points in them which depend on the definition at
	 *         <code>programPoint</code>.
	 */
	public Collection<Pair<Local, Stmt>> getDependents(final DefinitionStmt programPoint, final SootMethod method) {
		Collection<Pair<Local, Stmt>> _result = Collections.emptySet();

		final List<Collection<Pair<Local, Stmt>>> _dependents = dependee2dependent.get(method);

		if (_dependents != null) {
			final Collection<Pair<Local, Stmt>> _temp = _dependents.get(getStmtList(method).indexOf(programPoint));
			if (_temp != null) {
				_result = Collections.unmodifiableCollection(_temp);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection<IDependencyAnalysis.DependenceSort> getIds() {
		return Collections.singleton(IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA);
	}

	// /CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis. The representation includes the results of the analysis.
	 * 
	 * @return a stringized representation of this object.
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for Identifier-based Data dependence as calculated by "
				+ this.getClass().getName() + "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator<Map.Entry<SootMethod, List<Collection<Pair<Local, Stmt>>>>> _i = dependee2dependent.entrySet()
				.iterator(); _i.hasNext();) {
			final Map.Entry<SootMethod, List<Collection<Pair<Local, Stmt>>>> _entry = _i.next();
			_localEdgeCount = 0;

			final List<Stmt> _stmts = getStmtList(_entry.getKey());
			int _count = 0;

			for (final Iterator<Collection<Pair<Local, Stmt>>> _j = _entry.getValue().iterator(); _j.hasNext();) {
				final Collection<Pair<Local, Stmt>> _c = _j.next();
				final Stmt _stmt = _stmts.get(_count++);

				for (final Iterator<?> _k = _c.iterator(); _k.hasNext();) {
					_temp.append("\t\t" + _stmt + " <-- " + _k.next() + "\n");
				}
				_localEdgeCount += _c.size();
			}
			_result.append("\tFor " + _entry.getKey() + " there are " + _localEdgeCount
					+ " Identifier-based Data dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " Identifier-based Data dependence edges exist.");
		return _result.toString();
	}

	// /CLOVER:ON

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependenceRetriever()
	 */
	@Override protected IDependenceRetriever<Pair<Local, Stmt>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Pair<Local, Stmt>> getDependenceRetriever() {
		return new LocalStmtPairRetriever();
	}

	/**
	 * Sets up internal data structures.
	 * 
	 * @throws InitializationException when call graph service is not provided.
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	@Override protected void setup() throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Calculates dependence info for the given method.
	 * 
	 * @param method for which to calculate dependence.
	 * @param unitGraph of <code>method</code>.
	 * @pre method != null and unitGraph != null
	 */
	private void calculateDAForMethod(final SootMethod method, final UnitGraph unitGraph) {
		final SimpleLocalDefs _defs = new SimpleLocalDefs(unitGraph);
		final SimpleLocalUses _uses = new SimpleLocalUses(unitGraph, _defs);
		final Collection<Stmt> _t = getStmtList(method);
		final List<Map<Local, Collection<DefinitionStmt>>> _dependees = new ArrayList<Map<Local, Collection<DefinitionStmt>>>(
				_t.size());
		final List<Collection<Pair<Local, Stmt>>> _dependents = new ArrayList<Collection<Pair<Local, Stmt>>>(_t.size());

		for (final Iterator<Stmt> _j = _t.iterator(); _j.hasNext();) {
			final Stmt _currStmt = _j.next();
			Collection<Pair<Local, Stmt>> _currUses = Collections.emptySet();

			if (_currStmt instanceof DefinitionStmt) {
				final Collection<UnitValueBoxPair> _temp = _uses.getUsesOf(_currStmt);

				if (_temp.size() != 0) {
					_currUses = new ArrayList<Pair<Local, Stmt>>();

					for (final Iterator<UnitValueBoxPair> _k = _temp.iterator(); _k.hasNext();) {
						final UnitValueBoxPair _p = _k.next();
						_currUses.add(new Pair<Local, Stmt>((Local) _p.getValueBox().getValue(), (Stmt) _p.getUnit()));
					}
				}
			}
			_dependents.add(_currUses);

			Map<Local, Collection<DefinitionStmt>> _currDefs = Collections.emptyMap();

			if (_currStmt.getUseBoxes().size() > 0) {
				_currDefs = new HashMap<Local, Collection<DefinitionStmt>>();

				for (final Iterator<ValueBox> _k = _currStmt.getUseBoxes().iterator(); _k.hasNext();) {
					final ValueBox _currValueBox = _k.next();
					final Value _value = _currValueBox.getValue();

					if (_value instanceof Local && !_currDefs.containsKey(_value)) {
						final Local _local = (Local) _value;
						_currDefs.put(_local, _defs.getDefsOfAt(_local, _currStmt));
					}
				}
			}
			_dependees.add(_currDefs);
		}
		dependee2dependent.put(method, _dependents);
		dependent2dependee.put(method, _dependees);
	}
}

// End of File
