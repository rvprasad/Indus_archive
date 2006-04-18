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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This transformer combines two compatible transformers.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
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
	@Functional public O2 transform(final I1 input) {
		return transformer2.transform(transformer1.transform(input));
	}
}

// End of File
