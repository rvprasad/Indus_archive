
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

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;


/**
 * This is an abstract implementation of <code>ITransformer</code> interface that provides basic functionality required by
 * transformers.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractTransformer
  implements ITransformer {
	/**
	 * Retrieves the statement list for the transformed version of given untransformed method.
	 *
	 * @param method is the untransformed method.
	 *
	 * @return the statement list for the transformed version of the given method.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	public PatchingChain getSliceStmtListFor(final SootMethod method) {
		final SootMethod _transformedMethod = getTransformed(method);
		final Body _body = _transformedMethod.getActiveBody();
		final PatchingChain _result = _body.getUnits();
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/09/27 23:21:42  venku
 *** empty log message ***
     Revision 1.5  2003/09/26 15:06:05  venku
     - Formatting.
     - ITransformer has a new method initialize() via which the system
       being transformed can be specified.
     Revision 1.4  2003/08/19 12:44:39  venku
     Changed the signature of ITransformer.getLocal()
     Introduced reset() in ITransformer.
     Ripple effect of the above changes.
     Revision 1.3  2003/08/19 11:58:53  venku
     Remove any reference to slicing from the documentation.
     Revision 1.2  2003/08/19 11:52:25  venku
     The following renaming have occurred ITransformMap to ITransformer, SliceMapImpl to SliceTransformer,
     and  Slicer to SliceEngine.
     Ripple effect of the above.
     Revision 1.1  2003/08/19 11:37:41  venku
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
 */
