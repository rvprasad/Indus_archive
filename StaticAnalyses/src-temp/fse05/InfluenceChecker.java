
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

package fse05;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.common.soot.RootMethodTrapper;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.callgraphs.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CHABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.SafeLockAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationSensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.impl.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processors.StaticFieldUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class provides a command-line interface to xmlize dependence information.  Refer to <code>SootBasedDriver</code> for
 * more configuration infomration.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class InfluenceChecker
  extends SootBasedDriver {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(InfluenceChecker.class);

	/** 
	 * This is the flow analyser used by the analyses being tested.
	 */
	protected IValueAnalyzer aa;

	/** 
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(AbstractDependencyAnalysis))
	 */
	List das = new ArrayList();

	/** 
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Collection cdas = new ArrayList();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Collection ddas = new ArrayList();

	/** 
	 * The xmlizer used to xmlize dependence information.
	 */
	private final DependencyXMLizer xmlizer = new DependencyXMLizer();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private CallGraphInfo cgi;

	/** 
	 * This flag indicates if the simple version of aliased use-def information should be used.
	 */
	private boolean useAliasedUseDefv1;

	/** 
	 * This indicates if safe lock should be used.
	 */
	private boolean useSafeLockAnalysis;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	class InvCallGraph
	  extends SimpleEdgeGraph {
		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Collection processedNodes = new HashSet();

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param node DOCUMENT ME!
		 */
		public void processNode(IObjectNode node) {
			if (!processedNodes.contains(node)) {
				final Pair _pair = (Pair) node.getObject();
				final SootMethod _method = (SootMethod) _pair.getSecond();
				final Iterator _i = chacgi.getCallers(_method).iterator();
				final int _iEnd = chacgi.getCallers(_method).size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final CallTriple _triple = (CallTriple) _i.next();
					addEdgeFromTo(node, ILabel.CALLS, super.getNode(_triple.getMethod()));
				}
				processedNodes.add(node);
			}
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	class InvDependenceGraph
	  extends SimpleEdgeGraph {
		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Collection processedNodes = new HashSet();

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param node DOCUMENT ME!
		 */
		public void processNode(IObjectNode node) {
			if (!processedNodes.contains(node)) {
				final Pair _pair = (Pair) node.getObject();
				final Stmt _stmt = (Stmt) _pair.getFirst();
				final SootMethod _method = (SootMethod) _pair.getSecond();
				final Iterator _i = cdas.iterator();
				final int _iEnd = cdas.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
					final Collection _dees = _da.getDependents(_stmt, _method);
					final Iterator _j = _dees.iterator();
					final int _jEnd = _dees.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Object _o = _j.next();

						if (_o instanceof Pair) {
							addEdgeFromTo(node, ILabel.CD, super.getNode(_o));
						} else if (_o instanceof Stmt) {
							addEdgeFromTo(node, ILabel.CD, super.getNode(new Pair(_o, _method)));
						}
					}
				}

				final Iterator _k = ddas.iterator();
				final int _kEnd = ddas.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final IDependencyAnalysis _da = (IDependencyAnalysis) _k.next();
					final Collection _dees = _da.getDependents(_stmt, _method);
					final Iterator _j = _dees.iterator();
					final int _jEnd = _dees.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Object _o = _j.next();

						if (_o instanceof Pair) {
							addEdgeFromTo(node, ILabel.DD, super.getNode(_o));
						} else if (_o instanceof Stmt) {
							addEdgeFromTo(node, ILabel.DD, super.getNode(new Pair(_o, _method)));
						}
					}
				}
				processedNodes.add(node);
			}
		}
	}

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
				"Directory into which files will be written into.  Defaults to current directory if omitted");
		_option.setArgs(1);
		_option.setArgName("output-directory");
		_options.addOption(_option);
		_option = new Option("h", "help", false, "Display message.");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", false, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("sc", "start-class", false, "start class signature");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("class");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("sm", "start-method", false, "start method signature");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("method");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("si", "start-stmt-index", false, "start statement index");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("stmt-index");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("ec", "end-class", false, "end class signature");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("class");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("em", "end-method", false, "end method signature");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("method");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("ei", "end-stmt-index", false, "end statement index");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("stmt-index");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("t", "type", false, "influence type (ai, ddi, cdi, di, ci)");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("type");
		_option.setOptionalArg(false);
		_options.addOption(_option);

		final CommandLineParser _parser = new GnuParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				printUsage(_options);
				System.exit(1);
			}

			final InfluenceChecker _xmlizerCLI = new InfluenceChecker();

			_xmlizerCLI.setRootMethodTrapper(new RootMethodTrapper() {

			/** 
			 * @see edu.ksu.cis.indus.common.soot.RootMethodTrapper#isThisARootMethod(soot.SootMethod)
			 */

			   protected boolean isThisARootMethod(final SootMethod sm) {
			       return true;
			   }
			
			   } );
            
			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}

			_xmlizerCLI.xmlizer.setXmlOutputDir(_outputDir);

			if (_cl.hasOption('p')) {
				_xmlizerCLI.addToSootClassPath(_cl.getOptionValue('p'));
			}
			_xmlizerCLI.useAliasedUseDefv1 = true;
			_xmlizerCLI.useSafeLockAnalysis = true;

			final List _classNames = _cl.getArgList();

			if (_classNames.isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}
			_xmlizerCLI.setClassNames(_classNames);

			final InterferenceDAv1 _da1 = new InterferenceDAv3();
			_da1.setUseOFA(true);
			_xmlizerCLI.ddas.add(_da1);
			_xmlizerCLI.ddas.add(new IdentifierBasedDataDAv3());
			_xmlizerCLI.ddas.add(new ReferenceBasedDataDA());

			final ReadyDAv1 _da2 = ReadyDAv3.getBackwardReadyDA();
			_xmlizerCLI.cdas.add(_da2);
			_xmlizerCLI.cdas.add(new NonTerminationSensitiveEntryControlDA());

			_da2.setUseOFA(true);
			_da2.setUseSafeLockAnalysis(_xmlizerCLI.useSafeLockAnalysis);

			_xmlizerCLI.execute();

			_xmlizerCLI.processPatterns(_cl.getOptionValue("sc"), _cl.getOptionValue("sm"),
				Integer.parseInt(_cl.getOptionValue("si")), _cl.getOptionValue("ec"), _cl.getOptionValue("em"),
				Integer.parseInt(_cl.getOptionValue("ei")), _cl.getOptionValue('t'));
		} catch (final ParseException _e) {
			LOGGER.fatal("Error while parsing command line.", _e);
			printUsage(_options);
		} catch (final Throwable _e) {
			LOGGER.fatal("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * Prints the help/usage info for this class.
	 *
	 * @param options is the command line option.
	 *
	 * @pre options != null
	 */
	private static void printUsage(final Options options) {
		final String _cmdLineSyn = "java " + InfluenceChecker.class.getName() + " <options> <classnames>";
		(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", options, "");
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param type DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private IAutomata getAutomata(final String type) {
		final DFA _result = new DFA();

		if (type.equals("ddi") || type.equals("di")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			IState _s3 = new State("s3");
			IState _s4 = new State("s4");
			_result.addFinalState(_s4);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, ILabel.DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, ILabel.DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, ILabel.CD, _s3);
			_result.addLabelledTransitionFromTo(_s2, ILabel.EPSILON, _s3);
			_result.addLabelledTransitionFromTo(_s3, ILabel.CD, _s3);
			_result.addLabelledTransitionFromTo(_s3, ILabel.DD, _s4);
			_result.addLabelledTransitionFromTo(_s3, ILabel.EPSILON, _s4);
			_result.addLabelledTransitionFromTo(_s4, ILabel.DD, _s4);
		} else if (type.equals("cdi")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			IState _s3 = new State("s3");
			_result.addFinalState(_s3);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, ILabel.DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, ILabel.DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, ILabel.CD, _s3);
			_result.addLabelledTransitionFromTo(_s3, ILabel.CD, _s3);
		} else if (type.equals("ci")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			_result.addFinalState(_s2);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, ILabel.CD, _s2);
			_result.addLabelledTransitionFromTo(_s2, ILabel.CD, _s2);
		} else {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			_result.addFinalState(_s2);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, ILabel.CALLS, _s2);
			_result.addLabelledTransitionFromTo(_s2, ILabel.CALLS, _s2);
		}
		_result.initialize();

		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param type DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private SimpleEdgeGraph getGraph(String type) {
		final SimpleEdgeGraph _result;

		if (type.equals("ai")) {
			_result = new InvCallGraph();
		} else {
			_result = new InvDependenceGraph();
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param clazz DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private SootMethod getMethod(final String clazz, final String method) {
		final SootMethod _result;
		final SootClass _sc = getScene().getSootClass(clazz);
		_result = _sc.getMethod(method);
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param index DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Stmt getStmt(final SootMethod sm, final int index) {
		final List _units = new ArrayList(sm.retrieveActiveBody().getUnits());
		return (Stmt) _units.get(index);
	}

	/**
	 * Drives the analyses.
	 */
	private void execute() {
		setLogger(LOGGER);

		final String _tagName = "DependencyXMLizer:FA";
		aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager(new SootValueTypeManager()));

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final PairManager _pairManager = new PairManager(false, true);
		cgi = new CallGraphInfo(new PairManager(false, true));

        final IThreadGraphInfo _tgi = new ThreadGraph(cgi, new CFGAnalysis(cgi, getBbm()), _pairManager);
		final ProcessingController _xmlcgipc = new ProcessingController();
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
		final MetricsProcessor _countingProcessor = new MetricsProcessor();
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		_cgipc.setAnalyzer(aa);
		_cgipc.setProcessingFilter(new CGBasedProcessingFilter(cgi));
		_cgipc.setStmtSequencesRetriever(_ssr);

		_xmlcgipc.setEnvironment(aa.getEnvironment());
		_xmlcgipc.setProcessingFilter(new CGBasedXMLizingProcessingFilter(cgi));
		_xmlcgipc.setStmtSequencesRetriever(_ssr);

		final StaticFieldUseDefInfo _staticFieldUD = new StaticFieldUseDefInfo();
		final AliasedUseDefInfo _aliasUD;

		if (useAliasedUseDefv1) {
			_aliasUD = new AliasedUseDefInfo(aa, bbm, _pairManager);
		} else {
			_aliasUD = new AliasedUseDefInfov2(aa, cgi, _tgi, bbm, _pairManager);
		}
		info.put(ICallGraphInfo.ID, cgi);
		info.put(IThreadGraphInfo.ID, _tgi);
		info.put(PairManager.ID, _pairManager);
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, _aliasUD);
		info.put(IUseDefInfo.GLOBAL_USE_DEF_ID, _staticFieldUD);

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(cgi, getBbm());
		info.put(IEscapeInfo.ID, _ecba);

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
        
		_callGraphInfoCollector.reset();
		_processors.clear();
		_processors.add(_callGraphInfoCollector);
		_pc.reset();
		_pc.driveProcessors(_processors);
		cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());
		writeInfo("CALL GRAPH:\n" + cgi.toString());

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add(_tgi);
		_processors.add(_countingProcessor);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);
		writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).toString());

		final ByteArrayOutputStream _stream = new ByteArrayOutputStream();
		MapUtils.verbosePrint(new PrintStream(_stream), "STATISTICS:", new TreeMap(_countingProcessor.getStatistics()));
		writeInfo(_stream.toString());

		_aliasUD.hookup(_cgipc);
		_staticFieldUD.hookup(_cgipc);
		_cgipc.process();
		_staticFieldUD.unhook(_cgipc);
		_aliasUD.unhook(_cgipc);

		writeInfo("BEGIN: dependency analyses");

		final AnalysesController _ac = new AnalysesController(info, _cgipc, getBbm());
		_ac.addAnalyses(IMonitorInfo.ID, Collections.singleton(_monitorInfo));
		_ac.addAnalyses(IEscapeInfo.ID, Collections.singleton(_ecba));

		if (useSafeLockAnalysis) {
			_ac.addAnalyses(SafeLockAnalysis.ID, Collections.singleton(_sla));
		}

		for (final Iterator _i1 = IteratorUtils.chainedIterator(cdas.iterator(), ddas.iterator()); _i1.hasNext();) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i1.next();

			for (final Iterator _i2 = _da.getIds().iterator(); _i2.hasNext();) {
				final Object _id = _i2.next();
				_ac.addAnalyses(_id, Collections.singleton(_da));
			}
		}

		_ac.initialize();
		_ac.execute();

		// write xml
		for (final Iterator _i1 = das.iterator(); _i1.hasNext();) {
			final IDependencyAnalysis _da = (IDependencyAnalysis) _i1.next();

			for (final Iterator _i2 = _da.getIds().iterator(); _i2.hasNext();) {
				final Object _id = _i2.next();
				CollectionsUtilities.putIntoListInMap(info, _id, _da);
			}
		}
		xmlizer.setGenerator(new UniqueJimpleIDGenerator());
		xmlizer.writeXML(info);

		writeInfo("Total classes loaded: " + getScene().getClasses().size());
	}

    private CallGraphInfo chacgi;
    
	private void calculateCHACallgraph() {
        chacgi = new CallGraphInfo(new PairManager(false, true));
        final ProcessingController _pc = new ProcessingController();
        final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
        _ssr.setStmtGraphFactory(getStmtGraphFactory());
        _pc.setStmtSequencesRetriever(_ssr);
        _pc.setEnvironment(new Environment(getScene()));
        final CHABasedCallInfoCollector _col = new CHABasedCallInfoCollector();
        _col.hookup(_pc);
        _pc.process();
        _col.unhook(_pc);
        chacgi.createCallGraphInfo(_col.getCallInfo());
    }

    /**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param startClass DOCUMENT ME!
	 * @param startSig DOCUMENT ME!
	 * @param sIndex DOCUMENT ME!
	 * @param endClass DOCUMENT ME!
	 * @param endSig DOCUMENT ME!
	 * @param eIndex DOCUMENT ME!
	 * @param type DOCUMENT ME!
	 */
	private void processPatterns(final String startClass, final String startSig, final int sIndex, final String endClass,
		final String endSig, final int eIndex, final String type) {
		final SootMethod _sm = getMethod(startClass, startSig);
		final Stmt _ss = type.equals("ai") ? null
										   : getStmt(_sm, sIndex);
		final SootMethod _em = getMethod(endClass, endSig);
		final Stmt _es = type.equals("ai") ? null
										   : getStmt(_em, eIndex);
		final SimpleEdgeGraph _graph = getGraph(type);
		final IWorkBag _wb = new LIFOWorkBag();

        if (type.equals("ai")) {
            calculateCHACallgraph();
        }
        
		int _missedPaths = 0;
		final Collection _matchedPaths = new HashSet();

		final INode _target = _graph.getNode(new Pair(_es, _em));
		_wb.addWork(new Triple(_graph.getNode(new Pair(_ss, _sm)), new Stack(), getAutomata(type)));

		while (_wb.hasWork()) {
			final Triple _triple = (Triple) _wb.getWork();
			final INode _src = (INode) _triple.getFirst();
			final Stack _path = (Stack) _triple.getSecond();
			final IAutomata _auto = (IAutomata) _triple.getThird();

			_path.push(_src);

			if (_auto.isInFinalState() && _src.equals(_target)) {
				_matchedPaths.add(_path);
			} else {
				if (_auto.canPerformTransition(ILabel.EPSILON)) {
					final IAutomata _aclone = (IAutomata) _auto.clone();
					_aclone.performTransitionOn(ILabel.EPSILON);

					final Stack _temp = (Stack) _path.clone();
					_temp.push(ILabel.EPSILON);
					_wb.addWork(new Triple(_src, _temp, _aclone));
				}

				final Collection _temp = new HashSet();
				final Map _succsOf = _graph.getSuccsOf(_src);
				final Iterator _i = _succsOf.entrySet().iterator();
				final int _iEnd = _succsOf.entrySet().size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final Map.Entry _entry = (Map.Entry) _i.next();
					final ILabel _label = (ILabel) _entry.getKey();
					final Collection _coll = (Collection) _entry.getValue();

					for (final Iterator _j = _coll.iterator(); _j.hasNext();) {
						final INode _dest = (INode) _j.next();

						if (!_path.contains(_dest) && _auto.canPerformTransition(_label)) {
							final IAutomata _aclone = (IAutomata) _auto.clone();
							_aclone.performTransitionOn(_label);

							final Stack _t = (Stack) _path.clone();
							_t.push(_label);
							_temp.add(new Triple(_dest, _t, _aclone));
						}
					}
				}
				_wb.addAllWorkNoDuplicates(_temp);

				if (_temp.size() != _succsOf.size() || _succsOf.size() == 0) {
					_missedPaths++;
				}
			}
		}

		System.out.println(_matchedPaths);
		System.out.println(_missedPaths + " " + _matchedPaths.size());
	}
}

// End of File
