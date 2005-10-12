
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

import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
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
 * will be accessed.  This is typically used in the context of array access expressions and field access expressions.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> DOCUMENT ME!
 */
abstract class AbstractMemberDataAccessExprWork<N extends IFGNode<N, ?>>
  extends AbstractAccessExprWork<N> {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMemberDataAccessExprWork.class);

	/** 
	 * The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the
	 * primary.
	 *
	 * @invariant ast != null
	 */
	protected final N ast;

	/** 
	 * The connector to be used to connect the ast and non-ast node.
	 *
	 * @invariant connector != null
	 */
	protected final IFGNodeConnector<N> connector;

	/**
	 * Creates a new <code>ArrayAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access occurs.
	 * @param accessContext the context in which the access occurs.
	 * @param accessNode the flow graph node associated with the access expression.
	 * @param connectorToUse the connector to use to connect the ast node to the non-ast node.
	 * @param tokenSet used to store the tokens that trigger the execution of this work peice.
	 *
	 * @pre callerMethod != null and accessProgramPoint != null and accessContext != null and accessNode != null and
	 * 		connectorToUse != null and tokenSet != null
	 */
	public AbstractMemberDataAccessExprWork(final IMethodVariant<N, ?, ?, ?> callerMethod, final Context accessContext,
		final N accessNode, final IFGNodeConnector<N> connectorToUse, final ITokens tokenSet) {
		super(callerMethod, accessContext, tokenSet);
		this.ast = accessNode;
		this.connector = connectorToUse;
	}

	/**
	 * Connects non-ast nodes to ast nodes when new values arrive at the primary of the array access expression.
	 */
	public synchronized void execute() {
		final Collection _values = tokens.getValues();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(_values + " values arrived at base node of " + accessExprBox.getValue() + " in " + context);
		}

		for (final Iterator _i = _values.iterator(); _i.hasNext();) {
			final Value _v = (Value) _i.next();

			if (_v instanceof NullConstant) {
				continue;
			}

			if (context instanceof AllocationContext) {
				((AllocationContext) context).setAllocationSite(_v);
			}

			final N _nonast = getFGNodeForMemberData();
			connector.connect(ast, _nonast);
		}
	}

	/**
	 * Retrieves the flow graph node for the member data being accessed.
	 *
	 * @return the flow graph node.
	 *
	 * @post result != null
	 */
	protected abstract N getFGNodeForMemberData();
}

// End of File
