
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

package edu.ksu.cis.bandera.slicer;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This maintains the map of statements occurring in the unsliced and sliced version of methods.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SliceMap {
	/**
	 * This maps statements in unsliced methods to their counterparts in the sliced version of the methods.
	 *
	 * @invariant method2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant method2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 */
	private Map method2stmtMap = new HashMap();

	/**
	 * This maps statements in slice methods to their counterparts in the sliced version of the methods.
	 *
	 * @invariant sliceMethod2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant sliceMethod2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 */
	private Map sliceMethod2stmtMap = new HashMap();

	/**
	 * <p>
	 * The slicer from which the map is extracted.
	 * </p>
	 */
	private Slicer slicer;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param slicer in which this map exists.
	 */
	protected SliceMap(Slicer slicer) {
		this.slicer = slicer;
	}

	/**
	 * Provides the statement corresponding to the given statement in the sliced version of the method.
	 *
	 * @param stmt in the unsliced version of <code>method</code>.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return the sliced counterpart of the given statement. If the statement was sliced away, it returns <code>null</code>.
	 */
	public Stmt getSliceStmt(Stmt stmt, SootMethod method) {
		Stmt result = null;
		Map stmtMap = (Map) method2stmtMap.get(method);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(stmt);
		}
		return result;
	}

	/**
	 * Provides the statement corresponding to the given statement in the sliced version of the method.
	 *
	 * @param sliceStmt in the sliced version of <code>method</code>.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return the unsliced counterpart of the given statement.
	 *
	 * @post result != null
	 */
	public Stmt getStmt(Stmt sliceStmt, SootMethod method) {
		Stmt result = null;
		Map stmtMap = (Map) sliceMethod2stmtMap.get(method);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(sliceStmt);
		}
		return result;
	}

	/**
	 * Correct invalid mappings.  Mappings may be invalidated when transformations external to slicer are applied to  the
	 * slice.  This methods detects such mappings and corrects them.
	 */
	protected void cleanup() {
		for (Iterator i = sliceMethod2stmtMap.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			SootMethod sliceMethod = slicer.getCloneOf(method);

			if (sliceMethod == null) {
				method2stmtMap.put(method, null);
			} else {
				Map sliced = (Map) method2stmtMap.get(method);
				Map slice = (Map) sliceMethod2stmtMap.get(sliceMethod);
				StmtList slicedSl = ((JimpleBody) method.getBody(Jimple.v())).getStmtList();
				StmtList sliceSl = ((JimpleBody) sliceMethod.getBody(Jimple.v())).getStmtList();

				for (ca.mcgill.sable.util.Iterator j = slicedSl.iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();

					if (!sliceSl.contains(sliced.get(stmt))) {
						slice.remove(sliced.get(stmt));
						sliced.remove(stmt);
					}
				}
			}
		}
	}

	/**
	 * Registers the mapping between statements in the sliced and unsliced versions of a method.
	 *
	 * @param stmt in the unsliced version of the method.
	 * @param sliceStmt in the sliced version of the method.
	 * @param unslicedMethod in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and sliceStmt != null
	 */
	protected void put(Stmt stmt, Stmt sliceStmt, SootMethod unslicedMethod) {
		Map stmtMap = (Map) method2stmtMap.get(unslicedMethod);

		if (stmtMap != null) {
			stmtMap.put(stmt, sliceStmt);
		}
		stmtMap = (Map) sliceMethod2stmtMap.get(slicer.getCloneOf(unslicedMethod));

		if (stmtMap != null) {
			stmtMap.put(sliceStmt, stmt);
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
