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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.interfaces.AbstractStatus;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

/**
 * Abstract implementation of IProcessor.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractProcessor
		extends AbstractStatus
		implements IProcessor {

	/**
	 * @see IProcessor#callback(ValueBox, Context)
	 */
	@Empty public void callback(@SuppressWarnings("unused") final ValueBox vBox, @SuppressWarnings("unused") final Context context) {
		// does nothing
	}

	/**
	 * @see IProcessor#callback(Stmt, Context)
	 */
	@Empty public void callback(@SuppressWarnings("unused") final Stmt stmt, @SuppressWarnings("unused") final Context context) {
		// does nothing
	}

	/**
	 * @see IProcessor#callback(SootMethod)
	 */
	@Empty 	public void callback(@SuppressWarnings("unused") final SootMethod method) {
		// does nothing
	}

	/**
	 * @see IProcessor#callback(SootClass)
	 */
	@Empty 	public void callback(@SuppressWarnings("unused") final SootClass clazz) {
		// does nothing
	}

	/**
	 * @see IProcessor#callback(SootField)
	 */
	@Empty 	public void callback(@SuppressWarnings("unused") final SootField field) {
		// does nothing
	}

	/**
	 * @see IProcessor#consolidate()
	 */
	@Empty 	public void consolidate() {
		// does nothing
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	@Empty public void processingBegins() {
		// does nothing
	}

	/**
	 * @see IProcessor#reset()
	 */
	@Empty public void reset() {
		// does nothing
	}
}

// End of File
