
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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class LIFOWorkBag
  extends FIFOWorkBag {
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.WorkBag#addAllWork(java.util.Collection)
	 */
	public void addAllWork(Collection c) {
		container.addAll(0, c);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.WorkBag#addAllWorkNoDuplicates(java.util.Collection)
	 */
	public void addAllWorkNoDuplicates(Collection c) {
		container.addAll(0, CollectionUtils.subtract(c, container));
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.WorkBag#addWork(java.lang.Object)
	 */
	public void addWork(Object o) {
		container.add(0, o);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.WorkBag#addWorkNoDuplicates(java.lang.Object)
	 */
	public void addWorkNoDuplicates(Object o) {
		if (!container.contains(o)) {
			container.add(0, o);
		}
	}
}

/*
   ChangeLog:
   $Log$
 */
