
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

import soot.SootClass;
import soot.SootMethod;

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.processing.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashSet;


/**
 * This class manages class related primitive information and processing such as the processing of <code>&lt;
 * clinit&gt;</code> methods of classes being analyzed.
 * 
 * <p>
 * Created: Fri Mar  8 14:10:27 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ClassManager
  implements IPrototype {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ClassManager.class);

	/**
	 * The collection of classes for which the information has been processed.
	 */
	protected final Collection classes;

	/**
	 * Describe variable <code>context</code> here.
	 *
	 * @invariant context != null
	 */
	protected final Context context;

	/**
	 * The instance of the framework in which this object is used.
	 *
	 * @invariant fa != null
	 */
	protected final FA fa;

	/**
	 * Creates a new <code>ClassManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 *
	 * @pre theAnalysis != null
	 */
	public ClassManager(final FA theAnalysis) {
		classes = new HashSet();
		this.fa = theAnalysis;
		context = new Context();
	}

	/**
	 * Creates a concrete object of the same class as this object but parameterized by <code>o</code>.
	 *
	 * @param o the instance of the analysis for which this object shall process information.  The actual type of
	 * 		  <code>o</code> needs to be <code>FA</code>.
	 *
	 * @return an instance of <code>ClassManager</code> object parameterized by <code>o</code>.
	 *
	 * @pre o != null
	 * @post result != null
	 */
	public Object getClone(final Object o) {
		return new ClassManager((FA) o);
	}

	/**
	 * This method is not supported by this class.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException this method is not supported by this class.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype() method not supported.");
	}

	/**
	 * Processes the given class for assimilating class related primitive information into the analysis.  This implementation
	 * hooks in the class initialization method into the analysis.
	 *
	 * @param sc the class to be processed.  This cannot be <code>null</code>.
	 *
	 * @pre sc != null
	 */
	protected void process(final SootClass sc) {
		if (!classes.contains(sc)) {
			classes.add(sc);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("considered " + sc.getName());
			}

			if (sc.declaresMethodByName("<clinit>")) {
				SootMethod method = sc.getMethodByName("<clinit>");

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("considered " + method);
				}

				fa.getMethodVariant(method, context);
			}

			SootClass temp = sc;

			while (temp.hasSuperclass()) {
				temp = temp.getSuperclass();

				if (classes.contains(temp)) {
					break;
				}
				classes.add(temp);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("considered " + temp.getName());
				}

				if (temp.declaresMethodByName("<clinit>")) {
					SootMethod method = temp.getMethodByName("<clinit>");

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("considered " + method);
					}

					fa.getMethodVariant(method, context);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("STATS: # of classes processed is " + classes.size());
			}
		}
	}

	/**
	 * Resets the manager.  Removes all information maintained about any classes.
	 */
	protected void reset() {
		classes.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2003/11/26 01:23:59  venku
   - getMethod() is used instead of getMethodByName(). FIXED.
   Revision 1.11  2003/11/25 22:16:30  venku
   - logging
   - super classes were not being added to classes. FIXED.
   Revision 1.10  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.9  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.8  2003/08/30 23:15:17  venku
   Added support to display statistics in managers.
   Revision 1.7  2003/08/30 22:39:20  venku
   Added support to query statistics of the managers.
   Revision 1.6  2003/08/21 10:53:38  venku
   There was recursion bug - FIXED.
   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/16 03:02:42  venku
   Spruced up documentation and specification.
   Revision 1.2  2003/08/12 18:39:56  venku
   Ripple effect of moving IPrototype to Indus.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.8  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
