
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.fa.IAutomaton;
import edu.ksu.cis.indus.common.fa.IState;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

	/** 
	 * The collection of data dependence analyses.
	 */
	final Collection cdas = new ArrayList();

	/** 
	 * The collection of control dependence analyses.
	 */
	final Collection ddas = new ArrayList();

	/** 
	 * The call graph.
	 */
	CallGraphInfo cgi;

	/** 
	 * This label represents call graph based edges.
	 */
	IAutomaton.ITransitionLabel CALLS =
		new IAutomaton.ITransitionLabel() {
			public String toString() {
				return "-CALLS->";
			}
		};

	/** 
	 * This label represents control dependence based edges.
	 */
	IAutomaton.ITransitionLabel CD =
		new IAutomaton.ITransitionLabel() {
			public String toString() {
				return "-CD->";
			}
		};

	/** 
	 * This label represents data dependence based edges.
	 */
	IAutomaton.ITransitionLabel DD =
		new IAutomaton.ITransitionLabel() {
			public String toString() {
				return "-DD->";
			}
		};

	/** 
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(AbstractDependencyAnalysis))
	 */
	List das = new ArrayList();

	/** 
	 * The xmlizer used to xmlize dependence information.
	 */
	private final DependencyXMLizer xmlizer = new DependencyXMLizer();

	/** 
	 * This flag indicates if the simple version of aliased use-def information should be used.
	 */
	private boolean useAliasedUseDefv1;

	/** 
	 * This indicates if safe lock should be used.
	 */
	private boolean useSafeLockAnalysis;

	/**
	 * This provides a graph view of the call graph.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	class CallGraphView
	  implements IEdgeLabelledDirectedGraphView {
		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getIncomingEdgeLabels(IDirectedGraphView.INode)
		 */
		public Collection getIncomingEdgeLabels(final IDirectedGraphView.INode node) {
			return !cgi.getCallers((SootMethod) ((PairNode) node).second).isEmpty() ? Collections.singleton(CALLS)
																					: Collections.EMPTY_SET;
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getOutgoingEdgeLabels(IDirectedGraphView.INode)
		 */
		public Collection getOutgoingEdgeLabels(final IDirectedGraphView.INode node) {
			return !cgi.getCallees((SootMethod) ((PairNode) node).second).isEmpty() ? Collections.singleton(CALLS)
																					: Collections.EMPTY_SET;
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getPredsOf(IDirectedGraphView.INode)
		 */
		public Collection getPredsOf(IDirectedGraphView.INode node) {
			final Collection _result = new HashSet();
			final Iterator _iter = cgi.getCallers((SootMethod) ((PairNode) node).second).iterator();
			final int _iterEnd = cgi.getCallers((SootMethod) ((PairNode) node).second).size();

			for (int _iterIndex = 0; _iterIndex < _iterEnd; _iterIndex++) {
				final CallTriple _triple = (CallTriple) _iter.next();
				_result.add(new PairNode(_triple.getStmt(), _triple.getMethod()));
			}
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getPredsViaEdgesLabelled(IDirectedGraphView.INode,
		 * 		IEdgeLabel)
		 */
		public Collection getPredsViaEdgesLabelled(final IDirectedGraphView.INode node, final IEdgeLabel label) {
			return label.equals(CALLS) ? getPredsOf(node)
									   : Collections.EMPTY_SET;
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getSuccsOf(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
		 */
		public Collection getSuccsOf(final IDirectedGraphView.INode node) {
			final Collection _result = new HashSet();
			final SootMethod _method = (SootMethod) ((PairNode) node).second;
			final Iterator _i = cgi.getCallees(_method).iterator();
			final int _iEnd = cgi.getCallees(_method).size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final CallTriple _triple = (CallTriple) _i.next();
				_result.add(new PairNode(_triple.getSecond(), _triple.getMethod()));
			}
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getSuccsViaEdgesLabelled(IDirectedGraphView.INode,
		 * 		IEdgeLabel)
		 */
		public Collection getSuccsViaEdgesLabelled(final IDirectedGraphView.INode node, final IEdgeLabel label) {
			return label.equals(CALLS) ? getSuccsOf(node)
									   : Collections.EMPTY_SET;
		}
	}


	/**
	 * This provides a graph view of the control/dependence dependence information.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	class DependenceGraphView
	  implements IEdgeLabelledDirectedGraphView {
		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getIncomingEdgeLabels(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
		 */
		public Collection getIncomingEdgeLabels(final INode node) {
			throw new UnsupportedOperationException("This operation is unsupported.");
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getOutgoingEdgeLabels(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
		 */
		public Collection getOutgoingEdgeLabels(final INode node) {
			final Collection _result = new HashSet();
			final Stmt _s = (Stmt) ((PairNode) node).first;
			final SootMethod _sm = (SootMethod) ((PairNode) node).second;
			final Iterator _i = cdas.iterator();
			final int _iEnd = cdas.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
				final Collection _dents = _da.getDependents(_s, _sm);

				if (!_dents.isEmpty()) {
					_result.add(CD);
					break;
				}
			}

			final Iterator _j = ddas.iterator();
			final int _jEnd = ddas.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final IDependencyAnalysis _da = (IDependencyAnalysis) _j.next();
				final Collection _dents = _da.getDependents(_s, _sm);

				if (!_dents.isEmpty()) {
					_result.add(DD);
					break;
				}
			}
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getPredsOf(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
		 */
		public Collection getPredsOf(final INode node) {
			throw new UnsupportedOperationException("This operation is not supported.");
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getPredsViaEdgesLabelled(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode,
		 * 		edu.ksu.cis.indus.common.graph.IEdgeLabel)
		 */
		public Collection getPredsViaEdgesLabelled(final INode node, final IEdgeLabel label) {
			throw new UnsupportedOperationException("This operation is not supported.");
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IDirectedGraphView#getSuccsOf(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode)
		 */
		public Collection getSuccsOf(final INode node) {
			throw new UnsupportedOperationException("This operation is not supported.");
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraphView#getSuccsViaEdgesLabelled(edu.ksu.cis.indus.common.graph.IDirectedGraphView.INode,
		 * 		edu.ksu.cis.indus.common.graph.IEdgeLabel)
		 */
		public Collection getSuccsViaEdgesLabelled(final INode node, final IEdgeLabel label) {
			final Stmt _s = (Stmt) ((PairNode) node).first;
			final SootMethod _sm = (SootMethod) ((PairNode) node).second;
			final Collection _result = new HashSet();
			_result.addAll(getSuccs(_s, _sm, cdas));
			_result.addAll(getSuccs(_s, _sm, ddas));
			return _result;
		}

		/**
		 * Retrieves the successors of the given
		 *
		 * @param s is the statement of interest.
		 * @param sm is the method containing <code>s</code>.
		 * @param das is the collection of dependence analysis from which the graph-based information should be retrieved.
		 *
		 * @return a collection of successor nodes based on the dependence information available in <code>das</code>.
		 *
		 * @pre s != null and sm != null and das.oclIsKindOf(Collection(IDependencyAnalysis))
		 */
		private Collection getSuccs(final Stmt s, final SootMethod sm, final Collection das) {
			final Collection _result = new HashSet();
			final Iterator _i = das.iterator();
			final int _iEnd = das.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final IDependencyAnalysis _da = (IDependencyAnalysis) _i.next();
				final Collection _dents = _da.getDependents(s, sm);
				final Iterator _j = _dents.iterator();
				final int _jEnd = _dents.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final Object _o = _j.next();

					if (_o instanceof Pair) {
						final Pair _p = (Pair) _o;
						_result.add(new PairNode(_p.getFirst(), _p.getSecond()));
					} else if (_o instanceof Stmt) {
						_result.add(new PairNode(_o, sm));
					}
				}
			}
			return _result;
		}
	}


	/**
	 * A node that represents a pair.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static class PairNode
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
		public boolean equals(final Object object) {
			if (object == this) {
				return true;
			}

			if (!(object instanceof PairNode)) {
				return false;
			}

			PairNode rhs = (PairNode) object;
			return new EqualsBuilder().appendSuper(super.equals(object)).append(this.first, rhs.first)
										.append(this.second, rhs.second).isEquals();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return new HashCodeBuilder(7, 37).appendSuper(super.hashCode()).append(this.first).append(this.second).toHashCode();
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

			final InfluenceChecker _checker = new InfluenceChecker();

			_checker.setRootMethodTrapper(new RootMethodTrapper() {
					/**
					 * @see edu.ksu.cis.indus.common.soot.RootMethodTrapper#isThisARootMethod(soot.SootMethod)
					 */
					protected boolean isThisARootMethod(final SootMethod sm) {
						return true;
					}
				});

			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}

			_checker.xmlizer.setXmlOutputDir(_outputDir);

			if (_cl.hasOption('p')) {
				_checker.addToSootClassPath(_cl.getOptionValue('p'));
			}
			_checker.useAliasedUseDefv1 = true;
			_checker.useSafeLockAnalysis = true;

			final List _classNames = _cl.getArgList();

			if (_classNames.isEmpty()) {
				throw new MissingArgumentException("Please specify atleast one class.");
			}
			_checker.setClassNames(_classNames);

			final InterferenceDAv1 _da1 = new InterferenceDAv3();
			_da1.setUseOFA(true);
			_checker.ddas.add(_da1);
			_checker.ddas.add(new IdentifierBasedDataDAv3());
			_checker.ddas.add(new ReferenceBasedDataDA());

			final ReadyDAv1 _da2 = ReadyDAv3.getBackwardReadyDA();
			_checker.cdas.add(_da2);
			_checker.cdas.add(new NonTerminationSensitiveEntryControlDA());

			_da2.setUseOFA(true);
			_da2.setUseSafeLockAnalysis(_checker.useSafeLockAnalysis);

			_checker.execute();

			_checker.performCheck(_cl.getOptionValue("sc"), _cl.getOptionValue("sm"),
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
	 * Retrieve the automaton based on the type of influence check.
	 *
	 * @param type of influence check.  This has to be one of "ai", "ddi", "cdi", "ci", or "di".
	 *
	 * @return an automaton.
	 *
	 * @throws IllegalArgumentException if the type is not one of the specified values.
	 */
	private IAutomaton getAutomaton(final String type) {
		final NFA _result = new NFA();

		if (type.equals("ddi") || type.equals("di")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			IState _s3 = new State("s3");
			IState _s4 = new State("s4");
			_result.addFinalState(_s4);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, CD, _s3);
			_result.addLabelledTransitionFromTo(_s2, IAutomaton.EPSILON, _s3);
			_result.addLabelledTransitionFromTo(_s3, CD, _s3);
			_result.addLabelledTransitionFromTo(_s3, DD, _s4);
			_result.addLabelledTransitionFromTo(_s3, IAutomaton.EPSILON, _s4);
			_result.addLabelledTransitionFromTo(_s4, DD, _s4);
		} else if (type.equals("cdi")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			IState _s3 = new State("s3");
			_result.addFinalState(_s3);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, DD, _s2);
			_result.addLabelledTransitionFromTo(_s2, CD, _s3);
			_result.addLabelledTransitionFromTo(_s3, CD, _s3);
		} else if (type.equals("ci")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			_result.addFinalState(_s2);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, CD, _s2);
			_result.addLabelledTransitionFromTo(_s2, CD, _s2);
		} else if (type.equals("ai")) {
			IState _s1 = new State("s1");
			IState _s2 = new State("s2");
			_result.addFinalState(_s2);
			_result.setStartState(_s1);
			_result.addLabelledTransitionFromTo(_s1, CALLS, _s2);
			_result.addLabelledTransitionFromTo(_s2, CALLS, _s2);
		} else {
			throw new IllegalArgumentException("type has to be one of ai, ddi, cdi, ci, or di.");
		}
		_result.start();

		return _result;
	}

	/**
	 * Retrieves the graph based on the type of influence check.
	 *
	 * @param type of influence check.  This has to be one of "ai", "ddi", "cdi", "ci", or "di".
	 *
	 * @return an automaton.
	 *
	 * @throws IllegalArgumentException if the type is not one of the specified values.
	 */
	private IEdgeLabelledDirectedGraphView getGraph(String type) {
		final IEdgeLabelledDirectedGraphView _result;

		if (type.equals("ai")) {
			_result = new CallGraphView();
		} else if (type.equals("cdi") || type.equals("ddi") || type.equals("ci") || type.equals("di")) {
			_result = new DependenceGraphView();
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
	 *
	 * @return the method of the given signature in the named class.
	 *
	 * @pre clazz != null and methodSignature != null
	 */
	private SootMethod getMethod(final String clazz, final String methodSignature) {
		final SootMethod _result;
		final SootClass _sc = getScene().getSootClass(clazz);
		_result = _sc.getMethod(methodSignature);
		return _result;
	}

	/**
	 * Retrieves the statement at the given index in the method.
	 *
	 * @param sm containing the statement.
	 * @param index at which the statement occurs.
	 *
	 * @return the statement.
	 *
	 * @pre sm != null
	 */
	private Stmt getStmt(final SootMethod sm, final int index) {
		final List _units = new ArrayList(sm.retrieveActiveBody().getUnits());
		return (Stmt) _units.get(index);
	}

	/**
	 * Retrieves a call graph based on class hierarchy analysis.
	 */
	private void calculateCHACallgraph() {
		cgi = new CallGraphInfo(new PairManager(false, true));

		final ProcessingController _pc = new ProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setEnvironment(new Environment(getScene()));

		final CHABasedCallInfoCollector _col = new CHABasedCallInfoCollector();
		_col.hookup(_pc);
		_pc.process();
		_col.unhook(_pc);
		cgi.createCallGraphInfo(_col.getCallInfo());
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

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(cgi, null, getBbm());
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

	/**
	 * Does the check.
	 *
	 * @param startClass contains the starting method and statement.
	 * @param startSig contains the starting statement.
	 * @param sIndex is the index of the starting statement.
	 * @param endClass contains the ending method and statement.
	 * @param endSig contains the ending statement.
	 * @param eIndex is the index of the ending statement.
	 * @param type of check.  This has to be one of "ai", "ddi", "cdi", "ci", or "di".
	 *
	 * @pre startClass != null and startSig != null and sIndex != null and endClass != null and endSig != null and  eIndex !=
	 * 		null
	 */
	private void performCheck(final String startClass, final String startSig, final int sIndex, final String endClass,
		final String endSig, final int eIndex, final String type) {
		final SootMethod _sm = getMethod(startClass, startSig);
		final Stmt _ss = type.equals("ai") ? null
										   : getStmt(_sm, sIndex);
		final SootMethod _em = getMethod(endClass, endSig);
		final Stmt _es = type.equals("ai") ? null
										   : getStmt(_em, eIndex);
		final IEdgeLabelledDirectedGraphView _graph = getGraph(type);
		final IWorkBag _wb = new LIFOWorkBag();

		if (type.equals("ai")) {
			calculateCHACallgraph();
		}

		int _missedPaths = 0;
		final Collection _matchedPaths = new HashSet();

		final INode _target = new PairNode(_es, _em);
		_wb.addWork(new Triple(new PairNode(_ss, _sm), new Stack(), getAutomaton(type)));

		while (_wb.hasWork()) {
			final Triple _triple = (Triple) _wb.getWork();
			final INode _src = (INode) _triple.getFirst();
			final Stack _path = (Stack) _triple.getSecond();
			final IAutomaton _auto = (IAutomaton) _triple.getThird();

			_path.push(_src);

			if (_auto.isInFinalState() && _src.equals(_target)) {
				_matchedPaths.add(_path);
			} else {
				if (_auto.canPerformTransition(IAutomaton.EPSILON)) {
					final IAutomaton _aclone = (IAutomaton) _auto.clone();
					_aclone.performTransitionOn(IAutomaton.EPSILON);

					final Stack _temp = (Stack) _path.clone();
					_temp.push(IAutomaton.EPSILON);
					_wb.addWork(new Triple(_src, _temp, _aclone));
				}

				final Collection _temp = new HashSet();
				final Collection _succs = new ArrayList();
				final Collection _outgoingEdgeLabels = _graph.getOutgoingEdgeLabels(_src);
				final Iterator _i = _outgoingEdgeLabels.iterator();
				final int _iEnd = _outgoingEdgeLabels.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final IAutomaton.ITransitionLabel _label = (IAutomaton.ITransitionLabel) _i.next();

					if (_auto.canPerformTransition(_label)) {
						final Collection _succsViaLabel = _graph.getSuccsViaEdgesLabelled(_src, _label);
						final Iterator _j = _succsViaLabel.iterator();
						final int _jEnd = _succsViaLabel.size();

						for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
							final INode _dest = (INode) _j.next();

							if (!_path.contains(_dest)) {
								final IAutomaton _aclone = (IAutomaton) _auto.clone();
								_aclone.performTransitionOn(_label);

								final Stack _t = (Stack) _path.clone();
								_t.push(_label);
								_temp.add(new Triple(_dest, _t, _aclone));
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

		System.out.println(_matchedPaths);
		System.out.println(_missedPaths + " " + _matchedPaths.size());
	}
}

// End of File
