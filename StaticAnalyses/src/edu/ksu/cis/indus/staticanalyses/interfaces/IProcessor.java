
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

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;


/**
 * This is the interface to be provided by higher level analyses to a controlling agent.  The controlling agent walks over
 * the analyzed system and calls the interested processors at each value and statement in the analyzed system.  Upon
 * callback, the processors suitably collect/process the information available from the low-level analyzer.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IProcessor {
	/**
	 * Sets the analyzer that provides the low-level information to be processed..
	 *
	 * @param analyzer that provides low-level info.
	 *
	 * @pre analyzer != null
	 */
	void setAnalyzer(IValueAnalyzer analyzer);

	/**
	 * This method will be called by the controlling agent upon walking a value in the analyzed system.
	 *
	 * @param value to be processed.
	 * @param context in which to <code>value</code> should be processed.
	 *
	 * @pre value != null
	 */
	void callback(Value value, Context context);

	/**
	 * This method will be called by the controlling agent upon walking a statement in the analyzed system.
	 *
	 * @param stmt to be processed.
	 * @param context in which to <code>stmt</code> should be processed.
	 *
	 * @pre stmt != null
	 */
	void callback(Stmt stmt, Context context);

	/**
	 * This method will be called by the controlling agent for each method for each class in the analyzed system. This
	 * callback need not be registered.
	 *
	 * @param method to be processed.
	 *
	 * @pre method != null
	 */
	void callback(SootMethod method);

	/**
	 * This method will be called by the controlling agent for each class in the analyzed system. This callback need not be
	 * registered.
	 *
	 * @param clazz to be processed.
	 *
	 * @pre clazz != null
	 */
	void callback(SootClass clazz);

	/**
	 * This method will be called by the controlling agent for each field in each class in the analyzed system. This callback
	 * need not be registered.
	 *
	 * @param field to be processed.
	 *
	 * @pre field != null
	 */
	void callback(SootField field);

	/**
	 * This gives the post processors to consolidate before the information is available to the user.  This <i>should</i> be
	 * called before the post processors are queried for the results of the processing.
	 */
	void consolidate();

	/**
	 * This method will be called by the application.  The post processor should register it's interest with the controller
	 * via this method.
	 *
	 * @param ppc is the post processing controller.
	 *
	 * @pre ppc != null
	 */
	void hookup(ProcessingController ppc);

	/**
	 * This method will be called by the application.  The post processor should unregister it's interest with the controller
	 * via this method after it has participated in post-processing.
	 *
	 * @param ppc is the post processing controller.
	 *
	 * @pre ppc != null
	 */
	void unhook(ProcessingController ppc);
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
