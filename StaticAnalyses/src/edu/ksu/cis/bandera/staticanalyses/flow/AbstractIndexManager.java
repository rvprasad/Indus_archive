package edu.ksu.cis.bandera.staticanalyses.flow;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//AbstractIndexManager.java
/**
 * <p>This class encapsulates the index creation logic.  It is abstract and it provides an interface through which new indices
 * can be obtained.  The sub classes should provide the logic for the actual creation of the indices.</p>
 *
 * <p>Created: Tue Jan 22 04:54:38 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractIndexManager implements Prototype {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractIndexManager.class);

	/**
	 * <p>The collection of indices managed by this object.</p>
	 *
	 */
	protected Set indices = new HashSet();

	/**
	 * <p>Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, it returns
	 * <code>null</code>.</p>
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 * @return the index corresponding to the entity in the given context, if one exists; <code>null</code> otherwise.
	 */
	final Index queryIndex(Object o, Context c) {
		Index temp = getIndex(o, c);
		if (!indices.contains(temp)) {
			indices.add(temp);
		} // end of if (sm2indices.containsKey(sm)) else
		return temp;
	}

	/**
	 * <p>Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, a new index
	 * is created and returned.</p>
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 * @return the index corresponding to the entity in the given context.
	 */
	protected abstract Index getIndex(Object o, Context c);

	/**
	 * <p>Reset the manager.  Flush all the internal data structures to enable a new session.</p>
	 *
	 */
	void reset() {
		indices.clear();
	}

	/**
	 * <p>This operation is unsupported.</p>
	 *
	 * @throws <code>UnsupportedException</code> if the operation is not supported.
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * <p>This operation is unsupported.</p>
	 *
	 * @throws <code>UnsupportedException</code> if the operation is not supported.
	 */
	public Object prototype(Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

}// AbstractIndexManager
