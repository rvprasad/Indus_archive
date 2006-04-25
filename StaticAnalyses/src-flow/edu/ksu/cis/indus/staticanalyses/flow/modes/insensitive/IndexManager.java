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

package edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive;

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;

/**
 * This class implements insensitive index manager. In simple words, it generates indices such that entities can be
 * differentiated solely on their credentials and not on any other auxiliary information such as program point or call stack.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <E> is the type of the entity that has been indexed.
 */
public class IndexManager<E>
		extends AbstractIndexManager<IndexManager.DummyIndex<E>, E>
		implements IPrototype<IndexManager<E>> {

	/**
	 * This class represents an index that identifies an entity independent of any context information..
	 * 
	 * @param <E> is the type of the entity that has been indexed.
	 */
	private static class DummyIndex<E>
			implements IIndex<DummyIndex<E>> {

		/**
		 * The entity that this index identifies.
		 */
		E object;

		/**
		 * Creates a new <code>DummyIndex</code> instance.
		 * 
		 * @param o the entity being identified by this index.
		 */
		DummyIndex(final E o) {
			this.object = o;
		}

		/**
		 * Compares if the given object is the same as this object.
		 * 
		 * @param o the object to be compared with.
		 * @return <code>true</code> if <code>object</code> is the same as this object; <code>false</code> otherwise.
		 */
		@Override public boolean equals(final Object o) {
			boolean _result = o == this;

			if (!_result && o != null && o instanceof DummyIndex) {
				final DummyIndex<?> _di = (DummyIndex) o;
				_result = (object == _di.object) || ((object != null) && object.equals(_di.object));
			}
			return _result;
		}

		/**
		 * Returns the hash code for this object.
		 * 
		 * @return returns the hash code for this object.
		 */
		@Override public int hashCode() {
			final int _result;

			if (object != null) {
				_result = 37 * 17 + object.hashCode();
			} else {
				_result = 17;
			}

			return _result;
		}

		/**
		 * Returns the stringized representation of this object.
		 * 
		 * @return the stringized representation of this object.
		 */
		@Override public String toString() {
			return object.toString();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IndexManager<E> getClone(@SuppressWarnings("unused") Object... o) {
		return new IndexManager<E>();
	}

	/**
	 * Returns an index corresponding to the given entity.
	 * 
	 * @param o the entity for which the index in required.
	 * @param c <i>ignored</i>..
	 * @return the index that uniquely identifies <code>o</code>.
	 */
	@Override protected DummyIndex<E> createIndex(final E o, @SuppressWarnings("unused") final Context c) {
		return new DummyIndex<E>(o);
	}
}

// End of File
