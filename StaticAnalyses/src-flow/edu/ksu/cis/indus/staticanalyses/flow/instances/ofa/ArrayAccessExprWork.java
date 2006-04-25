
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
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.ArrayType;
import soot.Value;
import soot.jimple.ArrayRef;


/**
 * This class is the counter part of <code>FieldAccessExprWork</code>.  It encapsulates the logic to instrument the flow
 * values through array components.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T>  is the type of the token set object.
 */
class ArrayAccessExprWork<T extends ITokens<T, Value>>
  extends AbstractMemberDataAccessExprWork<T> {
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
	public ArrayAccessExprWork(final IMethodVariant<OFAFGNode<T>> callerMethod, final Context accessContext, final OFAFGNode<T> accessNode,
		final IFGNodeConnector<OFAFGNode<T>> connectorToUse, final T tokenSet) {
		super(callerMethod, accessContext, accessNode, connectorToUse, tokenSet);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.AbstractMemberDataAccessExprWork#getFGNodeForMemberData()
	 */
	@Override protected  OFAFGNode<T> getFGNodeForMemberData() {
		final ArrayType _atype = (ArrayType) ((ArrayRef) accessExprBox.getValue()).getBase().getType();
		final FA<?, ?, OFAFGNode<T>, ?> _fa = caller.getFA();
		return _fa.getArrayVariant(_atype, context).getFGNode();
	}
}

// End of File
