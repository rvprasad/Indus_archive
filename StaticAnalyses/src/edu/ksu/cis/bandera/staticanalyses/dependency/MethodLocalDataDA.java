
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.dependency;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.SimpleLocalDefs;
import ca.mcgill.sable.soot.jimple.SimpleLocalUses;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtGraph;
import ca.mcgill.sable.soot.jimple.StmtValueBoxPair;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides data dependency information pertaining to method local data.  Method local data constitutes local
 * variables of primitive types and reference types in a method.  It does not include array elements even though the array is
 * local.
 * 
 * <p>
 * The dependent information is stored as follows: For each method, a list of length equal to the number of statements in the
 * methods is maintained. In case of dependent information, at each location corresponding to the statement a set of dependent
 * statements is maintained in the list.  In case of dependee information, at each location corresponding to the statement a
 * map is maintained in the list.  The map maps a value box in the statement to a collection of dependee statements.
 * </p>
 * 
 * <p>
 * The rational for the way the information is maintained is only one local can be defined in a statement.  Also, if the
 * definition of a local reaches a statement, then all occurrences of that local at that statement must be dependent on the
 * same reaching def.
 * </p>
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
public class MethodLocalDataDA
  extends DependencyAnalysis {
	/**
	 * Returns  the statements on which <code>o</code>, depends in the given <code>context</code>. The context is the method
	 * in which the o occurs.
	 *
	 * @param o is a <code>Pair</code> containing the <code>Stmt</code> and the <code>ValueBox</code>(in the given order) of
	 * 		  interest.
	 * @param context is the <code>SootMethod</code> in which <code>o</code> occurs.
	 *
	 * @return a collection of <code>Stmt</code>s on which <code>o</code> depends.
	 *
	 * @pre o.oclType = Pair
	 * @pre o.getFirst().ocltype = Stmt
	 * @pre o.getSecond().ocltype = ValueBox
	 * @pre context.oclType = SootMethod
	 * @post result->forall(o | o.oclType = Stmt)
	 */
	public Collection getDependees(Object o, Object context) {
		Pair pair = (Pair) o;
		Stmt stmt = (Stmt) pair.getFirst();
		ValueBox vBox = (ValueBox) pair.getSecond();
		SootMethod method = (SootMethod) context;
		List dependees = (List) dependeeMap.get(method);
		StmtGraph stmtgraph = (StmtGraph) method2stmtGraph.get(method);
		Map local2defs = (Map) dependees.get(stmtgraph.getBody().getStmtList().indexOf(stmt));
		return (Collection) local2defs.get(vBox);
	}

	/**
	 * Returns the statements which depend on <code>o</code> in the given <code>context</code>. The context is the method in
	 * which the o occurs.
	 *
	 * @param o is a <code>Pair</code> containing the <code>Stmt</code> and the <code>ValueBox</code>(in the given order) of
	 * 		  interest.
	 * @param context is the <code>SootMethod</code> in which <code>o</code> occurs.
	 *
	 * @return a collection of <code>Stmt</code>s which depend on <code>o</code>.
	 *
	 * @pre o.ocltype = Stmt
	 * @pre context.oclType = SootMethod
	 * @post result->forall(o | o.oclType = Stmt)
	 */
	public Collection getDependents(Object o, Object context) {
		Stmt stmt = (Stmt) o;
		SootMethod method = (SootMethod) context;
		List dependents = (List) dependentMap.get(method);
		StmtGraph stmtgraph = (StmtGraph) method2stmtGraph.get(method);
		return (Collection) dependents.get(stmtgraph.getBody().getStmtList().indexOf(stmt));
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 *
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		for(Iterator i = method2stmtGraph.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod currMethod = (SootMethod) entry.getKey();
			CompleteStmtGraph stmtGraph = (CompleteStmtGraph) entry.getValue();
			SimpleLocalDefs defs = new SimpleLocalDefs(stmtGraph);
			SimpleLocalUses uses = new SimpleLocalUses(stmtGraph, defs);
			List dependees = new ArrayList();
			List dependents = new ArrayList();

			for(ca.mcgill.sable.util.Iterator j = stmtGraph.getBody().getStmtList().iterator(); j.hasNext();) {
				Stmt currStmt = (Stmt) j.next();
				Collection currUses = Collections.EMPTY_LIST;

				if(currStmt instanceof DefinitionStmt) {
					Collection temp = Util.convert("java.util.ArrayList", uses.getUsesOf((DefinitionStmt) currStmt));

					if(temp.size() != 0) {
						currUses = new ArrayList();

						for(Iterator k = currUses.iterator(); k.hasNext();) {
							StmtValueBoxPair element = (StmtValueBoxPair) k.next();
							currUses.add(element.stmt);
						}
					}
				}
				dependents.add(currUses);

				Map currDefs = Collections.EMPTY_MAP;

				if(currStmt.getUseBoxes().size() > 0) {
					currDefs = new HashMap();

					for(ca.mcgill.sable.util.Iterator k = currStmt.getUseBoxes().iterator(); k.hasNext();) {
						ValueBox currValueBox = (ValueBox) k.next();
						Value value = currValueBox.getValue();

						if(value instanceof Local) {
							currDefs.put(currValueBox,
								Util.convert("java.util.ArrayList", defs.getDefsOfAt((Local) value, currStmt)));
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
}

/*****
 ChangeLog:

$Log$

*****/
