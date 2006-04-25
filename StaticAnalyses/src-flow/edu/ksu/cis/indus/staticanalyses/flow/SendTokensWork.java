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

import edu.ksu.cis.indus.annotations.InternalUse;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a peice of work to inject a set of tokens into a flow graph node.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <SYM> is the type of symbol whose flow is being analyzed.
 * @param <T> is the type of the token set object.
 * @param <N> is the type of the summary node in the flow analysis.
 */
@InternalUse public class SendTokensWork<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>>
		extends AbstractTokenProcessingWork<T> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SendTokensWork.class);

	/**
	 * The flow graph node associated with this work.
	 */
	private N node;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param associatedNode is the desitination node on which this work operates.
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 */
	public SendTokensWork(final N associatedNode, final T tokenSet) {
		super(tokenSet);
		node = associatedNode;
	}

	/**
	 * Injects the tokens into the associated node.
	 */
	public final void execute() {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("execute() - Propagating tokens - " + tokens.getValues() + " into node " + node);
		}

		node.injectTokens(tokens);

		if (node instanceof AbstractFGNode) {
			((AbstractFGNode) node).forgetSendTokensWork();
		}

		tokens.clear();
	}
}

// End of File
