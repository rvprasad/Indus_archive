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
