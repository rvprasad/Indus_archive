
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

package edu.ksu.cis.indus.transformations.common;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This interface should be used to retrieve the mapping between statements in untransformed and transformed version of the
 * system. 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ITransformMap {
	/**
	 * Provides the transformed statement corresponding to the given statement in the untransformed version of the method.
	 *
	 * @param untransformedStmt in which <code>stmt</code> occurs.
	 * @param untransformedMethod in the untransformed version of <code>method</code>.
	 *
	 * @return the transformed counterpart of the given statement. If the statement was transformed away, it returns
	 * 		   <code>null</code>.
	 *
	 * @pre untransformedStmt != null and untransformedMethod != null
	 */
	Stmt getTransformedStmt(final Stmt untransformedStmt, final SootMethod untransformedMethod);

	/**
	 * Provides the untransformed statement corresponding to the given statement in the transformed of the method.
	 *
	 * @param transformedStmt in the transformed version of <code>method</code>.
	 * @param transformedMethod in which <code>stmt</code> occurs.
	 *
	 * @return the untransformed counterpart of the given statement.
	 *
	 * @pre transformedStmt != null and transformedMethod != null
	 */
	Stmt getUntransformedStmt(final Stmt transformedStmt, final SootMethod transformedMethod);

	/**
	 * Registers the mapping between statements in the transformed and untransformed versions of a method.
	 *
	 * @param transformedStmt in the transformed version of the method.
	 * @param untransformedStmt in the untransformed version of the method.
	 * @param untransformedMethod in which <code>stmt</code> occurs.
	 *
	 * @pre untransformedStmt != null and transformedStmt != null and untransformedMethod != null
	 */
	void addMapping(final Stmt transformedStmt, final Stmt untransformedStmt, final SootMethod untransformedMethod);

	/**
	 * Correct any mappings that may have been invalidated.
	 */
	void fixupMappings();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/18 04:01:52  venku
   Major changes:
    - Teased apart cloning logic in the slicer.  Made it transformation independent.
    - Moved it under transformation common location under indus.

 */
