
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

package edu.ksu.cis.indus.staticanalyses.processing;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;


/**
 * Abstract implementation of processor.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractProcessor
  implements IProcessor {
	/**
	 * Does nothing.
	 *
	 * @see IProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer analyzer) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(Value, Context)
	 */
	public void callback(final Value value, final Context context) {
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/08/21 03:43:04  venku
   Removed support for status check.  There can be no query
    on AbstractProcessor/IProcessor, hence, negating any reason
   for such support.
   Revision 1.5  2003/08/21 03:32:37  venku
   Incorporated IStatus interface into any interface that provides analysis information.
   Revision 1.4  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.3  2003/08/11 07:15:57  venku
   Finalized the parameters.
   Revision 1.2  2003/08/11 06:38:25  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
