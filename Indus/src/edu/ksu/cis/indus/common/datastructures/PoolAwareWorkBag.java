
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.interfaces.IPoolable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * This is a object pool aware work bag.  All duplicate objects that are poolable are returned to the pool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class PoolAwareWorkBag
  implements IWorkBag {
	/** 
	 * The container that actual contains the work peices.
	 */
	private final IWorkBag container;

	/**
	 * Creates a new PoolAwareWorkBag object.
	 *
	 * @param theContainer is the container that actually contains the work peices.
	 *
	 * @pre theContainer != null
	 */
	public PoolAwareWorkBag(final IWorkBag theContainer) {
		container = theContainer;
	}

	/**
	 * @see IWorkBag#getWork()
	 */
	public Object getWork() {
		return container.getWork();
	}

	/**
	 * @see IWorkBag#addAllWork(Collection)
	 */
	public void addAllWork(final Collection c) {
		container.addAllWork(c);
	}

	/**
	 * Adds the given collection of work to the bag. Duplicate work peices are returned to the pool.
	 *
	 * @param c is the collection of poolable objects.
	 *
	 * @return empty collection
	 *
	 * @pre c != null
	 * @post result != null and result.size() == 0
	 */
	public Collection addAllWorkNoDuplicates(final Collection c) {
		final Collection _coll = container.addAllWorkNoDuplicates(c);

		for (final Iterator _i = _coll.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (_o instanceof IPoolable) {
				final IPoolable _poolable = (IPoolable) _o;
				_poolable.returnToPool();
			}
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see IWorkBag#addWork(Object)
	 */
	public void addWork(final Object o) {
		container.addWork(o);
	}

	/**
	 * Adds the given work to the bag. If it is a duplicate work peice, it is returned to the pool.
	 *
	 * @param o is the work peice.
	 *
	 * @return <code>true</code> if the work peice was added to the work bag; <code>false</code>, otherwise.  In the latter
	 * 		   case the work peice is returned to he pool.
	 *
	 * @pre o != null
	 */
	public boolean addWorkNoDuplicates(final Object o) {
		final boolean _result = container.addWorkNoDuplicates(o);

		if (!_result && o instanceof IPoolable) {
			((IPoolable) o).returnToPool();
		}
		return _result;
	}

	/**
	 * @see IWorkBag#clear()
	 */
	public void clear() {
		container.clear();
	}

	/**
	 * @see IWorkBag#hasWork()
	 */
	public boolean hasWork() {
		return container.hasWork();
	}
}

// End of File
