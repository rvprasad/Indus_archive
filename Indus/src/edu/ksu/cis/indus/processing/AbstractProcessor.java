
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/07/11 09:42:15  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.7  2004/02/24 22:25:56  venku
   - documentation
   Revision 1.6  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.5  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.4  2003/11/17 15:58:12  venku
   - coding conventions.
   Revision 1.3  2003/11/17 15:42:49  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.2  2003/11/10 07:53:56  venku
   - added support to indicate the beginning of processing to the processors.
   Revision 1.1  2003/11/10 03:12:23  venku
   - added an abstract implementation of IProcessor.
 */
