
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

import soot.SootField;


/**
 * This class manages field variants.  This class only provides the implementation to create new field variants.  The super
 * class is responsible of managing the variants.
 * 
 * <p>
 * Created: Fri Jan 25 14:33:09 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldVariantManager
  extends AbstractVariantManager {
	/**
	 * Creates a new <code>FieldVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices which are used to map fields to their variants.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	public FieldVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new variant of the field represented by <code>o</code>.
	 *
	 * @param o the field whose variant is to be returned.
	 *
	 * @return the variant associated with the field represetned by <code>o</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(SootField)
	 */
	protected IVariant getNewVariant(final Object o) {
		return new FieldVariant((SootField) o, fa.getNewFGNode());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/16 03:01:49  venku
   Spruced up documentation and specification.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.7  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
