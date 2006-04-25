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

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.Value;
import soot.jimple.DefinitionStmt;

/**
 * This is used to process statements in object flow analysis. This class in turn uses a expression visitor to process
 * expressions that occur in a statement.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T>  is the type of the token set object.
 */
class StmtSwitch<T extends ITokens<T, Value>>
		extends AbstractStmtSwitch<StmtSwitch<T>, OFAFGNode<T>> {

	/**
	 * Creates a new <code>StmtSwitch</code> instance.
	 * 
	 * @param m the <code>IMethodVariant</code> which uses this object.
	 * @pre m != null
	 */
	public StmtSwitch(final IMethodVariant<OFAFGNode<T>> m) {
		super(m);
	}

	/**
	 * Returns a new instance of this class.
	 * 
	 * @param o the method variant which uses this object. This is of type <code>MethodVariant</code>.
	 * @return the new instance of this class.
	 * @pre o != null and o[0].oclIsKindOf(IMethodVariant)
	 * @post result != null
	 */
	@Override public StmtSwitch<T> getClone(final Object... o) {
		return new StmtSwitch<T>((IMethodVariant) o[0]);
	}

	/**
	 * AbstractStmtSwitch#processDefinitionStmt(DefinitionStmt)
	 */
	@Override protected void processDefinitionStmt(final DefinitionStmt stmt) {
		lexpr.process(stmt.getLeftOpBox());

		final OFAFGNode<T> _left = lexpr.getFlowNode();

		rexpr.process(stmt.getRightOpBox());

		final OFAFGNode<T> _right = rexpr.getFlowNode();

		if (Util.isReferenceType(stmt.getRightOp().getType())) {
			_right.addSucc(_left);
		}
	}
}

// End of File
