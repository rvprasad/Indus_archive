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
import edu.ksu.cis.indus.common.ToStringBasedComparator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class contains static utility methods that are useful in the context of <code>java.util.Collection</code> instances.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CollectionUtils {

	/**
	 * A bitset factory object.
	 */
	public static final IFactory<BitSet> BITSET_FACTORY = new IFactory<BitSet>() {

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
	 * Adds the objects returned by the given iterator to the given collection.
	 * 
	 * @param <T1> is the type of the objects in the given collection.
	 * @param <T2> is the type of the objects in the given iterable object.
	 * @param col is the collection into which objects will be added.
	 * @param i provides the objects that will be added.
	 */
	@Immutable public static <T1, T2 extends T1> void addAll(@NonNull final Collection<T1> col,
			@Immutable @NonNull final Iterable<T2> i) {
		for (final T2 _t : i) {
			col.add(_t);
		}
	}

	/**
	 * Adds the contents of <code>i</code> to the given collection <code>col</code>.
	 * 
	 * @param <T1> is the type of the objects in the given collection.
	 * @param <T2> is the type of the objects in the given iterator.
	 * @param col is the collection into which objects will be added.
	 * @param i provides the objects that will be added.
	 */
	@Immutable public static <T1, T2 extends T1> void addAll(@NonNull final Collection<T1> col,
			@Immutable @NonNull final Iterator<T2> i) {
		for (; i.hasNext();) {
			col.add(i.next());
		}
	}

	/**
	 * Calculates the cardinality of the given object in the given collection.
	 * 
	 * @param <T1> is the type of the objects in the given collection.
	 * @param <T2> is the type of the given object.
	 * @param obj is the object of interest.
	 * @param col is the collection of interest.
	 * @return the number of times <code>obj</code> occurs in <code>col</code>.
	 */
	@Functional @NumericalConstraint(value = NumericalValue.NON_NEGATIVE) public static <T1, T2 extends T1> int cardinality(
			final T2 obj, @NonNull final Collection<T1> col) {
		int _r;
		if (col instanceof Set) {
			if (col.contains(obj)) {
				_r = 1;
			} else {
				_r = 0;
			}
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
	 * Collects elements in the given collection that satisfy the given predicate.
	 * 
	 * @param <T2> is the type of the objects in the given collection.
	 * @param <T1> is the type of the input object for the given predicate.
	 * @param col is the collection of interest.
	 * @param predicate is the predicate to be used.
	 * @return a sequence of elements from <code>col</code> that satisfy <code>predicate</code>.
	 * @post result->forall(o | predicate.evaluate(o))
	 */
	@Functional public static <T1, T2 extends T1> List<T2> collect(@NonNull final Collection<T2> col,
			@NonNull final IPredicate<T1> predicate) {
		final List<T2> _result = new ArrayList<T2>(col.size());
		for (final T2 _t : col) {
			if (predicate.evaluate(_t)) {
				_result.add(_t);
			}
		}
		return _result;
	}

	/**
	 * Collects the results of transforming the elements in the given collection via the given transformer.
	 * 
	 * @param <TI> is the type of the objects in the given collection.
	 * @param <TO> is the type of the objects in the resulting collection.
	 * @param <I> is the type of the input objects to the given transformer.
	 * @param <O> is the type of the output objects to the given transformer.
	 * @param col is the collection of interest.
	 * @param transformer is the transformer to be used.
	 * @return a sequence of elements resulting from transforming the elements of <code>col</code> via
	 *         <code>transformer</code>.
	 * @post col->forall(o | result.contains(tranformer.transform(o)))
	 */
	@Functional public static <I, TI extends I, TO, O extends TO> List<TO> collect(@NonNull final Collection<TI> col,
			@NonNull final ITransformer<I, O> transformer) {
		final List<TO> _r = new ArrayList<TO>(col.size());
		transform(col, transformer, _r);
		return _r;
	}

	/**
	 * Checks if the given collections have a common element.
	 * 
	 * @param <T1> is the type of the objects in the first collection.
	 * @param <T2> is the type of the objects in the second collection.
	 * @param col1 is one of the collection of interest.
	 * @param col2 is another collection of interest.
	 * @return <code>true</code> if <code>col1</code> and <code>col2</code> have a common element; <code>false</code>,
	 *         otherwise.
	 * @post col1->exists(o | col2->contains(o))
	 */
	@Functional public static <T1, T2> boolean containsAny(@NonNull final Collection<T1> col1,
			@NonNull final Collection<T2> col2) {
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
	 * Checks if the given collection has an element that satisfies the given predicate.
	 * 
	 * @param <T2> is the type of the objects in the given collection.
	 * @param <T1> is the type of the input object to the givne predicate.
	 * @param col is the collection of interest.
	 * @param predicate is the predicate to be used.
	 * @return <code>true</code> if <code>col</code> contains an element that satisfies <code>predicate</code>;
	 *         <code>false</code>, otherwise.
	 * @post col->exists(o | predicate.evaluate(o))
	 */
	@Functional public static <T1, T2 extends T1> boolean exists(@NonNull final Collection<T2> col,
			@NonNull final IPredicate<T1> predicate) {
		for (final T2 _t : col) {
			if (predicate.evaluate(_t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes any elements of the given collection that does not satisfy the given predicate.
	 * 
	 * @param <T2> is the type of the objects in the given collection.
	 * @param <T1> is the type of the input object to the given predicate.
	 * @param col is the collection of interest.
	 * @param predicate is the predicate to be used.
	 * @post result->forall(o | predicate.evaluate(o))
	 */
	@Immutable public static <T1, T2 extends T1> void filter(@NonNull final Collection<T2> col,
			@NonNull final IPredicate<T1> predicate) {
		for (final Iterator<T2> _i = col.iterator(); _i.hasNext();) {
			if (!predicate.evaluate(_i.next())) {
				_i.remove();
			}
		}
	}

	/**
	 * Finds an element from the given collection that satisfies the given predicate, if one exists.
	 * 
	 * @param <T2> is the type of the objects in the given collection.
	 * @param <T1> is the type of the input object to the given predicate.
	 * @param values is the collection of interest.
	 * @param predicate is the predicate to be used.
	 * @return an element of <code>values</code> that satisfies <code>predicate</code>; if none exist, <code>null</code>
	 *         is returned.
	 * @post result != null implies values.contains(result) and predicate.evaluate(result)
	 */
	@Functional public static <T1, T2 extends T1> T2 find(@NonNull final Collection<T2> values,
			@NonNull final IPredicate<T1> predicate) {
		for (final T2 _t : values) {
			if (predicate.evaluate(_t)) {
				return _t;
			}
		}
		return null;
	}

	/**
	 * Executes the given closure for each element in the collection.
	 * 
	 * @param col is the collection of interest.
	 * @param closure is the closure to be executed.
	 * @param <T1> is the type of input objects to the given closure.
	 * @param <T2> is the type of objects in the given collection.
	 */
	@Functional public static <T1, T2 extends T1> void forAllDo(@NonNull final Collection<T2> col,
			@NonNull final IClosure<T1> closure) {
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
	 */
	@Functional @NonNull public static <T> String prettyPrint(@NonNull final Collection<T> collection) {
		final StringBuffer _sb = new StringBuffer();
		_sb.append("-----------------------Collection: " + collection.getClass().getName() + " / " + collection.hashCode()
				+ " [" + collection.size() + "]");

		final List<T> _t = new ArrayList<T>(collection);
		Collections.sort(_t, ToStringBasedComparator.getComparator());

		final Iterator<T> _i = _t.iterator();
		final int _iEnd = _t.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final T _element = _i.next();
			_sb.append("\n");
			_sb.append(_element.toString());
		}
		_sb.append("\n=====================================================");
		return _sb.toString();
	}

	/**
	 * Transforms the input collection via the given transforms and injects the results into the output collection.
	 * 
	 * @param <TI> is the type of objects in the input collection.
	 * @param <TO> is the type of objects in the output collection.
	 * @param <I> is the type of input objects to the given transformer.
	 * @param <O> is the type of output objects to the given transformer.
	 * @param inCol is the input collection.
	 * @param tranformer is the transformer to be used.
	 * @param outCol is the output collection.
	 * @post inCol$pre->forall(o | outCol.contains(transformer.transform(o)))
	 */
	@Immutable public static <I, TI extends I, TO, O extends TO> void transform(
			@Immutable @NonNull final Collection<TI> inCol, @NonNull final ITransformer<I, O> tranformer,
			@NonNull final Collection<TO> outCol) {
		for (final Iterator<TI> _i = inCol.iterator(); _i.hasNext();) {
			outCol.add(tranformer.transform(_i.next()));
		}
	}

	/**
	 * Returns the maximum of the sizes of the given collections.
	 * 
	 * @param col1 is one of the collection of interest.
	 * @param col2 is the other collection of interest.
	 * @return either the size of <code>col1</code> or <code>col2</code>.
	 */
	@Functional static int maxSize(@NonNull final Collection<?> col1, @NonNull final Collection<?> col2) {
		return Math.max(col1.size(), col2.size());
	}
}

// End of File
