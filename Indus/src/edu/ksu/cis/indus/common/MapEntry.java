
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

import java.util.Map.Entry;


/**
 * This is a <code>Map.Entry</code> implementation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class MapEntry
  implements Entry {
	/**
	 * The key.
	 */
	private final Object key;

	/**
	 * The value;
	 */
	private final Object value;

	/**
	 * The hash code.
	 */
	private int hashcode;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theKey is the key.
	 * @param theValue is the value.
	 */
	public MapEntry(final Object theKey, final Object theValue) {
		key = theKey;
		value = theValue;
		hashcode = 0;
	}

	/**
	 * @see java.util.Map.Entry#getKey()
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * @see java.util.Map.Entry#setValue(java.lang.Object)
	 */
	public Object setValue(final Object newValue) {
		throw new UnsupportedOperationException("setValue() is not supported.");
	}

	/**
	 * @see java.util.Map.Entry#getValue()
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj) {
		boolean _result;

		if (obj instanceof MapEntry) {
			final MapEntry _o = (MapEntry) obj;

			if (key != null && _o.key != null) {
				_result = key.equals(_o.key);
			} else {
				_result = _o.key == key;
			}

			if (value != null && _o.value != null) {
				_result &= value.equals(_o.value);
			} else {
				_result &= value == _o.value;
			}
		} else {
			_result = super.equals(obj);
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hashcode == 0) {
			int hashCode = 17;

			if (key != null) {
				hashCode += 37 * hashCode + key.hashCode();
			}

			if (value != null) {
				hashCode += 37 * hashCode + value.hashCode();
			}
		}
		return hashcode;
	}
}

/*
   ChangeLog:
   $Log$
 */
