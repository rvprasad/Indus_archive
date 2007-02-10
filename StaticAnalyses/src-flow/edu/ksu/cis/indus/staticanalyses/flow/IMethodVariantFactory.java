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

import soot.SootMethod;

/**
 * This is factory interface to create method variants in the flow analysis framework.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <SYM> is the type of symbol whose flow is being analyzed.
 * @param <T> is the type of the token set object.
 * @param <N> is the type of the summary node in the flow analysis.
 * @param <R> is the type of the symbol types.
 */
public interface IMethodVariantFactory<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>, R> {

	/**
	 * Creates a method variant.
	 * 
	 * @param sootMethod for which the method variant needs to be created.
	 * @param astVM to be used by the created method variant.
	 * @param fa is the flow analysis framework instance in which the created variant operates.
	 * @return a method variant
	 * @pre sootMethod != null and fa != null
	 */
	IMethodVariant<N> create(SootMethod sootMethod, ASTVariantManager<SYM, T, N, R> astVM, FA<SYM, T, N, R> fa);
}

// End of File
