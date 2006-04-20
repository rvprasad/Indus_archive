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
 * This annotation indicates the degree to which a method is functional (free of observational side-effect). Parameters and
 * return values of the method are immutable independent of the degree of functional-ness.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
@Target({ ElementType.METHOD }) @Documented public @interface Functional {

	/**
	 * The enumeration of various access specifiers in Java.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public enum AccessSpecifier {
		/**
		 * Private access specification.
		 */
		PRIVATE,
		/**
		 * Package private access specification.
		 */
		PACKAGE,
		/**
		 * Protected access specification.
		 */
		PROTECTED,
		/**
		 * Public access specification.
		 */
		PUBLIC
	};

	/**
	 * Provides the access specification level at and beyond (less exposure) which the method is functional.
	 * 
	 * @return the access specification level.
	 */
	AccessSpecifier level() default AccessSpecifier.PRIVATE;
}

// End of File
