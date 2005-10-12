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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class IteratorUtils {

	// / CLOVER:OFF
	/**
	 * Creates an instance of this class.
	 */
	private IteratorUtils() {
		super();
	}

	// / CLOVER:ON

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T3> DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param i1 DOCUMENT ME!
	 * @param i2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T3, T1 extends T3, T2 extends T3> Iterable<T3> chainedIterable(final Iterator<T1> i1, final Iterator<T2> i2) {
		return new Iterable<T3>() {

			public Iterator<T3> iterator() {
				return chainedIterator(i1, i2);
			}
		};
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T3> DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param i1 DOCUMENT ME!
	 * @param i2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T3, T1 extends T3, T2 extends T3> Iterator<T3> chainedIterator(final Iterator<T1> i1, final Iterator<T2> i2) {
		return new Iterator<T3>() {

			private Iterator<? extends T3> curr = i1;

			public boolean hasNext() {
				if (curr.hasNext()) {
					return true;
				}
				curr = i2;
				return curr.hasNext();
			}

			public T3 next() {
				if (hasNext()) {
					return curr.next();
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				curr.remove();
			}
		};
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param i DOCUMENT ME!
	 * @param p DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> Iterator<T2> filteredIterator(final Iterator<T2> i, final IPredicate<T1> p) {
		return new Iterator<T2>() {

			T2 nextItem = null;

			boolean nextItemUsed = true;

			public boolean hasNext() {
				final boolean _result;
				if (nextItemUsed) {
					_result = moveToNextObject();
				} else {
					_result = true;
				}
				return _result;
			}

			public T2 next() {
				if (nextItemUsed) {
					if (!moveToNextObject()) {
						throw new NoSuchElementException();
					}
				}
				nextItemUsed = true;
				return nextItem;
			}

			public void remove() {
				if (nextItemUsed) {
					i.remove();
				} else {
					throw new IllegalStateException("Either next() was not called or remove() was called successively.");
				}
			}

			private boolean moveToNextObject() {
				while (i.hasNext()) {
					final T2 _t = i.next();
					if (p.evaluate(_t)) {
						nextItem = _t;
						nextItemUsed = false;
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T> DOCUMENT ME!
	 * @param i DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T> List<T> toList(final Iterable<T> i) {
		final List<T> _r = new ArrayList<T>();
		for (final T _t : i) {
			_r.add(_t);
		}
		return _r;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T> DOCUMENT ME!
	 * @param i DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T> List<T> toList(final Iterator<T> i) {
		final List<T> _r = new ArrayList<T>();
		for (; i.hasNext();) {
			_r.add(i.next());
		}
		return _r;
	}

}

// End of File
