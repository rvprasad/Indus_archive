
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
 * This class represents variants of fields.
 * 
 * <p>
 * Created: Fri Jan 25 14:29:09 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldVariant
  extends ValuedVariant {
	/**
	 * The field represented by this variant.
	 *
	 * @invariant _field != null
	 */
	public final SootField _field;

	/**
	 * Creates a new <code>FieldVariant</code> instance.
	 *
	 * @param field the field to be represented by this variant.
	 * @param flowNode the node associated with this variant.
	 *
	 * @pre field != null and flowNode != null
	 */
	public FieldVariant(final SootField field, final IFGNode flowNode) {
		super(flowNode);
		this._field = field;
	}
}

/*
   ChangeLog:

   $Log$
   Revision 1.4  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.3  2003/08/16 21:50:51  venku
   Removed ASTVariant as it did not contain any data that was used.
   Concretized AbstractValuedVariant and renamed it to ValuedVariant.
   Ripple effect of the above change in some.
   Spruced up documentation and specification.
   Revision 1.2  2003/08/16 03:01:49  venku
   Spruced up documentation and specification.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 0.8  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
