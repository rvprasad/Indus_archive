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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IStatus;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;

import soot.SootMethod;

/**
 * This interface is provided by the flow-analyzers.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IAnalyzer
		extends IStatus {

	/**
	 * Analyzes the system represented by the given classes and and scene.
	 * 
	 * @param env is the environment of classes to be analyzed.
	 * @param methods that serve a entry points into the system.
	 * @pre env != null and methods != null
	 */
	void analyze(IEnvironment env, Collection<SootMethod> methods);

	/**
	 * Analyzes the system represented by the given classes starting at the given entry point.
	 * 
	 * @param env is the environment of classes to be analyzed.
	 * @param entry point into the system being analyzed.
	 * @pre env != null and entry != null
	 */
	void analyze(IEnvironment env, SootMethod entry);

	/**
	 * Retrieves the current context of the analysis.
	 * 
	 * @return the current context.
	 * @post result !=null
	 */
	Context getContext();

	/**
	 * Retrieves the enviroment in which the analysis operates.
	 * 
	 * @return the enviroment.
	 */
	IEnvironment getEnvironment();

	/**
	 * Resets the analyzer.
	 */
	void reset();
}

// End of File
