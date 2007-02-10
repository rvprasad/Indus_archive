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

import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * This class provides the logic to create new variants of AST nodes.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <SYM> is the type of symbol whose flow is being analyzed.
 * @param <T> is the type of the token set object.
 *  @param <N>is the type of the summary node in the flow analysis.
 * @param <R> is the type of the symbol types.
 */
public class ASTVariantManager<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>, R>
		extends AbstractVariantManager<ValuedVariant<N>, Value, SYM, T, N, R> {

	/**
	 * Creates a new <code>ASTVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this instance exists.
	 * @param indexManager the manager that shall provide the indices to lookup the variants.
	 * @pre theAnalysis != null and indexManager != null
	 */
	ASTVariantManager(final FA<SYM, T, N, R> theAnalysis,
			final IIndexManager<? extends IIndex<?>, Value> indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new variant representing the given AST node.
	 *
	 * @param o the AST node to be represented by the returned variant.
	 * @return the variant representing the AST node, <code>o</code>.
	 * @pre o != null
	 * @post o.oclIsKindOf(InvokeExpr) implies result.oclType = InvocationVariant
	 * @post (not o.oclIsKindOf(InvokeExpr)) implies result.oclType = ValuedVariant
	 */
	@Override protected ValuedVariant<N> getNewVariant(final Value o) {
		final ValuedVariant<N> _result;

		if (o instanceof InvokeExpr) {
			_result = new InvocationVariant<N>(fa.getNewFGNode(), fa.getNewFGNode());
		} else {
			_result = new ValuedVariant<N>(fa.getNewFGNode());
		}
		return _result;
	}
}

// End of File
