
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

import edu.ksu.cis.indus.processing.AbstractProcessor;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor;


/**
 * Abstract implementation of <code>IValueAnalyzerBasedProcessor</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractValueAnalyzerBasedProcessor
  extends AbstractProcessor
  implements IValueAnalyzerBasedProcessor {
	/**
	 * Does nothing.
	 *
	 * @see IValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer analyzer) {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/11/10 03:16:25  venku
   - uses abstract implementation of IProcessor.
   Revision 1.1  2003/11/10 03:15:19  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   Revision 1.8  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.7  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/21 03:43:04  venku
   Removed support for status check.  There can be no query
    on AbstractValueAnalyzerBasedProcessor/IValueAnalyzerBasedProcessor, hence, negating any reason
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
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
