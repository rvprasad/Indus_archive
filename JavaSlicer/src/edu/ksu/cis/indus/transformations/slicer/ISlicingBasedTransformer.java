
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

package edu.ksu.cis.indus.transformations.slicer;

import edu.ksu.cis.indus.transformations.common.ITransformer;


/**
 * This interface should be implemented by slicing-based transformers.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISlicingBasedTransformer
  extends ITransformer {
	/**
	 * Checks if the transformer can handle partial inclusions.  Note that one may want to slice based on an expression in a
	 * statement.  If the transformation generates marking the existing program, then only the expression can be marked.
	 * However, in case the transformation generates a new program then depending on the target language semantics, only
	 * that expression can be  included or the entire statement would need to be included.  We address this situation as
	 * partial inclusion.  The transformer should be able declare it's capabilities via this method.
	 *
	 * @return <code>true</code> if the transformer can handle partial inclusions; <code>false</code>, otherwise.
	 */
	boolean handlesPartialInclusions();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/09/15 07:52:08  venku
   - added a new transformer interface specifically targetted for slicing.
   - implemented the above interface.
 */
