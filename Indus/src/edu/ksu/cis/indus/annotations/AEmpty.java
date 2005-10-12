package edu.ksu.cis.indus.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation indicates empty constructors and methods.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD}) @Documented public @interface AEmpty {

	/**
	 * Provides a string description of the return value, if any, by the method decorated with this annotation type. Useful
	 * values may be "null", "false", and "0".
	 * 
	 * @return the string description.
	 */
	String value() default "void";
}

// End of File
