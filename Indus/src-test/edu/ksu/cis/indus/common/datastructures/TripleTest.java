
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.IndusTestCase;

import java.util.ArrayList;
import java.util.Collection;


/**
 * This class tests <code>TripleTest</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TripleTest
  extends IndusTestCase {
	/**
	 * A triple.
	 */
	private Triple triple1;

	/**
	 * A triple.
	 */
	private Triple triple2;

	/**
	 * Tests <code>getFirst</code>.
	 */
	public final void testGetFirst() {
		assertTrue(triple1.getFirst().equals("first"));
	}

	/**
	 * Tests <code>getSecond</code>.
	 */
	public final void testGetSecond() {
		assertTrue(triple1.getSecond().equals("second"));
	}

	/**
	 * Tests <code>getThird</code>.
	 */
	public final void testGetThird() {
		assertTrue(triple1.getThird().equals("third"));
	}

	/**
	 * Tests <code>hashCode</code> and <code>equals</code>.
	 */
	public final void testHashCodeAndEquals() {
		final Triple _t3 = new Triple("first", "second", "third");
		assertTrue(triple1.hashCode() != triple2.hashCode());
		assertTrue(triple1.hashCode() == _t3.hashCode());
		assertFalse(triple1.equals(triple2));
		assertTrue(triple1.equals(_t3));

		final Triple _t4 = new Triple(null, null, null);
		final Triple _t5 = new Triple(null, null, null);
		assertTrue(_t4.equals(_t5));
		assertTrue(_t4.hashCode() == _t5.hashCode());
		assertFalse(_t4.equals("hi"));
	}

	/**
	 * Tests <code>optimize</code> and <code>unoptimize</code>.
	 */
	public final void testOptimizeAndUnOptimize1() {
		final Collection _second = new ArrayList();
		_second.add("first");

		final Triple _t1 = new Triple("first", _second, "third");
		_t1.optimize();

		final int _hash1 = _t1.hashCode();
		_second.add("second");

		assertTrue(_t1.hashCode() == _hash1);
		_t1.unoptimize();
		assertTrue(_t1.hashCode() != _hash1);
		_t1.optimize();
		assertTrue(_t1.hashCode() != _hash1);
	}

	/**
	 * Tests <code>optimize</code> and <code>unoptimize</code>.
	 */
	public final void testOptimizeAndUnOptimize2() {
		final StringBuffer _second = new StringBuffer();
		_second.append("first");

		final Triple _t1 = new Triple("first", _second, "third");
		final int _hash1 = _t1.hashCode();
		_second.append("second");

		assertTrue(_t1.hashCode() == _hash1);
		_t1.unoptimize();
		assertTrue(_t1.hashCode() == _hash1);
		_t1.optimize();
		assertTrue(_t1.hashCode() == _hash1);
	}

	/**
	 * Tests <code>toString</code>.
	 */
	public final void testToString() {
		final StringBuffer _second = new StringBuffer();
		_second.append(true);

		final Triple _t1 = new Triple("first", _second, "third");
		_t1.optimize();

		final String _str1 = _t1.toString();
		_t1.unoptimize();
		_second.append(false);

		final String _str2 = _t1.toString();
		assertFalse(_str1.equals(_str2));
		_t1.optimize();

		final String _str3 = _t1.toString();
		assertFalse(_str1.equals(_str3));
		assertTrue(_str2.equals(_str3));
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		triple1 = new Triple("first", "second", "third");
		triple2 = new Triple("fourth", "fifth", "sixth");
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		triple1 = null;
		triple2 = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/02/07 16:13:29  venku
   - coding conventions.

   Revision 1.2  2004/01/28 22:42:27  venku
 *** empty log message ***
   Revision 1.1  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.
 */
