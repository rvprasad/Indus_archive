
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

import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import soot.jimple.Stmt;

import java.util.Collection;


/**
 * This interface is used to maintain the mapping between statements in untransformed and transformed version of the system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ITransformer {
	/**
	 * Retrieves the statement list for the transformed version of given transformed method.
	 *
	 * @param method is the transformed method.
	 *
	 * @return the statement list for the transformed version of the given method.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	PatchingChain getSliceStmtListFor(final SootMethod method);

	/**
	 * Retrieves the transformed version of the given untransformed class.
	 *
	 * @param clazz is the untransformed class.
	 *
	 * @return the transformed class.
	 *
	 * @pre clazz != null
	 */
	SootClass getTransformed(SootClass clazz);

	/**
	 * Retrieves the transformed version of the given untransformed method.
	 *
	 * @param method is the untransformed method.
	 *
	 * @return the transformed method.
	 *
	 * @pre method != null
	 */
	SootMethod getTransformed(SootMethod method);

	/**
	 * Retrieves the transformed version of the given untransformed field.
	 *
	 * @param field is the untransformed field.
	 *
	 * @return the transformed field.
	 *
	 * @pre field != null
	 */
	SootField getTransformed(SootField field);

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
	Stmt getTransformed(Stmt untransformedStmt, SootMethod untransformedMethod);

	/**
	 * Retrieves the classes in the transformed system.
	 *
	 * @return a collection of transformed classes.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 */
	Collection getTransformedClasses();

	/**
	 * Retrieve the transformed version of the given local in the transformed version of the given method.
	 *
	 * @param name of the local.
	 * @param method in which the local occurs.
	 *
	 * @return the transformed local.
	 *
	 * @pre name != null and method != null
	 */
	Local getTransformedLocal(String name, SootMethod method);

	/**
	 * Retrieves the transformed version of the named class.
	 *
	 * @param name is the name of the requested class.
	 *
	 * @return the transformed class.
	 *
	 * @pre name != null
	 */
	SootClass getTransformedSootClass(String name);

	/**
	 * Retrieves the untransformed version of the given  class.
	 *
	 * @param clazz is the transformed version of the class.
	 *
	 * @return the untransformed class.
	 *
	 * @pre clazz != null
	 */
	SootClass getUntransformed(SootClass clazz);

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
	Stmt getUntransformed(Stmt transformedStmt, SootMethod transformedMethod);

	/**
	 * Called by the transformation engine after the transformation is completed.  The implementation can suitably do any
	 * postprocessing on the mappings here.
	 */
	void completeTransformation();

	/**
	 * Transform the given statement.  This method will suffice for simple transformation which do not require any context
	 * information except the method in which the statement occurs.  Both parameters refer to untransformed versions.
	 *
	 * @param stmt to be transformed.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 */
	void transform(Stmt stmt, SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/19 11:52:25  venku
   The following renaming have occurred ITransformMap to ITransformer, SliceMapImpl to SliceTransformer,
   and  Slicer to SliceEngine.
   Ripple effect of the above.
   
   Revision 1.3  2003/08/19 11:37:41  venku
   Major changes:
    - Changed ITransformMap extensively such that it now provides
      interface to perform the actual transformation.
    - Extended ITransformMap as AbstractTransformer to provide common
      functionalities.
    - Ripple effect of the above change in SlicerMapImpl.
    - Ripple effect of the above changes in Slicer.
    - The slicer now actually detects what needs to be included in the slice.
      Hence, it is more of an analysis/driver/engine that drives the transformation
      and SliceMapImpl is the engine that does or captures the transformation.
   The immediate following change will be to rename ITransformMap to ITransformer,
    SliceMapImpl to SliceTransformer, and Slicer to SliceEngine.

   Revision 1.2  2003/08/18 04:45:31  venku
   Moved the code such that code common to transformations are in one location
   and independent of any specific transformation.

   Revision 1.1  2003/08/18 04:01:52  venku
   Major changes:
    - Teased apart cloning logic in the slicer.  Made it transformation independent.
    - Moved it under transformation common location under indus.
 */
