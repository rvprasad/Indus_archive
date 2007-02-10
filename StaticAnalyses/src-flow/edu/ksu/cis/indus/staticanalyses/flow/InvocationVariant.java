
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

/**
 * This class represents a variant for a method invocation expression.  It captures node information regarding return value
 * of the expression and the exceptions thrown by the expression.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <N> is the type of the summary node in the flow analysis.
 */
public class InvocationVariant <N extends IFGNode<?, ?, N>>
  extends ValuedVariant<N> {
	/** 
	 * This is the node corresponding to exception thrown by the invocation.
	 *
	 * @invariant thrownExceptionNode != null
	 */
	private final N thrownExceptionNode;

	/**
	 * Creates a new InvocationVariant object.
	 *
	 * @param returnNode is the node associated with the return value.
	 * @param thrownNode is the node associated with exception thrown.
	 *
	 * @pre returnNode != null and thrownNode != null
	 */
	protected InvocationVariant(final N returnNode, final N thrownNode) {
		super(returnNode);
		thrownExceptionNode = thrownNode;
	}

	/**
	 * Returns the node for the thrown exception.
	 *
	 * @return the node associated the exception thrown by the expression associated with this variant.
	 *
	 * @pre exception != null
	 */
	public N getThrowNode() {
		return thrownExceptionNode;
	}
}

// End of File
