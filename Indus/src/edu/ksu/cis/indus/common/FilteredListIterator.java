
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

package edu.ksu.cis.indus.common;

import java.util.ListIterator;

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.iterators.AbstractListIteratorDecorator;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FilteredListIterator
  extends AbstractListIteratorDecorator
  implements ListIterator {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Predicate predicate;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Object nextElement;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Object prevElement;

	/**
	 * DOCUMENT ME!
	 *
	 * @param theIterator DOCUMENT ME!
	 * @param thePredicate DOCUMENT ME!
	 */
	public FilteredListIterator(final ListIterator theIterator, final Predicate thePredicate) {
		super(theIterator);
		predicate = thePredicate;
	}

	/**
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	public void add(final Object object) {
		if (predicate.evaluate(object)) {
			super.add(object);
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		boolean _result = false;

		while (super.hasNext()) {
			nextElement = iterator.next();

			if (predicate.evaluate(nextElement)) {
				_result = true;
				iterator.previous();
				break;
			}
		}
		return _result;
	}

	/**
	 * @see java.util.ListIterator#hasPrevious()
	 */
	public boolean hasPrevious() {
		boolean _result = false;

		while (super.hasPrevious()) {
			nextElement = iterator.previous();

			if (predicate.evaluate(nextElement)) {
				_result = true;
				iterator.next();
				break;
			}
		}
		return _result;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		do {
			nextElement = super.next();
		} while (!predicate.evaluate(nextElement));
		return nextElement;
	}

	/**
	 * @see java.util.ListIterator#previous()
	 */
	public Object previous() {
		do {
			prevElement = super.previous();
		} while (!predicate.evaluate(prevElement));
		return prevElement;
	}

	/**
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	public void set(final Object object) {
		if (predicate.evaluate(object)) {
			super.set(object);
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
