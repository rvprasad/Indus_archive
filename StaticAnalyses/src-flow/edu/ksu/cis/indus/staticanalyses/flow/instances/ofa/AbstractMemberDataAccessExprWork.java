
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Value;

import soot.jimple.NullConstant;


/**
 * This class represents a flow graph node that is associated with an expression in which a member data of the complex type
 * will be accessed.  This is typically used in the context of array access expressions and field access expressions.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
abstract class AbstractMemberDataAccessExprWork
  extends AbstractAccessExprWork {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractMemberDataAccessExprWork.class);

	/**
	 * The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the
	 * primary.
	 *
	 * @invariant ast != null
	 */
	protected final IFGNode ast;

	/**
	 * The connector to be used to connect the ast and non-ast node.
	 *
	 * @invariant connector != null
	 */
	protected final IFGNodeConnector connector;

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
	public AbstractMemberDataAccessExprWork(final MethodVariant callerMethod, final Context accessContext,
		final IFGNode accessNode, final IFGNodeConnector connectorToUse, final ITokens tokenSet) {
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

			context.setAllocationSite(_v);

			final IFGNode _nonast = getFGNodeForMemberData();
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
	protected abstract IFGNode getFGNodeForMemberData();
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/04/16 20:10:38  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.

   Revision 1.1  2004/04/02 21:59:54  venku
   - refactoring.
     - all classes except OFAnalyzer is package private.
     - refactored work class hierarchy.
 */
