
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.ArrayType;


/**
 * This class manages variants corresponding to arrays.
 * 
 * <p>
 * Created: Fri Jan 25 13:50:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ArrayVariantManager
  extends AbstractVariantManager {
	/**
	 * Creates a new <code>ArrayVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 * @param indexManager the manager of indices which map array variants to arrays.  This cannot be <code>null</code>.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	ArrayVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new array variant corresponding to the given array type.
	 *
	 * @param o the <code>ArrayType</code> whose variant is to be returned.
	 *
	 * @return a new <code>ArrayVariant</code> corresponding to <code>o</code>.
	 */
	protected IVariant getNewVariant(final Object o) {
		return new ArrayVariant((ArrayType) o, fa.getNewFGNode());
	}
}

/*
   ChangeLog:

   $Log$
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 0.7  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
