
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

package edu.ksu.cis.indus.staticanalyses.support;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;


/**
 * This is a Last-in-First-out implementation of the workbag.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class LIFOWorkBag
  extends FIFOWorkBag {
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.IWorkBag#addAllWork(java.util.Collection)
	 */
	public void addAllWork(final Collection c) {
		container.addAll(0, c);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.IWorkBag#addAllWorkNoDuplicates(java.util.Collection)
	 */
	public Collection addAllWorkNoDuplicates(final Collection c) {
		Collection result = CollectionUtils.intersection(c, container);
		container.addAll(0, CollectionUtils.subtract(c, container));
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.IWorkBag#addWork(java.lang.Object)
	 */
	public void addWork(final Object o) {
		container.add(0, o);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.IWorkBag#addWorkNoDuplicates(java.lang.Object)
	 */
	public boolean addWorkNoDuplicates(final Object o) {
		boolean result = !container.contains(o);

		if (result) {
			container.add(0, o);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/11/16 19:09:42  venku
   - documentation.
   Revision 1.3  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.2  2003/11/06 05:01:57  venku
   - finalized the parameters.
   Revision 1.1  2003/11/05 09:27:10  venku
   - Split IWorkBag into an interface and an implementation
     for the sake of performance.
 */
