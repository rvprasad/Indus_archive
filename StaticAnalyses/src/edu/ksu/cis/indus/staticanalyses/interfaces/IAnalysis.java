
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.staticanalyses.interfaces;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

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
public interface IAnalysis {
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
	void initialize(final Map infoParam)
	  throws InitializationException;

	/**
	 * Resets all internal data structures.  This will <i>not</i> reset data structures provided by the application.
	 */
	void reset();
}

/*
   ChangeLog:
   $Log$
 */
