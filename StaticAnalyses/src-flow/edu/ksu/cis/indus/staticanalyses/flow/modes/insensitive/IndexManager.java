
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

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;


/**
 * This class implements insensitive index manager.  In simple words, it generates indices such that entities can be
 * differentiated solely on their credentials and not on any other auxiliary information such as program point or call
 * stack.
 * 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <O> DOCUMENT ME!
 */
public class IndexManager<O>
  extends AbstractIndexManager<IndexManager.DummyIndex<O>, O> {
	/**
	 * This class represents an index that identifies an entity independent of any context information..
	 */
	private static class DummyIndex<O>
	  implements IIndex<DummyIndex<O>> {
		/** 
		 * The entity that this index identifies.
		 */
		O object;

		/**
		 * Creates a new <code>DummyIndex</code> instance.
		 *
		 * @param o the entity being identified by this index.
		 */
		DummyIndex(final O o) {
			this.object = o;
		}

		/**
		 * Compares if the given object is the same as this object.
		 *
		 * @param o the object to be compared with.
		 *
		 * @return <code>true</code> if <code>object</code> is the same as this object; <code>false</code> otherwise.
		 */
		@Override public boolean equals(final Object o) {
			boolean _result = o == this;

			if (!_result && o != null && o instanceof DummyIndex) {
				final DummyIndex _di = (DummyIndex) o;
				_result = (this == o) || (object == _di.object) || ((object != null) && object.equals(_di.object));
			}
			return _result;
		}

		/**
		 * Returns the hash code for this object.
		 *
		 * @return returns the hash code for this object.
		 */
		@Override public int hashCode() {
			int _result = 17;

			if (object != null) {
				_result = 37 * _result * object.hashCode();
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
	 * This method throws an <code>UnsupportedOperationException</code> exception.
	 *
	 * @param o <i>ignored</i>.
	 *
	 * @return (This method throws an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported.
	 */
	@Override public IndexManager getClone(final Object... o) {
		throw new UnsupportedOperationException("Single parameter prototype() is not supported.");
	}

	/**
	 * Returns an index corresponding to the given entity.
	 *
	 * @param o the entity for which the index in required.
	 * @param c <i>ignored</i>..
	 *
	 * @return the index that uniquely identifies <code>o</code>.
	 */
	@Override protected DummyIndex<O> createIndex(final O o, final Context c) {
		return new DummyIndex<O>(o);
	}
}

// End of File
