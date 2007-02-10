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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.ITokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.IWorkBagProvider;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.Value;

/**
 * This class extends the flow graph node by associating a work peice with it.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T>  is the type of the token set object.
 */
class FGAccessNode<T extends ITokens<T, Value>>
		extends OFAFGNode<T> {

	/**
	 * The work associated with this node.
	 * 
	 * @invariant work != null
	 */
	private final ITokenProcessingWork<T> work;

	/**
	 * Creates a new <code>FGAccessNode</code> instance.
	 * 
	 * @param workPeice the work peice associated with this node.
	 * @param provider provides the workbag into which <code>work</code> will be added.
	 * @param tokenManager that manages the tokens used in the enclosing flow analysis.
	 * @pre workPeice != null and provider != null and tokenManager != null
	 */
	public FGAccessNode(final ITokenProcessingWork<T> workPeice, final IWorkBagProvider provider,
			final ITokenManager<T, Value, ?> tokenManager) {
		super(provider, tokenManager);
		this.work = workPeice;
	}

	/**
	 * Adds the given tokens to the work peice for processing.
	 * 
	 * @param newTokens the collection of values that need to be processed at the given node.
	 * @pre newTokens != null
	 */
	@Override protected void onNewTokens(final T newTokens) {
		super.onNewTokens(newTokens);

		if (!newTokens.isEmpty()) {
			work.addTokens(filterTokens(newTokens));
			workbagProvider.getWorkBag().addWork(work);
		}
	}
}

// End of File
