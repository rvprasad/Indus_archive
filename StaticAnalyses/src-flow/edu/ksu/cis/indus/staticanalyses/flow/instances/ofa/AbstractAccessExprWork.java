
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractTokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.ValueBox;


/**
 * This class encapsulates the logic and data related to work to be done in correspondence to access expressions.
 * 
 * <p>
 * Created: Tue Jan 22 04:27:47 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
abstract class AbstractAccessExprWork
  extends AbstractTokenProcessingWork {
	/**
	 * The context in which the access occurs.
	 *
	 * @invariant context != null
	 */
	protected final Context context;

	/**
	 * The method in which the access occurs.
	 *
	 * @invariant caller != null
	 */
	protected final MethodVariant caller;

	/**
	 * The program point at which the entity occurs.
	 *
	 * @invariant accessExprBox != null
	 */
	protected final ValueBox accessExprBox;

	/**
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access expression occurs.
	 * @param accessContext the context in which the access occurs.
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 *
	 * @pre callerMethod != null and accessContext != null and tokenSet != null
	 */
	protected AbstractAccessExprWork(final MethodVariant callerMethod, final Context accessContext, final ITokens tokenSet) {
		super(tokenSet);
		accessExprBox = accessContext.getProgramPoint();
		caller = callerMethod;
		context = (Context) accessContext.clone();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.

   Revision 1.8  2004/04/02 21:59:54  venku
   - refactoring.
     - all classes except OFAnalyzer is package private.
     - refactored work class hierarchy.
   Revision 1.7  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.6  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/17 10:33:03  venku
   WorkList does not inherit from IWorkBag rather contains an instance of IWorkBag.
   Ripple effect of the above change.
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
