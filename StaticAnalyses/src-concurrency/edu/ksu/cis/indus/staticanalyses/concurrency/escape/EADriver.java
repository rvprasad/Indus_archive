
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;


/**
 * This class drives escape analysis and displays the calculated information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class EADriver
  extends SootBasedDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(EADriver.class);

	/**
	 * The provided command line arguments.
	 */
	private String[] args;

	/**
	 * Creates a new EADriver object.
	 *
	 * @param argsParam is the command line arguments.
	 *
	 * @post args.oclAsType(Sequence(String)) == argsParam.oclAsType(Sequence(String))
	 */
	private EADriver(final String[] argsParam) {
		this.args = argsParam;
	}

	/**
	 * Entry point to the test driver.
	 *
	 * @param argsParam is the command line arguments.
	 */
	public static void main(final String[] argsParam) {
		if (argsParam.length == 0) {
			System.out.println("Please specify a class to consider for the analysis.");
		}
		(new EADriver(argsParam)).execute();
	}

	/**
	 * Drives escape analysis. It executes FA first, followed by any post process analysis, followed by the escape analysis.
	 */
	protected void execute() {
		setClassNames(args);
		initialize();

		final String _tagName = "EADriver:FA";
		final IValueAnalyzer _aa = OFAnalyzer.getFSOSAnalyzer(_tagName);
		final Collection _rm = new ArrayList();

		for (final Iterator _l = rootMethods.iterator(); _l.hasNext();) {
			_rm.clear();
			_rm.add(_l.next());

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: FA analysis");
			}

			long _start = System.currentTimeMillis();
			_aa.reset();
			_aa.analyze(scene, _rm);

			long _stop = System.currentTimeMillis();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: FA analysis");
			}
			addTimeLog("FA analysis", _stop - _start);

			final ValueAnalyzerBasedProcessingController _ppc = new ValueAnalyzerBasedProcessingController();
			_ppc.setAnalyzer(_aa);
			_ppc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

			// Create call graph
			final CallGraph _cg = new CallGraph();
			_cg.hookup(_ppc);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: FA postprocessing");
			}
			_start = System.currentTimeMillis();
			_ppc.process();
			_cg.unhook(_ppc);

			_ppc.reset();
			_ppc.setProcessingFilter(new CGBasedProcessingFilter(_cg));
			_ppc.setAnalyzer(_aa);

			// Create Thread graph
			final ThreadGraph _tg = new ThreadGraph(_cg, new CFGAnalysis(_cg, bbm));
			_tg.hookup(_ppc);
			_ppc.process();
			_tg.unhook(_ppc);

			// Perform equivalence-class-based escape analysis
			final EquivalenceClassBasedEscapeAnalysis _analysis = new EquivalenceClassBasedEscapeAnalysis(_cg, _tg, bbm);
			_analysis.hookup(_ppc);
			_ppc.process();
			_stop = System.currentTimeMillis();
			_analysis.unhook(_ppc);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: FA postprocessing");
			}
			addTimeLog("FA postprocessing took ", _stop - _start);
			System.out.println("CALL GRAPH:\n" + _cg.dumpGraph());
			System.out.println("THREAD GRAPH:\n" + _tg.dumpGraph());

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: " + _analysis.getClass().getName() + " processing");
			}
			_start = System.currentTimeMillis();
			_analysis.execute();
			_stop = System.currentTimeMillis();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: " + _analysis.getClass().getName() + " processing");
			}
			addTimeLog(_analysis.getClass().getName(), _stop - _start);

			final Collection _abstractObjects = new HashSet();
			int _accessSites = 0;
			int _allocationSites = 0;

			for (int _i = 0; _i < args.length; _i++) {
				final SootClass _sc = scene.getSootClass(args[_i]);
				System.out.println("Info for class " + _sc.getName() + "\n");

				for (final Iterator _j = CollectionUtils.intersection(_cg.getReachableMethods(), _sc.getMethods()).iterator();
					  _j.hasNext();) {
					final SootMethod _sm = (SootMethod) _j.next();
					System.out.println("\nInfo for Method " + _sm.getSignature());

					if (!_sm.isConcrete()) {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info(_sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
						}
						continue;
					}

					final JimpleBody _body = (JimpleBody) _sm.retrieveActiveBody();

					for (final Iterator _k = _body.getLocals().iterator(); _k.hasNext();) {
						final Local _local = (Local) _k.next();
						System.out.println(" Local " + _local + ":" + _local.getType() + "\n" + " escapes -> "
							+ _analysis.escapes(_local, _sm) + "\n" + " global -> " + _analysis.isGlobal(_local, _sm));
					}

					for (final Iterator _k = _body.getUseAndDefBoxes().iterator(); _k.hasNext();) {
						final ValueBox _box = (ValueBox) _k.next();
						final Value _value = _box.getValue();

						if (_value instanceof ArrayRef || _value instanceof FieldRef) {
							Object _as = _analysis.getAliasSetFor(_value, _sm);

							if (_as != null) {
								_as = ((AliasSet) _as).find();

								if (!_abstractObjects.contains(_as)) {
									_abstractObjects.add(_as);
								}

								if (_analysis.escapes(_value, _sm)) {
									_accessSites++;
								}
							}
						} else if (_value instanceof NewExpr
							  || _value instanceof NewArrayExpr
							  || _value instanceof NewMultiArrayExpr) {
							_allocationSites++;
						}
					}
				}
			}
			System.out.println("Total number of abstract objects is " + _abstractObjects.size());
			System.out.println("Total numbef of allocation sites is " + _allocationSites);
			System.out.println("Total number of shared accesses based on escape information is " + _accessSites);
			System.out.println("Total classes loaded: " + scene.getClasses().size());
			printTimingStats();
		}
	}

	/**
	 * Writes the stringized form of the given object onto System.out.
	 *
	 * @param o about which information should be written.
	 */
	protected void writeInfo(final Object o) {
		if (o != null) {
			System.out.println(o.toString());
		} else {
			System.out.println("null");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.26  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.25  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.24  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.23  2003/12/08 09:46:28  venku
 *** empty log message ***
     Revision 1.22  2003/12/02 09:42:38  venku
     - well well well. coding convention and formatting changed
       as a result of embracing checkstyle 3.2
     Revision 1.21  2003/11/30 01:38:52  venku
     - incorporated tag based filtering during CG construction.
     Revision 1.20  2003/11/30 01:07:58  venku
     - added name tagging support in FA to enable faster
       post processing based on filtering.
     - ripple effect.
     Revision 1.19  2003/11/30 00:10:24  venku
     - Major refactoring:
       ProcessingController is more based on the sort it controls.
       The filtering of class is another concern with it's own
       branch in the inheritance tree.  So, the user can tune the
       controller with a filter independent of the sort of processors.
     Revision 1.18  2003/11/17 01:17:12  venku
     - formatting.
     Revision 1.17  2003/11/16 19:09:42  venku
     - documentation.
     Revision 1.16  2003/11/12 10:50:55  venku
     - this is now based on SootBasedDriver.
     Revision 1.15  2003/11/06 05:15:07  venku
     - Refactoring, Refactoring, Refactoring.
     - Generalized the processing controller to be available
       in Indus as it may be useful outside static anlaysis. This
       meant moving IProcessor, Context, and ProcessingController.
     - ripple effect of the above changes was large.
     Revision 1.14  2003/11/02 22:09:57  venku
     - changed the signature of the constructor of
       EquivalenceClassBasedEscapeAnalysis.
     Revision 1.13  2003/10/31 01:02:04  venku
     - added code for extracting data for CC04 paper.
     Revision 1.12  2003/10/09 00:17:39  venku
     - changes to instrumetn statistics numbers.
     Revision 1.11  2003/10/05 06:31:35  venku
     - Things work.  The bug was the order in which the
       parameter alias sets were being accessed.  FIXED.
     Revision 1.10  2003/09/29 14:55:03  venku
     - don't use "use-orignal-names" option with Jimple.
       The variables referring to objects need to be unique if the
       results of the analyses should be meaningful.
     Revision 1.9  2003/09/29 09:04:30  venku
     - dump formatting.
     Revision 1.8  2003/09/29 07:30:51  venku
     - added support to spit out local variables names as they occur
       in the source rather than jimplified names.
     Revision 1.7  2003/09/29 06:37:31  venku
     - Each driver now handles each root method separately.
     Revision 1.6  2003/09/29 04:20:57  venku
     - coding convention.
     Revision 1.5  2003/09/28 07:32:30  venku
     - many basic block graphs were being constructed. Now, there
       is only one that will be used.
     Revision 1.4  2003/09/28 06:20:39  venku
     - made the core independent of hard code used to create unit graphs.
       The core depends on the environment to provide a factory that creates
       these unit graphs.
     Revision 1.3  2003/09/28 03:17:13  venku
     - I don't know.  cvs indicates that there are no differences,
       but yet says it is out of sync.
     Revision 1.2  2003/09/08 02:23:13  venku
     - Ripple effect of bbm support in Driver and change of constructor
       in ThreadGraph.
     Revision 1.1  2003/08/21 01:24:25  venku
      - Renamed src-escape to src-concurrency to as to group all concurrency
        issue related analyses into a package.
      - Renamed escape package to concurrency.escape.
      - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
     Revision 1.4  2003/08/17 10:48:34  venku
     Renamed BFA to FA.  Also renamed bfa variables to fa.
     Ripple effect was huge.
     Revision 1.3  2003/08/11 06:29:07  venku
     Changed format of change log accumulation at the end of the file
     Revision 1.2  2003/08/10 03:43:26  venku
     Renamed Tester to Driver.
     Refactored logic to pick entry points.
     Provided for logging timing stats into any specified stream.
     Ripple effect in others.
     Revision 1.1  2003/08/07 06:39:07  venku
     Major:
      - Moved the package under indus umbrella.
     Minor:
      - changes to accomodate ripple effect from support package.
     Revision 1.1  2003/07/30 08:27:03  venku
     Renamed IATester to EADriver.
     Also, staged various analyses.
 */
