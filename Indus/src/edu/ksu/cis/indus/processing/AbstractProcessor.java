
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
	 * Does nothing.
	 *
	 * @see IProcessor#callback(ValueBox, Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(SootMethod)
	 */
	public void callback(final SootMethod method) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(SootClass)
	 */
	public void callback(final SootClass clazz) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(SootField)
	 */
	public void callback(final SootField field) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#consolidate()
	 */
	public void consolidate() {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
	}

	/**
	 * @see IProcessor#reset()
	 */
	public void reset() {
	}
}

// End of File
