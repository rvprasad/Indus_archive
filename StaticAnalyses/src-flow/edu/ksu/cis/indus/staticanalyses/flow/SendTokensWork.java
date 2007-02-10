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
