
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive;

import edu.ksu.cis.indus.staticanalyses.flow.IIndex;


/**
 * This class represents an index which can be differentiated based on a context of unit size.  We consider any peice of
 * information which can be used to divide summary sets, as context information.  So, a context can be made up of many such
 * peices of information.  This class can encapsulate only one such peice of context information.  For example, an instance
 * can encapsulate either the  calling stack or the program point as the context information, but not both.
 * 
 * <p>
 * Created: Fri Jan 25 13:11:19 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class OneContextInfoIndex
  implements IIndex {
	/** 
	 * The context in which <code>value</code> needs to be differentiated.
	 */
	private final Object contextInfo;

	/** 
	 * This index is used in association with <code>value</code>.  This value is not available for retrieval, but rather adds
	 * to improve the performance of <code>hashCode()</code> and <code>equals(Object)</code>.
	 */
	private final Object value;

	/**
	 * Creates a new <code>OneContextInfoIndex</code> instance.
	 *
	 * @param v the value whose variant is identified by this index.
	 * @param c the context in which <code>value</code>'s variant is identified by this index.
	 */
	public OneContextInfoIndex(final Object v, final Object c) {
		this.value = v;
		this.contextInfo = c;
	}

	/**
	 * Compares this index with a given index.  The objects are equal when the <code>value</code> and <code>context</code>
	 * are equal.
	 *
	 * @param index the index to be compared with.
	 *
	 * @return <code>true</code> if this index is equal to <code>index</code>; <code>false</code> otherwise.
	 */
	public boolean equals(final Object index) {
		boolean _result = index == this;

		if (!_result && index != null && index instanceof OneContextInfoIndex) {
			final OneContextInfoIndex _d = (OneContextInfoIndex) index;

			if (value != null) {
				_result = value.equals(_d.value);
			} else {
				_result = value == _d.value;
			}

			if (_result) {
				if (contextInfo != null) {
					_result = contextInfo.equals(_d.contextInfo);
				} else {
					_result = contextInfo == _d.contextInfo;
				}
			}
		}
		return _result;
	}

	/**
	 * Generates a hash code for this object.
	 *
	 * @return the hash code for this object.
	 */
	public int hashCode() {
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
	public String toString() {
		return value + " " + contextInfo;
	}
}

// End of File
