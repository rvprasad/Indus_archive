
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

package edu.ksu.cis.indus.common.datastructures;

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
 */
public abstract class AbstractWorkBag
  implements IWorkBag {
	/** 
	 * This contains the work pieces put into the work bag.
	 */
	protected final List container = new ArrayList();

	/** 
	 * This is a backing structure to maintain element containment information.
	 */
	private final TObjectIntHashMap countingStructure = new TObjectIntHashMap();

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#getWork()
	 */
	public final Object getWork() {
		if (container.isEmpty()) {
			throw new IllegalStateException("The workbag is empty.");
		}

		final Object _result = container.remove(0);
		countingStructure.adjustValue(_result, -1);

		if (countingStructure.get(_result) == 0) {
			countingStructure.remove(_result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addAllWork(java.util.Collection)
	 */
	public final void addAllWork(final Collection c) {
		for (final Iterator _i = c.iterator(); _i.hasNext();) {
			addWork(_i.next());
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addAllWorkNoDuplicates(java.util.Collection)
	 */
	public final Collection addAllWorkNoDuplicates(final Collection c) {
		final Collection _result = new ArrayList();
		final Iterator _i = c.iterator();
		final int _iEnd = c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _element = _i.next();

			if (!countingStructure.containsKey(_element)) {
				addWork(_element);
			} else {
				_result.add(_element);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addWorkNoDuplicates(java.lang.Object)
	 */
	public final boolean addWorkNoDuplicates(final Object o) {
		final boolean _result = !countingStructure.containsKey(o);

		if (_result) {
			addWork(o);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#clear()
	 */
	public final void clear() {
		container.clear();
		countingStructure.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#hasWork()
	 */
	public final boolean hasWork() {
		return !container.isEmpty();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("work pieces", container).toString();
	}

	/**
	 * Updates any internal data structure.  All subclass implementation of <code>addWork</code> should call this with the
	 * object that was added to the container.
	 *
	 * @param o is the element that was added.
	 */
	protected final void updateInternal(final Object o) {
		if (countingStructure.contains(o)) {
			countingStructure.increment(o);
		} else {
			countingStructure.put(o, 1);
		}
	}
}

// End of File
