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

package edu.ksu.cis.indus.staticanalyses.concurrency.independence;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.independence.IndependentRegionDetector;
import edu.ksu.cis.indus.staticanalyses.concurrency.independence.IndependentStmtDetector;
import edu.ksu.cis.indus.staticanalyses.concurrency.independence.IndependentStmtDetectorv2;
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
import java.util.Iterator;
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

import soot.Body;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.Stmt;

/**
 * This is a command-line interface to drive independence detection implementation in Indus. This interface will generate
 * annotated jimple.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> dummy type parameter.
 */
public final class IndependenceDetectionCLI<T extends ITokens<T, Value>>
		extends SootBasedDriver {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndependenceDetectionCLI.class);

	/**
	 * The detector to be used.
	 */
	private final IndependentStmtDetector detector;

	/**
	 * The directory in which to dump jimple.
	 */
	private String outputDir;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param arg the detector to use.
	 * @pre arg != null
	 */
	public IndependenceDetectionCLI(final IndependentStmtDetector arg) {
		detector = arg;
	}

	/**
	 * The entry point to the program via command line.
	 * 
	 * @param args is the command line arguments.
	 * @throws RuntimeException when CLI fails.
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("o", "output", true, "Directory into which jimple files will be written into. [required]");
		_option.setArgs(1);
		_option.setArgName("ouput-directory");
		_option.setRequired(true);
		_options.addOption(_option);
		_option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("useV2", false, "Use version 2 of the atomicity detection algorithm.");
		_options.addOption(_option);
		_option = new Option("scheme", false,
				"Scheme to indicate atomicity. Valid values are 'tag-stmt' and 'tag-region'.  By default, 'tag-stmt' "
						+ "scheme is used. ");
		_option.setArgs(1);
		_option.setArgName("scheme-name");
		_options.addOption(_option);

		final CommandLineParser _parser = new GnuParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				final String _cmdLineSyn = "java " + IndependenceDetectionCLI.class.getName() + " <options> <classnames>";
				(new HelpFormatter()).printHelp(_cmdLineSyn, _options);
				System.exit(1);
			}

			if (_cl.getArgList().isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}

			final IndependenceDetectionCLI _cli;

			if (_cl.hasOption("useV2")) {
				_cli = new IndependenceDetectionCLI(new IndependentStmtDetectorv2());
			} else {
				_cli = new IndependenceDetectionCLI(new IndependentStmtDetector());
			}

			if (_cl.hasOption('p')) {
				_cli.addToSootClassPath(_cl.getOptionValue('p'));
			}

			_cli.setClassNames(_cl.getArgList());
			_cli.setOutputDir(_cl.getOptionValue('o'));
			_cli.execute(_cl);
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);

			final String _cmdLineSyn = "java " + IndependenceDetectionCLI.class.getName() + " <options> <classnames>";
			(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are:", _options, "");
		} catch (final Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * Sets the output directory.
	 * 
	 * @param arg is the output directory.
	 * @pre arg != null
	 */
	private void setOutputDir(final String arg) {
		outputDir = arg;
	}

	/**
	 * Annotates the atomic statements that are in the methods reachable in given call graph.
	 * 
	 * @param cgi provides the call graph.
	 * @pre cgi != null
	 */
	private void annotateAtomicStmts(final ICallGraphInfo cgi) {
		final NamedTag _atomicTag = new NamedTag("AtomicTag");

		for (final Iterator<SootMethod> _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = _i.next();

			if (_sm.isConcrete()) {
				final Body _body = getBbm().getStmtGraph(_sm).getBody();
				final Collection<Stmt> _sl = _body.getUnits();

				final Iterator<Stmt> _j = _sl.iterator();
				final int _jEnd = _sl.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final Stmt _stmt = _j.next();

					if (detector.isIndependent(_stmt)) {
						_stmt.addTag(_atomicTag);
					}
				}
			}
		}
	}

	/**
	 * Executes atomicity detection algorithm according to given option.
	 * 
	 * @param cl is the command line.
	 * @pre cl != null
	 */
	private void execute(final CommandLine cl) {
		setInfoLogger(LOGGER);

		final String _tagName = "AtomicityDetection:FA";
		final IValueAnalyzer _aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil
				.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final PairManager _pairManager = new PairManager(false, true);
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, new CFGAnalysis(_cgi, getBbm()), _pairManager);
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();

		_ssr.setStmtGraphFactory(getStmtGraphFactory());

		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(_aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		_cgipc.setStmtSequencesRetriever(_ssr);
		_cgipc.setAnalyzer(_aa);
		_cgipc.setProcessingFilter(new CGBasedProcessingFilter(_cgi));

		final Map _info = new HashMap();
		_info.put(ICallGraphInfo.ID, _cgi);
		_info.put(IThreadGraphInfo.ID, _tgi);
		_info.put(PairManager.ID, _pairManager);
		_info.put(IEnvironment.ID, _aa.getEnvironment());
		_info.put(IValueAnalyzer.ID, _aa);

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, null, getBbm());
		final IEscapeInfo _escapeInfo = _ecba.getEscapeInfo();
		_info.put(IEscapeInfo.ID, _escapeInfo);

		initialize();
		_aa.analyze(new Environment(getScene()), getRootMethods());

		_callGraphInfoCollector.reset();
		_processors.clear();
		_processors.add(_callGraphInfoCollector);
		_pc.reset();
		_pc.driveProcessors(_processors);
		_cgi.reset();
		_cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());
		writeInfo("CALL GRAPH:\n" + _cgi.toString());

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add(_tgi);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);
		writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).toString());

		final AnalysesController _ac = new AnalysesController(_info, _cgipc, getBbm());
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(_ecba));
		_ac.initialize();
		_ac.execute();
		writeInfo("END: Escape analysis");

		detector.setEscapeAnalysis(_escapeInfo);
		detector.hookup(_cgipc);
		_cgipc.process();
		detector.unhook(_cgipc);
		writeInfo("BEGIN: Independent statement detection");

		final String _optionValue = cl.getOptionValue("scheme", "tag-stmt");

		if (_optionValue.equals("tag-region")) {
			final IndependentRegionDetector _regionDetector = new IndependentRegionDetector();
			_regionDetector.setAtomicityDetector(detector);
			_regionDetector.setBasicBlockGraphMgr(getBbm());
			_regionDetector.hookup(_cgipc);
			_cgipc.process();
			_regionDetector.unhook(_cgipc);
			insertAtomicBoundaries(_regionDetector, _cgi);
		} else {
			annotateAtomicStmts(_cgi);
		}
		writeInfo("END: Independent region detection");
		dumpJimpleAndClassFiles(outputDir, true, false);
	}

	/**
	 * Annotates statements indicating boundaries of atomic regions.
	 * 
	 * @param regionDetector to be used.
	 * @param cgi provides the call graph of the reachable methods which need to be annotated.
	 * @pre regionDetector != null and cgi != null
	 */
	private void insertAtomicBoundaries(final IndependentRegionDetector regionDetector, final ICallGraphInfo cgi) {
		final NamedTag _atomicBeginBeforeTag = new NamedTag("Atomic-Begin-Before-Tag");
		final NamedTag _atomicBeginAfterTag = new NamedTag("Atomic-Begin-After-Tag");
		final NamedTag _atomicEndBeforeTag = new NamedTag("Atomic-End-Before-Tag");
		final NamedTag _atomicEndAfterTag = new NamedTag("Atomic-End-After-Tag");

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			for (final Iterator _j = regionDetector.getAtomicRegionBeginBeforeBoundariesFor(_sm).iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();
				_stmt.addTag(_atomicBeginBeforeTag);
			}

			for (final Iterator _j = regionDetector.getAtomicRegionBeginAfterBoundariesFor(_sm).iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();
				_stmt.addTag(_atomicBeginAfterTag);
			}

			for (final Iterator _j = regionDetector.getAtomicRegionEndBeforeBoundariesFor(_sm).iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();
				_stmt.addTag(_atomicEndBeforeTag);
			}

			for (final Iterator _j = regionDetector.getAtomicRegionEndAfterBoundariesFor(_sm).iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();
				_stmt.addTag(_atomicEndAfterTag);
			}
		}
	}
}

// End of File
