
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
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class contains the logic to calculate control dependences in the reverse direction of control flow considering the
 * exit points as the entry points.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExitControlDA
  extends AbstractDependencyAnalysis {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExitControlDA.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ICallGraphInfo callgraph;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IDependencyAnalysis entryControlDA;

	/**
	 * Returns the statements on which <code>dependentStmt</code> depends on in the given <code>method</code>.
	 *
	 * @param dependentStmt is the dependent of interest.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre dependentStmt.oclIsKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public final Collection getDependees(final Object dependentStmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final List _list = (List) dependent2dependee.get(method);

		if (_list != null) {
			final int _index = getStmtList((SootMethod) method).indexOf(dependentStmt);

			if (_list.get(_index) != null) {
				_result = Collections.unmodifiableCollection((Collection) _list.get(_index));
			}
		}
		return _result;
	}

	/**
	 * Returns the statements which depend on <code>dependeeStmt</code> in the given <code>method</code>.
	 *
	 * @param dependeeStmt is the dependee of interest.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre dependeeStmt.isOclKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public final Collection getDependents(final Object dependeeStmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final List _list = (List) dependee2dependent.get(method);

		if (_list != null) {
			final int _index = getStmtList((SootMethod) method).indexOf(dependeeStmt);

			if (_list.get(_index) != null) {
				_result = Collections.unmodifiableCollection((Collection) _list.get(_index));
			}
		}
		return _result;
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 *
	 * @post result != null
	 */
	public final String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for exit control dependence as calculated by " + getClass().getName() + "\n");
		int _localEdgeCount;
		int _localEntryPointDep;
		int _edgeCount = 0;
		int _entryPointDep = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependent2dependee.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _method = (SootMethod) _entry.getKey();
			final List _stmts = getStmtList(_method);
			final List _cd = (List) _entry.getValue();
			_localEdgeCount = 0;
			_localEntryPointDep = _stmts.size();

			for (int _j = 0; _j < _stmts.size(); _j++) {
				if (_cd == null) {
					continue;
				}

				final Collection _dees = (Collection) _cd.get(_j);

				if (_dees != null) {
					_temp.append("\t\t" + _stmts.get(_j) + " --> " + _dees + "\n");
					_localEdgeCount += _dees.size();
					_localEntryPointDep--;
				}
			}

			_result.append("\tFor " + _entry.getKey() + " there are " + _localEdgeCount + " control dependence edges with "
				+ _localEntryPointDep + " entry point dependences.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
			_entryPointDep += _localEntryPointDep;
		}
		_result.append("A total of " + _edgeCount + " control dependence edges exists with " + _entryPointDep
			+ " entry point dependences.");
		return _result.toString();
	}

	///CLOVER:ON
	
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDirection()
	 */
	public Object getDirection() {
		return FORWARD_DIRECTIONAL;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return IDependencyAnalysis.CONTROL_DA;
	}

	/**
	 * Calculates the control dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public final void analyze() {
		analyze(callgraph.getReachableMethods());
	}

	/**
	 * Calculates the control dependency information for the provided methods.  The use of this method does not require a
	 * prior call to <code>setup</code>.
	 *
	 * @param methods to be analyzed.
	 *
	 * @pre methods != null and methods.oclIsKindOf(Collection(SootMethod)) and not method->includes(null)
	 */
	public final void analyze(final Collection methods) {
		unstable();

		if (!entryControlDA.isStable()) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: Exit Control Dependence processing due to unstable entry control dependence info.");
			}
			return;
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Exit Control Dependence processing");
		}

		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = getBasicBlockGraph(_sm);
			final Collection _dependeeBBs = calculateDependeesOfSinksIn(_bbg, _sm);
			calculateDependenceForStmts(calculateDependenceForBBs(_bbg, _dependeeBBs), _sm);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Exit Control Dependence processing");
		}

		stable();
	}

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 * @pre info.get(IDependencyAnalysis.CONTROL_DA) != null
	 * @pre info.get(IDependencyAnalysis.ID).oclIsTypeOf(IDependencyAnalysis)
	 * @pre info.get(IDependencyAnalysis.ID).getDirection().equals(IDependencyAnalysis.FORWARD_DIRECTIONAL)
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

		final IDependencyAnalysis _temp = (IDependencyAnalysis) info.get(IDependencyAnalysis.CONTROL_DA);

		if (_temp == null) {
			throw new InitializationException(IDependencyAnalysis.CONTROL_DA + " was not provided.");
		} else if (!_temp.getDirection().equals(IDependencyAnalysis.BACKWARD_DIRECTIONAL)) {
			throw new InitializationException("Provided control dependence  is not backward directional.");
		}
		entryControlDA = _temp;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param bbg DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Collection calculateDependeesOfSinksIn(final BasicBlockGraph bbg, final SootMethod method) {
		final Collection _result = new HashSet();
		final Collection _sinks = new ArrayList();
		_sinks.addAll(bbg.getTails());
		_sinks.addAll(bbg.getPseudoTails());

		for (final Iterator _i = _sinks.iterator(); _i.hasNext();) {
			final BasicBlock _sink = (BasicBlock) _i.next();
			final Stmt _stmt = _sink.getLeaderStmt();
			final Collection _dependees = entryControlDA.getDependees(_stmt, method);

			for (final Iterator _j = _dependees.iterator(); _j.hasNext();) {
				final Stmt _dependee = (Stmt) _j.next();
				final BasicBlock _dependeeBB = bbg.getEnclosingBlock(_dependee);
				_result.add(_dependeeBB);
			}
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param bbg DOCUMENT ME!
	 * @param dependeeBBs DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Map calculateDependenceForBBs(final BasicBlockGraph bbg, final Collection dependeeBBs) {
		final Map _dependence = new HashMap();
		final Iterator _i = dependeeBBs.iterator();
		final int _iEnd = dependeeBBs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _dependeeBB = (BasicBlock) _i.next();
			final Collection _dependents = bbg.getReachablesFrom(_dependeeBB, false);
			_dependence.put(_dependeeBB, _dependents);
		}
		return _dependence;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param dependeeBB2dependentBBs DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	private void calculateDependenceForStmts(final Map dependeeBB2dependentBBs, final SootMethod method) {
		final List _methodLocalDee2Dent = CollectionsUtilities.getListFromMap(dependee2dependent, method);
		final List _methodLocalDent2Dee = CollectionsUtilities.getListFromMap(dependent2dependee, method);
		final List _stmtList = getStmtList(method);
		final int _noOfStmtsInMethod = _stmtList.size();
		CollectionsUtilities.ensureSize(_methodLocalDee2Dent, _noOfStmtsInMethod, null);
		CollectionsUtilities.ensureSize(_methodLocalDent2Dee, _noOfStmtsInMethod, null);

		for (final Iterator _i = dependeeBB2dependentBBs.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final BasicBlock _dependeeBB = (BasicBlock) _entry.getKey();
			final Stmt _dependee = _dependeeBB.getTrailerStmt();
			final Collection _dependents =
				(Collection) CollectionsUtilities.getSetAtIndexFromList(_methodLocalDee2Dent, _stmtList.indexOf(_dependee));
			final Collection _dependentBBs = (Collection) _entry.getValue();
			final Iterator _j = _dependentBBs.iterator();
			final int _jEnd = _dependentBBs.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final BasicBlock _dependentBB = (BasicBlock) _j.next();
				final List _stmtsOf = _dependentBB.getStmtsOf();
				final Iterator _k = _stmtsOf.iterator();
				final int _kEnd = _stmtsOf.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final Stmt _dependent = (Stmt) _k.next();
					final Collection _dependees =
						(Collection) CollectionsUtilities.getSetAtIndexFromList(_methodLocalDent2Dee,
							_stmtList.indexOf(_dependent));
					_dependees.add(_dependee);
				}
				_dependents.addAll(_stmtsOf);
			}
		}
		dependent2dependee.put(method, _methodLocalDent2Dee);
		dependee2dependent.put(method, _methodLocalDee2Dent);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2004/07/11 09:42:13  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.11  2004/06/12 06:59:57  venku
   - dependency analysis test was failing due to subtyping incorrectness.  FIXED.
   Revision 1.10  2004/06/05 09:52:24  venku
   - INTERIM COMMIT
     - Reimplemented EntryControlDA.  It provides indirect control dependence info.
     - DirectEntryControlDA provides direct control dependence info.
     - ExitControlDA will follow same suite as EntryControlDA with new implementation
       and new class for direct dependence.
   Revision 1.9  2004/03/03 10:11:40  venku
   - formatting.
   Revision 1.8  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.7  2004/01/30 23:55:18  venku
   - added a new analyze method to analyze only the given
     collection of methods.
   Revision 1.6  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.5  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.4  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.3  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/11/25 19:12:59  venku
   - documentation.
   Revision 1.1  2003/11/25 17:51:23  venku
   - split control dependence into 2 classes.
     EntryControlDA handled control DA as required for backward slicing.
     ExitControlDA handles control DA as required for forward slicing.
   - ripple effect.
 */
