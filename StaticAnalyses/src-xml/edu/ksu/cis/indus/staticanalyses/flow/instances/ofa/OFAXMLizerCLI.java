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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;
import edu.ksu.cis.indus.common.soot.MetricsProcessor.MetricKeys;
import edu.ksu.cis.indus.processing.IProcessingFilter;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
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
 * This class provides a command-line interface to xmlize object flow information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class OFAXMLizerCLI
		extends SootBasedDriver {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OFAXMLizerCLI.class);

	/**
	 * This indicates if analysis should be run for all root methods or separated for each root method.
	 */
	private boolean cumulative;

	/**
	 * The type/sort of OFA to use.
	 */
	private String type;

	/**
	 * The xmlizer used to xmlize information.
	 */
	private final OFAXMLizer xmlizer = new OFAXMLizer();

	/**
	 * Retrieves the name that serves as the base for the file names into which info will be dumped along with the root
	 * methods to be considered in one execution of the analyses.
	 * 
	 * @param root is the object based on which base name should be generated.
	 * @param methods is the collection that will contain the root methods upon return.
	 * @return a name along with the root methods via <code>methods</code>.
	 * @pre root != null and methods != null
	 * @post result != null and (methods.contains(root) or methods.containsAll(root))
	 */
	public static String getBaseNameOfFileAndRootMethods(final Object root, final Collection methods) {
		final String _result;

		if (root instanceof SootMethod) {
			final SootMethod _sm = (SootMethod) root;
			_result = (_sm.getDeclaringClass().getJavaStyleName() + "_" + _sm.getSubSignature()).replaceAll(" ", "_");
			methods.add(_sm);
		} else {
			_result = "cumulative_call_graph" + System.currentTimeMillis();
			methods.addAll((Collection) root);
		}
		return _result;
	}

	/**
	 * The entry point to the program via command line.
	 * 
	 * @param args is the command line arguments.
	 * @throws RuntimeException when object flow analysis fails.
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("c", "cumulative", false, "Consider all root methods in the same execution.");
		_options.addOption(_option);
		_option = new Option("o", "output", true, "Directory into which xml files will be written into.");
		_option.setArgs(1);
		_options.addOption(_option);
		_option = new Option("j", "jimple", false, "Dump xmlized jimple.");
		_options.addOption(_option);
		_option = new Option("h", "help", false, "Display message.");
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("t", "ofa-type", false, "Type of analysis : fioi, fsoi, fios, fsos, fioirt, fsoirt.");
		_option.setArgs(1);
		_option.setArgName("type");
		_option.setOptionalArg(false);
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

			if (_cl.hasOption("h")) {
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

			final OFAXMLizerCLI _cli = new OFAXMLizerCLI();
			_cli.xmlizer.setXmlOutputDir(_outputDir);
			_cli.xmlizer.setGenerator(new UniqueJimpleIDGenerator());
			_cli.setCumulative(_cl.hasOption('c'));
			_cli.setClassNames(_cl.getArgList());
			_cli.type = _cl.getOptionValue('t');

			if (_cl.hasOption('p')) {
				_cli.addToSootClassPath(_cl.getOptionValue('p'));
			}

			if (_cl.hasOption('S')) {
				_cli.setScopeSpecFile(_cl.getOptionValue('S'));
			}

			_cli.initialize();

			_cli.<ITokens> execute(_cl.hasOption('j'));
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			System.out.println("Error while parsing command line. \n" + _e);
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
		final String _cmdLineSyn = "java " + OFAXMLizerCLI.class.getName() + " <options> <classnames>";
		(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", options, "");
	}

	/**
	 * Xmlize the given system.
	 * 
	 * @param dumpJimple <code>true</code> indicates that the jimple should be xmlized as well; <code>false</code>,
	 *            otherwise.
	 * @param <T> dummy type parameter.
	 */
	private <T extends ITokens<T, Value>> void execute(final boolean dumpJimple) {
		setInfoLogger(LOGGER);

		final String _tagName = "CallGraphXMLizer:FA";
		final IValueAnalyzer<Value> _aa;
		if (type.equals("fioi")) {
			_aa = OFAnalyzer.getFIOIAnalyzer(_tagName,
					TokenUtil.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		} else if (type.equals("fios")) {
			_aa = OFAnalyzer.getFIOSAnalyzer(_tagName,
					TokenUtil.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		} else if (type.equals("fsoi")) {
			_aa = OFAnalyzer.getFSOIAnalyzer(_tagName,
					TokenUtil.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		} else if (type.equals("fsos")) {
			_aa = OFAnalyzer.getFSOSAnalyzer(_tagName,
					TokenUtil.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		} else if (type.equals("fioirt")) {
			_aa = OFAnalyzer.getFSOIRTAnalyzer(_tagName, TokenUtil
					.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		} else if (type.equals("fsoirt")) {
			_aa = OFAnalyzer.getFSOIRTAnalyzer(_tagName, TokenUtil
					.<T, Value, Type> getTokenManager(new SootValueTypeManager()), getStmtGraphFactory());
		} else {
			throw new IllegalArgumentException("ofa-type has to be one of the following: fioi, fsoi, fios, fsos, fioirt,"
					+ " fsoirt.");
		}

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection<IProcessor> _processors = new ArrayList<IProcessor>();
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		final Collection<SootMethod> _rm = new ArrayList<SootMethod>();
		final ProcessingController _xmlcgipc = new ProcessingController();
		final MetricsProcessor _countingProcessor = new MetricsProcessor();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(_aa);

		final IProcessingFilter _tagFilter = new TagBasedProcessingFilter(_tagName);
		_pc.setProcessingFilter(_tagFilter);
		_xmlcgipc.setEnvironment(_aa.getEnvironment());

		final IProcessingFilter _xmlFilter = new CGBasedXMLizingProcessingFilter(_cgi);
		_xmlFilter.chain(_tagFilter);
		_xmlcgipc.setProcessingFilter(_xmlFilter);
		_xmlcgipc.setStmtSequencesRetriever(_ssr);

		final Map _info = new HashMap();
		_info.put(IValueAnalyzer.ID, _aa);
		_info.put(IValueAnalyzer.TAG_ID, _tagName);

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
			final String _fileBaseName = getBaseNameOfFileAndRootMethods(_root, _rm);

			writeInfo("RootMethod: " + _root);
			writeInfo("BEGIN: FA");

			final long _start = System.currentTimeMillis();
			_aa.reset();
			getBbm().reset();

			_aa.analyze(getEnvironment(), _rm);

			final long _stop = System.currentTimeMillis();
			addTimeLog("FA", _stop - _start);
			writeInfo("END: FA");

			_callGraphInfoCollector.reset();
			_processors.clear();
			_processors.add(_callGraphInfoCollector);
			_processors.add(_countingProcessor);
			_pc.reset();
			_pc.driveProcessors(_processors);
			_processors.clear();
			_cgi.reset();
			_cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());

			final ByteArrayOutputStream _stream = new ByteArrayOutputStream();
			new PrintWriter(_stream).write("STATISTICS: "
					+ MapUtils
							.verbosePrint(new TreeMap<MetricKeys, Map<Object, Integer>>(_countingProcessor.getStatistics())));
			writeInfo(_stream.toString());

			_info.put(AbstractXMLizer.FILE_NAME_ID, _fileBaseName);
			_info.put(IStmtGraphFactory.ID, getStmtGraphFactory());
			xmlizer.writeXML(_info);

			if (dumpJimple) {
				((AbstractXMLizer) xmlizer).dumpJimple(_fileBaseName, xmlizer.getXmlOutputDir(), _xmlcgipc);
			}

			final OFAStringizer _str = new OFAStringizer();
			_str.getOFAInfoAsString(_info, new PrintWriter(System.out));
		}
	}

	/**
	 * Sets cumulative mode.
	 * 
	 * @param option <code>true</code> indicates all root methods should be analyzed in one go; <code>false</code>
	 *            indicates analysis should be executed once for each root method.
	 */
	private void setCumulative(final boolean option) {
		cumulative = option;
	}
}

// End of File
