
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.SootMethod;


/**
 * This class contains implementation that can be used by various control dependence implementation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractControlDA
  extends AbstractDependencyAnalysis {
	/** 
	 * This provides the call graph information.
	 */
	protected ICallGraphInfo callgraph;

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
		final List _list = (List) dependent2dependee.get(method);

		return getDependenceHelper(dependentStmt, method, _list);
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
		final List _list = (List) dependee2dependent.get(method);
		return getDependenceHelper(dependeeStmt, method, _list);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public final Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.CONTROL_DA);
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
			new StringBuffer("Statistics for control dependence as calculated by " + getClass().getName() + "\n");
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

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 * @pre info.get(IDependencyAnalysis.CONTROL_DA) != null
	 * @pre info.get(IDependencyAnalysis.ID).oclIsTypeOf(IDependencyAnalysis)
	 * @pre info.get(IDependencyAnalysis.ID).getDirection().equals(IDependencyAnalysis.FORWARD_DIRECTION)
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
	 * A helper method to extract dependence.
	 *
	 * @param stmt for which dependence is requested.
	 * @param method in which <code>stmt</code> occurs.
	 * @param list from which to extract dependence.
	 *
	 * @return the collection of statement on which <code>stmt</code> is control dependent on.
	 *
	 * @pre stmt != null and method != null and list.oclIsKindOf(Collection(Stmt))
	 * @post result != null
	 */
	private Collection getDependenceHelper(final Object stmt, final Object method, final List list) {
		Collection _result = Collections.EMPTY_LIST;

		if (list != null) {
			final int _index = getStmtList((SootMethod) method).indexOf(stmt);

			if (_index > -1) {
				_result =
					(Collection) CollectionsUtilities.getAtIndexFromList(list, _index, CollectionsUtilities.EMPTY_LIST_FACTORY);
			}
		}
		return _result;
	}

	///CLOVER:ON
}

// End of File
