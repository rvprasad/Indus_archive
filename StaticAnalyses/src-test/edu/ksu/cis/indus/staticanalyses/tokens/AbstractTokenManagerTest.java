
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

import edu.ksu.cis.indus.IndusTestCase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.jimple.IntConstant;
import soot.jimple.StringConstant;


/**
 * This class provides implementation to test any implementations of <code>ITokenManagers</code>.  So, specific test cases
 * for each such implementations should inherit from this class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractTokenManagerTest
  extends IndusTestCase {
	/** 
	 * The collection of values used to test token manager.
	 */
	protected Collection values = new HashSet();

	/** 
	 * The token manager to test.
	 */
	protected ITokenManager tokenManager;

	/**
	 * Tests <code>getTokens</code>.
	 */
	public void testGetTokens() {
		final ITokens _tokens = tokenManager.getTokens(values);
		assertNotNull(_tokens);
		assertTrue(_tokens.getValues().containsAll(values));
		assertTrue(values.containsAll(_tokens.getValues()));
	}

	/**
	 * Tests <code>getTypeBasedFilter</code>.
	 */
	public final void testGetTypeBasedFilter() {
		final ITypeManager _typeMgr = tokenManager.getTypeManager();
		final ITokenFilter _filter = tokenManager.getTypeBasedFilter(_typeMgr.getExactType(values.iterator().next()));
		assertNotNull(_filter);

		final ITokens _falseTokens = tokenManager.getTokens(Collections.singleton(IntConstant.v(1)));
		assertTrue(_filter.filter(_falseTokens).isEmpty());

		final ITokens _trueTokens = tokenManager.getTokens(Collections.singleton(StringConstant.v("string")));
		assertFalse(_filter.filter(_trueTokens).isEmpty());
	}

	/**
	 * Tests <code>getTypeManager</code>.
	 */
	public final void testGetTypeManager() {
		final ITypeManager _typeMgr = tokenManager.getTypeManager();
		assertNotNull(_typeMgr);

		for (final Iterator _i = values.iterator(); _i.hasNext();) {
			final Object _val = _i.next();
			assertNotNull(_typeMgr.getExactType(_val));
		}
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
		values.add(StringConstant.v("Hi"));
		values.add(StringConstant.v("Bye"));
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		super.tearDown();
		values.clear();
		values = null;
	}
}

// End of File
