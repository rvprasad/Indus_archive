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
