
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

package edu.ksu.cis.indus.processing;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


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
	 * This method will be called by the controlling agent upon walking a value in the analyzed system.
	 *
	 * @param vBox to be processed.
	 * @param context in which to <code>value</code> should be processed.
	 *
	 * @pre vBox != null
	 */
	void callback(ValueBox vBox, Context context);

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
	 * This method will be called by the controlling agent for each method for each class in the analyzed system.
	 *
	 * @param method to be processed.
	 *
	 * @pre method != null
	 */
	void callback(SootMethod method);

	/**
	 * This method will be called by the controlling agent for each class in the analyzed system.
	 *
	 * @param clazz to be processed.
	 *
	 * @pre clazz != null
	 */
	void callback(SootClass clazz);

	/**
	 * This method will be called by the controlling agent for each field in each class in the analyzed system.
	 *
	 * @param field to be processed.
	 *
	 * @pre field != null
	 */
	void callback(SootField field);

	/**
	 * This method will be called by the controlling agent for each local in each method in the analyzed system. This
	 * callback need not be registered.
	 *
	 * @param local to be processed.
	 * @param method to be processed.
	 *
	 * @pre method != null and local != null
	 */
	void callback(Local local, SootMethod method);
    
	/**
	 * This gives the  processors to consolidate before the information is available to the user.  This <i>should</i> be
	 * called before the  processors are queried for the results of the processing.
	 */
	void consolidate();

	/**
	 * This method will be called by the application.  The  processor should register it's interest with the controller via
	 * this method.
	 *
	 * @param ppc is the  processing controller.
	 *
	 * @pre ppc != null
	 */
	void hookup(ProcessingController ppc);

	/**
	 * This method indicates to the processors that the processing will begin.  Implementation can suitably initialize in
	 * this method.
	 */
	void processingBegins();

	/**
	 * Resets the processor.
	 */
	void reset();

	/**
	 * This method will be called by the application.  The  processor should unregister it's interest with the controller via
	 * this method after it has participated in processing.
	 *
	 * @param ppc is the  processing controller.
	 *
	 * @pre ppc != null
	 */
	void unhook(ProcessingController ppc);
}

// End of File
