
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
 * This class is used to indicate the status of an object before dispatching any methods on it.  The intent of this interface
 * is that the users of information-providing interfaces should be able to check if the object providing that interface is
 * in a stable state to do so.  Hence, it is adviced that information-providing interfaces inherit this interface.  On a
 * general note,  this interface can be used in situations where a support to query status is required.
 * 
 * <p>
 * A note on stability.  In most cases, active entities like analysis may provide different answers for identical queries at
 * different times when they are active, i.e, when analysis is happening. However, while not active, it will usually provide
 * the same answer for identical queries at different times.  Nevertheless the answer may be incorrect, but the analysis is
 * consistent in it's answer. It is this state in which the analysis provide consistent answers that we refer to as stable
 * state,  a state in which the external behavior of an object will be consistent.  Hence, implementation can use
 * <code>isStable</code> to indicate/detect activeness.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IStatus {
	/**
	 * Checks if the object is in a stable state so that it can be queried for information.
	 *
	 * @return <code>true</code> if the implementation is in a stable state; <code>false</code>, otherwise.
	 */
	boolean isStable();
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/27 23:21:42  venku
 *** empty log message ***
       Revision 1.2  2003/08/21 03:54:41  venku
       Documentation.
       Revision 1.1  2003/08/21 03:30:34  venku
       Added a new interface to query stableness of objects.
       Analyses/Engine/Transformation objects in particular.
 */
