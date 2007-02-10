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
package edu.ksu.cis.indus.staticanalyses.dependency;

import java.util.Collection;

/**
 * This is an abstract implementation of dependence retriever.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T1> is the type of the dependent in dependent to dependee query.
 * @param <C1> is the type of the context in dependent to dependee query.
 * @param <E1> is the type of the dependee in dependent to dependee query.
 * @param <E2> is the type of the dependee in dependee to dependent query.
 * @param <C2> is the type of the context in dependee to dependent query.
 * @param <T2> is the type of the dependent in dependee to dependent query.
 */
public abstract class AbstractDependenceRetriever<T1, C1, E1, E2, C2, T2>
		implements IDependenceRetriever<T1, C1, E1, E2, C2, T2> {

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDependenceRetriever#getDependees(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis,
	 *      java.lang.Object, java.lang.Object)
	 */
	public Collection<E1> getDependees(final IDependencyAnalysis<T1, C1, E1, E2, C2, T2> da, final T1 base, final C1 context) {
		return da.getDependees(base, context);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDependenceRetriever#getDependents(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis,
	 *      java.lang.Object, java.lang.Object)
	 */
	public Collection<T2> getDependents(final IDependencyAnalysis<T1, C1, E1, E2, C2, T2> da, final E2 base, final C2 context) {
		return da.getDependents(base, context);
	}
}

// End of File
