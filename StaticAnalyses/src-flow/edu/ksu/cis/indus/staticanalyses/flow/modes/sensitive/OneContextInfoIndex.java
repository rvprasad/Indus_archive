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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive;

import edu.ksu.cis.indus.staticanalyses.flow.IIndex;

/**
 * This class represents an index which can be differentiated based on a context of unit size. We consider any peice of
 * information which can be used to divide summary sets, as context information. So, a context can be made up of many such
 * peices of information. This class can encapsulate only one such peice of context information. For example, an instance can
 * encapsulate either the calling stack or the program point as the context information, but not both.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <C> is the type of the context in which the entity has been indexed.
 * @param <E> is the type of the entity that has been indexed.
 */
public class OneContextInfoIndex<E, C>
		implements IIndex<OneContextInfoIndex<E, C>> {

	/**
	 * The context in which <code>value</code> needs to be differentiated.
	 */
	private final C contextInfo;

	/**
	 * This index is used in association with <code>value</code>. This value is not available for retrieval, but rather
	 * adds to improve the performance of <code>hashCode()</code> and <code>equals(Object)</code>.
	 */
	private final E value;

	/**
	 * Creates a new <code>OneContextInfoIndex</code> instance.
	 *
	 * @param v the value whose variant is identified by this index.
	 * @param c the context in which <code>value</code>'s variant is identified by this index.
	 */
	public OneContextInfoIndex(final E v, final C c) {
		this.value = v;
		this.contextInfo = c;
	}

	/**
	 * Compares this index with a given index. The objects are equal when the <code>value</code> and <code>context</code>
	 * are equal.
	 *
	 * @param index the index to be compared with.
	 * @return <code>true</code> if this index is equal to <code>index</code>; <code>false</code> otherwise.
	 */
	@Override public boolean equals(final Object index) {
		boolean _result = false;

		if (index != null && index instanceof OneContextInfoIndex) {
			final OneContextInfoIndex<?, ?> _d = (OneContextInfoIndex) index;
			_result = (this == index) || ((value == _d.value) || ((value != null) && value.equals(_d.value)))
					&& ((contextInfo == _d.contextInfo) || ((contextInfo != null) && contextInfo.equals(_d.contextInfo)));
		}
		return _result;
	}

	/**
	 * Generates a hash code for this object.
	 *
	 * @return the hash code for this object.
	 */
	@Override public int hashCode() {
		int _result = 17;

		if (value != null) {
			_result = 37 * _result + value.hashCode();
		}

		if (contextInfo != null) {
			_result = 37 * _result + contextInfo.hashCode();
		}
		return _result;
	}

	/**
	 * Returns the stringized form of this object.
	 *
	 * @return returns the stringized form of this object.
	 */
	@Override public String toString() {
		return value + " " + contextInfo;
	}
}

// End of File
