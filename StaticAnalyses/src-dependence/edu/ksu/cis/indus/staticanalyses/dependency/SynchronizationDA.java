
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
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

/*
   ChangeLog:
   $Log$
   Revision 1.54  2004/07/27 07:08:25  venku
   - revamped IMonitorInfo interface.
   - ripple effect in MonitorAnalysis, SafeLockAnalysis, and SychronizationDA.
   - deleted WaitNotifyAnalysis
   - ripple effect in EquivalenceClassBasedEscapeAnalysis.
   Revision 1.53  2004/07/23 13:55:19  venku
   - Merging into main trunk.
   Revision 1.52.2.2  2004/07/23 13:23:16  venku
   - coding conventions.
   Revision 1.52.2.1  2004/07/23 13:09:44  venku
   - Refactoring in progress.
     - Extended IMonitorInfo interface.
     - Teased apart the logic to calculate monitor info from SynchronizationDA
       into MonitorAnalysis.
     - Casted EquivalenceClassBasedEscapeAnalysis as an AbstractAnalysis.
     - ripple effect.
     - Implemented safelock analysis to handle intraprocedural processing.
   Revision 1.52  2004/07/22 07:19:29  venku
   - coding conventions.
   Revision 1.51  2004/07/21 02:07:35  venku
   - spruced up IMonitorInfo interface with method to extract more information.
   - updated SynchronizationDA to provide the methods introduced in IMonitorInfo.
   Revision 1.50  2004/07/11 09:42:13  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.49  2004/07/09 09:43:23  venku
   - added clover tags to control coverage of toSting()
   Revision 1.48  2004/07/07 06:29:20  venku
   - coding convention and documentation.
   Revision 1.47  2004/07/07 06:25:07  venku
   - the way statement sub list was constructed in the basic block was incorrect.  FIXED.
   - ripple effect.
   Revision 1.46  2004/07/04 11:52:41  venku
   - renamed getStmtFrom() to getStmtsFrom().
   Revision 1.45  2004/06/27 04:57:58  venku
   - a subtlety about valid enter/exit monitor patterns in the code was addressed. FIXED.
   Revision 1.44  2004/06/27 03:58:20  venku
   - bug #395. FIXED.
   Revision 1.43  2004/06/16 14:30:12  venku
   - logging.
   Revision 1.42  2004/06/03 20:24:12  venku
   - documentation.
   Revision 1.41  2004/06/03 20:23:23  venku
   - MAJOR CHANGE - Reworked the impl as it was missing some dependencies.
   Revision 1.40  2004/06/01 06:29:57  venku
   - added new methods to CollectionUtilities.
   - ripple effect.
   Revision 1.39  2004/05/31 21:38:08  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.38  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.37  2004/05/14 06:27:24  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.36  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.35  2004/03/04 11:52:21  venku
   - modified ReadyDA to use CollectionsModifiers.
   - fixed some subtle bugs in SyncDA.
   Revision 1.34  2004/03/03 10:11:40  venku
   - formatting.
   Revision 1.33  2004/03/03 10:07:24  venku
   - renamed dependeeMap as dependent2dependee
   - renamed dependentmap as dependee2dependent
   Revision 1.32  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.31  2004/01/21 13:56:26  venku
   - tracking sync DA in synchronized methods is unnecessary.
   Revision 1.30  2004/01/21 13:52:12  venku
   - documentation.
   Revision 1.29  2004/01/19 08:57:29  venku
   - documentation and formatting.
   Revision 1.28  2004/01/19 08:26:59  venku
   - enabled logging of criteria when they are created in SlicerTool.
   Revision 1.27  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.26  2003/12/30 09:17:47  venku
   - method level synchronization Triple is explicitly optimized.
   Revision 1.25  2003/12/15 06:54:03  venku
   - formatting.
   Revision 1.24  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.23  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.22  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.21  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.20  2003/11/17 01:40:40  venku
   - documentation.
   Revision 1.19  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.18  2003/11/10 03:17:18  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.17  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.16  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.15  2003/11/05 09:29:05  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.14  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.13  2003/11/03 07:54:56  venku
   - added logging.
   Revision 1.12  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.11  2003/09/12 22:33:09  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.10  2003/09/10 11:50:23  venku
   - formatting.
   Revision 1.9  2003/09/10 11:49:31  venku
   - documentation change.
   Revision 1.8  2003/09/08 02:25:04  venku
   - Ripple effect of changes to ValueAnalyzerBasedProcessingController.
   Revision 1.7  2003/09/07 09:02:13  venku
   - Synchronization dependence now handles exception based
     sync dep edges.  This requires a Value Flow analysis which can
     provides value binding information for a local at a program point.
   - Ripple effect of the above change.
   Revision 1.6  2003/08/21 03:56:18  venku
   Ripple effect of adding IStatus.
   Revision 1.5  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
