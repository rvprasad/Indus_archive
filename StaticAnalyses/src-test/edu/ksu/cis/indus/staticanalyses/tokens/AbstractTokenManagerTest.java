
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.IndusTestCase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.Type;
import soot.Value;

import soot.jimple.IntConstant;
import soot.jimple.NullConstant;
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
		final Type _type = ((Value) values.iterator().next()).getType();
		final ITokenFilter _filter = tokenManager.getTypeBasedFilter(_typeMgr.getTokenTypeForRepType(_type));
		assertNotNull(_filter);

		final ITokens _falseTokens = tokenManager.getTokens(Collections.singleton(IntConstant.v(1)));
        assertFalse(_falseTokens.isEmpty());
		assertTrue(_filter.filter(_falseTokens).isEmpty());

		values.add(NullConstant.v());

		final ITokens _trueTokens = tokenManager.getTokens(values);
		final ITokens _filtrate = _filter.filter(_trueTokens);
		assertFalse(_filtrate.isEmpty());
		assertTrue(_filtrate.getValues().contains(NullConstant.v()));
	}

	/**
	 * Tests <code>getTypeManager</code>.
	 */
	public final void testGetTypeManager() {
		final ITypeManager _typeMgr = tokenManager.getTypeManager();
		assertNotNull(_typeMgr);

		for (final Iterator _i = values.iterator(); _i.hasNext();) {
			final Object _val = _i.next();
			assertNotNull(_typeMgr.getTokenTypeForRepType(((Value) _val).getType()));
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
