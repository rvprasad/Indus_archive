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

package edu.ksu.cis.indus.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation indicates immutability of entities via published (public, protected, package-private) interfaces.
 * Immutability is that the data reachable from the entities will not be mutated.
 * <ul>
 * <li>With fields and local variables, this annotation indicates requirement.
 * <li>With parameters, it indicates guarantee provided by the enclosed method.
 * <li>With instance methods, it indicates guarantee provided by the method about the receiver object.
 * <li>With static methods, it indicates guarantee provided by the method about class entities.
 * </ul>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD }) @Documented @Empty public @interface Immutable {
	// empty
}

// End of File
