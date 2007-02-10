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

package edu.ksu.cis.indus.processing;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

/**
 * This is the interface to be provided by higher level analyses to a controlling agent. The controlling agent walks over the
 * analyzed system and calls the interested processors at each value and statement in the analyzed system. Upon callback, the
 * processors suitably collect/process the information available from the low-level analyzer.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IProcessor {
	/**
	 * This method will be called by the controlling agent for each class in the analyzed system.
	 *
	 * @param clazz to be processed.
	 * @pre clazz != null
	 */
	void callback(SootClass clazz);

	/**
	 * This method will be called by the controlling agent for each field in each class in the analyzed system.
	 *
	 * @param field to be processed.
	 * @pre field != null
	 */
	void callback(SootField field);

	/**
	 * This method will be called by the controlling agent for each method for each class in the analyzed system.
	 *
	 * @param method to be processed.
	 * @pre method != null
	 */
	void callback(SootMethod method);

	/**
	 * This method will be called by the controlling agent upon walking a statement in the analyzed system.
	 *
	 * @param stmt to be processed.
	 * @param context in which to <code>stmt</code> should be processed.
	 * @pre stmt != null
	 */
	void callback(Stmt stmt, Context context);

	/**
	 * This method will be called by the controlling agent upon walking a value in the analyzed system.
	 *
	 * @param vBox to be processed.
	 * @param context in which to <code>value</code> should be processed.
	 * @pre vBox != null
	 */
	void callback(ValueBox vBox, Context context);

	/**
	 * This gives the processors to consolidate before the information is available to the user. This <i>should</i> be called
	 * before the processors are queried for the results of the processing.
	 */
	void consolidate();

	/**
	 * This method will be called by the application. The processor should register it's interest with the controller via this
	 * method.
	 *
	 * @param ppc is the processing controller.
	 * @pre ppc != null
	 */
	void hookup(ProcessingController ppc);

	/**
	 * This method indicates to the processors that the processing will begin. Implementation can suitably initialize in this
	 * method.
	 */
	void processingBegins();

	/**
	 * Resets the processor.
	 */
	void reset();

	/**
	 * This method will be called by the application. The processor should unregister it's interest with the controller via
	 * this method after it has participated in processing.
	 *
	 * @param ppc is the processing controller.
	 * @pre ppc != null
	 */
	void unhook(ProcessingController ppc);
}

// End of File
