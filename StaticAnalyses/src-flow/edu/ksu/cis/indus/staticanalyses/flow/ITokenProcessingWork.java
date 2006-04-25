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

import edu.ksu.cis.indus.common.datastructures.IWork;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

/**
 * This interface is provided by a work piece that processes tokens.
 * 
 * @version $Revision$
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @param <T> is the type of the token set object.
 */
public interface ITokenProcessingWork<T extends ITokens<T, ?>>
		extends IWork {

	/**
	 * Adds a collection of values to the collection of values associated with this work.
	 * 
	 * @param tokensToBeProcessed the collection of values to be added for processing.
	 * @pre valuesToBeProcessed != null
	 */
	void addTokens(final T tokensToBeProcessed);
}

// End of File
