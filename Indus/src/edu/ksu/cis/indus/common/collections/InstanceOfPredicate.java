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
 */package edu.ksu.cis.indus.common.collections;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> DOCUMENT ME!
 * @param <V> DOCUMENT ME!
 */
public class InstanceOfPredicate<T, V>
		implements IPredicate<V> {

	/**
	 * DOCUMENT ME!
	 */
	private Class<T> clazz;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param instanceClass DOCUMENT ME!
	 */
	public InstanceOfPredicate(final Class<T> instanceClass) {
		super();
		clazz = instanceClass;
	}

	/**
	 * @see edu.ksu.cis.indus.common.collections.IPredicate#evaluate(Object)
	 */
	public boolean evaluate(V t) {
		return clazz.isInstance(t);
	}

}

// End of File
