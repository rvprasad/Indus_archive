
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

import edu.ksu.cis.indus.common.datastructures.Pair;

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
 * This class provides data dependency information based on identifiers.  Local variables in a method enable such dependence.
 * Given a def site, the use site is tracked based on the id being defined and used. Hence, information about field/array
 * access via primaries which are local variables is inaccurate in such a setting, hence, it is not  provided by this class.
 * Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod,Sequence(Set(Stmt)))
 * @invariant dependee2dependent.values()->forall(o | o.getValue().size =
 * 			  o.getKey().getBody(Jimple.v()).getStmtList().size())
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod, Sequence(Map(Local, Set(Stmt)))))
 * @invariant dependent2dependee.entrySet()->forall(o | o.getValue().size() =
 * 			  o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class IdentifierBasedDataDA
  extends AbstractDependencyAnalysis {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(IdentifierBasedDataDA.class);

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
	public Collection getDependees(final Object programPoint, final Object method) {
		Collection _result = Collections.EMPTY_LIST;

		if (programPoint instanceof Stmt) {
			_result = new HashSet();

			final Stmt _stmt = (Stmt) programPoint;
			final SootMethod _m = (SootMethod) method;
			final List _dependees = (List) dependent2dependee.get(method);

			if (_dependees != null) {
				final Map _local2defs = (Map) _dependees.get(getStmtList(_m).indexOf(_stmt));

				for (final Iterator _i = _stmt.getUseBoxes().iterator(); _i.hasNext();) {
					final Value _o = ((ValueBox) _i.next()).getValue();
					final Collection _c = (Collection) _local2defs.get(_o);

					if (_c != null) {
						_result.addAll(_c);
					}
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No dependence information available for " + _stmt + " in " + method);
				}
			}
		} else if (programPoint instanceof Pair) {
			final Pair _pair = (Pair) programPoint;
			final Stmt _stmt = (Stmt) _pair.getFirst();
			final Local _local = (Local) _pair.getSecond();
			final SootMethod _m = (SootMethod) method;
			final List _dependees = (List) dependent2dependee.get(method);
			final Map _local2defs = (Map) _dependees.get(getStmtList(_m).indexOf(_stmt));
			final Collection _c = (Collection) _local2defs.get(_local);

			if (_c != null) {
				_result = Collections.unmodifiableCollection(_c);
			}
		}
		return _result;
	}

	/**
	 * Returns the statement and the program point in it which depends on <code>stmt</code> in the given
	 * <code>context</code>. The context is the method in which the o occurs.
	 *
	 * @param stmt is a definition statement.
	 * @param method is the method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statement and program points in them which depend on the definition in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		final SootMethod _sm = (SootMethod) method;
		final List _dependents = (List) dependee2dependent.get(_sm);
		Collection _result = Collections.EMPTY_LIST;

		if (_dependents != null) {
			final Collection _temp = (Collection) _dependents.get(getStmtList(_sm).indexOf(stmt));

			if (_temp != null) {
				_result = Collections.unmodifiableCollection(_temp);
			}
		}
		return _result;
	}

	/*
	 * The dependent information is stored as follows: For each method, a list of length equal to the number of statements in
	 * the methods is maintained. In case of dependent information, at each location corresponding to the statement a set of
	 * dependent statements is maintained in the list.  In case of dependee information, at each location corresponding to the
	 * statement a map is maintained in the list.  The map maps a value box in the statement to a collection of dependee
	 * statements.
	 *
	 * The rational for the way the information is maintained is only one local can be defined in a statement.  Also, if the
	 * definition of a local reaches a statement, then all occurrences of that local at that statement must be dependent on
	 * the same reaching def.
	 */

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return AbstractDependencyAnalysis.IDENTIFIER_BASED_DATA_DA;
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Identifier Based Data Dependence processing");
		}

		for (final Iterator _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _currMethod = (SootMethod) _i.next();
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

			final SimpleLocalDefs _defs = new SimpleLocalDefs(_unitGraph);
			final SimpleLocalUses _uses = new SimpleLocalUses(_unitGraph, _defs);
			final Collection _t = getStmtList(_currMethod);
			final List _dependees = new ArrayList(_t.size());
			final List _dependents = new ArrayList(_t.size());

			for (final Iterator _j = _t.iterator(); _j.hasNext();) {
				final Stmt _currStmt = (Stmt) _j.next();
				Collection _currUses = Collections.EMPTY_LIST;

				if (_currStmt instanceof DefinitionStmt) {
					final Collection _temp = _uses.getUsesOf(_currStmt);

					if (_temp.size() != 0) {
						_currUses = new ArrayList();

						for (final Iterator _k = _temp.iterator(); _k.hasNext();) {
							final UnitValueBoxPair _p = (UnitValueBoxPair) _k.next();
							_currUses.add(_p.getUnit());
						}
					}
				}
				_dependents.add(_currUses);

				Map _currDefs = Collections.EMPTY_MAP;

				if (_currStmt.getUseBoxes().size() > 0) {
					_currDefs = new HashMap();

					for (final Iterator _k = _currStmt.getUseBoxes().iterator(); _k.hasNext();) {
						final ValueBox _currValueBox = (ValueBox) _k.next();
						final Value _value = _currValueBox.getValue();

						if (_value instanceof Local && !_currDefs.containsKey(_value)) {
							_currDefs.put(_value, _defs.getDefsOfAt((Local) _value, _currStmt));
						}
					}
				}
				_dependees.add(_currDefs);
			}
			dependee2dependent.put(_currMethod, _dependents);
			dependent2dependee.put(_currMethod, _dependees);
		}
		stable = true;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END:  Identifier Based Data Dependence processing");
		}
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Identifier-based Data dependence as calculated by " + this.getClass().getName()
				+ "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependee2dependent.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final List _stmts = getStmtList((SootMethod) _entry.getKey());
			int _count = 0;

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Collection _c = (Collection) _j.next();
				final Stmt _stmt = (Stmt) _stmts.get(_count++);

				for (final Iterator _k = _c.iterator(); _k.hasNext();) {
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.35  2004/05/14 06:27:23  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.34  2004/03/04 13:08:15  venku
   - coding convention.
   Revision 1.33  2004/03/03 10:11:40  venku
   - formatting.
   Revision 1.32  2004/03/03 10:07:24  venku
   - renamed dependeeMap as dependent2dependee
   - renamed dependentmap as dependee2dependent
   Revision 1.31  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.30  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.29  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.28  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.27  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.26  2003/12/01 11:37:58  venku
   - logging.
   Revision 1.25  2003/11/28 22:11:35  venku
   - logging.
   Revision 1.24  2003/11/28 21:45:44  venku
   - logging and error condition handling.
   Revision 1.23  2003/11/12 05:00:36  venku
   - documentation.
   Revision 1.22  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.21  2003/11/10 02:12:52  venku
   - coding convention.
   Revision 1.20  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.19  2003/11/05 00:36:16  venku
   - changed the way dependence information was stored.
   Revision 1.18  2003/11/05 00:23:04  venku
   - documentation.
   Revision 1.17  2003/11/03 07:54:01  venku
   - extended the input type handled by getDependees().
   - Uses stores LocalUnitPair instances.
   Revision 1.16  2003/11/02 22:10:30  venku
   - uses unitgraphs instead of complete unit graphs.
   Revision 1.15  2003/11/02 00:37:59  venku
   - logging changes.
   Revision 1.14  2003/11/02 00:34:01  venku
   - formatting.
   Revision 1.13  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
   Revision 1.12  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.11  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.10  2003/09/15 07:31:00  venku
   - documentation.
   Revision 1.9  2003/09/13 05:56:08  venku
   - bumped up log levels to error.
   Revision 1.8  2003/09/13 05:42:07  venku
   - What if the unit graphs for all methods are unavailable?  Hence,
     added a method to AbstractAnalysis to retrieve the methods to
     process.  The subclasses work only on this methods.
   Revision 1.7  2003/09/12 22:33:08  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.6  2003/09/02 12:21:03  venku
   - Tested and it works.  A small bug was fixed.
   Revision 1.5  2003/08/25 09:30:41  venku
   Renamed AliasedDataDA to ReferenceBasedDataDA.
   Renamed NonAliasedDataDA to IdentifierBasedDataDA.
   Renamed the IDs for the above analyses.
   Revision 1.4  2003/08/18 11:07:16  venku
   Tightened specification.
   Revision 1.3  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.1  2003/08/09 23:29:09  venku
   Renamed InterProceduralDataDAv1 to AliasedDataDA
   Renamed IntraProceduralDataDA to NonAliasedDataDA
 */
