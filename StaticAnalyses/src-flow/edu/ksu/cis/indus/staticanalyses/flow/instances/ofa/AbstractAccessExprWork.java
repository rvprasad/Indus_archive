
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractTokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.ValueBox;


/**
 * This class encapsulates the logic and data related to work to be done in correspondence to access expressions.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
abstract class AbstractAccessExprWork
  extends AbstractTokenProcessingWork {
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
	protected final IMethodVariant caller;

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
	 *
	 * @pre callerMethod != null and accessContext != null and tokenSet != null
	 */
	protected AbstractAccessExprWork(final IMethodVariant callerMethod, final Context accessContext, final ITokens tokenSet) {
		super(tokenSet);
		accessExprBox = accessContext.getProgramPoint();
		caller = callerMethod;
		context = (Context) accessContext.clone();
	}
}

// End of File
