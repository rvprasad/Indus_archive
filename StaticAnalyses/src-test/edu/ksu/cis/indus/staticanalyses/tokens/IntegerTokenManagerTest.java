
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

import soot.jimple.StringConstant;


/**
 * This class tests <code>IntegerTokenManager</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class IntegerTokenManagerTest
  extends AbstractTokenManagerTest {
	/**
	 * Creates a new IntegerTokenManagerTest object.
	 */
	public IntegerTokenManagerTest() {
		tokenManager = new IntegerTokenManager(new SootValueTypeManager());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.AbstractTokenManagerTest#testGetTokens()
	 */
	public void testGetTokens() {
		super.testGetTokens();

		for (int _i = 0; _i <= IntegerTokenManager.NO_OF_BITS_IN_AN_INTEGER; _i++) {
			values.add(StringConstant.v(String.valueOf(_i)));
		}

		try {
			tokenManager.getTokens(values);
			fail("IntegerTokenManager should have failed.");
		} catch (final RuntimeException _e) {
			;
		}
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
	}
}

// End of File
