
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.dependency;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

import soot.toolkits.graph.CompleteUnitGraph;

import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.ValueUnitPair;

import edu.ksu.cis.indus.staticanalyses.support.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides data dependency information independent of aliasing.  Local variables in a method enable such
 * dependence. Hence, information about field/array access via primaries which are local variables is not provided by this
 * class. Please refer to {@link AliasedDataDA AliasedDataDA} for such information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod,Sequence(Set(Stmt))))
 * @invariant dependentMap.values()->forall(o | o.getValue().size = o.getKey().getBody(Jimple.v()).getStmtList().size())
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Map(ValueBox, Set(ca.mcgill. sable.soot.jimple.Stmt)))))
 * @invariant dependeeMap.entrySet()->forall(o | o.getValue().size() = o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class NonAliasedDataDA
  extends DependencyAnalysis {
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
	 * Returns  the statements on which <code>o</code>, depends in the given <code>method</code>.
	 *
	 * @param stmtValueBoxPair is a pair of statement and program point at which a local occurs in the statement.
	 * @param method in which <code>stmtValueBoxPair</code> occurs.
	 *
	 * @return a collection of statements on which <code>stmtValueBoxPair</code> depends.
	 *
	 * @pre stmtValueBoxPair.oclIsKindOf(Pair(Stmt, ValueBox))
	 * @pre stmtValueBoxPair.oclTypeOf(Pair).getFirst() != null
	 * @pre stmtValueBoxPair.oclTypeOf(Pair).getSecond() != null
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(DefinitionStmt))
	 */
	public Collection getDependees(final Object stmtValueBoxPair, final Object method) {
		Pair pair = (Pair) stmtValueBoxPair;
		Stmt stmt = (Stmt) pair.getFirst();
		ValueBox vBox = (ValueBox) pair.getSecond();
		SootMethod m = (SootMethod) method;
		List dependees = (List) dependeeMap.get(method);
		Map local2defs = (Map) dependees.get(getStmtList(m).indexOf(stmt));
		return (Collection) local2defs.get(vBox);
	}

	/**
	 * Returns the statements which depend on <code>stmt</code> in the given <code>context</code>. The context is the method
	 * in which the o occurs.
	 *
	 * @param stmt is a definition statement.
	 * @param context is the method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which depend on the definition in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(Stmt)
	 * @pre context.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 */
	public Collection getDependents(final Object stmt, final Object context) {
		SootMethod method = (SootMethod) context;
		List dependents = (List) dependentMap.get(method);
		return (Collection) dependents.get(getStmtList(method).indexOf(stmt));
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 *
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		for (Iterator i = method2stmtGraph.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod currMethod = (SootMethod) entry.getKey();
			CompleteUnitGraph stmtGraph = (CompleteUnitGraph) entry.getValue();
			SimpleLocalDefs defs = new SimpleLocalDefs(stmtGraph);
			SimpleLocalUses uses = new SimpleLocalUses(stmtGraph, defs);
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

						for (Iterator k = currUses.iterator(); k.hasNext();) {
							ValueUnitPair element = (ValueUnitPair) k.next();
							currUses.add(element.getUnit());
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

						if (value instanceof Local) {
							currDefs.put(currValueBox, defs.getDefsOfAt((Local) value, currStmt));
						}
					}
				}
				dependees.add(currDefs);
			}
			dependentMap.put(currMethod, dependents);
			dependeeMap.put(currMethod, dependees);
		}
		return true;
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for Non-Aliased Data dependence as calculated by " + this.getClass().getName()
				+ "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependentMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			List stmts = getStmtList((SootMethod) entry.getKey());

			for (Iterator j = ((List) entry.getValue()).iterator(); j.hasNext();) {
				Collection c = (Collection) j.next();
				int count = 0;

				for (Iterator k = c.iterator(); k.hasNext();) {
					temp.append("\t\t" + stmts.get(count++) + " --> " + k.next() + "\n");
				}
				localEdgeCount += c.size();
			}
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount
				+ " Non-Aliased Data dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Non-Aliased Data dependence edges exist.");
		return result.toString();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file

   Revision 1.2  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.1  2003/08/09 23:29:09  venku
   Renamed InterProceduralDataDAv1 to AliasedDataDA
   Renamed IntraProceduralDataDA to NonAliasedDataDA
 */
