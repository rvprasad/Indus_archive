
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

/**
 * This class manages variants corresponding to entities that have values.
 * 
 * <p>
 * Created: Fri Jan 25 13:50:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ValuedVariantManager
  extends AbstractVariantManager {
	/**
	 * Creates a new <code>ValuedVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 * @param indexManager the manager of indices which map array variants to arrays.  This cannot be <code>null</code>.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	ValuedVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager) {
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
		return new ValuedVariant(fa.getNewFGNode());
	}
}

// End of File
