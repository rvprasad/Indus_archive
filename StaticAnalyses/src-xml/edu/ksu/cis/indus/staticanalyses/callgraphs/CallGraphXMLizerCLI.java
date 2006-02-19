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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizerCLI;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAXMLizerCLI;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.impl.ClassHierarchy;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;
import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Type;
import soot.Value;

/**
 * This class provides the command line interface to xmlize call graphs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CallGraphXMLizerCLI
		extends SootBasedDriver {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CallGraphXMLizerCLI.class);

	/**
	 * The xmlizer to be used.
	 */
	private final CallGraphXMLizer xmlizer = new CallGraphXMLizer();

	/**
	 * This indicates if cumulative or separate call graphs should be generated when there are more than one root methods.
	 */
	private boolean cumulative;

	/**
	 * The entry point to the program via command line.
	 * 
	 * @param args is the command line arguments.
	 * @throws RuntimeException when the analyses fail.
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("c", "cumulative", false, "Builds one call graph that includes all root methods.");
		_options.addOption(_option);
		_option = new Option("o", "output", true,
				"Directory into which xml files will be written into.  Defaults to current directory if omitted");
		_option.setArgs(1);
		_option.setArgName("output-dir");
		_options.addOption(_option);
		_option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		_option.setArgName("dump-jimple");
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", true, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("t", "call-graph-type", true, "Call graph type.  This has to be one of {cha, rta, ofa-oi, "
				+ "ofa-oirt, ofa-os}.");
		_option.setArgs(1);
		_option.setArgName("type");
		_option.setRequired(true);
		_options.addOption(_option);
		_option = new Option("S", "scope", true, "The scope that should be analyzed.");
		_option.setArgs(1);
		_option.setArgName("scope");
		_option.setRequired(false);
		_options.addOption(_option);

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption('h')) {
				printUsage(_options);
				System.exit(1);
			}

			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}

			if (_cl.getArgList().isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}

			final CallGraphXMLizerCLI _cli = new CallGraphXMLizerCLI();

			_cli.xmlizer.setXmlOutputDir(_outputDir);
			_cli.xmlizer.setGenerator(new UniqueJimpleIDGenerator());
			_cli.setCumulative(_cl.hasOption('c'));
			_cli.setClassNames(_cl.getArgList());
			_cli.addToSootClassPath(_cl.getOptionValue('p'));

			if (_cl.hasOption('S')) {
				_cli.setScopeSpecFile(_cl.getOptionValue('S'));
			}

			_cli.initialize();

			_cli.execute(_cl.hasOption('j'), _cl.getOptionValue('t'));
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			System.out.println("Error while parsing command line." + _e);
			printUsage(_options);
		} catch (final Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * Prints the help/usage info for this class.
	 * 
	 * @param options is the command line option.
	 * @pre options != null
	 */
	private static void printUsage(final Options options) {
		final String _cmdLineSyn = "java " + DependencyXMLizerCLI.class.getName() + " <options> <classnames>";
		(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", options, "");
	}

	/**
	 * Sets cumulative mode.
	 * 
	 * @param option <code>true</code> indicates one cumulative call graph for all root methods; <code>false</code>
	 *            indicates separate call graphs for each root method.
	 */
	private void setCumulative(final boolean option) {
		cumulative = option;
	}

	/**
	 * Dumps information onto the stdout and as jimple.
	 * 
	 * @param dumpJimple <code>true</code> indicate that jimple should be dumped; <code>false</code>, otherwise.
	 * @param cgi to be dumped.
	 * @param fileBaseName provides the base for the name of the file.
	 * @param env for which call graph was generated.
	 * @pre cgi != null and fileBaseName != null and env != null
	 */
	private void dumpInfo(final boolean dumpJimple, final ICallGraphInfo cgi, final String fileBaseName,
			final IEnvironment env) {
		final Map _info = new HashMap();
		_info.put(ICallGraphInfo.ID, cgi);

		final ProcessingController _xmlcgipc = new ProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_xmlcgipc.setStmtSequencesRetriever(_ssr);
		_xmlcgipc.setEnvironment(env);
		_xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(cgi));

		_info.put(AbstractXMLizer.FILE_NAME_ID, fileBaseName);
		_info.put(IStmtGraphFactory.ID, getStmtGraphFactory());
		xmlizer.writeXML(_info);

		if (dumpJimple) {
			((AbstractXMLizer) xmlizer).dumpJimple(fileBaseName, xmlizer.getXmlOutputDir(), _xmlcgipc);
		}

		System.out.println(cgi.toString());
	}

	/**
	 * Xmlize the given system.
	 * 
	 * @param dumpJimple <code>true</code> indicates xmlized jimple should be dumped; <code>false</code>, otherwise.
	 * @param type of call graph analysis.
	 */
	private void execute(final boolean dumpJimple, final String type) {
		setInfoLogger(LOGGER);

		if (type.equals("cha")) {
			executeCHA(dumpJimple);
		} else if (type.equals("rta")) {
			executeRTA(dumpJimple);
		} else if (type.indexOf("ofa") == 0) {
			this.<ITokens> executeOFA(dumpJimple, type);
		}
	}

	/**
	 * Executed CHA-based call graph analysis.
	 * 
	 * @param dumpJimple <code>true</code> indicate that jimple should be dumped; <code>false</code>, otherwise.
	 */
	private void executeCHA(final boolean dumpJimple) {
		final ClassHierarchy _cha = new ClassHierarchy();
		final ProcessingController _pc = new ProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setEnvironment(getEnvironment());
		_cha.hookup(_pc);
		_pc.process();
		_cha.unhook(_pc);

		final CHABasedCallInfoCollector _chaci = new CHABasedCallInfoCollector();
		_chaci.initialize(_cha);
		_chaci.hookup(_pc);
		_pc.process();
		_chaci.unhook(_pc);

		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		_cgi.createCallGraphInfo(_chaci.getCallInfo());

		dumpInfo(dumpJimple, _cgi, "CHA-Based", getEnvironment());
	}

	/**
	 * Executed OFA-based call graph analysis.
	 * 
	 * @param dumpJimple <code>true</code> indicate that jimple should be dumped; <code>false</code>, otherwise.
	 * @param <T> dummy type parameter.
	 */
	private <T extends ITokens<T, Value>> void executeOFA(final boolean dumpJimple, final String OFAType) {
		final String _tagName = "CallGraphXMLizer:FA";
		final IValueAnalyzer<Value> _aa;
		if (OFAType.equals("ofa-oi"))
			_aa = OFAnalyzer.getFSOIAnalyzer(_tagName,
					TokenUtil.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		else if (OFAType.equals("ofa-oirt"))
			_aa = OFAnalyzer.getFSOIRTAnalyzer(_tagName, TokenUtil
					.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		else if (OFAType.equals("ofa-os"))
			_aa = OFAnalyzer.getFSOSAnalyzer(_tagName,
					TokenUtil.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		else {
			throw new IllegalArgumentException("callgraph-type has to be one of the following: ofa-oi, ofa-oirt, ofa-os,"
					+ " fsoirt.");
		}
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection<IProcessor> _processors = new ArrayList<IProcessor>();
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final OFABasedCallInfoCollector _ofaci = new OFABasedCallInfoCollector();
		final Collection<SootMethod> _rm = new ArrayList<SootMethod>();
		final MetricsProcessor _countingProcessor = new MetricsProcessor();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(_aa);
		_pc.setEnvironment(getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		final List<Object> _roots = new ArrayList<Object>();

		if (cumulative) {
			_roots.add(getRootMethods());
		} else {
			_roots.addAll(getRootMethods());
		}
		Collections.sort(_roots, ToStringBasedComparator.getComparator());
		writeInfo("Root methods are: " + _roots.size() + "\n" + _roots);

		for (final Iterator<Object> _k = _roots.iterator(); _k.hasNext();) {
			_rm.clear();

			final Object _root = _k.next();
			final String _fileBaseName = "OFA-Based_" + OFAXMLizerCLI.getBaseNameOfFileAndRootMethods(_root, _rm);
			writeInfo("RootMethod: " + _root);
			writeInfo("BEGIN: FA");

			final long _start = System.currentTimeMillis();
			_aa.reset();
			getBbm().reset();

			_aa.analyze(getEnvironment(), _rm);

			final long _stop = System.currentTimeMillis();
			addTimeLog("FA", _stop - _start);
			writeInfo("END: FA");

			_ofaci.reset();
			_processors.clear();
			_processors.add(_ofaci);
			_processors.add(_countingProcessor);
			_pc.reset();
			_pc.driveProcessors(_processors);
			_processors.clear();
			_cgi.reset();
			_cgi.createCallGraphInfo(_ofaci.getCallInfo());

			final ByteArrayOutputStream _stream = new ByteArrayOutputStream();
			new PrintWriter(_stream).write("STATISTICS: "
					+ MapUtils.verbosePrint(new TreeMap(_countingProcessor.getStatistics())));
			writeInfo(_stream.toString());

			dumpInfo(dumpJimple, _cgi, _fileBaseName, _aa.getEnvironment());
		}
	}

	/**
	 * Executed RTA-based call graph analysis.
	 * 
	 * @param dumpJimple <code>true</code> indicate that jimple should be dumped; <code>false</code>, otherwise.
	 */
	private void executeRTA(final boolean dumpJimple) {
		final ClassHierarchy _cha = new ClassHierarchy();
		final ProcessingController _pc = new ProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setEnvironment(getEnvironment());
		_cha.hookup(_pc);
		_pc.process();
		_cha.unhook(_pc);

		final CHABasedCallInfoCollector _chaci = new CHABasedCallInfoCollector();
		_chaci.initialize(_cha);
		_chaci.hookup(_pc);
		_pc.process();
		_chaci.unhook(_pc);

		final RTABasedCallInfoCollector _rtaci = new RTABasedCallInfoCollector();
		_rtaci.setRootMethods(getRootMethods());
		_rtaci.initialize(_chaci, _cha);
		_rtaci.hookup(_pc);
		_pc.process();
		_rtaci.unhook(_pc);

		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		_cgi.createCallGraphInfo(_rtaci.getCallInfo());

		dumpInfo(dumpJimple, _cgi, "RTA-Based", getEnvironment());
	}
}

// End of File
