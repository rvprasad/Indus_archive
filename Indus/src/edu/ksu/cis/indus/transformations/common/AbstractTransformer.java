
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

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;

/**
 * This is an abstract implementation of <code>ITransformer</code> interface that provides basic functionality
 * required by transformers. 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractTransformer
  implements ITransformer {
	/**
	 * The system resulting from the transformation.
	 */
	protected Scene transformedSystem;

	/**
	 * The system being transformed.
	 */
	protected Scene untransformedSystem;

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
		SootMethod sliceMethod = getTransformed(method);
		Body body = sliceMethod.getActiveBody();
		PatchingChain result = body.getUnits();
		return result;
	}
}

/*
   ChangeLog:
   $Log$
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
