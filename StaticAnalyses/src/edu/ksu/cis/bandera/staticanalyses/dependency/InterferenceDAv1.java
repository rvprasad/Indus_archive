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

import edu.ksu.cis.bandera.staticanalyses.InitializationException;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.AbstractProcessor;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Pair.PairManager;

import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.jimple.AssignStmt;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author venku
 */
public class InterferenceDAv1 extends DependencyAnalysis {
	/**
	 * This provide call graph information about the analyzed system.  This is required by the analysis.
	 */
	private CallGraphInfo callgraph;

	/**
	 * This manages pairs.  This is used to implement <i>flyweight</i> pattern to conserve memory.
	 */
	private PairManager pairMgr;
	
	public InterferenceDAv1() {
		preprocessor = new PreProcessor();
	}

	class PreProcessor extends AbstractProcessor {
		
		/**
		 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#hookup(edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
		 */
		public void hookup(ProcessingController ppc) {
			ppc.register(AssignStmt.class, this);
		}
	}
	/**
	 * Returns the map for containing dependee information pertaining to the given field.
	 *
	 * @param sf of interest.
	 *
	 * @return the map for containing dependee information pertaining to <code>sf</code>.
	 */
	protected Map getDependeeMapForField(SootField sf) {
		return getDependeXXMapHelper(dependeeMap, sf);
	}

	/**
	 * Returns the map for containing dependent information pertaining to the given field.
	 *
	 * @param sf of interest.
	 *
	 * @return the map for containing dependent information pertaining to <code>sf</code>.
	 */
	protected Map getDependentMapForField(SootField sf) {
		return getDependeXXMapHelper(dependentMap, sf);
	}

	/**
	 * Helper method for getDependeXXMap() methods.
	 *
	 * @param map from which to extract the result map.
	 * @param sf is the field of interest.
	 *
	 * @return the map corresponding to the <code>sf</code>.
	 */
	private Map getDependeXXMapHelper(Map map, SootField sf) {
		Map result = (Map) map.get(sf);

		if (result == null) {
			result = new HashMap();
			map.put(sf, result);
		}
		return result;
	}

	/**
	 * Returns the statements on which the given field at the given statement and method depends on.
	 *
	 * @param dependentField of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependentField.oclType = SootField
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(Object dependentField, Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map pair2set = getDependeeMapForField((SootField) dependentField);

		if (pair2set != null) {
			Collection set = (Set) pair2set.get(stmtMethodPair);

			if (set != null) {
				result = Collections.unmodifiableCollection(set);
			}
		}
		return result;
	}
	/**
	 * Returns the statements on which the given field at the given statement and method depends on.
	 *
	 * @param dependeeField of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependeeField.oclType = SootField
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(Object dependeeField, Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map method2map = getDependentMapForField((SootField) dependeeField);

		if (method2map != null) {
			Map stmt2set = (Map) method2map.get(((Pair) stmtMethodPair).getSecond());

			if (stmt2set != null) {
				Collection set = (Collection) stmt2set.get(((Pair) stmtMethodPair).getFirst());

				if (set != null) {
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
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize initialize}.
	 *
	 * @throws InitializationException when instances of call graph or pair managing service are not provided.
	 */
	protected void setup() {
		callgraph = (CallGraphInfo) info.get(CallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(CallGraphInfo.ID + " was not provided in info.");
		}
		pairMgr = (PairManager) info.get(PairManager.NAME);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.NAME + " was not provided in info.");
		}
	}

	/**
	 * Resets in the internal data structures.
	 */
	public void reset() {
		super.reset();
	}
}

/*****
ChangeLog:

$Log$
Revision 1.2  2003/02/20 18:01:15  venku
Committing before changing name to InterferenceDAv1.

Revision 1.1  2003/02/19 17:33:24  venku
This will provide naive type based interference dependency info.


*****/
