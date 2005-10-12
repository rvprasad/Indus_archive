
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;


/**
 * A piece of work that can be processed by <code>WorkList</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractTokenProcessingWork
  implements ITokenProcessingWork {
	/** 
	 * The collection of values to be processed.
	 */
	protected ITokens tokens;

	/**
	 * Creates a new AbstractTokenProcessingWork object.
	 *
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 *
	 * @pre tokenSet != null
	 */
	protected AbstractTokenProcessingWork(final ITokens tokenSet) {
		tokens = tokenSet;
	}

	/**
	 * Adds a collection of values to the collection of values associated with this work.
	 *
	 * @param tokensToBeProcessed the collection of values to be added for processing.
	 *
	 * @pre valuesToBeProcessed != null
	 */
	public final void addTokens(final ITokens tokensToBeProcessed) {
		tokens.addTokens(tokensToBeProcessed);
	}
}

// End of File
