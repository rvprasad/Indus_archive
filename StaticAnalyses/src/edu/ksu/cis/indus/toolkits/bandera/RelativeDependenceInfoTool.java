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
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;

/**
 * This class provides run-time dependence information and may-follow information required schedule instructions correctly for
 * in a concurrent setting. This is used for POR in Bogor.
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
	 * A class that houses constants use by other tools.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static final class Constants {

		/**
		 * This identifies the input option to analyze array refs in application class only.
		 */
		public static final String APPL_ARRAY_REFS_ONLY = "edu.ksu.cis.indus.toolkits.bandera.ArrayRefsInApplicationClassesOnly";

		/**
		 * This identifies the input option to analyze field refs in application class only.
		 */
		public static final String APPL_FIELD_REFS_ONLY = "edu.ksu.cis.indus.toolkits.bandera.FieldRefsInApplicationClassesOnly";

		/**
		 * This identifies the input option to analyze lock acquisition in application class only.
		 */
		public static final String APPL_LOCK_ACQS_ONLY = "edu.ksu.cis.indus.toolkits.bandera.LockAcquisitionsInApplicationClassesOnly";

		/**
		 * This identifies the array refs equivalence class in the to-be serizalized output map.
		 */
		public static final Object ARRAY_REFS = "edu.ksu.cis.projects.bogor.module.por.indus.DynamicRDPORSchedulingStrategist.arrayRefs";

		/**
		 * DOCUMENT ME!
		 */
		public static final String CALCULATOR_CLASS = "edu.ksu.cis.indus.toolkits.bandera.DependenceAndMayFollowInfoCalculator";

		/**
		 * This identifies the dependence info in the to-be serizalized output map.
		 */
		public static final Object DEPENDENCE = "edu.ksu.cis.projects.bogor.module.por.indus.RDPORSchedulingStrategist.dependence";

		/**
		 * This identifies the field refs equivalence class in the to-be serizalized output map.
		 */
		public static final Object FIELD_REFS = "edu.ksu.cis.projects.bogor.module.por.indus.DynamicRDPORSchedulingStrategist.fieldRefs";

		/**
		 * This identifies the known transitions info in the to-be serizalized output map.
		 */
		public static final Object KNOWN_TRANSITIONS = "edu.ksu.cis.projects.bogor.module.por.indus.RDPORSchedulingStrategist.knowntransitions";

		/**
		 * This identifies the lock acquisition equivalence class in the to-be serizalized output map.
		 */
		public static final Object LOCK_ACQUISITIONS = "edu.ksu.cis.projects.bogor.module.por.indus.DynamicRDPORSchedulingStrategist.lockAcquisitions";

		/**
		 * This identifies the may-flow relation in the to-be serizalized output map.
		 */
		public static final Object MAY_FOLLOW_RELATION = "edu.ksu.cis.projects.bogor.module.por.indus.RDPORSchedulingStrategist.mayfollow";

		/**
		 * Creates an instance of this class.
		 */
		private Constants() {
			// prevents the creation of an instance of this class.
		}

	}

	/**
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Comparable<String> ROOT_METHODS = "entryPoints";

	/**
	 * This identifies the scene in the input arguments.
	 */
	public static final Comparable<String> SCENE = "scene";

	/**
	 * This identifies the output map that contains the data that needs to be serialized. The map maps one of the above keys
	 * to an object.
	 */
	public static final Comparable<String> SERIALIZE_DATA_OUTPUT = "SerializedDataMap";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(RelativeDependenceInfoTool.class);

	/**
	 * This is the special location associated with lock acquisition while entering synchronized methods in bir models
	 * generated by j2b.
	 */
	static final String SYNC_METHOD_LOCATION = "sync";

	/**
	 * This is the special location associated with lock acquisition while exiting synchronized methods in bir models
	 * generated by j2b.
	 */
	static final String THROWEX_METHOD_LOCATION = "throwEx";

	/**
	 * This is the special location associated with lock release while exiting synchronized methods in bir models generated by
	 * j2b.
	 */
	static final String UNSYNC_METHOD_LOCATION = "unsync";

	/**
	 * This is the special location associated with lock release while exiting synchronized methods in bir models generated by
	 * j2b.
	 */
	static final String UNSYNCEX_METHOD_LOCATION = "unsyncEx";

	/**
	 * The collection of input argument identifiers.
	 */
	private static final List<Comparable<?>> IN_ARGUMENTS_IDS;

	/**
	 * This is the method name prefix in bir model from j2b.
	 */
	private static final String METHOD_PREFIX = "{|";

	/**
	 * This is the method name suffix in bir model from j2b.
	 */
	private static final String METHOD_SUFFIX = "|}";

	/**
	 * The collection of output argument identifiers.
	 */
	private static final List<Comparable<?>> OUT_ARGUMENTS_IDS;
	static {
		IN_ARGUMENTS_IDS = new ArrayList<Comparable<?>>();
		IN_ARGUMENTS_IDS.add(SCENE);
		IN_ARGUMENTS_IDS.add(ROOT_METHODS);
		OUT_ARGUMENTS_IDS = new ArrayList<Comparable<?>>();
		OUT_ARGUMENTS_IDS.add(SERIALIZE_DATA_OUTPUT);
	}

	/**
	 * This indicates if only the array refs in application class should be analyzed.
	 */
	boolean arrayRefInApplicationClassesOnly;

	/**
	 * The collection of array referring bir locations.
	 */
	final Collection<String> arrayRefs = new HashSet<String>();

	/**
	 * This is dependence info in terms of bir locations.
	 */
	final Map<String, Collection<String>> dependence = new HashMap<String, Collection<String>>();

	/**
	 * This indicates if only the field refs in application class should be analyzed.
	 */
	boolean fieldRefInApplicationClassesOnly;

	/**
	 * The collection of array referring bir locations.
	 */
	final Collection<String> fieldRefs = new HashSet<String>();

	/**
	 * This indicates if only the lock acquisitions in application class should be analyzed.
	 */
	boolean lockAcqInApplicationClassesOnly;

	/**
	 * The collection of array referring bir locations.
	 */
	final Collection<String> lockAcquisitions = new HashSet<String>();

	/**
	 * This is may-follow info in terms of bir locations.
	 */
	final Map<String, Collection<String>> mayFollow = new HashMap<String, Collection<String>>();

	/**
	 * This is the collection of bir location corresponding to the statements seen by the processor.
	 */
	final Collection<String> seenStmts = new HashSet<String>();

	/**
	 * This captures the signal from the tool framework to abort at the next suitable time.
	 */
	private boolean abort;

	/**
	 * This is the environment to be analyzed.
	 */
	private IEnvironment env;

	/**
	 * This maps methods to their bir signature.
	 * 
	 * @invariant method2birsig.oclIsKindOf(Map(SootMethod, String))
	 */
	private final Map<SootMethod, String> method2birsig = new HashMap<SootMethod, String>();

	/**
	 * This is entry points to the system.
	 */
	private Collection<SootMethod> rootMethods;

	/**
	 * DOCUMENT ME!
	 */
	private boolean useV2Calculator;

	/**
	 * This method constructs the BIR representation of the name of a method.
	 * 
	 * @param sm The soot method whose name will be compiled here.
	 * @return the BIR representation of the name of sm.
	 */
	static String constructMethodName(final SootMethod sm) {
		final SootClass _sc = sm.getDeclaringClass();
		final String _className = _sc.getName();
		final String _methodName = sm.getName();
		final int _size = sm.getParameterCount();
		final String[] _paramTypeNames = new String[_size];

		// get the array of the soot types of the parameters of the method
		for (int _i = 0; _i < _size; _i++) {
			_paramTypeNames[_i] = sm.getParameterType(_i).toString();
		}

		// construct the method name.
		final StringBuffer _sb = new StringBuffer(METHOD_PREFIX);
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
		_sb.append(METHOD_SUFFIX);

		return _sb.toString();
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() throws Exception {
		final Properties _result = new Properties();
		_result.setProperty(Constants.APPL_ARRAY_REFS_ONLY, String.valueOf(arrayRefInApplicationClassesOnly));
		_result.setProperty(Constants.APPL_FIELD_REFS_ONLY, String.valueOf(fieldRefInApplicationClassesOnly));
		_result.setProperty(Constants.APPL_LOCK_ACQS_ONLY, String.valueOf(lockAcqInApplicationClassesOnly));

		final ByteArrayOutputStream _out = new ByteArrayOutputStream();
		_result.store(_out, null);
		return _out.toString();
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List<Comparable<?>> getInputParameterList() {
		return Collections.unmodifiableList(IN_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputMap()
	 */
	public Map<Comparable<String>, Map<Object, Object>> getOutputMap() {
		final Map<Object, Object> _map = new HashMap<Object, Object>();
		_map.put(Constants.DEPENDENCE, dependence);
		_map.put(Constants.KNOWN_TRANSITIONS, seenStmts);
		_map.put(Constants.MAY_FOLLOW_RELATION, mayFollow);
		_map.put(Constants.LOCK_ACQUISITIONS, lockAcquisitions);
		_map.put(Constants.ARRAY_REFS, arrayRefs);
		_map.put(Constants.FIELD_REFS, fieldRefs);
		return Collections.singletonMap(SERIALIZE_DATA_OUTPUT, _map);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List<Comparable<?>> getOutputParameterList() {
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
	public void quit() throws Exception {
		abort = true;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run() throws Exception {
		abort = false;
		run(env, rootMethods);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String arg0) throws Exception {
		final Properties _p = new Properties();
		_p.load(new ByteArrayInputStream(arg0.getBytes()));
		fieldRefInApplicationClassesOnly = _p.contains(Constants.APPL_FIELD_REFS_ONLY)
				&& Boolean.valueOf(_p.getProperty(Constants.APPL_FIELD_REFS_ONLY)).booleanValue();
		arrayRefInApplicationClassesOnly = _p.contains(Constants.APPL_ARRAY_REFS_ONLY)
				&& Boolean.valueOf(_p.getProperty(Constants.APPL_ARRAY_REFS_ONLY)).booleanValue();
		lockAcqInApplicationClassesOnly = _p.contains(Constants.APPL_LOCK_ACQS_ONLY)
				&& Boolean.valueOf(_p.getProperty(Constants.APPL_LOCK_ACQS_ONLY)).booleanValue();
		useV2Calculator = _p.contains(Constants.CALCULATOR_CLASS)
				&& _p.getProperty(Constants.CALCULATOR_CLASS).equals(DependenceAndMayFollowInfoCalculatorV2.class.getName());
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map arg) throws Exception {
		final Scene _scene = (Scene) arg.get(SCENE);

		if (_scene == null) {
			LOGGER.error("A scene must be provided.");
			throw new IllegalArgumentException("A scene must be provided.");
		}
		env = new Environment(_scene);

		final Collection<SootMethod> _rootMethods = (Collection<SootMethod>) arg.get(ROOT_METHODS);

		if (_rootMethods == null || _rootMethods.isEmpty()) {
			final String _msg = "Atleast one method should be specified as the entry-point into the system.";
			LOGGER.error(_msg);
			throw new IllegalArgumentException(_msg);
		}
		rootMethods = new ArrayList<SootMethod>();
		rootMethods.addAll(_rootMethods);
	}

	/**
	 * Generates the bir locations for the given statement-method pair.
	 * 
	 * @param p of interest.
	 * @param getUnlocking <code>true</code> indicates that locations names of unlocking transitions are also required if
	 *            the method is synchronized and the statement is <code>null</code>; <code>false</code>, otherwise. This
	 *            is applicable only to entry-exit synchronization of sychronized methods.
	 * @return the bir location.
	 * @throws IllegalStateException when the given statement does not occur in the system.
	 * @pre p != null and p.getSecond() != null
	 * @post result != null
	 */
	Collection<String> generateBIRRep(final Pair<? extends Stmt, SootMethod> p, final boolean getUnlocking) {
		final Stmt _stmt = p.getFirst();
		final SootMethod _method = p.getSecond();
		final String _sig;

		if (method2birsig.containsKey(_method)) {
			_sig = method2birsig.get(_method);
		} else {
			_sig = RelativeDependenceInfoTool.constructMethodName(_method);
			method2birsig.put(_method, _sig);
		}

		final List<Stmt> _sl = new ArrayList<Stmt>(_method.retrieveActiveBody().getUnits());
		final int _index = _sl.indexOf(_stmt);
		final Collection<String> _result = new ArrayList<String>();

		if (_index != -1) {
			_result.add(_sig + " loc" + _index);
		} else if (_stmt == null) {
			_result.add(_sig + " sync");

			if (getUnlocking) {
				_result.add(_sig + " unsync");
				_result.add(_sig + " unsyncEx");
				_result.add(_sig + " throwEx");
			}
		} else {
			throw new IllegalStateException("Hmm");
		}
		return _result;
	}

	/**
	 * Executes the tool.
	 * 
	 * @param environment to be analyzed.
	 * @param entryPointMethods are the entry points to the environment.
	 * @param <T> dummy type parameter.
	 */
	<T extends ITokens<T, Value>> void run(final IEnvironment environment, final Collection<SootMethod> entryPointMethods) {
		final String _tagName = "RelativeDependenceInfoTool:FA";
		final IStmtGraphFactory<CompleteUnitGraph> _stmtGraphFactory = new CompleteStmtGraphFactory();
		final IValueAnalyzer<Value> _aa = OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil
				.<T, Value, Type> getTokenManager(new SootValueTypeManager()), _stmtGraphFactory);

		if (abort) {
			return;
		}
		_aa.analyze(environment, entryPointMethods);

		final BasicBlockGraphMgr _bbm = new BasicBlockGraphMgr();
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection<IProcessor> _processors = new ArrayList<IProcessor>();
		final PairManager _pairManager = new PairManager(false, true);
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final CFGAnalysis _cfgAnalysis = new CFGAnalysis(_cgi, _bbm);
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, _cfgAnalysis, _pairManager);
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
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

		final Map<Comparable<?>, Object> _info = new HashMap<Comparable<?>, Object>();
		_info.put(ICallGraphInfo.ID, _cgi);
		_info.put(IThreadGraphInfo.ID, _tgi);
		_info.put(PairManager.ID, _pairManager);
		_info.put(IEnvironment.ID, _aa.getEnvironment());
		_info.put(IValueAnalyzer.ID, _aa);

		if (abort) {
			return;
		}

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, null, _bbm);
		_info.put(IEscapeInfo.ID, _ecba.getEscapeInfo());
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
		_processors.add((ThreadGraph) _tgi);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);

		final AnalysesController _ac = new AnalysesController(_info, _cgipc, _bbm);
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(_ecba));

		if (abort) {
			return;
		}

		final InterferenceDAv3 _iDA = new InterferenceDAv3();
		_iDA.setUseOFA(true);
		_ac.addAnalyses(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA, Collections.singleton(_iDA));
		_ac.initialize();
		_ac.execute();

		final ProcessingController _pc2 = new ProcessingController();
		_pc2.setEnvironment(_aa.getEnvironment());
		_pc2.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));
		_pc2.setStmtSequencesRetriever(_ssr);

		if (abort) {
			return;
		}

		final LockAcquisitionBasedEquivalence _lbe = new LockAcquisitionBasedEquivalence(_ecba.getEscapeInfo(), _cgi);
		_lbe.hookup(_pc2);
		_pc2.process();
		_lbe.unhook(_pc2);

		if (abort) {
			return;
		}

		final DependenceAndMayFollowInfoCalculator _proc;
		if (useV2Calculator) {
			_proc = new DependenceAndMayFollowInfoCalculatorV2(this, _iDA, _lbe, _cgi, _tgi, _cfgAnalysis, _stmtGraphFactory);
		} else {
			_proc = new DependenceAndMayFollowInfoCalculator(this, _iDA, _lbe, _cgi, _tgi, _cfgAnalysis);
		}
		_proc.setApplicationClassFiltering(lockAcqInApplicationClassesOnly, fieldRefInApplicationClassesOnly,
				arrayRefInApplicationClassesOnly);
		_proc.hookup(_pc2);
		_pc2.process();
		_proc.unhook(_pc2);
	}
}

// End of File
