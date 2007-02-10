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

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractTokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.Value;
import soot.ValueBox;

/**
 * This class encapsulates the logic and data related to work to be done in correspondence to access expressions.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T> is the type of the token set object.
 */
abstract class AbstractAccessExprWork<T extends ITokens<T, Value>>
		extends AbstractTokenProcessingWork<T> {

	/**
	 * The context in which the access occurs.
	 *
	 * @invariant context != null
	 */
	protected final Context context;

	/**
	 * The method in which the access occurs.
	 *
	 * @invariant caller != null
	 */
	protected final IMethodVariant<OFAFGNode<T>> caller;

	/**
	 * The program point at which the entity occurs.
	 *
	 * @invariant accessExprBox != null
	 */
	protected final ValueBox accessExprBox;

	/**
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access expression occurs.
	 * @param accessContext the context in which the access occurs.
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 * @pre callerMethod != null and accessContext != null and tokenSet != null
	 */
	protected AbstractAccessExprWork(final IMethodVariant<OFAFGNode<T>> callerMethod, final Context accessContext,
			final T tokenSet) {
		super(tokenSet);
		accessExprBox = accessContext.getProgramPoint();
		caller = callerMethod;
		context = accessContext.clone();
	}
}

// End of File
