
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

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.support.Pair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod,Sequence(Set(Stmt)))
 * @invariant dependentMap.values()->forall(o | o.getValue().size = o.getKey().getBody(Jimple.v()).getStmtList().size())
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Map(Local, Set(Stmt)))))
 * @invariant dependeeMap.entrySet()->forall(o | o.getValue().size() = o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class IdentifierBasedDataDA
  extends DependencyAnalysis {
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
		Collection result = Collections.EMPTY_LIST;

		if (programPoint instanceof Stmt) {
			result = new HashSet();

			Stmt stmt = (Stmt) programPoint;
			SootMethod m = (SootMethod) method;
			List dependees = (List) dependeeMap.get(method);

			if (dependees != null) {
				Map local2defs = (Map) dependees.get(getStmtList(m).indexOf(stmt));

				for (Iterator i = stmt.getUseBoxes().iterator(); i.hasNext();) {
					Value o = ((ValueBox) i.next()).getValue();

					if (o instanceof Local) {
						Collection c = (Collection) local2defs.get(o);

						if (c != null) {
							result.addAll(c);
						}
					}
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No dependence information available for " + stmt + " in " + method);
				}
			}
		} else if (programPoint instanceof Pair) {
			Pair pair = (Pair) programPoint;
			Stmt stmt = (Stmt) pair.getFirst();
			Local local = (Local) pair.getSecond();
			SootMethod m = (SootMethod) method;
			List dependees = (List) dependeeMap.get(method);
			Map local2defs = (Map) dependees.get(getStmtList(m).indexOf(stmt));
			Collection c = (Collection) local2defs.get(local);

			if (c != null) {
				result = Collections.unmodifiableCollection(c);
			}
		}
		return result;
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
		SootMethod sm = (SootMethod) method;
		List dependents = (List) dependentMap.get(sm);
		return Collections.unmodifiableCollection((Collection) dependents.get(getStmtList(sm).indexOf(stmt)));
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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getId()
	 */
	public Object getId() {
		return DependencyAnalysis.IDENTIFIER_BASED_DATA_DA;
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Identifier Based Data Dependence processing");
		}

		for (Iterator i = callgraph.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod currMethod = (SootMethod) i.next();
			UnitGraph unitGraph = getUnitGraph(currMethod);

			if (unitGraph == null) {
				LOGGER.error("Method " + currMethod.getSignature() + " does not have a unit graph.");
				continue;
			}

			SimpleLocalDefs defs = new SimpleLocalDefs(unitGraph);
			SimpleLocalUses uses = new SimpleLocalUses(unitGraph, defs);
			Collection t = getStmtList(currMethod);
			List dependees = new ArrayList(t.size());
			List dependents = new ArrayList(t.size());

			for (Iterator j = t.iterator(); j.hasNext();) {
				Stmt currStmt = (Stmt) j.next();
				Collection currUses = Collections.EMPTY_LIST;

				if (currStmt instanceof DefinitionStmt) {
					Collection temp = uses.getUsesOf(currStmt);

					if (temp.size() != 0) {
						currUses = new ArrayList();

						for (Iterator k = temp.iterator(); k.hasNext();) {
							UnitValueBoxPair p = (UnitValueBoxPair) k.next();
							currUses.add(p.getUnit());
						}
					}
				}
				dependents.add(currUses);

				Map currDefs = Collections.EMPTY_MAP;

				if (currStmt.getUseBoxes().size() > 0) {
					currDefs = new HashMap();

					for (Iterator k = currStmt.getUseBoxes().iterator(); k.hasNext();) {
						ValueBox currValueBox = (ValueBox) k.next();
						Value value = currValueBox.getValue();

						if (value instanceof Local && !currDefs.containsKey(value)) {
							currDefs.put(value, defs.getDefsOfAt((Local) value, currStmt));
						}
					}
				}
				dependees.add(currDefs);
			}
			dependentMap.put(currMethod, dependents);
			dependeeMap.put(currMethod, dependees);
		}
		stable = true;

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
		StringBuffer result =
			new StringBuffer("Statistics for Identifier-based Data dependence as calculated by " + this.getClass().getName()
				+ "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependentMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			List stmts = getStmtList((SootMethod) entry.getKey());
			int count = 0;

			for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				Collection c = (Collection) j.next();
				Stmt stmt = (Stmt) stmts.get(count++);

				for (Iterator k = c.iterator(); k.hasNext();) {
					temp.append("\t\t" + stmt + " <-- " + k.next() + "\n");
				}
				localEdgeCount += c.size();
			}
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount
				+ " Identifier-based Data dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Identifier-based Data dependence edges exist.");
		return result.toString();
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
