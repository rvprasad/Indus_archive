
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;

import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class provides a command-line interface to xmlize dependence information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyXMLizerCLI
  extends SootBasedDriver {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependencyXMLizerCLI.class);

	/** 
	 * This is the flow analyser used by the analyses being tested.
	 */
	protected IValueAnalyzer aa;

	/** 
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(AbstractDependencyAnalysis))
	 */
	protected List das = new ArrayList();

	/** 
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

	/** 
	 * This provides use-def info based on aliasing.
	 */
	//private AliasedUseDefInfo aliasUD;

	/** 
	 * The xmlizer used to xmlize dependence information.
	 */
	private final DependencyXMLizer xmlizer = new DependencyXMLizer();

	/** 
	 * This provides equivalence class based escape analysis.
	 */
	//private EquivalenceClassBasedEscapeAnalysis ecba;

	/** 
	 * This provides call-graph based processing controller.
	 */
	//private ValueAnalyzerBasedProcessingController cgipc;

	/** 
	 * This flag indicates if jimple should be dumped.
	 */
	private boolean dumpJimple;

	/** 
	 * This flag indicates if the simple version of aliased use-def information should be used.
	 */
	private boolean useAliasedUseDefv1;

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args is the command line arguments.
	 *
	 * @throws RuntimeException when an Throwable exception beyond our control occurs.
	 *
	 * @pre args != null
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option =
			new Option("o", "output", true,
				"Directory into which xml files will be written into.  Defaults to current directory if omitted");
		_option.setArgs(1);
		_option.setArgName("output-directory");
		_options.addOption(_option);
		_option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		_options.addOption(_option);

		final DivergenceDA _ipdda = new DivergenceDA();
		_ipdda.setConsiderCallSites(true);

		final EntryControlDA _incda = new EntryControlDA();
		final DirectEntryControlDA _dncda = new DirectEntryControlDA();
		final Object[][] _dasOptions =
			{
				{ "a", "ibdda", "Identifier based data dependence (Soot)", new IdentifierBasedDataDA() },
				{ "b", "rbdda", "Reference based data dependence", new ReferenceBasedDataDA() },
				{ "d", "dncda", "Direct Entry control dependence", _dncda },
				{ "e", "xcda", "Exit control dependence", new ExitControlDA() },
				{ "f", "sda", "Synchronization dependence", new SynchronizationDA() },
				{ "g", "rda1", "Ready dependence v1", new ReadyDAv1() },
				{ "i", "rda2", "Ready dependence v2", new ReadyDAv2() },
				{ "k", "rda3", "Ready dependence v3", new ReadyDAv3() },
				{ "l", "ida1", "Interference dependence v1", new InterferenceDAv1() },
				{ "m", "ida2", "Interference dependence v2", new InterferenceDAv2() },
				{ "n", "ida3", "Interference dependence v3", new InterferenceDAv3() },
				{ "q", "dda", "Divergence dependence", new DivergenceDA() },
				{ "r", "incda", "Indirect Entry control dependence", _incda },
				{ "s", "ibdda2", "Identifier based data dependence (Indus)", new IdentifierBasedDataDAv2() },
				{ "t", "ipdda", "Interprocedural Divergence dependence", _ipdda },
			};
		_option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("au1", "aliasedUseDefv1", false, "Use version 1 of aliased use-def info.");
		_option.setOptionalArg(false);
		_options.addOption(_option);

		for (int _i = 0; _i < _dasOptions.length; _i++) {
			final String _shortOption = _dasOptions[_i][0].toString();
			final String _longOption = _dasOptions[_i][1].toString();
			final String _description = _dasOptions[_i][2].toString();
			_option = new Option(_shortOption, _longOption, false, _description);
			_options.addOption(_option);
		}

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				final String _cmdLineSyn = "java " + DependencyXMLizerCLI.class.getName();
				(new HelpFormatter()).printHelp(_cmdLineSyn.length(), _cmdLineSyn, "", _options, "");
				System.exit(1);
			}

			final DependencyXMLizerCLI _cli = new DependencyXMLizerCLI();
			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}

			_cli.xmlizer.setXmlOutputDir(_outputDir);

			if (_cl.hasOption('p')) {
				_cli.addToSootClassPath(_cl.getOptionValue('p'));
			}
			_cli.dumpJimple = _cl.hasOption('j');
			_cli.useAliasedUseDefv1 = _cl.hasOption("au1");

			final List _classNames = _cl.getArgList();

			if (_classNames.isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}
			_cli.setClassNames(_classNames);

			if (_cl.hasOption(_dasOptions[3][0].toString())) {
				_cli.das.add(_incda);
				CollectionsUtilities.putIntoCollectionInMap(_cli.info, _incda.getId(), _incda, new HashSet());
			}

			boolean _flag = true;

			for (int _i = 0; _i < _dasOptions.length; _i++) {
				if (_cl.hasOption(_dasOptions[_i][0].toString())) {
					_cli.das.add(_dasOptions[_i][3]);
					_flag = false;
				}
			}

			if (_flag) {
				throw new ParseException("Atleast one dependence analysis must be requested.");
			}

			_cli.execute();
		} catch (ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);

			final String _cmdLineSyn = "java " + DependencyXMLizerCLI.class.getName();
			(new HelpFormatter()).printHelp(_cmdLineSyn.length(), _cmdLineSyn, "", _options, "", true);
			System.exit(1);
		} catch (final Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * Drives the analyses.
	 */
	private void execute() {
		setLogger(LOGGER);

		final String _tagName = "DependencyXMLizer:FA";
		aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager());

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final ICallGraphInfo _cgi = new CallGraph();
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, new CFGAnalysis(_cgi, getBbm()));
		final ProcessingController _xmlcgipc = new ProcessingController();
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();

		_pc.setAnalyzer(aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));
		_pc.setStmtGraphFactory(getStmtGraphFactory());
		_cgipc.setAnalyzer(aa);
		_cgipc.setProcessingFilter(new CGBasedProcessingFilter(_cgi));
		_cgipc.setStmtGraphFactory(getStmtGraphFactory());
		_xmlcgipc.setEnvironment(aa.getEnvironment());
		_xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));
		_xmlcgipc.setStmtGraphFactory(getStmtGraphFactory());

		final AliasedUseDefInfo _aliasUD;
		if (useAliasedUseDefv1) {
			_aliasUD = new AliasedUseDefInfo(aa, bbm);
		} else {
			_aliasUD = new AliasedUseDefInfov2(aa, _cgi, bbm);
		}
		info.put(ICallGraphInfo.ID, _cgi);
		info.put(IThreadGraphInfo.ID, _tgi);
		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ID, _aliasUD);
		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, _tgi, getBbm());
		info.put(EquivalenceClassBasedEscapeAnalysis.ID, _ecba);

		writeInfo("BEGIN: FA");

		final long _start = System.currentTimeMillis();
		initialize();
		aa.analyze(getScene(), getRootMethods());

		final long _stop = System.currentTimeMillis();
		addTimeLog("FA", _stop - _start);
		writeInfo("END: FA");

		((CallGraph) _cgi).reset();
		_processors.clear();
		_processors.add(_cgi);
		_pc.reset();
		_pc.driveProcessors(_processors);
		writeInfo("CALL GRAPH:\n" + ((CallGraph) _cgi).dumpGraph());

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add(_tgi);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);
		writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).dumpGraph());

		_aliasUD.hookup(_cgipc);
		_ecba.hookup(_cgipc);
		_cgipc.process();
		_aliasUD.unhook(_cgipc);
		_ecba.unhook(_cgipc);
		_ecba.analyze();

		writeInfo("BEGIN: dependency analyses");

		final AnalysesController _ac = new AnalysesController(info, _cgipc, getBbm());

		for (final Iterator _i1 = das.iterator(); _i1.hasNext();) {
			final IDependencyAnalysis _da1 = (IDependencyAnalysis) _i1.next();
			_ac.addAnalyses(_da1.getId(), Collections.singleton(_da1));
		}

		final long _start1 = System.currentTimeMillis();
		_ac.initialize();
		_ac.execute();

		final long _stop1 = System.currentTimeMillis();
		addTimeLog("Dependency preprocessing", _stop1 - _start1);
		writeInfo("END: dependency analyses");
		xmlizer.setGenerator(new UniqueJimpleIDGenerator());
		xmlizer.writeXML(info);

		if (dumpJimple) {
			xmlizer.dumpJimple(null, xmlizer.getXmlOutputDir(), _xmlcgipc);
		}
		writeInfo("Total classes loaded: " + getScene().getClasses().size());
		printTimingStats();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.26  2004/07/17 19:37:18  venku
   - ECBA was incorrect for the following reasons.
     - it fails if the start sites are not in the same method.
     - it fails if the access in the threads occur in methods other than the
       one in which the new thread is started.
     - The above issues were addressed.

   Revision 1.25  2004/07/16 06:45:32  venku
   - added an option to vary versions of aliased use def info.

   Revision 1.24  2004/07/16 06:38:47  venku
   - added  a more precise implementation of aliased use-def information.
   - ripple effect.
   Revision 1.23  2004/07/11 14:50:30  venku
   - fixes to run exit control DA.
   Revision 1.22  2004/07/11 14:17:40  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.21  2004/07/11 11:21:29  venku
   - incorporated changes to drive forward control dependence analysis.
   Revision 1.20  2004/07/04 11:00:50  venku
   - added cli option for interprocedural divergence dependence.
   Revision 1.19  2004/06/23 06:17:25  venku
   - removed -c option.
   Revision 1.18  2004/06/23 04:41:28  venku
   - corrected the class of the logger.
   Revision 1.17  2004/06/15 08:55:27  venku
   - added command line option for new id-based DA.
   Revision 1.16  2004/06/12 06:45:22  venku
   - magically, the exception without "+ 10" in helpformatter of  CLI vanished.
   Revision 1.15  2004/06/05 09:52:24  venku
   - INTERIM COMMIT
     - Reimplemented EntryControlDA.  It provides indirect control dependence info.
     - DirectEntryControlDA provides direct control dependence info.
     - ExitControlDA will follow same suite as EntryControlDA with new implementation
       and new class for direct dependence.
   Revision 1.14  2004/06/03 03:50:34  venku
   - changed the way help will be output on command line classes.
   Revision 1.13  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.12  2004/05/14 06:27:23  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.11  2004/04/25 21:18:37  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
   Revision 1.10  2004/04/23 01:00:48  venku
   - trying to resolve issues with canonicalization of Jimple.
   Revision 1.9  2004/04/23 00:42:36  venku
   - trying to get canonical xmlized Jimple representation.
   Revision 1.8  2004/04/22 10:23:10  venku
   - added getTokenManager() method to OFAXMLizerCLI to create
     token manager based on a system property.
   - ripple effect.
   Revision 1.7  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.6  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.5  2004/03/05 11:59:45  venku
   - documentation.
   Revision 1.4  2004/03/03 05:59:33  venku
   - made aliased use-def info intraprocedural control flow reachability aware.
   Revision 1.3  2004/03/03 02:17:46  venku
   - added a new method to ICallGraphInfo interface.
   - implemented the above method in CallGraph.
   - made aliased use-def call-graph sensitive.
   Revision 1.2  2004/02/25 23:34:29  venku
   - classes that should not be visible should be invisible :-)
   Revision 1.1  2004/02/09 17:40:53  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
   Revision 1.4  2004/02/09 04:39:36  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.3  2004/02/09 02:00:14  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.2  2004/02/09 01:25:10  venku
   - getRootMethods() was defined in SootBasedDriver, hence,
     deleted in this class.
   Revision 1.1  2004/02/08 03:05:46  venku
   - renamed xmlizer packages to be in par with the packages
     that contain the classes whose data is being xmlized.
   Revision 1.41  2004/01/21 13:52:12  venku
   - documentation.
   Revision 1.40  2004/01/21 01:41:43  venku
   - Thread mapping is printed by ThreadGraph.dumpGraph().
      So, deleted duplicate code.
   Revision 1.39  2004/01/20 15:52:12  venku
   - enabled soot class path setting option.
   Revision 1.38  2004/01/09 07:27:34  venku
   - an overriding xmlOutDir variables exists.  FIXED.
   Revision 1.37  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.36  2003/12/28 02:10:07  venku
   - handling of command line arguments was changed.
   Revision 1.35  2003/12/27 20:07:40  venku
   - fixed xmlizers/driver to not throw exception
     when -h is specified
   Revision 1.34  2003/12/16 06:54:05  venku
   - moved preprocessing of analyses after initialization.
   Revision 1.33  2003/12/16 06:15:40  venku
   - jimple filename was indistinct. FIXED.
   Revision 1.32  2003/12/16 00:29:29  venku
   - documentation.
   Revision 1.31  2003/12/15 16:34:40  venku
   - added help switch to command line.
   Revision 1.30  2003/12/15 02:11:15  venku
   - added exception handling at outer most level.
   Revision 1.29  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.28  2003/12/09 09:50:50  venku
   - amended output of string output to be XML compliant.
     This means some characters that are unrepresentable in
     XML are omitted.
   Revision 1.27  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.26  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.25  2003/12/08 12:15:56  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.24  2003/12/08 11:59:47  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizer inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.
   Revision 1.23  2003/12/08 11:53:25  venku
   - formatting.
   Revision 1.22  2003/12/08 10:58:52  venku
   - changed command-line interface.
   Revision 1.21  2003/12/08 09:47:53  venku
   - set the logger.
   - reset() is called on used objects before they are reused.
   - changed the command-line interface.
   Revision 1.20  2003/12/05 13:46:55  venku
   - coding convention.
   Revision 1.19  2003/12/05 13:44:50  venku
   - xmlization messed up the controller.  FIXED.
   Revision 1.18  2003/12/05 09:17:44  venku
   - added support xmlize the jimple.
   Revision 1.17  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.16  2003/12/02 00:48:27  venku
   - coding conventions.
   Revision 1.15  2003/12/01 13:33:20  venku
   - added support to pick dependences to run from command line.
   Revision 1.14  2003/11/30 01:38:52  venku
   - incorporated tag based filtering during CG construction.
   Revision 1.13  2003/11/30 01:17:15  venku
   - renamed CGBasedXMLizingFilter to CGBasedXMLizingProcessingFilter.
   - renamed XMLizingController to XMLizingProcessingFilter.
   - ripple effect.
   Revision 1.12  2003/11/30 01:07:58  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.11  2003/11/30 00:10:24  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.10  2003/11/25 19:04:29  venku
   - aliased use def analysis was never executed. FIXED.
   Revision 1.9  2003/11/25 17:51:23  venku
   - split control dependence into 2 classes.
     EntryControlDA handled control DA as required for backward slicing.
     ExitControlDA handles control DA as required for forward slicing.
   - ripple effect.
   Revision 1.8  2003/11/25 17:24:23  venku
   - changed the order of dependence for convenience.
   Revision 1.7  2003/11/17 16:58:15  venku
   - populateDAs() needs to be called from outside the constructor.
   - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
   Revision 1.6  2003/11/17 15:42:46  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.5  2003/11/17 03:22:59  venku
   - added junit test support for Slicing.
   - refactored code in test for dependency to make it more
     simple.
   Revision 1.4  2003/11/16 18:45:32  venku
   - renamed UniqueIDGenerator to UniqueJimpleIDGenerator.
   - logging.
   - split writeXML() such that it can be used from other drivers
     such as SlicerDriver.
   Revision 1.3  2003/11/15 21:27:49  venku
   - added logging.
   Revision 1.2  2003/11/12 10:45:36  venku
   - soot class path can be set in SootBasedDriver.
   - dependency tests are xmlunit based.
   Revision 1.1  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.3  2003/11/12 05:08:10  venku
   - DependencyXMLizer.properties will hold dependency xmlization
     related properties.
   Revision 1.2  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.1  2003/11/11 10:11:27  venku
   - in the process of making XMLization a user
     application and at the same time a tester application.
 */
/*
   ChangeLog:
   $Log$
   Revision 1.26  2004/07/17 19:37:18  venku
   - ECBA was incorrect for the following reasons.
     - it fails if the start sites are not in the same method.
     - it fails if the access in the threads occur in methods other than the
       one in which the new thread is started.
     - The above issues were addressed.

   Revision 1.25  2004/07/16 06:45:32  venku
   - added an option to vary versions of aliased use def info.

   Revision 1.24  2004/07/16 06:38:47  venku
   - added  a more precise implementation of aliased use-def information.
   - ripple effect.
   Revision 1.23  2004/07/11 14:50:30  venku
   - fixes to run exit control DA.
   Revision 1.22  2004/07/11 14:17:40  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.21  2004/07/11 11:21:29  venku
   - incorporated changes to drive forward control dependence analysis.
   Revision 1.20  2004/07/04 11:00:50  venku
   - added cli option for interprocedural divergence dependence.
   Revision 1.19  2004/06/23 06:17:25  venku
   - removed -c option.
   Revision 1.18  2004/06/23 04:41:28  venku
   - corrected the class of the logger.
   Revision 1.17  2004/06/15 08:55:27  venku
   - added command line option for new id-based DA.
   Revision 1.16  2004/06/12 06:45:22  venku
   - magically, the exception without "+ 10" in helpformatter of  CLI vanished.
   Revision 1.15  2004/06/05 09:52:24  venku
   - INTERIM COMMIT
     - Reimplemented EntryControlDA.  It provides indirect control dependence info.
     - DirectEntryControlDA provides direct control dependence info.
     - ExitControlDA will follow same suite as EntryControlDA with new implementation
       and new class for direct dependence.
   Revision 1.14  2004/06/03 03:50:34  venku
   - changed the way help will be output on command line classes.
   Revision 1.13  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.12  2004/05/14 06:27:23  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.11  2004/04/25 21:18:37  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
   Revision 1.10  2004/04/23 01:00:48  venku
   - trying to resolve issues with canonicalization of Jimple.
   Revision 1.9  2004/04/23 00:42:36  venku
   - trying to get canonical xmlized Jimple representation.
   Revision 1.8  2004/04/22 10:23:10  venku
   - added getTokenManager() method to OFAXMLizerCLI to create
     token manager based on a system property.
   - ripple effect.
   Revision 1.7  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.6  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.5  2004/03/05 11:59:45  venku
   - documentation.
   Revision 1.4  2004/03/03 05:59:33  venku
   - made aliased use-def info intraprocedural control flow reachability aware.
   Revision 1.3  2004/03/03 02:17:46  venku
   - added a new method to ICallGraphInfo interface.
   - implemented the above method in CallGraph.
   - made aliased use-def call-graph sensitive.
   Revision 1.2  2004/02/25 23:34:29  venku
   - classes that should not be visible should be invisible :-)
   Revision 1.1  2004/02/09 17:40:53  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
 */
