
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

package edu.ksu.cis.indus.interfaces;

/**
 * This interface helps realize the <i>IPrototype</i> design pattern as defined in the Gang of Four book. It provides the
 * methods via which concrete object can be created from a prototype object.  The default implementation for these methods
 * should raise <code>UnsupportedOperationException</code>.
 * 
 * <p>
 * Created: Sun Jan 27 18:04:58 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface IPrototype {
	/**
	 * Creates a concrete object from this prototype object.
	 *
	 * @return concrete object based on this prototype object.
	 *
	 * @throws UnsupportedOperationException when this operation is not supported.
	 */
	Object getClone();

	/**
	 * Creates a concrete object from this prototype object.  The concrete object can be parameterized by the information in
	 * <code>o</code>.
	 *
	 * @param o object containing the information to parameterize the concrete object.
	 *
	 * @return concrete object based on this prototype object.
	 *
	 * @throws UnsupportedOperationException when this operation is not supported.
	 *
	 * @pre o != null
	 */
	Object getClone(Object o);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/27 23:21:42  venku
 *** empty log message ***
       Revision 1.3  2003/08/18 04:16:35  venku
       Documentation change.
       Revision 1.2  2003/08/15 02:54:06  venku
       Spruced up specification and documentation for flow-insensitive classes.
       Changed names in AbstractExprSwitch.
       Ripple effect of above change.
       Formatting changes to IPrototype.
       Revision 1.1  2003/08/12 18:33:41  venku
       Created an umbrella project to host generic interfaces related to design patterns.
       Moving prototype pattern interface under this umbrella.
       Revision 1.1  2003/08/07 06:40:24  venku
       Major:
        - Moved the package under indus umbrella.
       Revision 1.1  2003/05/22 22:18:31  venku
       All the interfaces were renamed to start with an "I".
       Optimizing changes related Strings were made.
 */
