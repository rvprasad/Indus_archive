
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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;


/**
 * This class tests <code>Quadraple</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class QuadrapleTest
  extends TestCase {
	/**
	 * A quadraple.
	 */
	private Quadraple triple1;

	/**
	 * A quadraple.
	 */
	private Quadraple triple2;

	/**
	 * Tests <code>clone</code>.
	 */
	public final void testClone() {
		try {
			final Quadraple _q1 = (Quadraple) triple1.clone();
			assertTrue(_q1 != triple1);
			assertTrue(_q1.getFirst() == triple1.getFirst());
			assertTrue(_q1.getSecond() == triple1.getSecond());
			assertTrue(_q1.getThird() == triple1.getThird());
			assertTrue(_q1.getFourth() == triple1.getFourth());
			assertTrue(_q1.equals(triple1));
			assertTrue(_q1.hashCode() == triple1.hashCode());
		} catch (CloneNotSupportedException _e) {
			fail("This is incorrect");
		}
	}

	/**
	 * Tests <code>getFirst</code>.
	 */
	public final void testGetFirst() {
		assertTrue(triple1.getFirst().equals("first"));
	}

	/**
	 * Tests <code>getFourth</code>.
	 */
	public final void testGetFourth() {
		assertTrue(triple1.getFourth().equals("fourth"));
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
		final Quadraple _t3 = new Quadraple("first", "second", "third", "fourth");
		assertTrue(triple1.hashCode() != triple2.hashCode());
		assertTrue(triple1.hashCode() == _t3.hashCode());
		assertFalse(triple1.equals(triple2));
		assertTrue(triple1.equals(_t3));

		final Quadraple _t4 = new Quadraple(null, null, null, null);
		final Quadraple _t5 = new Quadraple(null, null, null, null);
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

		final Quadraple _t1 = new Quadraple("first", _second, "third", "fourth", true);

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

		final Quadraple _t1 = new Quadraple("first", _second, "third", "fourth", true);
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

		final Quadraple _t1 = new Quadraple("first", _second, "third", "fourth", false);
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
		triple1 = new Quadraple("first", "second", "third", "fourth");
		triple2 = new Quadraple("fifth", "sixth", "seventh", "eighth");
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
 */
