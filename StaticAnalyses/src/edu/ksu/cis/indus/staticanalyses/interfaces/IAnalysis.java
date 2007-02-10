
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

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.IStatus;

import edu.ksu.cis.indus.processing.IProcessor;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.Map;


/**
 * This is the interface of an analysis required to execute it.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IAnalysis
  extends IStatus {
	/**
	 * Sets the basic block graph manager to use.
	 *
	 * @param bbm is the basic block graph manager.
	 *
	 * @pre bbm != null
	 */
	void setBasicBlockGraphManager(final BasicBlockGraphMgr bbm);

	/**
	 * Returns the pre-processor.
	 *
	 * @return the pre-processor.
	 *
	 * @post doesPreProcessing() == true implies result != null
	 */
	IProcessor getPreProcessor();

	/**
	 * Analyzes the given methods and classes for "some" information.
	 */
	void analyze();

	/**
	 * Checks if this analysis does any preprocessing.
	 *
	 * @return <code>true</code> if the analysis will preprocess; <code>false</code>, otherwise.
	 */
	boolean doesPreProcessing();

	/**
	 * Initializes the analyzer with the information from the system to perform the analysis.
	 *
	 * @param infoParam contains the value for the member variable<code>info</code>.
	 *
	 * @throws InitializationException if the initialization in the sub classes fails.
	 *
	 * @pre infoParam != null
	 */
	void initialize(final Map<Comparable<?>, Object> infoParam)
	  throws InitializationException;

	/**
	 * Resets all internal data structures.  This will <i>not</i> reset data structures provided by the application.
	 */
	void reset();
}

// End of File
