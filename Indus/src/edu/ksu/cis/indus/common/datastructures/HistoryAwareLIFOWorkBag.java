
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

import java.util.Collection;


/**
 * This is a Last-in-First-out implementation of the workbag that can remember previous work pieces put into it.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class HistoryAwareLIFOWorkBag
  extends AbstractHistoryAwareWorkBag {
	/**
	 * Creates a new LIFOWorkBag object.
	 *
	 * @param processed is the collection to be used to remember work pieces put into the bag.  Refer to
	 * 		  <code>AbstractHistoryAwareWorkBag#AbstractHistoryAwareWorkBag(Collection)</code>.
	 */
	public HistoryAwareLIFOWorkBag(final Collection processed) {
		super(processed);
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.AbstractHistoryAwareWorkBag#subAddWork(java.lang.Object)
	 */
	protected void subAddWork(final Object o) {
		container.add(0, o);
		updateInternal(o);
	}
}

// End of File
