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

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <E> DOCUMENT ME!
 */
public final class FixedSizeSet<E>
		extends ListOrderedSet<E> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -4466214306965769464L;

	/**
	 * DOCUMENT ME!
	 */
	private final int maximumCapacity;

	/** 
	 * @see java.util.HashSet#add(java.lang.Object)
	 */
	@Override public boolean add(E o) {
		final boolean _result = super.add(o);
		if (_result && size() > maximumCapacity) {
			remove(0);
		}
		return _result;
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param c DOCUMENT ME!
	 * @param maxCapacity DOCUMENT ME!
	 */
	public FixedSizeSet(final Collection<E> c, final int maxCapacity) {
		this(maxCapacity);
		addAll(c);
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param loadFactor DOCUMENT ME! 
	 * @param maxCapacity DOCUMENT ME!
	 */
	public FixedSizeSet(final float loadFactor, final int maxCapacity) {
		super(maxCapacity, loadFactor);
		maximumCapacity = maxCapacity;
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param maxCapacity DOCUMENT ME!
	 */
	public FixedSizeSet(final int maxCapacity) {
		super(maxCapacity);
		maximumCapacity = maxCapacity;
	}

}

// End of File
