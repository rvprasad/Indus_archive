
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

package edu.ksu.cis.indus.interfaces;

import soot.SootMethod;
import soot.Value;

import soot.jimple.InvokeStmt;


/**
 * The interface to access escape analysis information. 
 * 
 * @version $Revision$ 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 */
public interface IEscapeInfo
  extends IIdentification,
	  IStatus {
	/** 
	 * This is the unique identifier that can be used to identify an instance of this class.
	 */
	Object ID = "Escape Information";

	/**
	 * Checks if the given statement containing a <code>wait</code> invocation is coupled to the given statement containing
	 * <code>notify/All</code> invocation.  By coupling we mean that the notification via the given notify  invocation may
	 * reach the given wait invocation.
	 *
	 * @param wait is the statement containing <code>wait</code> invocation.
	 * @param waitMethod is the method in which <code>wait</code> occurs.
	 * @param notify is the statement containing <code>notify/All</code> invocation.
	 * @param notifyMethod is the method in which <code>notify</code> occurs.
	 *
	 * @return <code>true</code> if <code>wait</code> is ready dependent on <code>notify</code>; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre wait != null and waitMethod != null and notify != null and notifyMethod != null
	 */
	boolean areWaitAndNotifyCoupled(final InvokeStmt wait, final SootMethod waitMethod, final InvokeStmt notify,
		final SootMethod notifyMethod);

	/**
	 * Checks if the object bound to the given value in the given method shared or escapes.
	 *
	 * @param v is the object value being checked for sharing.
	 * @param sm is the method in which <code>v</code> occurs.
	 *
	 * @return <code>true</code> if <code>v</code> is shared; <code>false</code>, otherwise.
	 *
	 * @pre v != null and sm != null
	 */
	boolean escapes(final Value v, final SootMethod sm);

	/**
	 * Checks if the given values are shared.  This is more stricter than escape-ness.  This requires that the values be
	 * escaping as well as represent a common entity.
	 *
	 * @param v1 is one of the value in the check.
	 * @param sm1 is the method in which <code>v1</code> occurs.
	 * @param v2 is the other value in the check.
	 * @param sm2 is the method in which <code>v2</code> occurs.
	 *
	 * @return <code>true</code> if the given values are indeed shared across threads; <code>false</code>, otherwise.
	 *
	 * @pre v1 != null and sm1 != null and v2 != null and sm2 != null
	 */
	boolean shared(final Value v1, final SootMethod sm1, final Value v2, final SootMethod sm2);

	/**
	 * Checks if "this" variable of the given method escapes.  If the method is static then the result is pessimistic, hence,
	 * <code>true</code> is returned.
	 *
	 * @param method in which "this" occurs.
	 *
	 * @return <code>true</code> if "this" escapes; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	boolean thisEscapes(final SootMethod method);
}

// End of File
