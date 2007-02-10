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

import edu.ksu.cis.indus.interfaces.IPrototype;

import soot.ValueBox;
import soot.jimple.JimpleValueSwitch;

/**
 * This is the interface to be provided by expression walkers/visitors used in flow analysis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <N> is the type of the summary node in the flow analysis.
 */
public interface IExprSwitch<N extends IFGNode<?, ?, N>>
		extends IPrototype<IExprSwitch<N>>, JimpleValueSwitch {

	/**
	 * Retreives the result of visiting the expression.
	 * 
	 * @return the result of visiting the expression.
	 */
	N getFlowNode();

	/**
	 * Sets the node resulting from visiting the expression.
	 * 
	 * @param node resulting from visiting the expression.
	 */
	void setFlowNode(N node);

	/**
	 * Processes the expression at the given program point, <code>v</code>.
	 * 
	 * @param v the program point at which the to-be-processed expression occurs.
	 * @pre v != null
	 */
	void process(final ValueBox v);
}

// End of File
