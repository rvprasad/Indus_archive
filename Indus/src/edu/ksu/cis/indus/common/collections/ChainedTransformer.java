/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <I1> DOCUMENT ME!
 * @param <O1> DOCUMENT ME!
 * @param <O2> DOCUMENT ME!
 */
public class ChainedTransformer<I1, O1, O2>
		implements ITransformer<I1, O2> {

	/**
	 * DOCUMENT ME!
	 */
	private ITransformer<I1, O1> transformer1;

	/**
	 * DOCUMENT ME!
	 */
	private ITransformer<O1, O2> transformer2;

	/**
	 * Creates an instance of this class.
	 */
	public ChainedTransformer(final ITransformer<I1, O1> t1, final ITransformer<O1, O2> t2) {
		super();
		transformer1 = t1;
		transformer2 = t2;
	}

	/**
	 * @see edu.ksu.cis.indus.common.collections.ITransformer#transform(java.lang.Object)
	 */
	public O2 transform(final I1 input) {
		return transformer2.transform(transformer1.transform(input));
	}
}

// End of File
