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
 * A piece of work that can be processed by <code>WorkList</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T> is the type of the token set object.
 */
public abstract class AbstractTokenProcessingWork<T extends ITokens<T, ?>>
		implements ITokenProcessingWork<T> {

	/**
	 * The collection of values to be processed.
	 */
	protected T tokens;

	/**
	 * Creates a new AbstractTokenProcessingWork object.
	 * 
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 * @pre tokenSet != null
	 */
	protected AbstractTokenProcessingWork(final T tokenSet) {
		tokens = tokenSet;
	}

	/**
	 * Adds a collection of values to the collection of values associated with this work.
	 * 
	 * @param tokensToBeProcessed the collection of values to be added for processing.
	 * @pre valuesToBeProcessed != null
	 */
	public final void addTokens(final T tokensToBeProcessed) {
		tokens.addTokens(tokensToBeProcessed);
	}
}

// End of File
