
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

package edu.ksu.cis.indus.common;

import org.apache.commons.collections.Predicate;


/**
 * This class can be used to check if a given class object is the super type of a sub type.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ReflectionBasedSupertypePredicate
  implements Predicate {
	/** 
	 * The sub type against which the check will occur.
	 */
	private Class subtype;

	/**
	 * Checks if the given object is a super type of the subtype set on this object.
	 *
	 * @param object to be checked.
	 *
	 * @return <code>true</code> if <code>object</code> is a class object and it is a super type of the subtype set on this
	 * 		   object; <code>false</code>, otherwise.
	 */
	public boolean evaluate(final Object object) {
		final boolean _result;

		if (object instanceof Class) {
			final Class _superClass = (Class) object;
			_result = _superClass.isAssignableFrom(subtype);
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * Sets the subtype for the check to be performed by this object.
	 *
	 * @param clazz is the subtype.
	 *
	 * @pre class != null
	 */
	public void setsubType(final Class clazz) {
		subtype = clazz;
	}
}

// End of File
