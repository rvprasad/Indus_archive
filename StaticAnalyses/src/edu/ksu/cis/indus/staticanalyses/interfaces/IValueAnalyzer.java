
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
 * This is the interface to be provided by an analysis that operates on values (which may be symbolic).  The analysis that
 * implement this interface are behavioral analysis rather than structural analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IValueAnalyzer
  extends IStatus {
	/** 
	 * The id of this interface.
	 */
	Object ID = "value flow analyzer";

	/** 
	 * The id of the tag used by the underlying flow analysis.
	 */
	Object TAG_ID = "id of tag used by flow analysis";

	/**
	 * Retrieves the enviroment in which the analysis operates.
	 *
	 * @return the enviroment.
	 */
	IEnvironment getEnvironment();

	/**
	 * Retrieves the values associated with the given entity in the given context.
	 *
	 * @param entity for which values are requested.
	 * @param context in which the returned values will be associated with the entity.
	 *
	 * @return the collection of values.
	 *
	 * @pre context != null
	 * @pre entity != null
	 * @post result != null
	 */
	Collection getValues(Object entity, Context context);

	/**
	 * Retrieves the values associated with <code>this</code> variable in the given context.
	 *
	 * @param context in which the returned values will be associatd with <code>this</code> variable.
	 *
	 * @return the collection of values
	 *
	 * @pre context != null
	 * @pre context.getCurrentMethod() != null
	 * @post result != null
	 */
	Collection getValuesForThis(Context context);

	/**
	 * Analyzes the system represented by the given classes and and scene.
	 *
	 * @param env is the environment of classes to be analyzed.
	 * @param classes which were mentioned by the user as being part of the system.  This generally serves as the starting
	 * 		  point to discover other constituents of the system.  These may serve as the classes in which to explore for
	 * 		  the  entry point of the system.
	 *
	 * @pre env != null and classes != null and classes.size() != 0
	 */
	void analyze(IEnvironment env, Collection classes);

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
