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
import java.util.List;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class ListUtils {

	// / CLOVER:OFF
	/**
	 * Creates an instance of this class.
	 */
	private ListUtils() {
		super();
	}

	/**
	 * Ensures that <code>list.size()</code> returns <code>finalSize</code>. This is achieved by appending
	 * <code>defaultValue</code> appropriate number of times to <code>list</code>.
	 * 
	 * @param <T> The type of the elements in the list.
	 * @param list to be modified.
	 * @param finalSize of <code>list</code>
	 * @param defaultValue to be injected into the list, if required, to ensure it's size.
	 * @pre list != null
	 * @post list.size() = finalSize
	 * @post list$pre.size() &lt; finalSize implies list.contains(defaultValue)
	 * @post list$pre.size() &gt;= finalSize implies list$pre.equals(list)
	 */
	public static <T> void ensureSize(final List<T> list, final int finalSize, final T defaultValue) {
		final int _size = list.size();

		if (finalSize > _size) {
			for (int _i = finalSize - _size; _i > 0; _i--) {
				list.add(defaultValue);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T> DOCUMENT ME!
	 * @param list DOCUMENT ME!
	 * @param index DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T> T getAtIndexFromList(final List<T> list, final int index, final T defaultValue) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("invalid index: " + index + " < 0");
		}

		if (index >= list.size()) {
			ensureSize(list, index + 1, null);
			list.set(index, defaultValue);
		} else if (list.get(index) == null) {
			list.set(index, defaultValue);
		}
		return list.get(index);
	}

	// / CLOVER:ON

	/**
	 * Retrieves an element at the given index in the list. If the value at the given index is <code>null</code> then the
	 * null value is replaced by the <code>defaultValue</code> and the same is returned. If the index does not occur in the
	 * list (list is short) then the list is extended with null values, <code>defaultValue</code> is added at the
	 * appropriate index, and the same is returned.
	 * 
	 * @param <T> is the type of the elements of the list
	 * @param <T2> DOCUMENT ME!
	 * @param list from which to retrieve the value.
	 * @param index in <code>list</code> from which to retrive the value.
	 * @param factory provides the default value to be injected and returned if none exists or if <code>null</code> exists.
	 * @return the value at <code>index</code> in <code>list</code>.
	 * @throws IndexOutOfBoundsException when <code>index</code> is less than 0.
	 * @pre list != null and index != null and defaultValue != null
	 * @post list$pre.get(index) = null or list$pre.size() &lt; index implies result = defaultValue
	 * @post list.get(index) = result
	 */
	public static <T, T2 extends T> T getAtIndexFromListUsingFactory(final List<T> list, final int index,
			final IFactory<T2> factory) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("invalid index: " + index + " < 0");
		}

		if (index >= list.size()) {
			ensureSize(list, index + 1, null);
			list.set(index, factory.create());
		} else if (list.get(index) == null) {
			list.set(index, factory.create());
		}
		return list.get(index);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T> DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T> IFactory<List<T>> getFactory() {
		return new IFactory<List<T>>() {

			public List<T> create() {
				return new ArrayList<T>();
			}
		};
	}

}

// End of File
