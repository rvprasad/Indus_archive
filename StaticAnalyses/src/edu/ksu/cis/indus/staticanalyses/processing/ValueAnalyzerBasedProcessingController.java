
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

import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Iterator;


/**
 * This class controls the post processing for an analysis.  The analysis as realised by FA is very low-level.  The
 * information is raw.  This needs to be massaged via post processing.  Each post processor can registered interest in
 * particular types of AST chunks.  The controller will walk over the analyzed system and call the registered post
 * processors. The post processors then collect information from the analysis in form which is more accessible to the other
 * applications. This visitor will notify the interested post processors with the given AST node and then visit it's
 * children.
 * 
 * <p>
 * Please note that the processor should be registered/unregistered separately for interface-level (class/method)  processing
 * and functional (method-body) processing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ValueAnalyzerBasedProcessingController
  extends ProcessingController {
	/**
	 * The analyzer instance that provides the low-level analysis information to be be further processed.
	 *
	 * @invariant analyzer != null
	 */
	protected IValueAnalyzer analyzer;

	/**
	 * Sets the analyzer which provides the information to be processed.
	 *
	 * @param analyzerParam an instance of the FA.
	 */
	public void setAnalyzer(final IValueAnalyzer analyzerParam) {
		analyzer = analyzerParam;
		setEnvironment(analyzer.getEnvironment());
	}

	/**
	 * Sets the analyzer on all the processors which require the analyzer.
	 *
	 * @param processors which need to be initialized.
	 */
	protected void initializeProcessors(final Collection processors) {
		for (Iterator i = processors.iterator(); i.hasNext();) {
			Object o = i.next();

			if (o instanceof IValueAnalyzerBasedProcessor) {
				((IValueAnalyzerBasedProcessor) o).setAnalyzer(analyzer);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/10/21 08:41:04  venku
   - Changed the methods/classes get filtered.
   Revision 1.7  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/09/08 02:21:16  venku
   - processors will need to register separately for functional procesing
     and inteface processing.
   Revision 1.5  2003/08/25 08:36:27  venku
   Coding convention.
   Revision 1.4  2003/08/25 08:07:26  venku
   Extracts the classes for processing from the environment.
   It now has support to be driven by the environment alone.
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/11 06:38:25  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
 */
