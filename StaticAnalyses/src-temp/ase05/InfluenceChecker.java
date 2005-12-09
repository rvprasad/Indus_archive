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

package ase05;

import edu.ksu.cis.indus.common.collections.IteratorUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.fa.IAutomaton;
import edu.ksu.cis.indus.common.fa.ITransitionLabel;
import edu.ksu.cis.indus.common.fa.NFA;
import edu.ksu.cis.indus.common.fa.State;
import edu.ksu.cis.indus.common.graph.IDirectedGraphView;
import edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode;
import edu.ksu.cis.indus.common.graph.IEdgeLabel;
import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.common.soot.RootMethodTrapper;
import edu.ksu.cis.indus.common.soot.SootBasedDriver;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.cfg.StaticFieldUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.SafeLockAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationSensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv3;
import edu.ksu.cis.indus.staticanalyses.dependency.ReferenceBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

/**
 * This class provides a command-line interface to xmlize dependence information. Refer to <code>SootBasedDriver</code> for
 * more configuration infomration.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class InfluenceChecker
		extends SootBasedDriver {

	/**
	 * A node that represents a pair.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	static class PairNode
			implements IDirectedGraphView.INode {

		/**
		 * First element of the pair.
		 */
		final Object first;

		/**
		 * Second element of the pair.
		 */
		final Object second;

		/**
		 * Creates a new PairNode object.
		 * 
		 * @param f first element of the pair.
		 * @param s second element of the pair.
		 */
		PairNode(final Object f, final Object s) {
			first = f;
			second = s;
		}

		/**
		 * @see java.lang.Object#equals(Object)
		 */
		@Override public boolean equals(final Object object) {
			if (object == this) {
				return true;
			}

			if (!(object instanceof PairNode)) {
				return false;
			}

			final PairNode _rhs = (PairNode) object;
			return new EqualsBuilder().append(this.first, _rhs.first).append(this.second, _rhs.second).isEquals();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override public int hashCode() {
			return new HashCodeBuilder(7, 37).append(this.first).append(this.second).toHashCode();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override public String toString() {
			return "(" + first + ", " + second + ")";
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static class TransitionLabel
			implements ITransitionLabel<TransitionLabel> {

		/**
		 * DOCUMENT ME!
		 */
		final String label;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param theLabel DOCUMENT ME!
		 */
		TransitionLabel(final String theLabel) {
			super();
			this.label = theLabel;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override public String toString() {
			return label;
		}

	}

	/**
	 * DOCUMENT ME!
	 */
	static final TransitionLabel EPSILON = new TransitionLabel("-Epsilon->");

	/**
	 * This label represents call graph based edges.
	 */
	static final TransitionLabel CALLS = new TransitionLabel("-CALLS->");

	/**
	 * This label represents control dependence based edges.
	 */
	static final TransitionLabel CD = new TransitionLabel("-CD->");

	/**
	 * This label represents data dependence based edges.
	 */
	static final TransitionLabel DD = new TransitionLabel("-DD->");

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(InfluenceChecker.class);

	/**
	 * This is the flow analyser used by the analyses being tested.
	 */
	protected IValueAnalyzer<Value> aa;

	/**
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 */
	protected final Map<Comparable<?>, Object> info = new HashMap<Comparable<?>, Object>();

	/**
	 * The collection of data dependence analyses.
	 */
	final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> cdas = new ArrayList<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

	/**
	 * The call graph.
	 */
	CallGraphInfo cgi;

	/**
	 * A collection of dependence analyses.
	 * 
	 * @invariant das.oclIsKindOf(Collection(AbstractDependencyAnalysis))
	 */
	List<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> das = new ArrayList<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

	/**
	 * The collection of control dependence analyses.
	 */
	final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> ddas = new ArrayList<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

	/**
	 * DOCUMENT ME!
	 */
	private final boolean invoked;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param b DOCUMENT ME!
	 */
	public InfluenceChecker(final boolean b) {
		invoked = b;
	}

	/**
	 * This is the entry point via command-line.
	 * 
	 * @param args is the command line arguments.
	 * @throws RuntimeException when an Throwable exception beyond our control occurs.
	 * @pre args != null
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
		_option = new Option("t", "type", false, "influence type (ai, di, ci)");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("type");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("i", "invoked method", false, "consider invoked method for ai");
		_option.setArgs(0);
		_options.addOption(_option);
		_option = new Option("c", "classnames", false, "list of class of interest");
		_option.setRequired(true);
		_option.setArgs(1);
		_option.setArgName("list of classes");
		_option.setOptionalArg(false);
		_options.addOption(_option);

		final CommandLineParser _parser = new GnuParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				printUsage(_options);
				System.exit(1);
			}

			final InfluenceChecker _checker = new InfluenceChecker(_cl.hasOption('i'));

			_checker.setRootMethodTrapper(new RootMethodTrapper() {

				/**
				 * @see edu.ksu.cis.indus.common.soot.RootMethodTrapper#isThisARootMethod(soot.SootMethod)
				 */
				@Override protected boolean isThisARootMethod(@SuppressWarnings("unused") final SootMethod sm) {
					return true;
				}
			});

			if (_cl.hasOption('p')) {
				_checker.addToSootClassPath(_cl.getOptionValue('p'));
			}

			final String _classNames = _cl.getOptionValue('c');

			if (_classNames.length() == 0) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}
			_checker.setClassNames(Arrays.asList(_classNames.split(" ")));

			final InterferenceDAv1 _da1 = new InterferenceDAv3();
			_da1.setUseOFA(true);
			_checker.ddas.add(_da1);
			_checker.ddas.add(new IdentifierBasedDataDAv3());
			_checker.ddas.add(new ReferenceBasedDataDA());

			final ReadyDAv1 _da2 = ReadyDAv3.getBackwardReadyDA();
			_checker.cdas.add(_da2);
			_checker.cdas.add(new NonTerminationSensitiveEntryControlDA());

			_da2.setUseOFA(true);
			_da2.setUseSafeLockAnalysis(true);

			_checker.execute();
			_checker.performCheck(_cl.getArgList(), _cl.getOptionValue('t'));
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
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
		final String _cmdLineSyn = "java " + InfluenceChecker.class.getName() + " <options> <classnames>";
		(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", options, "");
	}

	/**
	 * Drives the analyses.
	 * 
	 * @param <T> dummy type parameter.
	 */
	private <T extends ITokens<T, Value>> void execute() {
		setInfoLogger(LOGGER);

		final String _tagName = "DependencyXMLizer:FA";
		aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.<T, Value> getTokenManager(new SootValueTypeManager()),
				getStmtGraphFactory());

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection<IProcessor> _processors = new ArrayList<IProcessor>();
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
		final AliasedUseDefInfo _aliasUD = new AliasedUseDefInfov2(aa, cgi, _tgi, bbm, _pairManager);
		info.put(ICallGraphInfo.ID, cgi);
		info.put(IThreadGraphInfo.ID, _tgi);
		info.put(PairManager.ID, _pairManager);
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, _aliasUD);
		info.put(IUseDefInfo.GLOBAL_USE_DEF_ID, _staticFieldUD);

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(cgi, null, getBbm());
		info.put(IEscapeInfo.ID, _ecba);

		final MonitorAnalysis _monitorInfo = new MonitorAnalysis();
		info.put(IMonitorInfo.ID, _monitorInfo);

		final SafeLockAnalysis _sla = new SafeLockAnalysis();
		info.put(SafeLockAnalysis.ID, _sla);

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
		_processors.add((ThreadGraph) _tgi);
		_processors.add(_countingProcessor);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);
		writeInfo("THREAD GRAPH:\n" + ((ThreadGraph) _tgi).toString());

		writeInfo(MapUtils.verbosePrint("STATISTICS:", _countingProcessor.getStatistics()));

		_aliasUD.hookup(_cgipc);
		_staticFieldUD.hookup(_cgipc);
		_cgipc.process();
		_staticFieldUD.unhook(_cgipc);
		_aliasUD.unhook(_cgipc);

		writeInfo("BEGIN: dependency analyses");

		final AnalysesController _ac = new AnalysesController(info, _cgipc, getBbm());
		_ac.addAnalyses(IMonitorInfo.ID, Collections.singleton(_monitorInfo));
		_ac.addAnalyses(IEscapeInfo.ID, Collections.singleton(_ecba));
		_ac.addAnalyses(SafeLockAnalysis.ID, Collections.singleton(_sla));

		for (final Iterator<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i1 = IteratorUtils.chainedIterator(cdas.iterator(), ddas
				.iterator()); _i1.hasNext();) {
			final IDependencyAnalysis<?, ?, ?, ?, ?, ?> _da = _i1.next();

			for (final Iterator<? extends Comparable<?>> _i2 = _da.getIds().iterator(); _i2.hasNext();) {
				final Comparable<?> _id = _i2.next();
				_ac.addAnalyses(_id, Collections.singleton(_da));
			}
		}

		_ac.initialize();
		_ac.execute();
	}

	/**
	 * Retrieve the automaton based on the type of influence check.
	 * 
	 * @param type of influence check. This has to be one of "ai", "ddi", "cdi", "ci", or "di".
	 * @return an automaton.
	 * @throws IllegalArgumentException if the type is not one of the specified values.
	 */
	private IAutomaton<State, TransitionLabel> getAutomaton(final String type) {
		final NFA<State, TransitionLabel> _result = new NFA<State, TransitionLabel>(
				new ITransitionLabel.IEpsilonLabelFactory<TransitionLabel>() {

					public TransitionLabel getEpsilonTransitionLabel() {
						return EPSILON;
					}

				});
		if (type.equals("di")) {
			final State _s1 = new State("s1");
			final State _s2 = new State("s2");
			final State _s3 = new State("s3");
			_result.addFinalState(_s3);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, DD, _s1);
			_result.addLabelledTransitionFromTo(_s1, EPSILON, _s2);
			_result.addLabelledTransitionFromTo(_s2, CD, _s2);
			_result.addLabelledTransitionFromTo(_s2, EPSILON, _s3);
			_result.addLabelledTransitionFromTo(_s3, DD, _s3);

		} else if (type.equals("ci")) {
			final State _s1 = new State("s1");
			final State _s2 = new State("s2");
			final State _s3 = new State("s3");
			final State _s4 = new State("s4");
			final State _s5 = new State("s5");
			_result.addFinalState(_s5);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, DD, _s1);
			_result.addLabelledTransitionFromTo(_s1, EPSILON, _s2);
			_result.addLabelledTransitionFromTo(_s2, CD, _s2);
			_result.addLabelledTransitionFromTo(_s2, EPSILON, _s3);
			_result.addLabelledTransitionFromTo(_s3, DD, _s3);
			_result.addLabelledTransitionFromTo(_s3, EPSILON, _s4);
			_result.addLabelledTransitionFromTo(_s4, CD, _s5);
			_result.addLabelledTransitionFromTo(_s5, CD, _s5);
		} else if (type.equals("ai")) {
			final State _s1 = new State("s1");
			final State _s2 = new State("s2");
			_result.addFinalState(_s2);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, CALLS, _s2);
			_result.addLabelledTransitionFromTo(_s2, CALLS, _s2);
		} else {
			throw new IllegalArgumentException("type has to be one of ai, di, ci.");
		}
		_result.start();

		return _result;
	}

	/**
	 * Retrieves the graph based on the type of influence check.
	 * 
	 * @param type of influence check. This has to be one of "ai", "ddi", "cdi", "ci", or "di".
	 * @return an automaton.
	 * @throws IllegalArgumentException if the type is not one of the specified values.
	 */
	private IEdgeLabelledDirectedGraphView<INode> getGraph(final String type) {
		final IEdgeLabelledDirectedGraphView<INode> _result;

		if (type.equals("ai")) {
			_result = new CallGraphView(this, invoked);
		} else if (type.equals("di") || type.equals("ci")) {
			_result = new DependenceGraphView(this);
		} else {
			throw new IllegalArgumentException("type has to be one of ai, ddi, cdi, ci, or di.");
		}
		return _result;
	}

	/**
	 * Retrieves the method from the class based on it's signature.
	 * 
	 * @param clazz that contains the method.
	 * @param methodSignature obviously.
	 * @return the method of the given signature in the named class.
	 * @pre clazz != null and methodSignature != null
	 */
	private SootMethod getMethod(final String clazz, final String methodSignature) {
		final SootMethod _result;
		final SootClass _sc = getScene().getSootClass(clazz);
		_result = _sc.getMethod(methodSignature);
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param args DOCUMENT ME!
	 * @param type DOCUMENT ME!
	 * @param targets DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private PairNode getSourceAndTargets(final List<String> args, final String type, final Collection<PairNode> targets) {
		final Iterator<String> _i = args.iterator();
		final SootMethod _sm = getMethod(_i.next(), _i.next());
		final Stmt _ss = type.equals("ai") ? null : getStmt(_sm, Integer.parseInt(_i.next()));

		for (; _i.hasNext();) {
			final String _ecName = _i.next();
			final String _emSig = _i.next();
			final SootMethod _em = getMethod(_ecName, _emSig);
			final Stmt _es = type.equals("ai") ? null : getStmt(_em, Integer.parseInt(_i.next()));
			targets.add(new PairNode(_es, _em));
		}

		return new PairNode(_ss, _sm);
	}

	/**
	 * Retrieves the statement at the given index in the method.
	 * 
	 * @param sm containing the statement.
	 * @param index at which the statement occurs.
	 * @return the statement.
	 * @pre sm != null
	 */
	private Stmt getStmt(final SootMethod sm, final int index) {
		final List<Stmt> _units = new ArrayList<Stmt>(getBbm().getStmtGraph(sm).getBody().getUnits());
		return _units.get(index);
	}

	/**
	 * Does the check.
	 * 
	 * @param args DOCUMENT ME!
	 * @param type of check. This has to be one of "ai", "ddi", "cdi", "ci", or "di".
	 * @pre cl != null and type != null
	 */
	private void performCheck(final List<String> args, final String type) {
		final IEdgeLabelledDirectedGraphView<INode> _graph = getGraph(type);
		final IWorkBag<Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>> _wb = new LIFOWorkBag<Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>>();
		final Collection<PairNode> _targets = new HashSet<PairNode>();
		final PairNode _source = getSourceAndTargets(args, type, _targets);
		final Collection<Stack<Object>> _matchedPaths = new HashSet<Stack<Object>>();
		_wb.addWork(new Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>(_source, new Stack<Object>(),
				getAutomaton(type)));

		int _missedPaths = 0;

		while (_wb.hasWork()) {
			final Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>> _triple = _wb.getWork();
			final INode _src = _triple.getFirst();
			final Stack<Object> _path = _triple.getSecond();
			final IAutomaton<State, TransitionLabel> _auto = _triple.getThird();

			_path.push(_src);

			if (_auto.isInFinalState() && _targets.contains(_src)) {
				_matchedPaths.add(_path);
			} else {
				if (_auto.canPerformTransition(EPSILON)) {
					final IAutomaton<State, TransitionLabel> _aclone = _auto.clone();
					_aclone.performTransitionOn(EPSILON);

					final Stack<Object> _temp = _path.clone();
					_temp.push(EPSILON);
					_wb.addWork(new Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>(_src, _temp, _aclone));
				}

				final Collection<Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>> _temp = new HashSet<Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>>();
				final Collection<Collection<INode>> _succs = new ArrayList<Collection<INode>>();
				final Collection<IEdgeLabel> _outgoingEdgeLabels = _graph.getOutgoingEdgeLabels(_src);
				final Iterator<IEdgeLabel> _i = _outgoingEdgeLabels.iterator();
				final int _iEnd = _outgoingEdgeLabels.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final TransitionLabel _label = (TransitionLabel) _i.next();

					if (_auto.canPerformTransition(_label)) {
						final Collection<INode> _succsViaLabel = _graph.getSuccsViaEdgesLabelled(_src, _label);
						final Iterator<INode> _j = _succsViaLabel.iterator();
						final int _jEnd = _succsViaLabel.size();

						for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
							final INode _dest = _j.next();

							if (!_path.contains(_dest)) {
								final IAutomaton<State, TransitionLabel> _aclone = _auto.clone();
								_aclone.performTransitionOn(_label);

								final Stack<Object> _t = _path.clone();
								_t.push(_label);
								_temp.add(new Triple<INode, Stack<Object>, IAutomaton<State, TransitionLabel>>(_dest, _t,
										_aclone));
							}
						}
						_succs.add(_succsViaLabel);
					}
				}
				_wb.addAllWorkNoDuplicates(_temp);

				if (_temp.isEmpty() || _temp.size() != _succs.size()) {
					_missedPaths++;
				}
			}
		}

		System.out.println("Starting at statement '" + _source.first + "' in method '" + _source.second + "'");
		final Iterator<PairNode> _i = _targets.iterator();
		final int _iEnd = _targets.size();
		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final PairNode _p = _i.next();
			System.out.println("Ending at statement '" + _p.first + "' in method '" + _p.second + "'");
		}
		System.out.println("Type: " + type);
		System.out.println("Lower bound on the number of Missed paths: " + _missedPaths);
		System.out.println("Number of Matched paths: " + _matchedPaths.size());
		System.out.println("Matched paths are ");

		final Iterator<Stack<Object>> _j = _matchedPaths.iterator();
		final int _jEnd = _matchedPaths.size();
		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Stack<Object> _path = _j.next();
			System.out.println(_path + "\n");
		}
	}
}

// End of File
