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

import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;

/**
 * This class encapsulates the logic to connect ast flow graph nodes with non-ast flow graph nodes when the ast nodes
 * correspond to l-values.
 * <p>
 * Created: Wed Jan 30 15:19:44 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <N>  is the type of the token set object.
 */
class LHSConnector<N extends IFGNode<?, ?, N>>
		implements IFGNodeConnector<N> {

	/**
	 * Connects the given ast flow graph node to the non-ast flow graph node. This is used to connect flow nodes corresponding
	 * to LHS expressions.
	 *
	 * @param ast the ast flow graph node to be connected.
	 * @param nonast the non-ast flow graph node to be connnected.
	 * @pre ast != null and nonast != null
	 */
	public void connect(final N ast, final N nonast) {
		ast.addSucc(nonast);
	}
}

// End of File
