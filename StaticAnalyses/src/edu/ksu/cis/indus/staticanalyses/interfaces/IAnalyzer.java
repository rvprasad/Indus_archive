
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
	 * Retrieves the current context of the  analysis.
	 *
	 * @return the current context.
	 *
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
	 * Analyzes the system represented by the given classes and and scene.
	 *
	 * @param env is the environment of classes to be analyzed.
	 * @param methods that serve a entry points into the system.
	 *
	 * @pre env != null and methods != null and methods.oclIsKindOf(Collection(SootMethod))
	 */
	void analyze(IEnvironment env, Collection methods);

	/**
	 * Analyzes the system represented by the given classes starting at the given entry point.
	 *
	 * @param env is the environment of classes to be analyzed.
	 * @param entry point into the system being analyzed.
	 *
	 * @pre env != null and entry != null
	 */
	void analyze(IEnvironment env, SootMethod entry);

	/**
	 * Resets the analyzer.
	 */
	void reset();
}

// End of File
