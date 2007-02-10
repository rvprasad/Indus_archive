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
 * The super interface to be implemented by classes which connect AST nodes to Non-AST nodes. An implementation of this
 * interface separates the logic of connecting AST and Non-AST nodes depending on whether the AST node corresponds to a
 * r-value or l-value expression when constructing the flow graph. This helps realize something similar to the <i>Strategy</i>
 * pattern as given in "Gang of Four" book.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <N> is the type of the summary node in the flow analysis.
 */
public interface IFGNodeConnector<N extends IFGNode<?, ?, N>> {

	/**
	 * Connects the given AST node to the Non-AST node.
	 * 
	 * @param ast the AST node to be connected to the Non-AST node.
	 * @param nonast the Non-AST node to be connected to the AST node.
	 * @pre ast != null and nonast != null
	 */
	void connect(N ast, N nonast);
}

// End of File
