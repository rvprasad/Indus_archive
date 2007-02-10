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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import soot.SootMethod;
import soot.toolkits.graph.UnitGraph;

/**
 * This is the interface via which the user can plugin various sorts of unit graphs into the analyses and also reuse the same
 * implementation at many places.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the type of the manufactured graph.
 */
public interface IStmtGraphFactory<T extends UnitGraph> {

	/**
	 * The id of this interface.
	 */
	Comparable<?> ID = "Statement Graph IFactory";

	/**
	 * Retrieves the unit graph of the given method.
	 * 
	 * @param method for which the unit graph is requested.
	 * @return the requested unit graph.
	 */
	@NonNull T getStmtGraph(final SootMethod method);

	/**
	 * Sets the scope specification.
	 * 
	 * @param scopeDef is the scope definition.
	 * @param env is the environment in which the scope is defined.
	 */
	void setScope(@Immutable final SpecificationBasedScopeDefinition scopeDef, @NonNull @Immutable final IEnvironment env);

	/**
	 * Resets all internal datastructures.
	 */
	void reset();
}

// End of File
