
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;

import edu.ksu.cis.indus.processing.Context;

import java.util.HashSet;
import java.util.Set;


/**
 * This class encapsulates the index creation logic.  It is abstract and it provides an interface through which new indices
 * can be obtained.  The sub classes should provide the logic for the actual creation of the indices.
 * 
 * <p>
 * Created: Tue Jan 22 04:54:38 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractIndexManager
  implements IPrototype {
	/** 
	 * The collection of indices managed by this object.
	 *
	 * @invariant indices != null
	 */
	protected final Set indices = new HashSet();

	/**
	 * This operation is unsupported.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * This operation is unsupported.
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, a new index
	 * is created and returned.
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context.
	 *
	 * @pre o != null and c != null
	 * @post result != null
	 */
	protected abstract IIndex getIndex(final Object o, final Context c);

	/**
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, it returns
	 * <code>null</code>.
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context, if one exists; <code>null</code> otherwise.
	 *
	 * @pre o != null and c != null
	 */
	final IIndex queryIndex(final Object o, final Context c) {
		final IIndex _temp = getIndex(o, c);

		if (!indices.contains(_temp)) {
			indices.add(_temp);
		}

		return _temp;
	}

	/**
	 * Reset the manager.  Flush all the internal data structures to enable a new session.
	 */
	void reset() {
		indices.clear();
	}
}

// End of File
