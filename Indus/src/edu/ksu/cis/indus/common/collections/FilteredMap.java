
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.map.AbstractMapDecorator;


/**
 * This class provides a filtered access to the keys and values in the decorated map.  Like <code>FilteredCollection</code>,
 * all operations are filtered.
 * 
 * <p>
 * To be more precise, an entry in the decorated map occurs in the filtered view when  both key and value satisfy
 * corresponding  predicates.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class FilteredMap
  extends AbstractMapDecorator {
	/** 
	 * The predicate to filter keys.
	 */
	private final Predicate keyPred;

	/** 
	 * The predicate to filter values.
	 */
	private final Predicate valuePred;

	/**
	 * Creates a new FilteredMap object.
	 *
	 * @param decoratedMap to be filtered.
	 * @param keyPredicate to filter the keys in <code>backedMap</code>. <code>null</code> means no check.
	 * @param valuePredicate to filter the values in <code>backedMap</code>. <code>null</code> means no check.
	 *
	 * @throws IllegalArgumentException when <code>keyPredicate</code> and <code>valuePredicate</code> are <code>null</code>.
	 *
	 * @pre decoratedMap != null
	 *
	 * @see AbstractMapDecorator#AbstractMapDecorator(Map)
	 */
	FilteredMap(final Map decoratedMap, final Predicate keyPredicate, final Predicate valuePredicate) {
		super(decoratedMap);

		if (keyPredicate == null && valuePredicate == null) {
			throw new IllegalArgumentException("If both predicates are null, please use an unfiltered map.");
		}
		keyPred = keyPredicate;
		valuePred = valuePredicate;
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		for (final Iterator _i = keySet().iterator(); _i.hasNext();) {
			final Object _key = _i.next();
			final Object _value = super.get(_key);

			if (validateValue(_value)) {
				super.remove(_key);
			}
		}
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(final Object key) {
		return (validateKey(key)) && super.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(final Object value) {
		boolean _result = validateValue(value);

		if (_result) {
			_result = false;

			for (final Iterator _i = keySet().iterator(); _i.hasNext() && !_result;) {
				final Object _key = _i.next();
				_result |= super.get(_key).equals(value);
			}
		}
		return _result;
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet() {
		final Set _result;

		if (valuePred == null && keyPred == null) {
			_result = super.entrySet();
		} else {
			_result =
				new FilteredSet(super.entrySet(),
					new Predicate() {
						public boolean evaluate(final Object object) {
							boolean _r = false;

							if (object instanceof Map.Entry) {
								final Entry _entry = ((Map.Entry) object);

								if (validateKey(_entry.getKey()) && validateValue(_entry.getValue())) {
									_r = true;
								}
							}
							return _r;
						}
					});
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object object) {
		boolean _result = object instanceof Map;
		final Set _otherEntrySet = ((Map) object).entrySet();
		final Set _localEntrySet = entrySet();
		_result = _localEntrySet.size() == _otherEntrySet.size() && _otherEntrySet.containsAll(_localEntrySet);
		return _result;
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(final Object key) {
		Object _result = null;

		if (validateKey(key)) {
			final Object _value = super.get(key);

			if (validateValue(_value)) {
				_result = _value;
			}
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _hashCode = 17;

		for (final Iterator _i = entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_hashCode += 37 * _hashCode + _entry.hashCode();
		}
		return _hashCode;
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set keySet() {
		final Map _map = getMap();
		return new FilteredSet(super.keySet(),
			new Predicate() {
				public boolean evaluate(final Object object) {
					boolean _result = false;

					if (validateKey(object) && validateValue(_map.get(object))) {
						_result = true;
					}
					return _result;
				}
			});
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(final Object key, final Object value) {
		Object _result = null;

		if (validateKey(key) && validateValue(value)) {
			_result = super.put(key, value);
		}
		return _result;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(final Map mapToCopy) {
		for (final Iterator _i = mapToCopy.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			put(_entry.getKey(), _entry.getValue());
		}
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(final Object key) {
		Object _result = null;

		if (validateKey(key)) {
			final Object _value = super.get(key);

			if (validateValue(_value)) {
				_result = super.remove(key);
			}
		}
		return _result;
	}

	/**
	 * @see java.util.Map#size()
	 */
	public int size() {
		return keySet().size();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final Set _entrySet = entrySet();
		final StringBuffer _result = new StringBuffer();
		_result.append("[\n");

		for (final Iterator _i = _entrySet.iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_result.append(_entry.getKey());
			_result.append(" -> ");
			_result.append(_entry.getValue());
			_result.append("\n");
		}
		_result.append("]");
		return _result.toString();
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection values() {
		final Map _map = getMap();

		return new FilteredCollection(super.values(),
			new Predicate() {
				public boolean evaluate(final Object object) {
					boolean _r = false;

					for (final Iterator _i = keySet().iterator(); _i.hasNext() && !_r;) {
						final Object _key = _i.next();

						if (validateKey(_key) && validateValue(object) && _map.get(_key) == object) {
							_r = true;
						}
					}
					return _r;
				}
			});
	}

	/**
	 * Validates the given key by the predicate.
	 *
	 * @param key to be validated.
	 *
	 * @return <code>true</code> if the key is valid by predicate rule; <code>false</code>, otherwise.
	 *
	 * @pre key != null
	 */
	boolean validateKey(final Object key) {
		return (keyPred != null && keyPred.evaluate(key)) || keyPred == null;
	}

	/**
	 * Validates the given value by the predicate.
	 *
	 * @param value to be validated.
	 *
	 * @return <code>true</code> if the value is valid by predicate rule; <code>false</code>, otherwise.
	 *
	 * @pre value != null
	 */
	boolean validateValue(final Object value) {
		return ((valuePred != null && valuePred.evaluate(value)) || valuePred == null);
	}
}

// End of File
