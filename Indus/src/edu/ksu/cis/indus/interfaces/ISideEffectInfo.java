
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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import soot.SootMethod;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISideEffectInfo
  extends IIdentification,
	  IStatus {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	Object ID = "Side Effect Information";

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 * @param argPos DOCUMENT ME!
	 * @param accesspath DOCUMENT ME!
	 * @param recurse DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isParameterBasedAccessPathSideAffected(final SootMethod method, final int argPos, final Object[] accesspath,
		final boolean recurse);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callerTriple DOCUMENT ME!
	 * @param argPos DOCUMENT ME!
	 * @param accesspath DOCUMENT ME!
	 * @param recurse DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isParameterBasedAccessPathSideAffected(final CallTriple callerTriple, final int argPos,
		final Object[] accesspath, final boolean recurse);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 * @param argPos DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isParameterSideAffected(final SootMethod method, final int argPos);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callerTriple DOCUMENT ME!
	 * @param argPos DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isParameterSideAffected(final CallTriple callerTriple, final int argPos);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 * @param accesspath DOCUMENT ME!
	 * @param recurse DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isThisBasedAccessPathSideAffected(final SootMethod method, final Object[] accesspath, final boolean recurse);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callerTriple DOCUMENT ME!
	 * @param accesspath DOCUMENT ME!
	 * @param recurse DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isThisBasedAccessPathSideAffected(final CallTriple callerTriple, final Object[] accesspath, final boolean recurse);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isThisSideAffected(final SootMethod method);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callerTriple DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isThisSideAffected(final CallTriple callerTriple);
}

// End of File
