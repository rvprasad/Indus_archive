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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;

/**
 * This is a command line interface to exercise deadlock analysis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DeadlockAnalysisCLI
		extends SootBasedDriver {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DeadlockAnalysisCLI.class);

	/**
	 * The entry point to this class.
	 * 
	 * @param args command line arguments.
	 * @throws RuntimeException when escape information and side-effect information calculation fails.
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("S", "scope", true, "The scope that should be analyzed.");
		_option.setArgs(1);
		_option.setArgName("scope");
		_option.setRequired(false);
		_options.addOption(_option);

		final CommandLineParser _parser = new GnuParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				final String _cmdLineSyn = "java " + DeadlockAnalysisCLI.class.getName() + " <options> <classnames>";
				(new HelpFormatter()).printHelp(_cmdLineSyn, _options);
				System.exit(1);
			}

			if (_cl.getArgList().isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}

			final DeadlockAnalysisCLI _cli = new DeadlockAnalysisCLI();

			if (_cl.hasOption('p')) {
				_cli.addToSootClassPath(_cl.getOptionValue('p'));
			}

			if (_cl.hasOption('S')) {
				_cli.setScopeSpecFile(_cl.getOptionValue('S'));
			}
			_cli.setClassNames(_cl.getArgList());
			_cli.<ITokens> execute();
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			System.out.println("Error while parsing command line." + _e);
			final String _cmdLineSyn = "java " + DeadlockAnalysisCLI.class.getName() + " <options> <classnames>";
			(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are:", _options, "");
		} catch (final Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * This contains the driver logic.
	 * 
	 * @param <T> dummy type parameter.
	 */
	private <T extends ITokens<T, Value>> void execute() {
		setInfoLogger(LOGGER);

		final String _tagName = "SideEffect:FA";
		final IValueAnalyzer<Value> _aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil
				.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection<IProcessor> _processors = new ArrayList<IProcessor>();
		final PairManager _pairManager = new PairManager(false, true);
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final MonitorAnalysis _monitorInfo = new MonitorAnalysis();
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, new CFGAnalysis(_cgi, getBbm()), _pairManager);
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();

		_ssr.setStmtGraphFactory(getStmtGraphFactory());

		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(_aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		_cgipc.setAnalyzer(_aa);
		_cgipc.setProcessingFilter(new CGBasedProcessingFilter(_cgi));
		_cgipc.setStmtSequencesRetriever(_ssr);

		final Map _info = new HashMap();
		_info.put(ICallGraphInfo.ID, _cgi);
		_info.put(IThreadGraphInfo.ID, _tgi);
		_info.put(PairManager.ID, _pairManager);
		_info.put(IEnvironment.ID, _aa.getEnvironment());
		_info.put(IValueAnalyzer.ID, _aa);

		initialize();
		_aa.analyze(getEnvironment(), getRootMethods());

		_processors.clear();
		_processors.add(_callGraphInfoCollector);
		_pc.reset();
		_pc.driveProcessors(_processors);
		_cgi.reset();
		_cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());
		writeInfo("CALL GRAPH:\n" + _cgi.toString());

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add((IProcessor) _tgi);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);
		writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).toString());
		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, _tgi, getBbm());
		final IEscapeInfo _escapeInfo = _ecba.getEscapeInfo();
		final AnalysesController _ac = new AnalysesController(_info, _cgipc, getBbm());
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(_ecba));
		_ac.addAnalyses(IMonitorInfo.ID, Collections.singleton(_monitorInfo));
		_ac.initialize();
		_ac.execute();
		writeInfo("END: Escape analysis");

		final LockAcquisitionBasedEquivalence _lbe = new LockAcquisitionBasedEquivalence(_escapeInfo, _cgi);
		_lbe.hookup(_cgipc);
		_cgipc.process();
		_lbe.unhook(_cgipc);

		System.out.println("Deadlock Analysis:");
		System.out.println("Total number of monitors: " + _monitorInfo.getMonitorTriples().size());
		calculateDeadlockInfo(_aa, _monitorInfo, null, null);
		calculateDeadlockInfo(_aa, _monitorInfo, _escapeInfo, null);
		calculateDeadlockInfo(_aa, _monitorInfo, null, _lbe);
		calculateDeadlockInfo(_aa, _monitorInfo, _escapeInfo, _lbe);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param aa DOCUMENT ME!
	 * @param monitorInfo DOCUMENT ME!
	 * @param escapeInfo DOCUMENT ME!
	 * @param lbe DOCUMENT ME!
	 */
	private void calculateDeadlockInfo(final IValueAnalyzer<Value> aa, final MonitorAnalysis monitorInfo,
			final IEscapeInfo escapeInfo, final LockAcquisitionBasedEquivalence lbe) {
		final DeadlockAnalysis _dla = new DeadlockAnalysis(monitorInfo, aa, lbe, escapeInfo);
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _deadlockingMonitors = _dla
				.getDeadlockingMonitors();
		System.out.println("Deadlocking Monitors: " + _deadlockingMonitors.size() + " -- using escapeInfo :"
				+ (escapeInfo != null) + " -- using locking based equiv : " + (lbe != null));
		for (final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _m : _deadlockingMonitors) {
			System.out.println("\t" + _m);
		}
	}
}

// End of File
