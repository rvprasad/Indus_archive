
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

package edu.ksu.cis.indus.transformations.slicer;

import soot.PatchingChain;
import soot.SootMethod;

import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.transformations.common.Cloner;
import edu.ksu.cis.indus.transformations.common.ITransformMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This maintains the map of statements occurring in the untransformed and transformed version of methods.  Things get
 * twisted here. When we say "untransformed" version of a method we refer to the method before slicing.  Hence,  when we say
 * "transformed" version of the method we refer to the method after slicing.  Just trying to keep it with english like in
 * "This is the slice of the mango transformed earlier."
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SliceMapImpl
  implements ITransformMap {
	/**
	 * The cloner from which the map is extracted.
	 */
	private Cloner cloner;

	/**
	 * This maps statements in untransformed methods to their counterparts in the transformed version of the methods.
	 *
	 * @invariant method2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant method2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 * @invariant method2stmtMap != null
	 */
	private Map method2stmtMap = new HashMap();

	/**
	 * This maps statements in transformed version of methods to their counterparts in the untransformed version of the
	 * methods.
	 *
	 * @invariant transformedMethod2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant transformedMethod2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 * @invariant transformedMethod2stmtMap != null
	 */
	private Map transformedMethod2stmtMap = new HashMap();

	/**
	 * @see edu.ksu.cis.indus.slicer.ITransformMap#getSlicedStmt(Stmt, SootMethod)
	 */
	public Stmt getTransformedStmt(final Stmt untransformedStmt, final SootMethod untransformedMethod) {
		Stmt result = null;
		Map stmtMap = (Map) method2stmtMap.get(untransformedMethod);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(untransformedStmt);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.ITransformMap#getUntransformedStmt(Stmt, SootMethod)
	 */
	public Stmt getUntransformedStmt(final Stmt transformedStmt, final SootMethod transformedMethod) {
		Stmt result = null;
		Map stmtMap = (Map) transformedMethod2stmtMap.get(transformedMethod);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(transformedStmt);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @post getUntransformedStmt(transformedStmt, slicer.getCloneOf(untransformedMethod)) == untransformedStmt
	 * @post getSlicedStmt(untransformedStmt, untransformedMethod) == transformedStmt
	 */
	public void addMapping(final Stmt transformedStmt, final Stmt untransformedStmt, final SootMethod untransformedMethod) {
		Map stmtMap = (Map) method2stmtMap.get(untransformedMethod);

		if (stmtMap != null) {
			stmtMap.put(untransformedStmt, transformedStmt);
		}
		stmtMap = (Map) transformedMethod2stmtMap.get(cloner.getCloneOf(untransformedMethod));

		if (stmtMap != null) {
			stmtMap.put(transformedStmt, untransformedStmt);
		}
	}

	/**
	 * Correct invalid mappings.  Mappings may be invalidated when transformations external to slicer are applied to  the
	 * slice.  This methods detects such mappings and corrects them.
	 */
	public void fixupMappings() {
		for (Iterator i = method2stmtMap.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			SootMethod transformedMethod = cloner.getCloneOf(method);

			if (transformedMethod == null) {
				transformedMethod2stmtMap.put(transformedMethod, null);
			} else {
				Map untransformedStmt2transformedStmt = (Map) method2stmtMap.get(method);
				Map transformedStmt2untransformedStmt = (Map) transformedMethod2stmtMap.get(transformedMethod);
				PatchingChain untransformedSL = ((JimpleBody) method.getActiveBody()).getUnits();
				PatchingChain transformedSL = ((JimpleBody) transformedMethod.getActiveBody()).getUnits();

				for (Iterator j = untransformedSL.iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();

					if (!transformedSL.contains(untransformedStmt2transformedStmt.get(stmt))) {
						transformedStmt2untransformedStmt.remove(untransformedStmt2transformedStmt.get(stmt));
						untransformedStmt2transformedStmt.remove(stmt);
					}
				}
			}
		}
	}

	/**
	 * Initializes the map with a cloner which manages the clone to clonee mappings.
	 *
	 * @param theCloner manages the clone to clonee mappings.
	 *
	 * @pre theCloner != null
	 */
	protected void initialize(final Cloner theCloner) {
		cloner = theCloner;
	}
}

/*
   ChangeLog:
   
   $Log$
   Revision 1.9  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.

   Revision 1.8  2003/08/18 04:49:47  venku
   Modified SlicerMap to be an specific implementation of ITransformMap specific to the Slicer.

   
   Revision 1.7  2003/08/18 02:40:23  venku
   It is better to elevate the mapping interface to a Type and implement in SliceMap.
   This is the last commit in that direction.  After this I will move SliceMap to SliceMapImpl.
   
   Revision 1.6  2003/08/17 11:56:18  venku
   Renamed SliceCriterion to AbstractSliceCriterion.
   Formatting, documentation, and specification.
   
   Revision 1.5  2003/05/22 22:23:50  venku
   Changed interface names to start with a "I".
   Formatting.
 */
