
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.SafeLockAnalysis;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class provides a command-line interface to xmlize dependence information.  Refer to <code>SootBasedDriver</code> for
 * more configuration infomration.
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
	 * The xmlizer used to xmlize dependence information.
	 */
	private final DependencyXMLizer xmlizer = new DependencyXMLizer();

	/** 
	 * This flag indicates if jimple should be dumped.
	 */
	private boolean dumpJimple;

	/** 
	 * This flag indicates if the simple version of aliased use-def information should be used.
	 */
	private boolean useAliasedUseDefv1;

	/** 
	 * This indicates if safe lock should be used.
	 */
	private boolean useSafeLockAnalysis;

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

		final DivergenceDA _fipdda = DivergenceDA.getForwardDivergenceDA();
		_fipdda.setConsiderCallSites(true);

		final DivergenceDA _bipdda = DivergenceDA.getBackwardDivergenceDA();
		_bipdda.setConsiderCallSites(true);

		final EntryControlDA _ncda = new EntryControlDA();
		final Object[][] _dasOptions =
			{
				{ "ibdda1", "Identifier based data dependence (Soot)", new IdentifierBasedDataDA() },
				{ "ibdda2", "Identifier based data dependence (Indus)", new IdentifierBasedDataDAv2() },
				{ "ibdda3", "Identifier based data dependence (Indus Optimized)", new IdentifierBasedDataDAv3() },
				{ "rbdda", "Reference based data dependence", new ReferenceBasedDataDA() },
				{ "ncda", "Entry control dependence", _ncda },
				{ "xcda", "Exit control dependence", new ExitControlDA() },
				{ "sda", "Synchronization dependence", new SynchronizationDA() },
				{ "frda1", "Forward Ready dependence v1", ReadyDAv1.getForwardReadyDA() },
				{ "brda1", "Backward Ready dependence v1", ReadyDAv1.getBackwardReadyDA() },
				{ "frda2", "Forward Ready dependence v2", ReadyDAv2.getForwardReadyDA() },
				{ "brda2", "Backward Ready dependence v2", ReadyDAv2.getBackwardReadyDA() },
				{ "frda3", "Forward Ready dependence v3", ReadyDAv3.getForwardReadyDA() },
				{ "brda3", "Backward Ready dependence v3", ReadyDAv3.getBackwardReadyDA() },
				{ "ida1", "Interference dependence v1", new InterferenceDAv1() },
				{ "ida2", "Interference dependence v2", new InterferenceDAv2() },
				{ "ida3", "Interference dependence v3", new InterferenceDAv3() },
				{ "fdda", "Forward Divergence dependence", DivergenceDA.getForwardDivergenceDA() },
				{ "bdda", "Backward Divergence dependence", DivergenceDA.getBackwardDivergenceDA() },
				{ "fpdda", "Forward Interprocedural Divergence dependence", _fipdda },
				{ "bpdda", "Backward Interprocedural Divergence dependence", _bipdda },
			};
		_option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("aliasedusedefv1", false, "Use version 1 of aliased use-def info.");
		_options.addOption(_option);
		_option = new Option("safelockanalysis", false, "Use safe-lock-analysis for ready dependence.");
		_options.addOption(_option);
		_option = new Option("ofaforinterference", false, "Use OFA for interference dependence.");
		_options.addOption(_option);
		_option = new Option("ofaforready", false, "Use OFA for ready dependence.");
		_options.addOption(_option);

		for (int _i = 0; _i < _dasOptions.length; _i++) {
			final String _shortOption = _dasOptions[_i][0].toString();
			final String _description = _dasOptions[_i][1].toString();
			_option = new Option(_shortOption, false, _description);
			_options.addOption(_option);
		}

		final CommandLineParser _parser = new GnuParser();

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
			_cli.useAliasedUseDefv1 = _cl.hasOption("aliasedusedefv1");
			_cli.useSafeLockAnalysis = _cl.hasOption("safelockanalysis");

			final List _classNames = _cl.getArgList();

			if (_classNames.isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}
			_cli.setClassNames(_classNames);

			if (_cl.hasOption(_dasOptions[5][0].toString())) {
				_cli.das.add(_ncda);
				CollectionsUtilities.putIntoCollectionInMap(_cli.info, _ncda.getId(), _ncda,
					CollectionsUtilities.HASH_SET_FACTORY);
			}

			boolean _flag = true;

			for (int _i = 0; _i < _dasOptions.length; _i++) {
				if (_cl.hasOption(_dasOptions[_i][0].toString())) {
					final Object _da = _dasOptions[_i][2];
					_cli.das.add(_da);
					_flag = false;

					if (_da instanceof InterferenceDAv1) {
						((InterferenceDAv1) _da).setUseOFA(_cl.hasOption("ofaforinterference"));
					}

					if (_da instanceof ReadyDAv1) {
						((ReadyDAv1) _da).setUseOFA(_cl.hasOption("ofaforready"));
						((ReadyDAv1) _da).setUseSafeLockAnalysis(_cli.useSafeLockAnalysis);
					}
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
		final PairManager _pairManager = new PairManager(false, true);
		final ICallGraphInfo _cgi = new CallGraph(_pairManager);
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, new CFGAnalysis(_cgi, getBbm()), _pairManager);
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
			_aliasUD = new AliasedUseDefInfo(aa, bbm, _pairManager);
		} else {
			_aliasUD = new AliasedUseDefInfov2(aa, _cgi, _tgi, bbm, _pairManager);
		}
		info.put(ICallGraphInfo.ID, _cgi);
		info.put(IThreadGraphInfo.ID, _tgi);
		info.put(PairManager.ID, _pairManager);
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, _aliasUD);

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, getBbm());
		info.put(EquivalenceClassBasedEscapeAnalysis.ID, _ecba);

		final IMonitorInfo _monitorInfo = new MonitorAnalysis();
		info.put(IMonitorInfo.ID, _monitorInfo);

		final SafeLockAnalysis _sla;

		if (useSafeLockAnalysis) {
			_sla = new SafeLockAnalysis();
			info.put(SafeLockAnalysis.ID, _sla);
		} else {
			_sla = null;
		}

		initialize();
		aa.analyze(new Environment(getScene()), getRootMethods());

		((CallGraph) _cgi).reset();
		_processors.clear();
		_processors.add(_cgi);
		_pc.reset();
		_pc.driveProcessors(_processors);
		writeInfo("CALL GRAPH:\n" + ((CallGraph) _cgi).toString());

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add(_tgi);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);
		writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).toString());

		_aliasUD.hookup(_cgipc);
		_cgipc.process();
		_aliasUD.unhook(_cgipc);

		writeInfo("BEGIN: dependency analyses");

		final AnalysesController _ac = new AnalysesController(info, _cgipc, getBbm());
		_ac.addAnalyses(IMonitorInfo.ID, Collections.singleton(_monitorInfo));
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(_ecba));

		if (useSafeLockAnalysis) {
			_ac.addAnalyses(SafeLockAnalysis.ID, Collections.singleton(_sla));
		}

		for (final Iterator _i1 = das.iterator(); _i1.hasNext();) {
			final IDependencyAnalysis _da1 = (IDependencyAnalysis) _i1.next();
			_ac.addAnalyses(_da1.getId(), Collections.singleton(_da1));
		}

		_ac.initialize();
		_ac.execute();

		// write xml
		for (final Iterator _i1 = das.iterator(); _i1.hasNext();) {
			final IDependencyAnalysis _da1 = (IDependencyAnalysis) _i1.next();
			CollectionsUtilities.putIntoListInMap(info, _da1.getId(), _da1);
		}
		xmlizer.setGenerator(new UniqueJimpleIDGenerator());
		xmlizer.writeXML(info);

		if (dumpJimple) {
			xmlizer.dumpJimple(null, xmlizer.getXmlOutputDir(), _xmlcgipc);
		}
		writeInfo("Total classes loaded: " + getScene().getClasses().size());
		printTimingStats();
	}
}

// End of File
