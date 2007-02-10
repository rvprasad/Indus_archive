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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

/**
 * This class manages variants corresponding to entities that have values.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <E> is the type of the entity whose variance is being tracked.!
 * @param <SYM> is the type of symbol whose flow is being analyzed.
 * @param <T> is the type of the token set object.
 * @param <N> is the type of the summary node in the flow analysis.
 * @param <R> is the type of the symbol types.
 */
class ValuedVariantManager<E, SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>, R>
		extends AbstractVariantManager<ValuedVariant<N>, E, SYM, T, N, R> {

	/**
	 * Creates a new <code>ValuedVariantManager</code> instance.
	 * 
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices which map array variants to arrays.
	 * @pre theAnalysis != null and indexManager != null
	 */
	ValuedVariantManager(final FA<SYM, T, N, R> theAnalysis, final IIndexManager<? extends IIndex<?>, E> indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new valued variant corresponding to the given ast object.
	 * 
	 * @param o the ast object whose variant is to be returned.
	 * @return a new variant corresponding to <code>o</code>.
	 */
	@Override protected ValuedVariant<N> getNewVariant(@SuppressWarnings("unused") final E o) {
		return new ValuedVariant<N>(fa.getNewFGNode());
	}
}

// End of File
