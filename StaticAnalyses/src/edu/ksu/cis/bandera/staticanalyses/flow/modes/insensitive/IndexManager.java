package edu.ksu.cis.bandera.bfa.modes.insensitive;


import edu.ksu.cis.bandera.bfa.AbstractIndexManager;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.Index;

import org.apache.log4j.Logger;

/**
 * IndexManager.java
 *
 *
 * Created: Fri Jan 25 13:11:19 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class IndexManager extends AbstractIndexManager {

	private static final Logger logger = Logger.getLogger(IndexManager.class.getName());

	protected Index getIndex(Object o, Context c) {
		return new DummyIndex(o);
	}

	public Object prototype() {
		return new IndexManager();
	}

	class DummyIndex implements Index {
		Object o;

		DummyIndex(Object o) {
			this.o = o;
		}

		public boolean equals(Object o) {
			return hashCode() == o.hashCode();
		}

		public int hashCode() {
			return o.hashCode();
		}

		public String toString() {
			return o.toString();
		}
	}

}// IndexManager
