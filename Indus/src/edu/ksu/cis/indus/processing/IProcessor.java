
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
	void callback(final ValueBox vBox, final Context context);

	/**
	 * This method will be called by the controlling agent upon walking a statement in the analyzed system.
	 *
	 * @param stmt to be processed.
	 * @param context in which to <code>stmt</code> should be processed.
	 *
	 * @pre stmt != null
	 */
	void callback(final Stmt stmt, final Context context);

	/**
	 * This method will be called by the controlling agent for each method for each class in the analyzed system. This
	 * callback need not be registered.
	 *
	 * @param method to be processed.
	 *
	 * @pre method != null
	 */
	void callback(final SootMethod method);

	/**
	 * This method will be called by the controlling agent for each class in the analyzed system. This callback need not be
	 * registered.
	 *
	 * @param clazz to be processed.
	 *
	 * @pre clazz != null
	 */
	void callback(final SootClass clazz);

	/**
	 * This method will be called by the controlling agent for each field in each class in the analyzed system. This callback
	 * need not be registered.
	 *
	 * @param field to be processed.
	 *
	 * @pre field != null
	 */
	void callback(final SootField field);

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
	void hookup(final ProcessingController ppc);

	/**
	 * This method indicates to the processors that the processing will begin.  Implementation can suitably initialize in
	 * this method.
	 */
	void processingBegins();

	/**
	 * This method will be called by the application.  The  processor should unregister it's interest with the controller via
	 * this method after it has participated in processing.
	 *
	 * @param ppc is the  processing controller.
	 *
	 * @pre ppc != null
	 */
	void unhook(final ProcessingController ppc);
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.4  2003/11/17 15:58:12  venku
   - coding conventions.
   Revision 1.3  2003/11/17 15:42:49  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.2  2003/11/10 07:53:56  venku
   - added support to indicate the beginning of processing to the processors.
   Revision 1.1  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.1  2003/11/06 05:15:06  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.3  2003/09/28 03:08:03  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.2  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
