
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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import edu.ksu.cis.bandera.util.BaseObservable;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.callgraphs.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.SharedWriteBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.impl.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;


/**
 * This class provides run-time dependence information and may-follow information required schedule instructions correctly
 * for in a concurrent setting.  This is used for POR in Bogor.
 * 
 * <p>
 * PUT A LINK TO THE TECH REPORT FIX_ME
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class RelativeDependenceInfoTool
  extends BaseObservable
  implements Tool {
	/** 
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "scene";

	/** 
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "entryPoints";

	/** 
	 * This identifies the dependence info in the output arguments.
	 */
	public static final Object DEPENDENCE = "dependence information";

	/** 
	 * This identifies the known transitions info in the output arguments.
	 */
	public static final Object KNOWN_TRANSITIONS = "known transitions information";

	/** 
	 * This identifies the may-flow relation in the output arguments.
	 */
	public static final Object MAY_FOLLOW_RELATION = "may follow relation";

	/** 
	 * This identifies the lock acquisition equivalence class in the output arguments.
	 */
	public static final Object LOCK_ACQUISITIONS = "lock acquisitions";

	/** 
	 * This identifies the array refs equivalence class in the output arguments.
	 */
	public static final Object ARRAY_REFS = "array refs";

	/** 
	 * This identifies the field refs equivalence class in the output arguments.
	 */
	public static final Object FIELD_REFS = "field refs";

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(RelativeDependenceInfoTool.class);

	/** 
	 * This is the special location associated with lock acquisition while entering synchronized methods in bir models
	 * generated by j2b.
	 */
	static final String SYNC_METHOD_LOCATIONS = "sync";

	/** 
	 * This is the method name prefix in bir model from j2b.
	 */
	private static final String METHOD_PREFIX = "{|";

	/** 
	 * This is the method name suffix in bir model from j2b.
	 */
	private static final String METHOD_SUFFIX = "|}";

	/** 
	 * This is the virtual method name prefix in bir model from j2b.
	 */
	private static final String VIRTUAL_PREFIX = "+|";

	/** 
	 * This is the virtual method name suffix in bir model from j2b.
	 */
	private static final String VIRTUAL_SUFFIX = "|+";

	/** 
	 * The collection of input argument identifiers.
	 */
	private static final List IN_ARGUMENTS_IDS;

	/** 
	 * The collection of output argument identifiers.
	 */
	private static final List OUT_ARGUMENTS_IDS;

	static {
		IN_ARGUMENTS_IDS = new ArrayList();
		IN_ARGUMENTS_IDS.add(SCENE);
		IN_ARGUMENTS_IDS.add(ROOT_METHODS);
		OUT_ARGUMENTS_IDS = new ArrayList();
		OUT_ARGUMENTS_IDS.add(DEPENDENCE);
		OUT_ARGUMENTS_IDS.add(KNOWN_TRANSITIONS);
		OUT_ARGUMENTS_IDS.add(MAY_FOLLOW_RELATION);
		OUT_ARGUMENTS_IDS.add(LOCK_ACQUISITIONS);
		OUT_ARGUMENTS_IDS.add(ARRAY_REFS);
		OUT_ARGUMENTS_IDS.add(FIELD_REFS);
	}

	/** 
	 * The collection of array referring bir locations.
	 */
	final Collection arrayRefs = new HashSet();

	/** 
	 * The collection of array referring bir locations.
	 */
	final Collection fieldRefs = new HashSet();

	/** 
	 * The collection of array referring bir locations.
	 */
	final Collection lockAcquisitions = new HashSet();

	/** 
	 * This is the collection of bir location corresponding to the statements seen by the processor.
	 *
	 * @invariant seenStmts.oclIsKindOf(Collection(Stmt))
	 */
	final Collection seenStmts = new HashSet();

	/** 
	 * This is dependence info in terms of bir locations.
	 *
	 * @invariant dependence.oclIsKindOf(Map(Stmt, Collection(Stmt)))
	 */
	final Map dependence = new HashMap();

	/** 
	 * This is may-follow info in terms of bir locations.
	 *
	 * @invariant dependence.oclIsKindOf(Map(Stmt, Collection(Stmt)))
	 */
	final Map mayFollow = new HashMap();

	/** 
	 * This is entry points to the system.
	 *
	 * @invariant rootMethods.oclIsKindOf(Collection(SootMethod))
	 */
	private Collection rootMethods;

	/** 
	 * This is the environment to be analyzed.
	 */
	private IEnvironment env;

	/** 
	 * This captures the signal from the tool framework to abort at the next suitable time.
	 */
	private boolean abort;

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String arg0)
	  throws Exception {
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration()
	  throws Exception {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map arg)
	  throws Exception {
		final Scene _scene = (Scene) arg.get(SCENE);

		if (_scene == null) {
			LOGGER.error("A scene must be provided.");
			throw new IllegalArgumentException("A scene must be provided.");
		}
		env = new Environment(_scene);

		final Collection _rootMethods = (Collection) arg.get(ROOT_METHODS);

		if (_rootMethods == null || _rootMethods.isEmpty()) {
			final String _msg = "Atleast one method should be specified as the entry-point into the system.";
			LOGGER.fatal(_msg);
			throw new IllegalArgumentException(_msg);
		}
		rootMethods = new ArrayList();
		rootMethods.addAll(_rootMethods);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List getInputParameterList() {
		return Collections.unmodifiableList(IN_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputMap()
	 */
	public Map getOutputMap() {
		final Map _map = new HashMap();
		_map.put(DEPENDENCE, dependence);
		_map.put(KNOWN_TRANSITIONS, seenStmts);
		_map.put(MAY_FOLLOW_RELATION, mayFollow);
		_map.put(LOCK_ACQUISITIONS, lockAcquisitions);
		_map.put(ARRAY_REFS, arrayRefs);
		_map.put(FIELD_REFS, fieldRefs);
		return _map;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List getOutputParameterList() {
		return Collections.unmodifiableList(OUT_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolConfigurationView()
	 */
	public ToolConfigurationView getToolConfigurationView() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	public void quit()
	  throws Exception {
		abort = true;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run()
	  throws Exception {
		abort = false;
		run(env, rootMethods);
	}

	/**
	 * This method constructs the BIR representation of the name of a method.
	 *
	 * @param isVirtual shows whether or not this method is invoked in a virtual invoke expression.
	 * @param sm The soot method whose name will be compiled here.
	 *
	 * @return the BIR representation of the name of sm.
	 */
	static String constructMethodName(final boolean isVirtual, final SootMethod sm) {
		final SootClass _sc = sm.getDeclaringClass();
		final String _className = _sc.getName();
		final String _methodName = sm.getName();
		final int _size = sm.getParameterCount();
		final String[] _paramTypeNames = new String[_size];

		//get the array of the soot types of the parameters of the method
		for (int _i = 0; _i < _size; _i++) {
			_paramTypeNames[_i] = sm.getParameterType(_i).toString();
		}

		//construct the method name.
		final StringBuffer _sb = new StringBuffer(isVirtual ? VIRTUAL_PREFIX
															: METHOD_PREFIX);
		_sb.append(_className);
		_sb.append('.');
		_sb.append(_methodName);
		_sb.append('(');

		final int _size1 = _paramTypeNames.length;

		if (_size1 > 0) {
			_sb.append(_paramTypeNames[0]);

			for (int _i = 1; _i < _size1; _i++) {
				_sb.append(',');
				_sb.append(_paramTypeNames[_i]);
			}
		}

		_sb.append(')');
		_sb.append(isVirtual ? VIRTUAL_SUFFIX
							 : METHOD_SUFFIX);

		return _sb.toString();
	}

	/**
	 * Executes the tool.
	 *
	 * @param environment to be analyzed.
	 * @param entryPointMethods are the entry points to the environment.
	 */
	void run(final IEnvironment environment, final Collection entryPointMethods) {
		final String _tagName = "DependenceInfoTool:FA";
		final IValueAnalyzer _aa =
			OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager(new SootValueTypeManager()));

		if (abort) {
			return;
		}
		_aa.analyze(environment, entryPointMethods);

		final IStmtGraphFactory _stmtGraphFactory = new CompleteStmtGraphFactory();
		final BasicBlockGraphMgr _bbm = new BasicBlockGraphMgr();
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final PairManager _pairManager = new PairManager(false, true);
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final CFGAnalysis _cfgAnalysis = new CFGAnalysis(_cgi, _bbm);
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, _cfgAnalysis, _pairManager);
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
		final MetricsProcessor _countingProcessor = new MetricsProcessor();
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_bbm.setStmtGraphFactory(_stmtGraphFactory);
		_ssr.setBbgFactory(_bbm);

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

		if (abort) {
			return;
		}

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, null, _bbm);
		_info.put(IEscapeInfo.ID, _ecba);
		_callGraphInfoCollector.reset();
		_processors.clear();
		_processors.add(_callGraphInfoCollector);
		_pc.reset();
		_pc.driveProcessors(_processors);
		_cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());

		if (abort) {
			return;
		}

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add(_tgi);
		_processors.add(_countingProcessor);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);

		final AnalysesController _ac = new AnalysesController(_info, _cgipc, _bbm);
		_ac.addAnalyses(IEscapeInfo.ID, Collections.singleton(_ecba));

		if (abort) {
			return;
		}

		final InterferenceDAv3 _iDA = new InterferenceDAv3();
		_iDA.setUseOFA(true);
		_ac.addAnalyses(IDependencyAnalysis.INTERFERENCE_DA, Collections.singleton(_iDA));
		_ac.initialize();
		_ac.execute();

		final ProcessingController _pc2 = new ProcessingController();
		_pc2.setEnvironment(_aa.getEnvironment());
		_pc2.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));
		_pc2.setStmtSequencesRetriever(_ssr);

		if (abort) {
			return;
		}

		final LockAcquisitionBasedEquivalence _lbe = new LockAcquisitionBasedEquivalence(_ecba, _cgi);
		_lbe.hookup(_pc2);
		_pc2.process();
		_lbe.unhook(_pc2);

		if (abort) {
			return;
		}

		final SharedWriteBasedEquivalence _swbe = new SharedWriteBasedEquivalence(_ecba);
		_swbe.hookup(_pc2);
		_pc2.process();
		_swbe.unhook(_pc2);

		if (abort) {
			return;
		}

		final DependenceAndMayFollowInfoCalculator _proc;
		_proc = new DependenceAndMayFollowInfoCalculator(this, _iDA, _lbe, _swbe, _tgi, _cfgAnalysis);
		_proc.hookup(_pc2);
		_pc2.process();
		_proc.unhook(_pc2);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("consolidate()");
            _proc.writeDataToFiles();
        }
	}
}

// End of File
