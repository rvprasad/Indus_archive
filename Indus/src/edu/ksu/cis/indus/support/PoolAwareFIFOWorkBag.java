
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

package edu.ksu.cis.indus.support;

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
public final class PoolAwareFIFOWorkBag
  extends FIFOWorkBag {
	/**
	 * Adds the given collection of work to the bag. Duplicate work peices are returned to the pool.
	 *
	 * @param c is the collection of poolable objects.
	 *
	 * @return empty collection
	 *
	 * @pre c != null and c->forall(o | o.oclIsKindOf(IPoolable))
	 * @post result != null and result.size() == 0
	 */
	public Collection addAllWorkNoDuplicates(final Collection c) {
		final Collection _coll = super.addAllWorkNoDuplicates(c);

		for (final Iterator _i = _coll.iterator(); _i.hasNext();) {
			final IPoolable _poolable = (IPoolable) _i.next();
			_poolable.returnToPool();
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * Adds the given work to the bag. If it is a duplicate work peice, it is returned to the pool.
	 *
	 * @param o is the work peice.
	 *
	 * @return <code>true</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(IPoolable)
	 * @post result == true
	 */
	public boolean addWorkNoDuplicates(final Object o) {
		if (!super.addWorkNoDuplicates(o)) {
			((IPoolable) o).returnToPool();
		}
		return true;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/04 09:43:13  venku
   - extended FIFOWorkBag to return poolable objects to their pool.

 */
