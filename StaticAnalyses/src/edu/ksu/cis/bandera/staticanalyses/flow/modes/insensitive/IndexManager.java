package edu.ksu.cis.bandera.bfa.modes.insensitive;


import edu.ksu.cis.bandera.bfa.AbstractIndexManager;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.Index;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// IndexManager.java
/**
 * <p>This class implements insensitive index manager.  In simple words, it generates indices such that entities can be
 * differentiated solely on their credentials and not on any other auxiliary information such as program point or call
 * stack.</p>
 *
 * Created: Fri Jan 25 13:11:19 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class IndexManager extends AbstractIndexManager {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(IndexManager.class.getName());

	/**
	 * <p>Returns an index corresponding to the given entity.</p>
	 *
	 * @param o the entity for which the index in required.
	 * @param c this parameter is ignored.  This can be <code>null</code>.
	 * @return the index that uniquely identifies <code>o</code>.
	 */
	protected Index getIndex(Object o, Context c) {
		return new DummyIndex(o);
	}

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @return a new instance of this class.
	 */
	public Object prototype() {
		return new IndexManager();
	}

	/**
	 * <p>This method throws an <code>UnsupportedOperationException</code> exception.</p>
	 *
	 * @param o (This parameter is ignored.)
	 * @return (This method throws an exception.)
	 */
	public Object prototype(Object o) {
		throw new UnsupportedOperationException("Single parameter prototype() is not supported.");
	}

	/**
	 * <p>This class represents an index that identifies an entity independent of any context information..</p>
	 *
	 */
	class DummyIndex implements Index {
		/**
		 * <p>The entity that this index identifies.</p>
		 *
		 */
		Object o;

		/**
		 * <p>Creates a new <code>DummyIndex</code> instance.</p>
		 *
		 * @param o the entity being identified by this index.
		 */
		DummyIndex(Object o) {
			this.o = o;
		}

		/**
		 * <p>Compares if the given object is the same as this object.</p>
		 *
		 * @param o the object to be compared with.
		 * @return <code>true</code> if <code>o</code> is the same as this object; <code>false</code> otherwise.
		 */
		public boolean equals(Object o) {
			return this.o.hashCode() == o.hashCode();
		}

		/**
		 * <p>Returns the hash code for this object.</p>
		 *
		 * @return returns the hash code for this object.
		 */
		public int hashCode() {
			return o.hashCode();
		}

		/**
		 * <p>Returns the stringized representation of this object.</p>
		 *
		 * @return the stringized representation of this object.
		 */
		public String toString() {
			return o.toString();
		}
	}

}// IndexManager
