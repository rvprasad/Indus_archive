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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Immutable;

/**
 * This exception is thrown by the factory when object creation fails.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class UnableToCreateInstanceException
		extends RuntimeException {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -5452536480248170725L;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param message to be conveyed.
	 * @param cause to be conveyed.
	 */
	public UnableToCreateInstanceException(@Immutable final String message, @Immutable final Throwable cause) {
		super(message, cause);
	}
}
