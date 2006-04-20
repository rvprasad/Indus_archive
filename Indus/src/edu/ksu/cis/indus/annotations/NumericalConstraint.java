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
 * This annotation indicates the numerical partition to which a number belongs to. When used on methods, it applies to the
 * return value of the method.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
@Target({ ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER }) @Documented public @interface NumericalConstraint {

	/**
	 * The numerical value partition type.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public enum NumericalValue {

		/**
		 * Negative (&lt; 0) value.
		 */
		NEGATIVE,

		/**
		 * Non-negative (&gt; -1) value.
		 */
		NON_NEGATIVE,

		/**
		 * Non-positive (&lt; 1) value.
		 */
		NON_POSITIVE,

		/**
		 * Positive (&gt; 0) value.
		 */
		POSITIVE,

		/**
		 * Zero value.
		 */
		ZERO
	}

	/**
	 * Provides a value partition to which the annotated numerical entity belongs to.
	 * 
	 * @return a partition.
	 */
	NumericalValue value();

}

// End of File
