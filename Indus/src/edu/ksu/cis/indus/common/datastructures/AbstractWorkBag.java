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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This is an abstract implementation of <code>IWorkBag</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> The type of work handled by this work bag.
 */
public abstract class AbstractWorkBag<T>
		implements IWorkBag<T> {

	/**
	 * This contains the work pieces put into the work bag.
	 */
	protected final List<T> container = new ArrayList<T>();

	/**
	 * This is a backing structure to maintain element containment information.
	 */
	private final TObjectIntHashMap countingStructure = new TObjectIntHashMap();

	/**
	 * {@inheritDoc}
	 */
	public final T getWork() {
		if (container.isEmpty()) {
			throw new IllegalStateException("The workbag is empty.");
		}

		final T _result = container.remove(0);
		countingStructure.adjustValue(_result, -1);

		if (countingStructure.get(_result) == 0) {
			countingStructure.remove(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addAllWork(@NonNull @Immutable final Collection<? extends T> c) {
		for (final Iterator<? extends T> _i = c.iterator(); _i.hasNext();) {
			addWork(_i.next());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull public final Collection<T> addAllWorkNoDuplicates(@NonNull @Immutable final Collection<? extends T> c) {
		final Collection<T> _result = new ArrayList<T>();
		final Iterator<? extends T> _i = c.iterator();
		final int _iEnd = c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final T _element = _i.next();

			if (!countingStructure.containsKey(_element)) {
				addWork(_element);
			} else {
				_result.add(_element);
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean addWorkNoDuplicates(@Immutable final T o) {
		final boolean _result = !countingStructure.containsKey(o);

		if (_result) {
			addWork(o);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void clear() {
		container.clear();
		countingStructure.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public final boolean hasWork() {
		return !container.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public String toString() {
		return new ToStringBuilder(this).append("work pieces", container).toString();
	}

	/**
	 * Updates any internal data structure. All subclass implementation of <code>addWork</code> should call this with the
	 * object that was added to the container.
	 * 
	 * @param o is the element that was added.
	 */
	protected final void updateInternal(@Immutable final T o) {
		if (countingStructure.contains(o)) {
			countingStructure.increment(o);
		} else {
			countingStructure.put(o, 1);
		}
	}
}

// End of File
