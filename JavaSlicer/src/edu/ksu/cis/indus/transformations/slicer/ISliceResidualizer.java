
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

import java.util.Collection;


/**
 * This interface should be implemented by slice residualizer or slicing-based transformers.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISliceResidualizer
  extends ITransformer {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param theSliceType DOCUMENT ME!
	 * @param executableSlice DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean handleSliceType(Object theSliceType, boolean executableSlice);

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

	/**
	 * Transforms the slice into an executable slice.
	 */
	void makeExecutable();

	/**
	 * Deals with seed criteria based on the nature of the implementation.
	 *
	 * @param seedcriteria is the collection of seed criteria.
	 *
	 * @pre seedCriteria != null and seedCriteria.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	void processSeedCriteria(Collection seedcriteria);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/11/16 22:55:31  venku
   - added new methods to support processing of seed criteria.
     This is not same as slicing seed criteria of which we do not
     make any distinction.

   Revision 1.3  2003/11/13 14:08:08  venku
   - added a new tag class for the purpose of recording branching information.
   - renamed fixReturnStmts() to makeExecutable() and raised it
     into ISliceResidualizer interface.
   - ripple effect.
   Revision 1.2  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.1  2003/10/13 00:58:04  venku
 *** empty log message ***
           Revision 1.2  2003/09/27 22:38:30  venku
           - package documentation.
           - formatting.
           Revision 1.1  2003/09/15 07:52:08  venku
           - added a new transformer interface specifically targetted for slicing.
           - implemented the above interface.
 */
