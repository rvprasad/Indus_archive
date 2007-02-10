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
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Value;
import soot.jimple.NullConstant;

/**
 * This class represents a flow graph node that is associated with an expression in which a member data of the complex type
 * will be accessed. This is typically used in the context of array access expressions and field access expressions.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T>  is the type of the token set object.
 */
abstract class AbstractMemberDataAccessExprWork<T extends ITokens<T, Value>>
		extends AbstractAccessExprWork<T> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMemberDataAccessExprWork.class);

	/**
	 * The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the primary.
	 * 
	 * @invariant ast != null
	 */
	protected final OFAFGNode<T> ast;

	/**
	 * The connector to be used to connect the ast and non-ast node.
	 * 
	 * @invariant connector != null
	 */
	protected final IFGNodeConnector<OFAFGNode<T>> connector;

	/**
	 * Creates a new <code>ArrayAccessExprWork</code> instance.
	 * 
	 * @param callerMethod the method in which the access occurs.
	 * @param accessContext the context in which the access occurs.
	 * @param accessNode the flow graph node associated with the access expression.
	 * @param connectorToUse the connector to use to connect the ast node to the non-ast node.
	 * @param tokenSet used to store the tokens that trigger the execution of this work peice.
	 * @pre callerMethod != null and accessProgramPoint != null and accessContext != null and accessNode != null and
	 *      connectorToUse != null and tokenSet != null
	 */
	public AbstractMemberDataAccessExprWork(final IMethodVariant<OFAFGNode<T>> callerMethod, final Context accessContext,
			final OFAFGNode<T> accessNode, final IFGNodeConnector<OFAFGNode<T>> connectorToUse, final T tokenSet) {
		super(callerMethod, accessContext, tokenSet);
		this.ast = accessNode;
		this.connector = connectorToUse;
	}

	/**
	 * Connects non-ast nodes to ast nodes when new values arrive at the primary of the array access expression.
	 */
	public synchronized void execute() {
		final Collection<Value> _values = tokens.getValues();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(_values + " values arrived at base node of " + accessExprBox.getValue() + " in " + context);
		}

		for (final Iterator<Value> _i = _values.iterator(); _i.hasNext();) {
			final Value _v = _i.next();

			if (_v instanceof NullConstant) {
				continue;
			}

			if (context instanceof AllocationContext) {
				((AllocationContext) context).setAllocationSite(_v);
			}

			final OFAFGNode<T> _nonast = getFGNodeForMemberData();
			connector.connect(ast, _nonast);
		}

		tokens.clear();
	}

	/**
	 * Retrieves the flow graph node for the member data being accessed.
	 * 
	 * @return the flow graph node.
	 * @post result != null
	 */
	protected abstract OFAFGNode<T> getFGNodeForMemberData();
}

// End of File
