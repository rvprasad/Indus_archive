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
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

/**
 * This class provides intraprocedural data dependency information based on identifiers. Local variables in a method enable
 * such dependence. Given a def site, the use site is tracked based on the id being defined and used. Hence, information about
 * field/array access via primaries which are local variables is inaccurate in such a setting, hence, it is not provided by
 * this class. Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
 * <p>
 * This implementation is based on <code>edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysis</code> class.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class IdentifierBasedDataDAv2
		extends
		AbstractDependencyAnalysis<Pair<Local, Stmt>, SootMethod, DefinitionStmt, SootMethod, IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>>, DefinitionStmt, SootMethod, Pair<Local, Stmt>, SootMethod, IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>>> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierBasedDataDAv2.class);

	/**
	 * This provides call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * Creates an instance of this class.
	 */
	public IdentifierBasedDataDAv2() {
		super(Direction.BI_DIRECTIONAL);
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public final void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Identifier Based Data Dependence processing");
		}

		for (final Iterator<SootMethod> _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _currMethod = _i.next();
			final UnitGraph _unitGraph = getUnitGraph(_currMethod);

			if (_unitGraph != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing " + _currMethod.getSignature());
				}

				final IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> _useDef = getLocalUseDefAnalysis(_currMethod);
				dependee2dependent.put(_currMethod, _useDef);
				dependent2dependee.put(_currMethod, _useDef);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Method " + _currMethod.getSignature() + " does not have a unit graph.");
				}
			}
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
	 * @param programPoint is the program point at which a local occurs in the statement. If it is a statement, then
	 *            information about all the locals in the statement is provided. If it is a pair of statement and program
	 *            point in it, then only information about the local at that program point is provided.
	 * @param method in which <code>programPoint</code> occurs.
	 * @return a collection of statements on which <code>programPoint</code> depends.
	 * @pre programPoint.oclTypeOf(Pair).getFirst() != null and programPoint.oclTypeOf(Pair).getSecond() != null
	 */
	public final Collection<DefinitionStmt> getDependees(final Pair<Local, Stmt> programPoint, final SootMethod method) {
		Collection<DefinitionStmt> _result = Collections.emptyList();
		if (programPoint != null) {
			final IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> _useDefAnalysis = dependee2dependent.get(method);

			if (_useDefAnalysis != null) {
				final Stmt _stmt = programPoint.getSecond();
				final Local _local = programPoint.getFirst();
				_result = _useDefAnalysis.getDefs(_local, _stmt, method);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("getDependees(programPoint = " + programPoint + ", method = " + method
							+ ") - No dependents found for ");
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
		Collection<DefinitionStmt> _result = Collections.emptyList();
		final IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> _useDefAnalysis = dependee2dependent.get(method);

		if (_useDefAnalysis != null) {
			_result = _useDefAnalysis.getDefs(stmt, method);
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("getDependees(stmt = " + stmt + ", method = " + method + ") - No dependents found for ");
			}
		}

		return _result;
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
	public final Collection<Pair<Local, Stmt>> getDependents(final DefinitionStmt programPoint, final SootMethod method) {
		final IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> _useDefAnalysis = dependee2dependent.get(method);
		Collection<Pair<Local, Stmt>> _result = Collections.emptyList();

		if (_useDefAnalysis != null) {
			_result = _useDefAnalysis.getUses(programPoint, method);
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
	@Override public final String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for Identifier-based Data dependence as calculated by "
				+ this.getClass().getName() + "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator<Map.Entry<SootMethod, IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>>>> _i = dependee2dependent
				.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<SootMethod, IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>>> _entry = _i.next();
			_localEdgeCount = 0;

			final IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> _useDef = _entry.getValue();

			final SootMethod _sm = _entry.getKey();

			if (_sm.hasActiveBody()) {
				for (final Iterator<Stmt> _j = getStmtList(_sm).iterator(); _j.hasNext();) {
					final Stmt _stmt = _j.next();

					if (_stmt instanceof DefinitionStmt) {
						final Collection<?> _uses = _useDef.getUses((DefinitionStmt) _stmt, _sm);

						for (final Iterator<?> _k = _uses.iterator(); _k.hasNext();) {
							_temp.append("\t\t" + _stmt + " <-- " + _k.next() + "\n");
						}
						_localEdgeCount += _uses.size();
					}
				}
			}
			_result.append("\tFor " + _sm + " there are " + _localEdgeCount + " Identifier-based Data dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " Identifier-based Data dependence edges exist.");
		return _result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependenceRetriever()
	 */
	@Override protected IDependenceRetriever<Pair<Local, Stmt>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Pair<Local, Stmt>> getDependenceRetriever() {
		return new LocalStmtPairRetriever();
	}

	// /CLOVER:ON

	/**
	 * Retrieves the local use def analysis for the given method.
	 * 
	 * @param method of interest.
	 * @return local use-def analysis.
	 * @pre method != null
	 */
	protected IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> getLocalUseDefAnalysis(final SootMethod method) {
		return new LocalUseDefAnalysis(getUnitGraph(method));
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
}

// End of File
