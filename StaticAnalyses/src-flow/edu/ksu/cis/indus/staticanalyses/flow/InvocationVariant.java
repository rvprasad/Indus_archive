
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

/**
 * This class represents a variant for a method invocation expression.  It captures node information regarding return value
 * of the expression and the exceptions thrown by the expression.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <N> DOCUMENT ME!
 */
public class InvocationVariant <N extends IFGNode<N, ?>>
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
