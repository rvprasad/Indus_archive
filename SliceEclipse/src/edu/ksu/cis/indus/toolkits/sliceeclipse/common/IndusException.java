
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

package edu.ksu.cis.indus.toolkits.sliceeclipse.common;

/**
 * The Indus Exception.
 *
 * @author ganeshan The Indus exception.
 */
public class IndusException
  extends Exception {
	/** 
	 * The Constructor.
	 * @param strExceptionName The parameter to the exception.
	 */
	public IndusException(final String strExceptionName) {
		super(strExceptionName);
	}
}
