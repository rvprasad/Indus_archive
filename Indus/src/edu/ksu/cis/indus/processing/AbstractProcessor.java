
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

package edu.ksu.cis.indus.processing;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
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
  implements IProcessor {
	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(Value, Context)
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/11/10 07:53:56  venku
   - added support to indicate the beginning of processing to the processors.

   Revision 1.1  2003/11/10 03:12:23  venku
   - added an abstract implementation of IProcessor.
 */
