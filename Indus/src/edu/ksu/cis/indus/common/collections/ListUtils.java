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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NumericalConstraint;
import edu.ksu.cis.indus.annotations.NumericalConstraint.NumericalValue;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains static utility methods that are useful in the context of <code>java.util.List</code> instances.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ListUtils {

	// / CLOVER:OFF
	/**
	 * Creates an instance of this class.
	 */
	private ListUtils() {
		super();
	}

	// / CLOVER:ON

	/**
	 * Ensures that <code>list.size()</code> returns <code>finalSize</code>. This is achieved by appending
	 * <code>defaultValue</code> appropriate number of times to <code>list</code>.
	 * 
	 * @param <T> The type of the elements in the list.
	 * @param list to be modified.
	 * @param finalSize of <code>list</code>
	 * @param defaultValue to be injected into the list, if needed.
	 * @post list.size() = finalSize
	 * @post list$pre.size() &lt; finalSize implies list.contains(defaultValue)
	 * @post list$pre.size() &gt;= finalSize implies list$pre.equals(list)
	 */
	@Functional public static <T> void ensureSize(@NonNull final List<T> list,
			@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final int finalSize, final T defaultValue) {
		final int _size = list.size();

		if (finalSize > _size) {
			for (int _i = finalSize - _size; _i > 0; _i--) {
				list.add(defaultValue);
			}
		}
	}

	/**
	 * Retrieves the element at the given index from the given list; if none exists, then the list is extended to accomodate
	 * the index, the default value is inserted at the given index, and the default value is returned.
	 * 
	 * @param <T> is the type of object in the list.
	 * @param list of interest.
	 * @param index of interest.
	 * @param defaultValue to be used, if needed.
	 * @return the value in <code>list</code> at index <code>i</code>.
	 * @post list$pre.size() &lt; index implies (list.size() = index and list.get(index) = defaultValue)
	 * @post list.containsAll(list$pre)
	 * @post result = list.get(index)
	 */
	@Immutable public static <T> T getAtIndexFromList(@NonNull final List<T> list,
			@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final int index, @Immutable final T defaultValue) {
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

	/**
	 * Retrieves an element at the given index in the list. If the value at the given index is <code>null</code> then the
	 * <code>null</code> value is replaced by the defaultValue and the same is returned. If the index does not occur in the
	 * list (list is short) then the list is extended with null values, defaultValue is added at the appropriate index, and
	 * the same is returned.
	 * 
	 * @param <T> is the type of the elements of the list.
	 * @param <T2> is the type of object created by the factory.
	 * @param list from which to retrieve the value.
	 * @param index in <code>list</code> from which to retrive the value.
	 * @param factory provides the default value to be injected and returned if none exists or if <code>null</code> exists.
	 * @return the value at <code>index</code> in <code>list</code>.
	 * @throws IndexOutOfBoundsException when <code>index</code> is less than 0.
	 * @post list$pre.get(index) = null or list$pre.size() &lt; index implies result = factory.create()
	 * @post list.get(index) = result
	 */
	@NonNull @Functional public static <T, T2 extends T> T getAtIndexFromListUsingFactory(@NonNull final List<T> list,
			@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final int index, @NonNull final IFactory<T2> factory)
			throws IndexOutOfBoundsException {
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
	 * Retrieves a list factory.
	 * 
	 * @param <T> is the type of object stored in the list created by the returned factory.
	 * @return a factory.
	 */
	@NonNull @Functional public static <T> IFactory<List<T>> getFactory() {
		return new IFactory<List<T>>() {

			public List<T> create() {
				return new ArrayList<T>();
			}
		};
	}

}

// End of File
