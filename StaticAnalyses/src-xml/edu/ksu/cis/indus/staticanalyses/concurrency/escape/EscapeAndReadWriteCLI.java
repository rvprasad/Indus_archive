
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Local;
import soot.SootField;
import soot.SootMethod;


/**
 * This is a command line interface to exercise side effect and escape information analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class EscapeAndReadWriteCLI
  extends SootBasedDriver {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(EscapeAndReadWriteCLI.class);

	/**
	 * The entry point to this class.
	 *
	 * @param args command line arguments.
	 *
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

		final CommandLineParser _parser = new GnuParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				final String _cmdLineSyn = "java " + EscapeAndReadWriteCLI.class.getName() + " <options> <classnames>";
				(new HelpFormatter()).printHelp(_cmdLineSyn, _options);
				System.exit(1);
			}

			if (_cl.getArgList().isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}

			final EscapeAndReadWriteCLI _cli = new EscapeAndReadWriteCLI();

			if (_cl.hasOption('p')) {
				_cli.addToSootClassPath(_cl.getOptionValue('p'));
			}

			_cli.setClassNames(_cl.getArgList());
			_cli.execute();
		} catch (final ParseException _e) {
			LOGGER.fatal("Error while parsing command line.", _e);

			final String _cmdLineSyn = "java " + EscapeAndReadWriteCLI.class.getName() + " <options> <classnames>";
			(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are:", _options, "");
		} catch (final Throwable _e) {
			LOGGER.fatal("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * This contains the driver logic.
	 */
	private void execute() {
		setLogger(LOGGER);

		final String _tagName = "SideEffect:FA";
		final IValueAnalyzer _aa =
			OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager(new SootValueTypeManager()));
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
		_aa.analyze(new Environment(getScene()), getRootMethods());

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
        final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, _tgi, getBbm());
		final IReadWriteInfo _rwInfo = _ecba.getReadWriteInfo();
		final IEscapeInfo _escapeInfo = _ecba.getEscapeInfo();
		final AnalysesController _ac = new AnalysesController(_info, _cgipc, getBbm());
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(_ecba));
		_ac.initialize();
		_ac.execute();
		writeInfo("END: Escape analysis");

		System.out.println("ReadWrite-Effect and Escape Information:");
        final String[] _emptyStringArray = new String[0];

		for (final Iterator _i = _cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			System.out.println("Method: " + _sm.getSignature());
            if (!_sm.isStatic()) {
				System.out.println("\tthis:");
				System.out.println("\t\thread =  " + _rwInfo.isThisBasedAccessPathRead(_sm, _emptyStringArray, true));
				System.out.println("\t\twritten =  " + _rwInfo.isThisBasedAccessPathWritten(_sm, _emptyStringArray, true));
				System.out.println("\t\tescapes = " + _escapeInfo.thisEscapes(_sm));
				System.out.println("\t\tfield reading threads = " + _escapeInfo.getReadingThreadsOfThis(_sm));
				System.out.println("\t\tfield writing threads = " + _escapeInfo.getWritingThreadsOfThis(_sm));

				for (final Iterator _j = _sm.getDeclaringClass().getFields().iterator(); _j.hasNext();) {
					final String[] _accessPath = { ((SootField) _j.next()).getSignature() };
					System.out.println("\t\t\t" + _accessPath[0] + ": ["
						+ _rwInfo.isThisBasedAccessPathRead(_sm, _accessPath, true) + ", "
						+ _rwInfo.isThisBasedAccessPathWritten(_sm, _accessPath, true) + "]");
				}
			}

			for (int _j = 0; _j < _sm.getParameterCount(); _j++) {
				System.out.println("\tParam" + (_j + 1) + "[" + _sm.getParameterType(_j) + "]:");
				System.out.println("\t\thread = " + _rwInfo.isParameterBasedAccessPathRead(_sm, _j, _emptyStringArray, true));
				System.out.println("\t\twritten = " + _rwInfo.isParameterBasedAccessPathWritten(_sm, _j, _emptyStringArray, true));
				System.out.println("\t\tfield reading threads: " + _escapeInfo.getReadingThreadsOf(_j, _sm));
				System.out.println("\t\tfield writing threads: " + _escapeInfo.getWritingThreadsOf(_j, _sm));
			}

			if (_sm.hasActiveBody()) {
				final Body _body = _sm.getActiveBody();

				for (final Iterator _j = _body.getLocals().iterator(); _j.hasNext();) {
					final Local _local = (Local) _j.next();
					System.out.println("\tLocal " + _local.getName() + " : ");
					System.out.println("\t\tescapes = " + _escapeInfo.escapes(_local, _sm));
					System.out.println("\t\tfield reading threads: " + _escapeInfo.getReadingThreadsOf(_local, _sm));
					System.out.println("\t\tfield writing threads: " + _escapeInfo.getWritingThreadsOf(_local, _sm));
				}
			}
		}
	}
}

// End of File
