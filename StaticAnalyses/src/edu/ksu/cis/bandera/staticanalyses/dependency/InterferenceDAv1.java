
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

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.Stmt;

import edu.ksu.cis.bandera.staticanalyses.InitializationException;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.AbstractProcessor;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Pair.PairManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * This class does a very naive interference dependency calculation.  For fields, it assumes any assignment to a field can
 * affect  any reference to the same field.  This is imprecise in the light of thread local objects and unrelated primaries.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InterferenceDAv1
  extends DependencyAnalysis {
	/**
	 * This manages pairs.  This is used to implement <i>flyweight</i> pattern to conserve memory.
	 */
	PairManager pairMgr;

	/**
	 * This provide call graph information about the analyzed system.  This is required by the analysis.
	 */
	private CallGraphInfo callgraph;

	/**
	 * Creates a new InterferenceDAv1 object.
	 */
	public InterferenceDAv1() {
		preprocessor = new PreProcessor();
	}

	/**
	 * A preprocessor which captures all the array and field access locations in the system.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Called by the controller when it encounters a <code>AssignStmt</code>.  This records the array and field access
		 * expression as they occur in the system.
		 *
		 * @param stmt in which the access expression occurs.
		 * @param context in which <code>stmt</code> occurs.
		 *
		 * @pre stmt.isOclKindOf(AssignStmt)
		 *
		 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#callback(ca.mcgill.sable.soot.jimple.Stmt,
		 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
		 */
		public void callback(Stmt stmt, Context context) {
			SootMethod method = context.getCurrentMethod();
			AssignStmt as = (AssignStmt) stmt;

			if(as.getLeftOp() instanceof FieldRef) {
				SootField sf = ((FieldRef) as.getLeftOp()).getField();
				Map temp = getDependeXXMapHelper(dependentMap, sf);
				temp.put(pairMgr.getPair(as, method), Collections.EMPTY_SET);
			} else if(as.getLeftOp() instanceof ArrayRef) {
				ArrayType at = (ArrayType) ((ArrayRef) as.getLeftOp()).getBase().getType();
				Map temp = getDependeXXMapHelper(dependentMap, at);
				temp.put(pairMgr.getPair(as, method), Collections.EMPTY_SET);
			} else if(as.getRightOp() instanceof FieldRef) {
				SootField sf = ((FieldRef) as.getRightOp()).getField();
				Map temp = getDependeXXMapHelper(dependeeMap, sf);
				temp.put(pairMgr.getPair(as, method), Collections.EMPTY_SET);
			} else if(as.getRightOp() instanceof ArrayRef) {
				ArrayType at = (ArrayType) ((ArrayRef) as.getRightOp()).getBase().getType();
				Map temp = getDependeXXMapHelper(dependeeMap, at);
				temp.put(pairMgr.getPair(as, method), Collections.EMPTY_SET);
			}
		}

		/**
		 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#hookup(edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
		 */
		public void hookup(ProcessingController ppc) {
			ppc.register(AssignStmt.class, this);
		}

		/**
		 * Helper method for getDependeXXMap() methods.
		 *
		 * @param map from which to extract the result map.
		 * @param o is the field/array reference of interest.
		 *
		 * @return the map corresponding to the <code>o</code>.
		 */
		private Map getDependeXXMapHelper(Map map, Object o) {
			Map result = (Map) map.get(o);

			if(result == null) {
				result = new HashMap();
				map.put(o, result);
			}
			return result;
		}
	}

	/**
	 * Returns the statements on which the given field/array reference at the given statement and method depends on.
	 *
	 * @param dependent of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependent.oclType = SootField  or dependent.oclType = ArrayRef
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(Object dependent, Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map pair2set = getDependeeMapFor(dependent);

		if(pair2set != null) {
			Collection set = (Set) pair2set.get(stmtMethodPair);

			if(set != null) {
				result = Collections.unmodifiableCollection(set);
			}
		}
		return result;
	}

	/**
	 * Returns the statements on which the given field/array reference at the given statement and method depends on.
	 *
	 * @param dependee of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependee.oclType = SootField or dependee.oclType = ArrayRef
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(Object dependee, Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map method2map = getDependentMapFor(dependee);

		if(method2map != null) {
			Map stmt2set = (Map) method2map.get(((Pair) stmtMethodPair).getSecond());

			if(stmt2set != null) {
				Collection set = (Collection) stmt2set.get(((Pair) stmtMethodPair).getFirst());

				if(set != null) {
					result = Collections.unmodifiableCollection(set);
				}
			}
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		for(Iterator i = dependeeMap.keySet().iterator(); i.hasNext();) {
			Object o = i.next();

			if(dependentMap.get(o) == null) {
				continue;
			}

			Collection temp = new HashSet();
			Map dtMap = (Map) dependentMap.get(o);
			temp.addAll(dtMap.keySet());

			for(Iterator j = ((Map) dependeeMap.get(o)).entrySet().iterator(); j.hasNext();) {
				Map.Entry entry = (Map.Entry) j.next();
				entry.setValue(temp);
			}
			temp = new HashSet();

			Map deMap = (Map) dependeeMap.get(o);
			temp.addAll(deMap.keySet());

			for(Iterator k = ((Map) dependentMap.get(o)).entrySet().iterator(); k.hasNext();) {
				Map.Entry entry = (Map.Entry) k.next();
				entry.setValue(temp);
			}
		}
		return true;
	}

	/**
	 * Resets in the internal data structures.
	 */
	public void reset() {
		super.reset();
	}

	/**
	 * Returns the map for containing dependee information pertaining to the given field or array reference.
	 *
	 * @param o of interest.
	 *
	 * @return the map for containing dependee information pertaining to <code>o</code>.
	 */
	protected Map getDependeeMapFor(Object o) {
		Map result = (Map) dependeeMap.get(o);

		if(result == null) {
			result = Collections.EMPTY_MAP;
		}
		return result;
	}

	/**
	 * Returns the map for containing dependent information pertaining to the given field or array reference.
	 *
	 * @param o of interest.
	 *
	 * @return the map for containing dependent information pertaining to <code>o</code>.
	 */
	protected Map getDependentMapFor(Object o) {
		Map result = (Map) dependentMap.get(o);

		if(result == null) {
			result = Collections.EMPTY_MAP;
		}
		return result;
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize initialize}.
	 *
	 * @throws InitializationException when instances of call graph or pair managing service are not provided.
	 */
	protected void setup() {
		callgraph = (CallGraphInfo) info.get(CallGraphInfo.ID);

		if(callgraph == null) {
			throw new InitializationException(CallGraphInfo.ID + " was not provided in info.");
		}
		pairMgr = (PairManager) info.get(PairManager.NAME);

		if(pairMgr == null) {
			throw new InitializationException(PairManager.NAME + " was not provided in info.");
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
