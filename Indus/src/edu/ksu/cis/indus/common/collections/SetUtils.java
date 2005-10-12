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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SetUtils {

	// /CLOVER:OFF

	/**
	 * DOCUMENT ME!
	 */
	public static final IFactory<Set> SET_FACTORY = new IFactory<Set>() {

		public Set create() {
			return new HashSet();
		}
	};

	/**
	 * Creates an instance of this class.
	 */
	public SetUtils() {
		super();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T3> DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col1 DOCUMENT ME!
	 * @param col2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T3, T1 extends T3, T2 extends T3> Set<T3> difference(final Collection<T1> col1, final Collection<T2> col2) {
		final Set<T3> _r = new HashSet<T3>(col1);
		for (final Iterator<T3> _i = _r.iterator(); _i.hasNext();) {
			if (col2.contains(_i.next()))
				_i.remove();
		}
		return _r;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static <T> IFactory<Set<T>> getFactory() {
		return (IFactory<Set<T>>) SET_FACTORY;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <O> DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col1 DOCUMENT ME!
	 * @param col2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <O, T1 extends O, T2 extends O> Set<O> intersection(final Collection<T1> col1, final Collection<T2> col2) {
		final Set<O> _result = new HashSet<O>(CollectionUtils.maxSize(col1, col2));
		for (final O _t : col1) {
			if (col2.contains(_t)) {
				_result.add(_t);
			}
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param col1 DOCUMENT ME!
	 * @param col2 DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2 extends T1> boolean isSubCollection(final Collection<T2> col1, final Collection<T1> col2) {
		for (final T2 _t1 : col1) {
			if (!col2.contains(_t1)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T> DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param col1 DOCUMENT ME!
	 * @param col2 DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T, T1 extends T, T2 extends T> Set<T> union(final Collection<T1> col1, final Collection<T2> col2) {
		final Set<T> _r = new HashSet<T>(CollectionUtils.maxSize(col1, col2));
		_r.addAll(col1);
		_r.addAll(col2);
		return _r;
	}
}

// End of File
