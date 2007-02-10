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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This transformer combines two compatible transformers.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <I1> the input type of parameter to the first transformer.
 * @param <O1> the output type of the result of the first transformer and the input type of the parameter to the second
 *            transformer.
 * @param <O2> the output type of the result of the second transformer.
 */
public class ChainedTransformer<I1, O1, O2>
		implements ITransformer<I1, O2> {

	/**
	 * The transformer to be applied first.
	 */
	@NonNull private final ITransformer<I1, O1> transformer1;

	/**
	 * The transformer to be applied second.
	 */
	@NonNull private final ITransformer<O1, O2> transformer2;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param t1 is the transformer to be applied first.
	 * @param t2 is the transformer to be applied second.
	 */
	public ChainedTransformer(@NonNull @Immutable final ITransformer<I1, O1> t1,
			@NonNull @Immutable final ITransformer<O1, O2> t2) {
		super();
		transformer1 = t1;
		transformer2 = t2;
	}

	/**
	 * {@inheritDoc}
	 */
	public O2 transform(final I1 input) {
		return transformer2.transform(transformer1.transform(input));
	}
}

// End of File
