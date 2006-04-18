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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class contains static utility methods that are useful in the context of <code>java.util.Iterator</code> instances.
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
	 * Composes the given iterable in the given order.
	 * 
	 * @param <T3> is the type of objects provided by the resulting iterable.
	 * @param <T1> is the type of objects provided by the first component iterable.
	 * @param <T2> is the type of objects provided by the second component iterable.
	 * @param i1 is one of the component iterable.
	 * @param i2 is the other component iterable.
	 * @return a iterable that will provide the elements from <code>i1</code> and then from <code>i2</code>.
	 */
	@Functional @NonNull public static <T3, T1 extends T3, T2 extends T3> Iterable<T3> chainedIterable(
			@NonNull @Immutable final Iterator<T1> i1, @NonNull @Immutable final Iterator<T2> i2) {
		return new Iterable<T3>() {

			public Iterator<T3> iterator() {
				return chainedIterator(i1, i2);
			}
		};
	}

	/**
	 * Composes the given iterators in the given order.
	 * 
	 * @param <T3> is the type of objects provided by the resulting iterator.
	 * @param <T1> is the type of objects provided by the first component iterator.
	 * @param <T2> is the type of objects provided by the second component iterator.
	 * @param i1 is one of the component iterator.
	 * @param i2 is the other component iterator.
	 * @return a iterator that will provide the elements from <code>i1</code> and then from <code>i2</code>.
	 */
	@Functional @NonNull public static <T3, T1 extends T3, T2 extends T3> Iterator<T3> chainedIterator(
			@NonNull @Immutable final Iterator<T1> i1, @NonNull @Immutable final Iterator<T2> i2) {
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
	 * Provides an iterable for the given collection that returns only the elements that satisfy the given predicate.
	 * 
	 * @param <T1> is the type of object in the collection.
	 * @param <T2> is the type of input object to the predicate.
	 * @param c is the given collection.
	 * @param p is the given predicate.
	 * @return an iterable.
	 */
	@Functional @NonNull public static <T1, T2 extends T1> Iterable<T2> filteredIterable(
			@NonNull @Immutable final Collection<T2> c, @NonNull @Immutable final IPredicate<T1> p) {
		return new Iterable<T2>() {

			public Iterator<T2> iterator() {
				return filteredIterator(c.iterator(), p);
			}
		};
	}

	/**
	 * Provides an iterator that returns only the elements from the given iterator that satisfy the given predicate.
	 * 
	 * @param <T1> is the type of object returned by the given iterator.
	 * @param <T2> is the type of input object to the predicate.
	 * @param i is the given iterator.
	 * @param p is the given predicate.
	 * @return an iterator.
	 */
	@Functional @NonNull public static <T1, T2 extends T1> Iterator<T2> filteredIterator(
			@NonNull @Immutable final Iterator<T2> i, @NonNull @Immutable final IPredicate<T1> p) {
		return new Iterator<T2>() {

			T2 nextItem;

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
	 * Provides the content of the iterable as a list.
	 * 
	 * @param <T> is the type of object in the iterable.
	 * @param i is the iterable.
	 * @return the list containing the contents accessible via <code>i</code>.
	 */
	@Functional @NonNull public static <T> List<T> toList(@NonNull @Immutable final Iterable<T> i) {
		final List<T> _r = new ArrayList<T>();
		for (final T _t : i) {
			_r.add(_t);
		}
		return _r;
	}

	/**
	 * Provides the content of the iterator as a list.
	 * 
	 * @param <T> is the type of object in the iterator.
	 * @param i is the iterator.
	 * @return the list containing the contents accessible via <code>i</code>.
	 */
	@Functional @NonNull public static <T> List<T> toList(@NonNull @Immutable final Iterator<T> i) {
		final List<T> _r = new ArrayList<T>();
		for (; i.hasNext();) {
			_r.add(i.next());
		}
		return _r;
	}

}

// End of File
