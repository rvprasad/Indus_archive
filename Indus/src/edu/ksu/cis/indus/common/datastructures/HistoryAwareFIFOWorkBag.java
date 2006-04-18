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

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is a First-in-First-out implementation of the workbag that can remember previous work pieces put into it.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> The type of work handled by this work bag.
 */
public final class HistoryAwareFIFOWorkBag<T>
		extends AbstractHistoryAwareWorkBag<T> {

	/**
	 * Creates a new FIFOWorkBag object.
	 * 
	 * @param processed is the collection to be used to remember work pieces put into the bag. Refer to
	 *            <code>AbstractHistoryAwareWorkBag#AbstractHistoryAwareWorkBag(Collection)</code>.
	 */
	public HistoryAwareFIFOWorkBag(@NonNull final Collection<T> processed) {
		super(processed);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void subAddWork(@NonNull @Immutable final T o) {
		container.add(o);
		updateInternal(o);
	}
}

// End of File
