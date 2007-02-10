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

package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.annotations.Immutable;

/**
 * This represents an object as a state in a finite state automata.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class State
		implements IState<State> {

	/**
	 * The object being represented by this state.
	 */
	@Immutable final Object object;

	/**
	 * Creates a new State object.
	 * 
	 * @param obj is the object to be represented by this state.
	 */
	public State(@Immutable final Object obj) {
		object = obj;
	}
}

// End of File
