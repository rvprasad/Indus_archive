
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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides intraprocedural data dependency information based on identifiers.  Local variables in a method enable
 * such dependence. Given a def site, the use site is tracked based on the id being defined and used. Hence, information
 * about field/array access via primaries which are local variables is inaccurate in such a setting, hence, it is not
 * provided by this class. Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
 * 
 * <p>
 * This implementation is based on <code>edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysis</code> class.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod,IUseDefInfo))
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod,IUseDefInfo))
 */
public class IdentifierBasedDataDAv2
  extends AbstractDependencyAnalysis {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(IdentifierBasedDataDAv2.class);

	/** 
	 * This provides call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * Returns  the statements on which <code>o</code>, depends in the given <code>method</code>.
	 *
	 * @param programPoint is the program point at which a local occurs in the statement.  If it is a statement, then
	 * 		  information about all the locals in the statement is provided.  If it is a pair of statement and program point
	 * 		  in it, then only  information about the local at that program point is provided.
	 * @param method in which <code>programPoint</code> occurs.
	 *
	 * @return a collection of statements on which <code>programPoint</code> depends.
	 *
	 * @pre programPoint.oclIsKindOf(Pair(Stmt, Local)) implies programPoint.oclTypeOf(Pair).getFirst() != null and
	 * 		programPoint.oclTypeOf(Pair).getSecond() != null
	 * @pre programPoint.oclIsKindOf(Stmt) or programPoint.oclIsKindOf(Pair(Stmt, Local))
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(DefinitionStmt))
	 */
	public final Collection getDependees(final Object programPoint, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final IUseDefInfo _useDefAnalysis = (IUseDefInfo) dependee2dependent.get(method);

		if (_useDefAnalysis != null) {
			if (programPoint instanceof Stmt) {
				_result = collectDefsForAllLocalsIn((Stmt) programPoint, _useDefAnalysis, (SootMethod) method);
			} else if (programPoint instanceof Pair) {
				final Pair _pair = (Pair) programPoint;
				final Stmt _stmt = (Stmt) _pair.getFirst();
				final Local _local = (Local) _pair.getSecond();
				_result = _useDefAnalysis.getDefs(_local, _stmt, (SootMethod) method);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("getDependees(programPoint = " + programPoint + ", method = " + method
						+ ") - No dependents found for ");
				}
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No dependence information available for " + programPoint + " in " + method);
			}
		}

		return _result;
	}

	/**
	 * Returns the statement and the program point in it which depends on statement provided via <code>programPoint</code>
	 * occurring  in the given <code>method</code>.
	 *
	 * @param programPoint is the definition statement or a pair containing the definition statement as the first element.
	 * 		  Although, only one variable can be defined in a Jimple statement, we allow for a pair in this query to make
	 * 		  <code>getDependees</code> and <code>getDependents</code> symmetrical for ease of usage.
	 * @param method is the method in which the statement provided by <code>programPoint</code> occurs.
	 *
	 * @return a collection of statement and program points in them which depend on the definition at
	 * 		   <code>programPoint</code>.
	 *
	 * @pre programPoint.isOclKindOf(Stmt) or programPoint.isOclIsKindOf(Pair(Stmt, Object))
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 */
	public final Collection getDependents(final Object programPoint, final Object method) {
		final IUseDefInfo _useDefAnalysis = (IUseDefInfo) dependee2dependent.get(method);
		Collection _result = Collections.EMPTY_LIST;

		if (_useDefAnalysis != null) {
			final Stmt _stmt;

			if (programPoint instanceof Stmt) {
				_stmt = (Stmt) programPoint;
			} else if (programPoint instanceof Pair) {
				_stmt = (Stmt) ((Pair) programPoint).getFirst();
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("getDependents(entity = " + programPoint + ", method = " + method
						+ ") - No dependents found for ");
				}
				_stmt = null;
			}

			if (_stmt != null && _stmt instanceof DefinitionStmt) {
				_result = _useDefAnalysis.getUses((DefinitionStmt) _stmt, (SootMethod) method);
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  This implementation is bi-directional.
	 */
	public Object getDirection() {
		return BI_DIRECTIONAL;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.STMT_DEP_RETRIEVER);
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public final void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Identifier Based Data Dependence processing");
		}

		for (final Iterator _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _currMethod = (SootMethod) _i.next();
			final UnitGraph _unitGraph = getUnitGraph(_currMethod);

			if (_unitGraph != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing " + _currMethod.getSignature());
				}

				final IUseDefInfo _useDef = getLocalUseDefAnalysis(_currMethod);
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

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public final String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Identifier-based Data dependence as calculated by " + this.getClass().getName()
				+ "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependee2dependent.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final IUseDefInfo _useDef = (IUseDefInfo) _entry.getValue();

			final SootMethod _sm = (SootMethod) _entry.getKey();

			if (_sm.hasActiveBody()) {
				for (final Iterator _j = _sm.getActiveBody().getUnits().iterator(); _j.hasNext();) {
					final Stmt _stmt = (Stmt) _j.next();

					if (_stmt instanceof DefinitionStmt) {
						final Collection _uses = _useDef.getUses((DefinitionStmt) _stmt, _sm);

						for (final Iterator _k = _uses.iterator(); _k.hasNext();) {
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

	///CLOVER:ON

	/**
	 * Retrieves the local use def analysis for the given method.
	 *
	 * @param method of interest.
	 *
	 * @return local use-def analysis.
	 *
	 * @pre method != null
	 */
	protected IUseDefInfo getLocalUseDefAnalysis(final SootMethod method) {
		return new LocalUseDefAnalysis(getUnitGraph(method));
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
	protected void setup()
	  throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Collects the def info available from <code>useDefAnalysis</code> for each local used in <code>stmt</code>.
	 *
	 * @param stmt in which the locals occur for which the def info is requested.
	 * @param useDefAnalysis to be used to retrieve the def into for each variable.
	 * @param method of interest.
	 *
	 * @return a collection of definition statement.
	 *
	 * @pre stmt != null and useDefAnalysis != null
	 * @post result != null and result.oclIsKindOf(Collection(DefinitionStmt))
	 */
	private Collection collectDefsForAllLocalsIn(final Stmt stmt, final IUseDefInfo useDefAnalysis, final SootMethod method) {
		final Collection _result = new HashSet();

		for (final Iterator _i = stmt.getUseBoxes().iterator(); _i.hasNext();) {
			final Value _o = ((ValueBox) _i.next()).getValue();

			if (_o instanceof Local) {
				final Collection _c = useDefAnalysis.getDefs((Local) _o, stmt, method);

				if (!_c.isEmpty()) {
					_result.addAll(_c);
				}
			}
		}
		return _result;
	}
}

// End of File
