
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

package edu.ksu.cis.indus.slicer;

import soot.PatchingChain;
import soot.SootMethod;

import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This maintains the map of statements occurring in the unsliced and sliced version of methods.  Things get twisted here.
 * When we say "unsliced" version of a method we refer to the method before slicing.  Hence,  when we say "sliced" version
 * of the method we refer to the method after slicing.  Just trying to keep it with english like in "This is the slice of
 * the mango sliced earlier."
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
	 * @invariant method2stmtMap != null
	 */
	private Map method2stmtMap = new HashMap();

	/**
	 * This maps statements in sliced version of methods to their counterparts in the unsliced version of the methods.
	 *
	 * @invariant slicedMethod2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant slicedMethod2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 * @invariant slicedMethod2stmtMap != null
	 */
	private Map slicedMethod2stmtMap = new HashMap();

	/**
	 * The slicer from which the map is extracted.
	 *
	 * @invariant slicer != null
	 */
	private Slicer slicer;

	/**
	 * Creates a new instance of this class.
	 *
	 * @param associatedSlicer in which this map exists.
	 *
	 * @pre slicer != null
	 */
	protected SliceMap(final Slicer associatedSlicer) {
		this.slicer = associatedSlicer;
	}

	/**
	 * Provides the sliced statement corresponding to the given statement in the unsliced version of the method.
	 *
	 * @param stmt in the unsliced version of <code>method</code>.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return the sliced counterpart of the given statement. If the statement was sliced away, it returns <code>null</code>.
	 *
	 * @pre stmt != null and method != null
	 */
	public Stmt getSliceStmt(final Stmt stmt, final SootMethod method) {
		Stmt result = null;
		Map stmtMap = (Map) method2stmtMap.get(method);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(stmt);
		}
		return result;
	}

	/**
	 * Provides the unsliced statement corresponding to the given statement in the sliced of the method.
	 *
	 * @param sliceStmt in the sliced version of <code>method</code>.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return the unsliced counterpart of the given statement.
	 *
	 * @pre sliceStmt != null and method != null
	 */
	public Stmt getStmt(final Stmt sliceStmt, final SootMethod method) {
		Stmt result = null;
		Map stmtMap = (Map) slicedMethod2stmtMap.get(method);

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
		for (Iterator i = slicedMethod2stmtMap.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			SootMethod sliceMethod = slicer.getCloneOf(method);

			if (sliceMethod == null) {
				method2stmtMap.put(method, null);
			} else {
				Map sliced = (Map) method2stmtMap.get(method);
				Map slice = (Map) slicedMethod2stmtMap.get(sliceMethod);
				PatchingChain slicedSl = ((JimpleBody) method.getActiveBody()).getUnits();
				PatchingChain sliceSl = ((JimpleBody) sliceMethod.getActiveBody()).getUnits();

				for (Iterator j = slicedSl.iterator(); j.hasNext();) {
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
	 * @post getStmt(sliceStmt, slicer.getCloneOf(
	 */
	protected void put(final Stmt stmt, final Stmt sliceStmt, final SootMethod unslicedMethod) {
		Map stmtMap = (Map) method2stmtMap.get(unslicedMethod);

		if (stmtMap != null) {
			stmtMap.put(stmt, sliceStmt);
		}
		stmtMap = (Map) slicedMethod2stmtMap.get(slicer.getCloneOf(unslicedMethod));

		if (stmtMap != null) {
			stmtMap.put(sliceStmt, stmt);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/05/22 22:23:50  venku
   Changed interface names to start with a "I".
   Formatting.
   
 */
