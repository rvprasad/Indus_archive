
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

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;


/**
 * This class tests <code>Pair</code> and <code>PairManager</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class PairTest
  extends TestCase {
	/**
	 * One of the pairs.
	 */
	private Pair pair1;

	/**
	 * Another pair.
	 */
	private Pair pair2;

	/**
	 * Tests <code>getFirst</code>.
	 */
	public final void testGetFirst() {
		assertTrue(pair1.getFirst().equals("first"));
	}

	/**
	 * Tests <code>getFirst</code>.
	 */
	public final void testGetSecond() {
		assertTrue(pair1.getSecond().equals("second"));
	}

	/**
	 * Tests <code>hashCode</code> and <code>equals</code>.
	 */
	public final void testHashCodeAndEquals() {
		final Pair _p3 = new Pair("first", "second");
		assertTrue(pair1.hashCode() != pair2.hashCode());
		assertTrue(pair1.hashCode() == _p3.hashCode());
		assertFalse(pair1.equals(pair2));
		assertTrue(pair1.equals(_p3));

		final Pair _p4 = new Pair(null, null);
		final Pair _p5 = new Pair(null, null);
		assertTrue(_p4.equals(_p5));
		assertTrue(_p4.hashCode() == _p5.hashCode());
		assertFalse(_p4.equals("hi"));
	}

	/**
	 * Tests <code>mapify</code>.
	 */
	public final void testMapify() {
		final Collection _temp = new ArrayList();
		_temp.add(pair1);
		_temp.add(pair2);

		Map _map = Pair.mapify(_temp, true);
		final Collection _c1 = (Collection) _map.get(pair1.getFirst());
		assertTrue(_c1.size() == 1);
		assertTrue(_c1.contains(pair1.getSecond()));

		final Collection _c2 = (Collection) _map.get(pair2.getFirst());
		assertTrue(_c2.size() == 1);
		assertTrue(_c2.contains(pair2.getSecond()));

		final Pair _p1 = new Pair("first", "fifth");
		_temp.add(_p1);
		_map = Pair.mapify(_temp, true);
		assertTrue(_map.get(pair1.getFirst()) instanceof Collection);
		assertTrue(((Collection) _map.get(pair1.getFirst())).contains(_p1.getSecond()));
		assertTrue(((Collection) _map.get(pair1.getFirst())).contains(pair1.getSecond()));

		_map = Pair.mapify(_temp, false);
		assertTrue(((Collection) _map.get(pair1.getSecond())).contains(pair1.getFirst()));
		assertTrue(((Collection) _map.get(_p1.getSecond())).contains(_p1.getFirst()));
		assertTrue(((Collection) _map.get(pair2.getSecond())).contains(pair2.getFirst()));

		assertTrue(Pair.mapify(Collections.EMPTY_LIST, true).isEmpty());
	}

	/**
	 * Tests <code>optimize</code> and <code>unoptimize</code>.
	 */
	public final void testOptimizeAndUnoptimize1() {
		final Collection _second = new ArrayList();
		_second.add("first");

		final Pair _p1 = new Pair("first", _second);
		final int _hash1 = _p1.hashCode();
		_second.add("second");

		assertTrue(_p1.hashCode() == _hash1);
		_p1.unoptimize();
		assertTrue(_p1.hashCode() != _hash1);
		_p1.optimize();
		assertTrue(_p1.hashCode() != _hash1);
	}

	/**
	 * Tests <code>optimize</code> and <code>unoptimize</code>.
	 */
	public final void testOptimizeAndUnoptimize2() {
		final StringBuffer _second = new StringBuffer();
		_second.append("first");

		final Pair _p1 = new Pair("first", _second);
		final int _hash1 = _p1.hashCode();
		_second.append("second");

		assertTrue(_p1.hashCode() == _hash1);
		_p1.unoptimize();
		assertTrue(_p1.hashCode() == _hash1);
		_p1.optimize();
		assertTrue(_p1.hashCode() == _hash1);
	}

	/**
	 * Tests <code>PairManager</code>.
	 */
	public final void testPairManager() {
		final PairManager _pmgr = new PairManager();
		final Pair _p1 = _pmgr.getOptimizedPair("first", "second");
		final Pair _p2 = _pmgr.getOptimizedPair("first", "second");
		final Pair _p3 = _pmgr.getUnOptimizedPair("first", "second");
		final Pair _p4 = _pmgr.getUnOptimizedPair("first", "second");
		assertTrue(_p1 == _p2);
		assertTrue(_p3 == _p4);
		assertTrue(_p1 == _p3);
		_pmgr.reset();

		final Pair _p5 = _pmgr.getUnOptimizedPair("first", "second");
		assertTrue(_p1 != _p5);
	}

	/**
	 * Tests <code>toString</code>.
	 */
	public final void testToString() {
		final StringBuffer _second = new StringBuffer();
		_second.append(true);

		final Pair _p1 = new Pair("first", _second);
		_p1.optimize();

		final String _str1 = _p1.toString();
		_p1.unoptimize();
		_second.append(false);

		final String _str2 = _p1.toString();
		assertFalse(_str1.equals(_str2));
		_p1.optimize();

		final String _str3 = _p1.toString();
		assertFalse(_str1.equals(_str3));
		assertTrue(_str2.equals(_str3));
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		pair1 = new Pair("first", "second");
		pair2 = new Pair("third", "fourth");
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		pair1 = null;
		pair2 = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.
 */
