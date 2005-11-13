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

import edu.ksu.cis.indus.common.ToStringBasedComparator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class CollectionUtils {

	/**
	 * DOCUMENT ME!
	 */
	public static final IFactory<BitSet> BITSET_FACTORY = new IFactory<BitSet>() {

		// TODO
		public BitSet create() {
			return new BitSet();
		}

	};

	// / CLOVER:OFF
	/**
	 * Creates an instance of this class.
	 */
	private CollectionUtils() {
		super();
	}

	// / CLOVER:ON

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @param i DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> void addAll(final Collection<T1> col, final Iterable<T2> i) {
		for (final T2 _t : i) {
			col.add(_t);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @param i DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> void addAll(final Collection<T1> col, final Iterator<T2> i) {
		for (; i.hasNext();) {
			col.add(i.next());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param obj DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> int cardinality(final T2 obj, final Collection<T1> col) {
		int _r;
		if (col instanceof Set) {
			_r = col.contains(obj) ? 1 : 0;
		} else {
			_r = 0;
			for (final T1 _t : col) {
				if (_t == obj) {
					_r++;
				}
			}
		}
		return _r;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @param predicate DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> Collection<T2> collect(final Collection<T2> col, final IPredicate<T1> predicate) {
		final List<T2> _result = new ArrayList<T2>(col.size());
		for (final T2 _t : col) {
			if (predicate.evaluate(_t)) {
				_result.add(_t);
			}
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <TI> DOCUMENT ME!
	 * @param <TO> DOCUMENT ME!
	 * @param <I> DOCUMENT ME!
	 * @param <O> DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @param tranformer DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <I, TI extends I, TO, O extends TO> Collection<TO> collect(final Collection<TI> col,
			final ITransformer<I, O> tranformer) {
		final Collection<TO> _r = new ArrayList<TO>(col.size());
		transform(col, tranformer, _r);
		return _r;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col1 DOCUMENT ME!
	 * @param col2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2> boolean containsAny(final Collection<T1> col1, final Collection<T2> col2) {
		final Collection<?> _minCol;
		final Collection<?> _maxCol;
		if (col1.size() > col2.size()) {
			_minCol = col2;
			_maxCol = col1;
		} else {
			_minCol = col1;
			_maxCol = col2;
		}
		for (final Object _t2 : _minCol) {
			if (_maxCol.contains(_t2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @param predicate DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> boolean containsAny(final Collection<T2> col, final IPredicate<T1> predicate) {
		for (final T2 _t : col) {
			if (predicate.evaluate(_t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param col DOCUMENT ME!
	 * @param predicate DOCUMENT ME!
	 * @param <T> DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T> boolean exists(final Collection<T> col, final IPredicate<T> predicate) {
		for (final T _t : col) {
			if (predicate.evaluate(_t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @param predicate DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> void filter(final Collection<T2> col, final IPredicate<T1> predicate) {
		for (final Iterator<T2> _i = col.iterator(); _i.hasNext();) {
			if (!predicate.evaluate(_i.next())) {
				_i.remove();
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T> DOCUMENT ME!
	 * @param values DOCUMENT ME!
	 * @param predicate DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T> T find(final Collection<T> values, final IPredicate<T> predicate) {
		for (final T _t : values) {
			if (predicate.evaluate(_t)) {
				return _t;
			}
		}
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param col DOCUMENT ME!
	 * @param closure DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> void forAllDo(final Collection<T2> col, final IClosure<T1> closure) {
		for (final T2 _t : col) {
			closure.execute(_t);
		}
	}

	/**
	 * Returns a pretty print representation of the given collection.
	 * 
	 * @param <T> the type of the elements in the collection.
	 * @param collection to be pretty printed.
	 * @return pretty print representation.
	 * @pre collection != null
	 * @post result != null
	 */
	public static <T> String prettyPrint(final Collection<T> collection) {
		final StringBuffer _sb = new StringBuffer();
		_sb.append("-----------------------Collection: " + collection.getClass().getName() + " / " + collection.hashCode()
				+ " [" + collection.size() + "]");

		final List<T> _t = new ArrayList<T>(collection);
		Collections.sort(_t, ToStringBasedComparator.SINGLETON);

		final Iterator _i = _t.iterator();
		final int _iEnd = _t.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _element = _i.next();
			_sb.append("\n");
			_sb.append(_element.toString());
		}
		_sb.append("\n=====================================================");
		return _sb.toString();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <TI> DOCUMENT ME!
	 * @param <TO> DOCUMENT ME!
	 * @param <I> DOCUMENT ME!
	 * @param <O> DOCUMENT ME!
	 * @param inCol DOCUMENT ME!
	 * @param tranformer DOCUMENT ME!
	 * @param outCol DOCUMENT ME!
	 */
	public static <I, TI extends I, TO, O extends TO> void transform(final Collection<TI> inCol,
			final ITransformer<I, O> tranformer, final Collection<TO> outCol) {
		for (final Iterator<TI> _i = inCol.iterator(); _i.hasNext();) {
			outCol.add(tranformer.transform(_i.next()));
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param col1 DOCUMENT ME!
	 * @param col2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	static int maxSize(final Collection<?> col1, final Collection<?> col2) {
		final int _size;
		final int _col1Size = col1.size();
		final int _col2Size = col2.size();
		if (_col1Size < _col2Size) {
			_size = _col2Size;
		} else {
			_size = _col1Size;
		}
		return _size;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param col DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String verbosePrint(final Collection<?> col) {
		final StringBuilder _ret = new StringBuilder();
		_ret.append("Collection : (hashcode :");
		_ret.append(col.hashCode());
		_ret.append(")\n");
		for (final Object _o : col) {
			_ret.append(_o.toString());
			_ret.append("\n");
		}
		_ret.append("\n");
		return _ret.toString();
	}
}

// End of File
