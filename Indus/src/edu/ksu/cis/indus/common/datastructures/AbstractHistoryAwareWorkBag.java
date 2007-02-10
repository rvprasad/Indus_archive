/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is an abstract implementation of <code>IWorkBag</code> with the ability to remember about processing of work pieces.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> The type of work handled by this work bag.
 */
public abstract class AbstractHistoryAwareWorkBag<T>
		extends AbstractWorkBag<T> {

	/**
	 * The container that remembers the items that were processed by this work bag.
	 */
	@NonNull private final Collection<T> processedWorkPieces;

	/**
	 * Creates a new AbstractWorkBag object. If a non-null collection is provided via <code>processed</code>, then a work
	 * piece will be processed only once even if it is added multiple times.
	 * 
	 * @param processed If it is non-null, this collection is used to remember the work pieces added to this work bag.
	 */
	protected AbstractHistoryAwareWorkBag(@NonNull final Collection<T> processed) {
		processedWorkPieces = processed;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addWork(@Immutable final T o) {
		if (processedWorkPieces != null) {
			if (!processedWorkPieces.contains(o)) {
				processedWorkPieces.add(o);
				subAddWork(o);
			}
		} else {
			subAddWork(o);
		}
	}

	/**
	 * Subclasses should add the work to the container by overriding this method.
	 * 
	 * @param o is the object to be added.
	 */
	protected abstract void subAddWork(final T o);
}

// End of File
