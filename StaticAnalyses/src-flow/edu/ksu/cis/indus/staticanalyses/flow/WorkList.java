
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.graph.IWorkBag;
import edu.ksu.cis.indus.common.graph.LIFOWorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A worklist implementation.
 * 
 * <p>
 * Created: Tue Jan 22 02:43:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class WorkList {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(WorkList.class);

	/**
	 * The backend workbag object which holds the work piece.
	 */
	private final IWorkBag workbag;

	/**
	 * Creates a new <code>WorkList</code> instance.
	 */
	WorkList() {
		workbag = new LIFOWorkBag();
	}

	/**
	 * Adds a work piece into the worklist.  If <code>w</code> exists in the worklist, it will not be added.  The existence
	 * check is done using <code>java.lang.Object.equals()</code> method.
	 *
	 * @param w the work to be added into the worklist.
	 *
	 * @pre w != null
	 */
	public final void addWork(final AbstractWork w) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added new work:" + w);
		}

		if (!workbag.addWorkNoDuplicates(w)) {
			w.finished();
		}
	}

	/**
	 * Removes any work in the work list without processing them.
	 */
	final void clear() {
		workbag.clear();
	}

	/**
	 * Executes the work pieces in the worklist.  This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.
	 */
	void process() {
		while (workbag.hasWork()) {
			AbstractWork w = (AbstractWork) workbag.getWork();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing work:" + w);
			}
			w.execute();
			w.finished();
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2003/12/08 12:15:58  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.12  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.11  2003/12/02 01:30:54  venku
   - coding conventions and formatting.
   Revision 1.10  2003/12/01 13:49:38  venku
   - added support to utilize pooling support.
   Revision 1.9  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/11/05 09:33:13  venku
   - ripple effect of splitting Workbag.
   Revision 1.7  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.5  2003/08/18 11:08:10  venku
   Name change for pooling support.
   Revision 1.4  2003/08/17 11:19:13  venku
   Placed the simple SendValuesWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractWork and WorkList to enable work pool support.
   Revision 1.3  2003/08/17 10:33:03  venku
   WorkList does not inherit from IWorkBag rather contains an instance of IWorkBag.
   Ripple effect of the above change.
   Revision 1.2  2003/08/15 04:07:56  venku
   Spruced up documentation and specification.
   - Important change is that previously all types of retype and nullconstant were let through.
     This is incorrect as there is not type filtering happening.  This has been fixed.  We now
     only let those that are not of the monitored type.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.9  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
