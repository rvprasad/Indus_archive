
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


/**
 * This class test <code>FastUnionFindElement</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FastUnionFindElementTest
  extends TestCase {
	/**
	 * One of the test element.
	 */
	FastUnionFindElement f1;

	/**
	 * Other test element.
	 */
	FastUnionFindElement f2;

	/**
	 * Tests <code>find</code> and <code>union</code>.
	 */
	public void testFindAndUnion() {
		assertTrue(f1.find().equals(f1));
		assertTrue(f2.find().equals(f2));
		f1.union(f2);

		final FastUnionFindElement _f3 = f1.find();
		final FastUnionFindElement _f4 = f2.find();
		assertTrue(_f3.equals(_f4));
		assertTrue(_f3.equals(f1) || _f3.equals(f2));

		final FastUnionFindElement _f5 = new FastUnionFindElement();
		final FastUnionFindElement _f6 = new FastUnionFindElement();
		_f5.setType("1");
		_f6.union(_f5);
		assertTrue(_f6.find().equals(_f5));
	}

	/**
	 * Tests <code>getType</code> and <code>setType</code>.
	 */
	public void testGetTypeAndSetType() {
		final Object _object = new Object();
		f1.setType(_object);
		assertTrue(_object.equals(f1.getType()));

		try {
			f1.setType(_object);
			// CLOVER: OFF
			fail("This is incorrect.");
			// CLOVER: ON
		} catch (IllegalStateException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Tests <code>isAtomic</code>.
	 */
	public void testIsAtomic() {
		assertTrue(f1.isAtomic());
		f1.addChild(f2);
		assertFalse(f1.isAtomic());
	}

	/**
	 * Tests <code>isBound</code>.
	 */
	public void testIsBound() {
		assertFalse(f1.isBound());
		assertFalse(f2.isBound());
		f2.union(f1);
		f1.setType(new Object());
		assertTrue(f1.isBound());
		assertTrue(f2.isBound());
	}

	/**
	 * Tests <code>sameType</code>.
	 */
	public void testSameType() {
		f1.setType(new Object());

		final Object _object = new Object();
		f2.setType(_object);
		assertFalse(f1.sameType(f2));

		final FastUnionFindElement _f3 = new FastUnionFindElement();
		_f3.setType(_object);
		assertTrue(f2.sameType(_f3));
	}

	/**
	 * Tests <code>unify</code>.
	 */
	public void testUnify1() {
		assertTrue(f1.unify(f2));
		f1.union(f2);
		assertTrue(f1.unify(f2));
	}

	/**
	 * Tests <code>unify</code>.
	 */
	public void testUnify2() {
		final FastUnionFindElement _f3 = new FastUnionFindElement();
		final FastUnionFindElement _f4 = new FastUnionFindElement();
		_f3.setType(new Object());
		_f4.setType(new Object());
		assertFalse(_f3.unify(_f4));
		f1.addChild(_f3);
		f2.addChild(_f4);
		assertFalse(f1.unify(f2));
	}

	/**
	 * Tests <code>unifyChildren</code>.
	 */
	public void testUnifyChildren() {
		assertFalse(f1.unifyChildren(f2));

		final FastUnionFindElement _f3 = new FastUnionFindElement();
		final FastUnionFindElement _f4 = new FastUnionFindElement();
		f1.addChild(_f3);
		f2.addChild(_f4);
		assertTrue(f1.unifyChildren(f2));

		final FastUnionFindElement _f5 = new FastUnionFindElement();
		final FastUnionFindElement _f6 = new FastUnionFindElement();
		_f5.setType("1");
		_f6.setType("2");
		f1.addChild(_f5);
		f2.addChild(_f6);
		assertFalse(f1.unifyChildren(f2));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		f1 = new FastUnionFindElement();
		f2 = new FastUnionFindElement();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		f1 = null;
		f2 = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/01/25 09:05:57  venku
   - coding convention.
   Revision 1.1  2004/01/06 15:06:23  venku
   - started to add test case for data structures.
 */
