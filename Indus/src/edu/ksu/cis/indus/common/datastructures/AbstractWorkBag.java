
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

package edu.ksu.cis.indus.common.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;


import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * This is an abstract implementation of <code>IWorkBag</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractWorkBag
  implements IWorkBag {
	/**
	 * This contains the work pieces put into the work bag.
	 */
	protected List container = new ArrayList();

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#getWork()
	 */
	public final Object getWork() {
		if (container.isEmpty()) {
			throw new IllegalStateException("The workbag is empty.");
		}
		return container.remove(0);
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addAllWork(java.util.Collection)
	 */
	public final void addAllWork(final Collection c) {
		for (final Iterator _i = c.iterator(); _i.hasNext();) {
			addWork(_i.next());
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addAllWorkNoDuplicates(java.util.Collection)
	 */
	public final Collection addAllWorkNoDuplicates(final Collection c) {
		final Collection _result = CollectionUtils.intersection(c, container);
		addAllWork(CollectionUtils.subtract(c, container));
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addWorkNoDuplicates(java.lang.Object)
	 */
	public final boolean addWorkNoDuplicates(final Object o) {
		final boolean _result = !container.contains(o);

		if (_result) {
			addWork(o);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#clear()
	 */
	public final void clear() {
		container.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#hasWork()
	 */
	public final boolean hasWork() {
		return !container.isEmpty();
	}
    
	/**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this).append("work pieces", container).toString();
    }
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.

   Revision 1.2  2004/01/28 00:16:55  venku
   - getWork() throws exception if the work bag is empty.
   Revision 1.1  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.1  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.8  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/12/01 13:42:02  venku
   - added support to provide information about which work peices
     were added to the bag and which weren't.
   Revision 1.6  2003/11/16 19:11:57  venku
   - documentation.
   Revision 1.5  2003/11/16 19:09:42  venku
   - documentation.
   Revision 1.4  2003/11/06 06:50:53  venku
   - subtle error of using get() instead of remove() on
     the container.
   Revision 1.3  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.2  2003/11/06 05:01:57  venku
   - finalized the parameters.
   Revision 1.1  2003/11/05 09:27:10  venku
   - Split IWorkBag into an interface and an implementation
     for the sake of performance.
 */
