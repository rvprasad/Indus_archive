
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
 * This class represents variants of arrays.
 * 
 * <p>
 * Created: Fri Jan 25 16:05:27 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ArrayVariant
  extends ValuedVariant {
	/**
	 * The array type being represented by this variant.
	 *
	 * @invariant _type != null
	 */
	public final ArrayType _type;

	/**
	 * Creates a new <code>ArrayVariant</code> instance.
	 *
	 * @param a the array type to which this variant corresonds to.
	 * @param flowNode the flow graph node corresponding to this variant.
	 *
	 * @pre a != null and flowNode != null
	 */
	protected ArrayVariant(final ArrayType a, final IFGNode flowNode) {
		super(flowNode);
		this._type = a;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.8  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
