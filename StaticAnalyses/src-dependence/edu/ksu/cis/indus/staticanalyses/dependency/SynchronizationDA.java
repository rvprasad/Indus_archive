
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

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.interfaces.IMonitorInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Stmt;


/**
 * This class provides synchronization dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.
 * 
 * <p>
 * <i>Synchronization dependence</i>: All non-monitor statement in a method are synchronization dependent on the immediately
 * enclosing monitor statements in the same method.
 * </p>
 * 
 * <p>
 * In case of synchronized methods, the statements in the method not enclosed by monitor statements are dependent on the
 * entry and exit into the method which is tied to the call-sites.  Hence, <code>getDependents()</code> and
 * <code>getDependees()</code> do not include this dependence as it is application specific and can be derived from the
 * control-flow.  If the return points and  entry point are assumed to comprise the monitor then there may be more than one
 * monitor pair as there are many return points, hence, not all statements in the method may be dependent on the same
 * monitor pair.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod, Map(ExitMonitorStmt, Collection(EnterMonitorStmt))))
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod, Map(EnterMonitortmt, Collection(ExitMonitorStmt))))
 */
public final class SynchronizationDA
  extends AbstractDependencyAnalysis {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(SynchronizationDA.class);

	/** 
	 * This provides monitor information.
	 */
	private IMonitorInfo monitorInfo;

	/**
	 * Returns the enter and exit monitor statements on which the given statement is dependent on in the given method.
	 *
	 * @param dependentStmt is a statement in the method.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of enter and exit monitor statements.
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.oclIsKindOf(Stmt)
	 * @post result->forall( o | o.oclIsKindOf(ExitMonitorStmt) or o.oclIsKindOf(EnterMonitorStmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
		final Map _temp = CollectionsUtilities.getMapFromMap(dependent2dependee, method);
		Collection _result = (Collection) _temp.get(dependentStmt);

		if (_result == null) {
			_result = new HashSet(monitorInfo.getEnclosingMonitorStmts((Stmt) dependentStmt, (SootMethod) method, false));

			final Stmt _stmt = (Stmt) dependentStmt;

			if (_stmt instanceof ExitMonitorStmt || _stmt instanceof EnterMonitorStmt) {
				final Collection _monitorTriples = monitorInfo.getMonitorTriplesFor(_stmt, ((SootMethod) method));

				for (final Iterator _i = _monitorTriples.iterator(); _i.hasNext();) {
					final Triple _triple = (Triple) _i.next();
					final Object _enter = _triple.getFirst();
					final Object _exit = _triple.getSecond();

					if (_enter.equals(_stmt) || _exit.equals(_stmt)) {
						_result.add(_enter);
						_result.add(_exit);
					}
				}
			}
			_temp.put(dependentStmt, _result);
		}
		return _result;
	}

	/**
	 * Returns the statements which depend on the given enter or exit monitor statement in the given method.
	 *
	 * @param dependeeStmt is the enter or exit monitor statement.
	 * @param method in which<code>dependeeStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependeeStmt.oclIsKindOf(ExitMonitorStmt) or dependeeStmt.oclIsKindOf(EnterMonitorStmt)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		final Map _temp = CollectionsUtilities.getMapFromMap(dependee2dependent, method);
		Collection _result = (Collection) _temp.get(dependeeStmt);

		if (_result == null) {
			final Collection _monitors = monitorInfo.getMonitorTriplesFor((Stmt) dependeeStmt, (SootMethod) method);
			_result = new HashSet();

			final Iterator _i = _monitors.iterator();
			final int _iEnd = _monitors.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple _monitor = (Triple) _i.next();
				final Object _enter = _monitor.getFirst();
				final Object _exit = _monitor.getSecond();

				if (dependeeStmt.equals(_enter) || dependeeStmt.equals(_exit)) {
					final Collection _enclosedStmts = monitorInfo.getEnclosedStmts(_monitor, false);
					_result.addAll(_enclosedStmts);
					_result.add(_enter);
					_result.add(_exit);
				}
			}
			_temp.put(dependeeStmt, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDirection()
	 */
	public Object getDirection() {
		return DIRECTIONLESS;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return IDependencyAnalysis.SYNCHRONIZATION_DA;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.STMT_DEP_RETRIEVER);
	}

	/**
	 * Calculates the synchronization dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (monitorInfo.isStable()) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: Synchronization Dependence processing");
			}

			stable();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("analyze() - " + toString());
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: Synchronization Dependence processing");
			}
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Monitor Info is unstable. So, passing up .");
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		return "The Statistics for Synchronization dependence is given by the monitor analysis used.\n"
		+ monitorInfo.toString();
	}

	///CLOVER:ON

	/**
	 * {@inheritDoc}
	 *
	 * @throws InitializationException when monitor analysis is not provided.
	 *
	 * @pre info.get(IMonitorInfo.ID) != null and info.get(IMonitorInfo.ID).oclIsTypeOf(IMonitorInfo)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		monitorInfo = (IMonitorInfo) info.get(IMonitorInfo.ID);

		if (monitorInfo == null) {
			throw new InitializationException(IMonitorInfo.ID + " was not provided in the info.");
		}
	}
}

// End of File
