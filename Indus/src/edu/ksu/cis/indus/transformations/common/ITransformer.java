
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

package edu.ksu.cis.indus.transformations.common;

import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

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
	 * @param local of interest.
	 * @param method in which the local occurs.
	 *
	 * @return the transformed local.
	 *
	 * @pre name != null and method != null
	 */
	Local getTransformedLocal(Local local, SootMethod method);

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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param scene DOCUMENT ME!
	 */
	void initialize(Scene scene);

	/**
	 * Reset any internal state.
	 */
	void reset();

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

	/**
	 * Transform the given program point.  This method will suffice for simple transformation which do not require any
	 * context information except the method in which the statement occurs.  All parameters refer to untransformed versions.
	 *
	 * @param vBox is the program point to be transformed.
	 * @param stmt in which <code>vBox</code> occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 */
	void transform(ValueBox vBox, Stmt stmt, SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/08/21 09:30:31  venku
    - added a new transform() method which can transform at the level of ValueBox.
    - CloningBasedSlicingTransformer does not do anything in this new method.
   Revision 1.6  2003/08/19 12:44:39  venku
   Changed the signature of ITransformer.getLocal()
   Introduced reset() in ITransformer.
   Ripple effect of the above changes.
   Revision 1.5  2003/08/19 11:58:53  venku
   Remove any reference to slicing from the documentation.
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
