
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

package edu.ksu.cis.indus;

import junit.framework.TestCase;


/**
 * This is a test case which will always cause an error via an exception.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ErringTestCase
  extends TestCase {
	/**
	 * The exception to be thrown.
	 */
	final RuntimeException exception;

	/**
	 * Creates a new ErringTestCase object.
	 *
	 * @param message to be embedded in the exception to be thrown.
	 */
	public ErringTestCase(final String message) {
		exception = new RuntimeException(message);
	}

	/**
	 * This is the erring test.
	 */
	public void testError() {
		throw exception;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/04/17 23:35:43  venku
   - failures due to unavailable resources were not flagged. FIXED
     - added a new class which always errs.
     - this new class is used to setup a test case for cases where an error should occur.
     - ripple effect.
 */