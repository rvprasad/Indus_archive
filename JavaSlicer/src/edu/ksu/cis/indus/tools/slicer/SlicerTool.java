
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.slicer.BackwardSliceGotoProcessor;
import edu.ksu.cis.indus.slicer.CompleteSliceGotoProcessor;
import edu.ksu.cis.indus.slicer.ForwardSliceGotoProcessor;
import edu.ksu.cis.indus.slicer.ISliceGotoProcessor;
import edu.ksu.cis.indus.slicer.SliceCollector;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.SlicingEngine;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.SafeLockAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.EntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.NewExpr2InitMapper;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;

import edu.ksu.cis.indus.tools.AbstractTool;
import edu.ksu.cis.indus.tools.CompositeToolConfiguration;
import edu.ksu.cis.indus.tools.CompositeToolConfigurator;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.processing.ExecutableSlicePostProcessor;
import edu.ksu.cis.indus.tools.slicer.processing.ISlicePostProcessor;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import soot.Scene;
import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This is a facade that exposes the slicer as a tool.  This is recommended interface to interact with the slicer if the
 * slicer is being used as a tool in a tool chain.
 * 
 * <p>
 * The term "immediate slice" in the context of this file implies the slice containing only entities on which the given term
 * depends on, not the transitive closure.
 * </p>
 * 
 * <p>
 * There are 3 types of slices: forward, backward, and complete(forward and backward).  Also, there are 2  flavours of
 * slices: executable and non-executable.
 * </p>
 * 
 * <p>
 * Backward slicing is inclusion of anything that leads to the slice criterion from the given entry points to the system.
 * This can provide a executable system which will  simulate the given system along all paths from the entry points leading
 * to the slice criterion independent of the input. In case the input causes a divergence in this path then the simulation
 * ends there.
 * </p>
 * 
 * <p>
 * However, in case of forward slicing, one would include everything that is affected by the slice criterion.  This  will
 * never lead to an semantically meaningful executable slice as the part of the system that leads to the slice criterion is
 * not captured. Rather a more meaningful notion is that of a complete slice. This includes everything that affects the
 * given slice criterion and  everything affected by the slice criterion.
 * </p>
 * 
 * <p>
 * Due to the above view we only support non-executable slices of all types and only executable slices of backward and
 * complete type.
 * </p>
 * 
 * <p>
 * If slice for deadlock option is true in the slicer configuration, then criteria to preserve deadlocking are generated.
 * However, these are generated on behalf of the application/driver (as a convenience) and not for internal use.  Hence, the
 * application/driver is responsible for the disposal of these criteria.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerTool
  extends AbstractTool {
	static {
		final Phase _i = Phase.createPhase();
		_i.nextMajorPhase();
		DEPENDENCE_MAJOR_PHASE = (Phase) _i.clone();
		_i.nextMajorPhase();
		SLICE_MAJOR_PHASE = (Phase) _i.clone();
	}

	/** 
	 * This represents the phase in which dependence analysis happens.
	 */
	public static final Object DEPENDENCE_MAJOR_PHASE;

	/** 
	 * This represents the phase in which slicing happens.
	 */
	public static final Object SLICE_MAJOR_PHASE;

	/** 
	 * The tag used to identify the parts touched by flow analysis.
	 */
	public static final String FLOW_ANALYSIS_TAG_NAME = "indus.tools.slicer.SlicerTool:FA";

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerTool.class);

	/** 
	 * This is the indentation step to be used during stringization of the configuration.
	 */
	private static final int INDENT = 4;

	/** 
	 * This controls dependency analysis.
	 *
	 * @invariant daController != null
	 */
	private final AnalysesController daController;

	/** 
	 * This manages the basic block graphs for the methods being transformed.
	 *
	 * @invariant bbgMgr != null
	 */
	private final BasicBlockGraphMgr bbgMgr;

	/** 
	 * This provides the call graph.
	 *
	 * @invariant callGraph != null
	 */
	private final CallGraph callGraph;

	/** 
	 * The slicing criteria.
	 *
	 * @invariant criteria != null and criteria.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	private final Collection criteria;

	/** 
	 * The entry point methods.
	 *
	 * @invariant rootMethods.oclIsKindOf(Collection(SootMethod))
	 */
	private final Collection rootMethods;

	/** 
	 * This provides <code>UnitGraph</code>s for the analyses.
	 */
	private final IStmtGraphFactory stmtGraphFactory;

	/** 
	 * This provides object flow anlaysis.
	 */
	private final OFAnalyzer ofa;

	/** 
	 * The phase in which the tool's execution is in.
	 */
	private Phase phase;

	/** 
	 * This is the slicing engine that identifies the slice.
	 */
	private final SlicingEngine engine;

	/** 
	 * This provides thread graph.
	 */
	private final ThreadGraph threadGraph;

	/** 
	 * This is a call-graph based pre processing controller.
	 */
	private final ValueAnalyzerBasedProcessingController cgBasedPreProcessCtrl;

	/** 
	 * This controls the processing of callgraph.
	 */
	private final ValueAnalyzerBasedProcessingController cgPreProcessCtrl;

	/** 
	 * The system to be sliced.
	 */
	private Scene system;

	/** 
	 * This is the slice criterion factory that will be used.
	 */
	private final SliceCriteriaFactory criteriaFactory;

	/** 
	 * This provides use-def information based on aliasing.
	 */
	private AliasedUseDefInfov2 aliasUD;

	/** 
	 * This is the instance of equivalence class based escape analysis used by this object.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/** 
	 * This is the information map used to initialized analyses.
	 */
	private final Map info = new HashMap();

	/** 
	 * This provides monitor information.
	 */
	private final MonitorAnalysis monitorInfo;

	/** 
	 * This provides mapping from init invocation expression to corresponding new expression.
	 */
	private NewExpr2InitMapper initMapper;

	/** 
	 * This provides safe lock information.
	 */
	private SafeLockAnalysis safelockAnalysis;

	/**
	 * Creates a new SlicerTool object.
	 *
	 * @param tokenMgr is the token manager to be used with this instance of slicer tool.
	 * @param stmtGraphFactoryToUse is the statement graph factory to use.
	 *
	 * @pre tokenMgr != null and stamtGraphFactoryToUse != null
	 */
	public SlicerTool(final ITokenManager tokenMgr, final IStmtGraphFactory stmtGraphFactoryToUse) {
		phase = Phase.createPhase();

		rootMethods = new HashSet();
		criteria = new HashSet();

		// create the flow analysis.
		ofa = OFAnalyzer.getFSOSAnalyzer(FLOW_ANALYSIS_TAG_NAME, tokenMgr);

		stmtGraphFactory = stmtGraphFactoryToUse;

		// create the pre processor for call graph construction.
		cgPreProcessCtrl = new ValueAnalyzerBasedProcessingController();
		cgPreProcessCtrl.setAnalyzer(ofa);
		cgPreProcessCtrl.setProcessingFilter(new TagBasedProcessingFilter(FLOW_ANALYSIS_TAG_NAME));
		cgPreProcessCtrl.setStmtGraphFactory(getStmtGraphFactory());

		// create the call graph.
		callGraph = new CallGraph();

		// create the pre processor for thread graph construction.
		cgBasedPreProcessCtrl = new ValueAnalyzerBasedProcessingController();
		cgBasedPreProcessCtrl.setProcessingFilter(new CGBasedProcessingFilter(callGraph));
		cgBasedPreProcessCtrl.setAnalyzer(ofa);
		cgBasedPreProcessCtrl.setStmtGraphFactory(getStmtGraphFactory());

		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setUnitGraphFactory(getStmtGraphFactory());
		// create the thread graph.
		threadGraph = new ThreadGraph(callGraph, new CFGAnalysis(callGraph, bbgMgr));
		// create equivalence class-based escape analysis.
		ecba = new EquivalenceClassBasedEscapeAnalysis(callGraph, threadGraph, bbgMgr);
		// create monitor analysis
		monitorInfo = new MonitorAnalysis();
		// create safe lock analysis
		safelockAnalysis = new SafeLockAnalysis();
		// create alias use def analysis
		aliasUD = new AliasedUseDefInfov2(ofa, callGraph, threadGraph, bbgMgr);
		
		// set up data required for dependency analyses.
		info.put(ICallGraphInfo.ID, callGraph);
		info.put(IThreadGraphInfo.ID, threadGraph);
		info.put(IEnvironment.ID, ofa.getEnvironment());
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, aliasUD);
		info.put(Pair.PairManager.ID, new Pair.PairManager());
		info.put(IValueAnalyzer.ID, ofa);
		info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		info.put(IMonitorInfo.ID, monitorInfo);
		info.put(SafeLockAnalysis.ID, safelockAnalysis);

		// create dependency analyses controller 
		daController = new AnalysesController(info, cgBasedPreProcessCtrl, bbgMgr);

		// create the slicing engine.
		engine = new SlicingEngine();

		// create the <init> call to new expr mapper
		initMapper = new NewExpr2InitMapper();

		criteriaFactory = SliceCriteriaFactory.getFactory();
	}

	/**
	 * Sets configuration named by <code>configName</code> as the active configuration.
	 *
	 * @param configID is id of the configuration to activate.
	 *
	 * @pre configID != null
	 */
	public void setActiveConfiguration(final String configID) {
		if (configurationInfo instanceof CompositeToolConfiguration) {
			((CompositeToolConfiguration) configurationInfo).setActiveToolConfigurationID(configID);
		}
	}

	/**
	 * Retrieves the basic block graph manager used by this tool.
	 *
	 * @return the basic block graph manager.
	 */
	public BasicBlockGraphMgr getBasicBlockGraphManager() {
		return bbgMgr;
	}

	/**
	 * Returns the call graph used by the slicer.
	 *
	 * @return the call graph used by the slicer.
	 */
	public ICallGraphInfo getCallGraph() {
		return callGraph;
	}

	/**
	 * Set the slicing criteria.
	 *
	 * @param theCriteria is a collection of slicing criteria.
	 *
	 * @pre theCriteria != null and theCriteria.oclIsKindOf(Collection(AbstractSlicingCriteria))
	 * @pre theCriteria->forall(o | o != null)
	 */
	public void setCriteria(final Collection theCriteria) {
		criteria.clear();
		criteria.addAll(theCriteria);
	}

	/**
	 * Retrieves the slicing criteria.
	 *
	 * @return returns the criteria.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriteria() {
		return criteria;
	}

	/**
	 * Returns the dependency analyses used by this object.
	 *
	 * @return the collection of dependency analyses.
	 *
	 * @post result != null and result.oclIsKindOf(Set(AbstractDependencyAnalysis))
	 */
	public Collection getDAs() {
		final Collection _result = new LinkedHashSet();
		final SlicerConfiguration _config = ((SlicerConfiguration) getActiveConfiguration());
		final List _daNames = new ArrayList(_config.getIDsOfDAsToUse());
		Collections.sort(_daNames);

		for (final Iterator _i = _daNames.iterator(); _i.hasNext();) {
			_result.addAll(_config.getDependenceAnalyses(_i.next()));
		}
		return _result;
	}

	/**
	 * Returns the phase in which the tool's execution.
	 *
	 * @return an object that represents the phase of the tool's execution.
	 */
	public Object getPhase() {
		return phase;
	}

	/**
	 * Set the methods which serve as the entry point into the system to be sliced.
	 *
	 * @param theRootMethods is a collection of methods.
	 *
	 * @pre theRootMethods != null and theRootMethods.oclIsKindOf(Collection(SootMethod))
	 * @pre theRootMethods->forall(o | o != null)
	 */
	public void setRootMethods(final Collection theRootMethods) {
		rootMethods.clear();
		rootMethods.addAll(theRootMethods);
	}

	/**
	 * Returns the methods which serve as the entry point into the system to be sliced.
	 *
	 * @return Returns the root methods of the system.
	 *
	 * @post result!= null and result.oclIsKindOf(Collection(SootMethod))
	 */
	public Collection getRootMethods() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Retrieves the statement graph (CFG) provider/factory used by the tool.
	 *
	 * @return the factory object.
	 */
	public IStmtGraphFactory getStmtGraphFactory() {
		return stmtGraphFactory;
	}

	/**
	 * Set the system to be sliced.
	 *
	 * @param theSystem contains the class of the system to be sliced.
	 *
	 * @pre theSystem != null
	 */
	public void setSystem(final Scene theSystem) {
		system = theSystem;
	}

	/**
	 * Retrieves the system being sliced.
	 *
	 * @return the system being sliced.
	 *
	 * @post result != null
	 */
	public Scene getSystem() {
		return this.system;
	}

	/**
	 * Set the tag name to identify the slice.
	 *
	 * @param tagName of the slice.
	 *
	 * @pre tagName != null
	 */
	public void setTagName(final String tagName) {
		engine.setTagName(tagName);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implimentation will load a default configuration if the the given configuration cannot be loaded.
	 * </p>
	 */
	public boolean destringizeConfiguration(final String stringizedForm) {
		IBindingFactory _bindingFactory;
		IUnmarshallingContext _unmarshallingContext;
		boolean _result = false;

		try {
			_bindingFactory = BindingDirectory.getFactory(CompositeToolConfiguration.class);
			_unmarshallingContext = _bindingFactory.createUnmarshallingContext();
		} catch (JiBXException _e) {
			LOGGER.fatal("Error while setting up JiBX.  Aborting.", _e);
			throw new RuntimeException(_e);
		}

		configurationInfo = null;

		if (stringizedForm != null && stringizedForm.length() != 0) {
			try {
				final StringReader _reader = new StringReader(stringizedForm);
				configurationInfo = (IToolConfiguration) _unmarshallingContext.unmarshalDocument(_reader, null);
				_result = true;
			} catch (JiBXException _e) {
				LOGGER.error("Error while unmarshalling Slicer configurationCollection. Recovering with new clean"
					+ " configuration.", _e);
				configurationInfo = null;
			}
		}

		if (configurationInfo == null) {
			initialize();
		}
		configurator =
			new CompositeToolConfigurator((CompositeToolConfiguration) configurationInfo, new SlicerConfigurator(),
				SlicerConfiguration.getFactory());
		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation of the tool remembers the last phase in which it was stopped.  This saved phase is used when the
	 * tool is executed again with a <code>null</code> valued <code>phaseParam</code>.  If a non-null
	 * <code>phaseParam</code>  is provided, the tool starts executing from the earliest of the saved phase or the given
	 * phase.
	 * </p>
	 */
	public void execute(final Object phaseParam)
	  throws InterruptedException {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Execution of the slicer tool");
		}

		Phase _ph = phase;

		if (phaseParam != null && ((Phase) phaseParam).isEarlierThan(phase)) {
			phase = (Phase) ((Phase) phaseParam).clone();
			_ph = phase;
		}

		if (_ph.equalsMajor(Phase.STARTING_PHASE)) {
			lowLevelAnalysisPhase();
		}

		movingToNextPhase();

		final SlicerConfiguration _slicerConfig = (SlicerConfiguration) getActiveConfiguration();

		if (_ph.equalsMajor((Phase) DEPENDENCE_MAJOR_PHASE)) {
			dependencyAnalysisPhase(_slicerConfig);
		}

		movingToNextPhase();

		if (_ph.equalsMajor((Phase) SLICE_MAJOR_PHASE)) {
			slicingPhase(_slicerConfig);
		}
		phase.finished();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Execution of the slicer tool");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractTool#initialize()
	 */
	public void initialize() {
		configurationInfo = new CompositeToolConfiguration();

		final IToolConfiguration _toolConfig = SlicerConfiguration.getFactory().createToolConfiguration();
		_toolConfig.initialize();
		((CompositeToolConfiguration) configurationInfo).addToolConfiguration(_toolConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		phase.reset();
		daController.reset();
		aliasUD.reset();
		bbgMgr.reset();
		callGraph.reset();
		cgBasedPreProcessCtrl.reset();
		cgPreProcessCtrl.reset();
		initMapper.reset();
		ecba.reset();
		monitorInfo.reset();
		ofa.reset();
		engine.reset();
		stmtGraphFactory.reset();
		threadGraph.reset();
		criteria.clear();
		rootMethods.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public String stringizeConfiguration() {
		final StringWriter _result = new StringWriter();

		try {
			final IBindingFactory _bindingFactory = BindingDirectory.getFactory(CompositeToolConfiguration.class);
			final IMarshallingContext _marshallingContext = _bindingFactory.createMarshallingContext();
			_marshallingContext.setIndent(INDENT);
			_marshallingContext.marshalDocument(configurationInfo, "UTF-8", null, _result);
		} catch (JiBXException _e) {
			LOGGER.error("Error while marshalling Slicer configurationCollection.");
			throw new RuntimeException(_e);
		}
		return _result.toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalStateException if forward executable slice is requested.
	 */
	protected void checkConfiguration() {
		final IToolConfiguration _slicerConf = getActiveConfiguration();

		if (((Boolean) _slicerConf.getProperty(SlicerConfiguration.EXECUTABLE_SLICE)).booleanValue()
			  && _slicerConf.getProperty(SlicerConfiguration.SLICE_TYPE).equals(SlicingEngine.FORWARD_SLICE)) {
			LOGGER.error("Forward Executable slice is unsupported.");
			throw new IllegalStateException("Forward Executable slice is unsupported.");
		}
	}

	/**
	 * Executes dependency analyses and monitor analysis.
	 *
	 * @param slicerConfig provides the configuration.
	 *
	 * @pre slicerConfig != null
	 */
	private void dependencyAnalysisPhase(final SlicerConfiguration slicerConfig) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: dependence analyses phase");
		}

		// perform dependency analyses
		daController.reset();

		/*
		 * Exit control dependence required for complete/forward slices depends on entry control dependence.
		 * Setup entry control dependence in the info map and for execution via daController.
		 */
		final Collection _controlDAs = slicerConfig.getDependenceAnalyses(IDependencyAnalysis.CONTROL_DA);

		if (slicerConfig.getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			_controlDAs.add(new EntryControlDA());
		}
		CollectionsUtilities.getSetFromMap(info, IDependencyAnalysis.CONTROL_DA).addAll(_controlDAs);

		// drive the analyses
		for (final Iterator _i = slicerConfig.getIDsOfDAsToUse().iterator(); _i.hasNext();) {
			final Object _id = _i.next();
			final Collection _c = slicerConfig.getDependenceAnalyses(_id);
			daController.addAnalyses(_id, _c);
		}
		daController.addAnalyses(IMonitorInfo.ID, Collections.singleton(monitorInfo));
		daController.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(ecba));

		if (slicerConfig.isSafeLockAnalysisUsedForReady()) {
			daController.addAnalyses(SafeLockAnalysis.ID, Collections.singleton(safelockAnalysis));
		}
		daController.initialize();
		daController.execute();

		/*
		 * We fixed the dependences in the slicer and daController for execution purposes.  Here we unfix it.
		 */
		if (slicerConfig.getSliceType().equals(SlicingEngine.FORWARD_SLICE)) {
			for (final Iterator _i = daController.getAnalyses(IDependencyAnalysis.CONTROL_DA).iterator(); _i.hasNext();) {
				final IDependencyAnalysis _cd = (IDependencyAnalysis) _i.next();

				if (_cd.getDirection().equals(IDependencyAnalysis.BACKWARD_DIRECTION)) {
					_controlDAs.remove(_cd);
					_i.remove();
				}
			}
		}

		phase.nextMajorPhase();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: dependence analyses phase");
		}
	}

	/**
	 * Executes low level analyses.
	 *
	 * @throws InterruptedException when the tool is interrupted when moving between phases.
	 */
	private void lowLevelAnalysisPhase()
	  throws InterruptedException {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: low level static analyses phase");
		}

		phase.reset();
		// do the flow analyses
		ofa.reset();
		bbgMgr.reset();
		stmtGraphFactory.reset();
		ofa.analyze(system, rootMethods);
		phase.nextMinorPhase();

		movingToNextPhase();

		// process flow information into a more meaningful call graph
		callGraph.reset();
		cgPreProcessCtrl.reset();
		callGraph.hookup(cgPreProcessCtrl);
		cgPreProcessCtrl.process();
		callGraph.unhook(cgPreProcessCtrl);
		phase.nextMinorPhase();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Call Graph:\n" + callGraph.dumpGraph());
		}

		movingToNextPhase();

		// process flow information into a more meaningful thread graph. Also, initialize <init> call to new expression 
		// mapper.
		threadGraph.reset();
		cgBasedPreProcessCtrl.reset();
		initMapper.hookup(cgBasedPreProcessCtrl);
		threadGraph.hookup(cgBasedPreProcessCtrl);
		cgBasedPreProcessCtrl.process();
		threadGraph.unhook(cgBasedPreProcessCtrl);
		initMapper.unhook(cgBasedPreProcessCtrl);
		phase.nextMinorPhase();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Thread Graph:\n" + threadGraph.toString());
		}

		movingToNextPhase();

		// process escape analyses.
		cgBasedPreProcessCtrl.reset();
		aliasUD.reset();
		aliasUD.hookup(cgBasedPreProcessCtrl);
		cgBasedPreProcessCtrl.process();
		aliasUD.unhook(cgBasedPreProcessCtrl);

		phase.nextMajorPhase();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: low level static analyses phase");
		}
	}

	/**
	 * Creates criterion based on synchronization constructs and populates <code>criteria</code>.
	 */
	private void populateDeadlockCriteria() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Populating deadlock criteria.");
		}

		for (final Iterator _i = monitorInfo.getMonitorTriples().iterator(); _i.hasNext();) {
			final Triple _mTriple = (Triple) _i.next();
			final SootMethod _method = (SootMethod) _mTriple.getThird();

			if (!_method.getDeclaringClass().isApplicationClass()) {
				continue;
			}

			if (_mTriple.getFirst() == null) {
				// add all entry points and return points (including throws) of the method as the criteria
				final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(_method);

				if (_bbg == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Could not retrieve the basic block graph for " + _method.getSignature()
							+ ".  Moving on.");
					}
					continue;
				}

				final BasicBlock _head = _bbg.getHead();

				if (_head != null) {
					criteria.addAll(criteriaFactory.getCriteria(_method, _head.getLeaderStmt(), false));

					for (final Iterator _j = _bbg.getTails().iterator(); _j.hasNext();) {
						final BasicBlock _bb = (BasicBlock) _j.next();
						final Stmt _stmt = _bb.getTrailerStmt();
						criteria.addAll(criteriaFactory.getCriteria(_method, _stmt, false));
					}
				} else {
					LOGGER.error("Skipping slicing criteria generation for " + _method + " as it has 0 or more than 1 head.");
				}
			} else {
				Collection _criteria = criteriaFactory.getCriteria(_method, (Stmt) _mTriple.getFirst(), true, true);
				criteria.addAll(_criteria);
				_criteria = criteriaFactory.getCriteria(_method, (Stmt) _mTriple.getSecond(), true, true);
				criteria.addAll(_criteria);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Populating deadlock criteria. - " + criteria);
		}
	}

	/**
	 * Process the slice to ensure control flow due to unstructured bytecode processing and also enforce properties such
	 * executability.
	 */
	private void postProcessSlice() {
		final SlicerConfiguration _slicerConfig = (SlicerConfiguration) getActiveConfiguration();

		if (((Boolean) _slicerConfig.getProperty(SlicerConfiguration.EXECUTABLE_SLICE)).booleanValue()) {
			final ISlicePostProcessor _postProcessor = new ExecutableSlicePostProcessor();
			final Object _sliceType = _slicerConfig.getSliceType();
			final SliceCollector _collector = engine.getCollector();
			final Collection _methods = _collector.getMethodsInSlice();
			ISliceGotoProcessor _gotoProcessor = null;

			if (_sliceType.equals(SlicingEngine.FORWARD_SLICE)) {
				_gotoProcessor = new ForwardSliceGotoProcessor(_collector);
			} else if (_sliceType.equals(SlicingEngine.BACKWARD_SLICE)) {
				_gotoProcessor = new BackwardSliceGotoProcessor(_collector);
			} else if (_sliceType.equals(SlicingEngine.COMPLETE_SLICE)) {
				_gotoProcessor = new CompleteSliceGotoProcessor(_collector);
			}
			_postProcessor.process(_methods, bbgMgr, _collector);
			_gotoProcessor.process(_methods, bbgMgr);
		}
	}

	/**
	 * Executes the slicer.
	 *
	 * @param slicerConfig provides the configuration.
	 *
	 * @pre slicerConfig != null
	 */
	private void slicingPhase(final SlicerConfiguration slicerConfig) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: slicing phase");
		}

		// perform slicing
		engine.reset();

		if (slicerConfig.getSliceForDeadlock()) {
			populateDeadlockCriteria();
		}

		if (!criteria.isEmpty()) {
			// setup the slicing engine and slice
			engine.setCgi(callGraph);
			engine.setSliceType(slicerConfig.getProperty(SlicerConfiguration.SLICE_TYPE));
			engine.setInitMapper(initMapper);
			engine.setBasicBlockGraphManager(bbgMgr);
			engine.setAnalysesControllerAndDependenciesToUse(daController, slicerConfig.getIDsOfDAsToUse());
			engine.setSliceCriteria(criteria);
			engine.initialize();
			engine.slice();

			// post process the slice as required
			postProcessSlice();
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No slicing criteria were specified. Hence, no slicing was done.\nIf \"slice for deadlock\" was "
					+ "selected then the system did not have any synchronized methods are blocks.");
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: slicing phase");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.106  2004/07/28 16:52:06  venku
   - documentation.

   Revision 1.105  2004/07/28 09:09:35  venku
   - changed aliased use def analysis to consider thread.
   - also fixed a bug in the same analysis.
   - ripple effect.
   - deleted entry control dependence and renamed direct entry control da as
     entry control da.

   Revision 1.104  2004/07/27 11:07:21  venku
   - updated project to use safe lock analysis.

   Revision 1.103  2004/07/25 01:34:36  venku
   - if a throw was marked and not the exception value, then the code to create
     a value is injected independent of the fact that the thrown value is in the slice.
     This was fixed by using local use def analysis.  FIXED.
   Revision 1.102  2004/07/23 13:10:06  venku
   - Refactoring in progress.
     - Extended IMonitorInfo interface.
     - Teased apart the logic to calculate monitor info from SynchronizationDA
       into MonitorAnalysis.
     - Casted EquivalenceClassBasedEscapeAnalysis as an AbstractAnalysis.
     - ripple effect.
     - Implemented safelock analysis to handle intraprocedural processing.
   Revision 1.101  2004/07/21 11:36:27  venku
   - Extended IUseDefInfo interface to provide both local and non-local use def info.
   - ripple effect.
   - deleted ContainmentPredicate.  Instead, used CollectionUtils.containsAny() in
     ECBA and AliasedUseDefInfo analysis.
   - Added new faster implementation of LocalUseDefAnalysisv2
   - Used LocalUseDefAnalysisv2
   Revision 1.100  2004/07/21 06:28:06  venku
   - changed the signature of methods in SliceCriteriaFactory.
   - When creating expression-based criteria the criteria for the enclosing statement
     is not generated.
   Revision 1.99  2004/07/20 05:20:28  venku
   - EntryControlDA needs to be added only for daController based execution
     and not for slicer execution purposes during forward slicing.  FIXED.
   Revision 1.98  2004/07/20 01:19:48  venku
   - addressed bug #408.
   Revision 1.97  2004/07/20 00:53:09  venku
   - addressed bug #408.
   Revision 1.96  2004/07/20 00:31:04  venku
   - addressed bug #408.
   Revision 1.95  2004/07/16 06:38:48  venku
   - added  a more precise implementation of aliased use-def information.
   - ripple effect.
   Revision 1.94  2004/07/11 14:17:40  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.93  2004/07/09 05:05:25  venku
   - refactored the code to enable the criteria creation to be completely hidden
     from the user.
   - exposed the setting of the considerExecution flag of the criteria in the factory.
   - made SliceCriteriaFactory a singleton.
   Revision 1.92  2004/07/04 11:09:00  venku
   - headless and multiple headed methods cause issue with statement graphs and basic blocks.  FIXED.
   Revision 1.91  2004/06/26 10:15:13  venku
   - documentation.
   Revision 1.90  2004/06/26 06:52:49  venku
   - logging.
   Revision 1.89  2004/06/24 06:53:53  venku
   - refactored SliceConfiguration
     - added processBooleanProperty()
     - renamed getNamesOfDAToUse() to getIDOfDAToUse()
   - ripple effect
   - made AbstractSliceCriterion package private
   - made ISliceCriterion public
   Revision 1.88  2004/06/12 06:47:27  venku
   - documentation.
   - refactoring.
   - coding conventions.
   - catered feature request 384, 385, and 386.
   Revision 1.87  2004/05/31 21:38:11  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.86  2004/05/14 09:02:57  venku
   - refactored:
     - The ids are available in IDependencyAnalysis, but their collection is
       available via a utility class, DependencyAnalysisUtil.
     - DependencyAnalysis will have a sanity check via Unit Tests.
   - ripple effect.
   Revision 1.85  2004/05/14 06:27:21  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.84  2004/05/14 04:43:56  venku
   - enhanced reset() method.
   Revision 1.83  2004/05/10 08:12:03  venku
   - streamlined the names of tags that are used.
   - deleted SlicingTag class.  NamedTag is used instead.
   - ripple effect.
   - SliceCriteriaFactory's interface is enhanced to generate individual
     slice criterion as well as criteria set for all nodes in the given AST chunk.
   Revision 1.82  2004/05/09 11:09:46  venku
   - the client can now specify the statement graph factory to use during slicing.
   Revision 1.81  2004/04/16 20:10:41  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.80  2004/03/29 01:55:08  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.79  2004/03/26 00:25:07  venku
   - added a method to programmatically change the active configuration.
   - ripple effect of refactoring indus soot package.
   Revision 1.78  2004/03/03 10:09:42  venku
   - refactored code in ExecutableSlicePostProcessor and TagBasedSliceResidualizer.
   Revision 1.77  2004/03/03 05:59:37  venku
   - made aliased use-def info intraprocedural control flow reachability aware.
   Revision 1.76  2004/03/03 02:17:54  venku
   - added a new method to ICallGraphInfo interface.
   - implemented the above method in CallGraph.
   - made aliased use-def call-graph sensitive.
   Revision 1.75  2004/02/27 10:15:51  venku
   - incorrect initialization of seed criteria. FIXED.
   Revision 1.74  2004/02/23 07:22:32  venku
   - logging.
   Revision 1.73  2004/02/23 06:49:17  venku
   - logging.
   Revision 1.72  2004/02/23 04:40:17  venku
   - uses ExceptionFlowSensitiveStmtGraph as the default unit graph.
   Revision 1.71  2004/02/01 22:16:16  venku
   - renamed set/getSlicedBBGMgr to set/getBasicBlockGraphManager
     in SlicingEngine.
   - ripple effect.
   Revision 1.70  2004/01/23 16:01:11  venku
   - coding convention.
   Revision 1.69  2004/01/22 13:35:14  venku
   - while generating deadlock criteria, return and entry statements
     of synchronized methods were included in their entirety when
     all that is required is just the reachability to this statement. FIXED.
   Revision 1.68  2004/01/22 01:06:13  venku
   - coding convention.
   Revision 1.67  2004/01/22 01:01:40  venku
   - coding convention.
   Revision 1.66  2004/01/21 02:53:43  venku
   - logging
   - call to reset() methods controllers.
   Revision 1.65  2004/01/20 22:26:12  venku
   - AnalysisController can now set basic block graph managers
     on the controlled analyses.
   - SlicerTool uses the above feature.
   Revision 1.64  2004/01/20 00:46:36  venku
   - criteria are sorted in SlicingEngine instead of SlicerTool.
   - formatting and logging.
   Revision 1.63  2004/01/19 23:53:44  venku
   - moved the logic to order criteria to enforce pseudo-determinism
     during slicing into SlicingEngine.
   Revision 1.62  2004/01/19 23:00:23  venku
   - imposed ordering on the criteria to try to make slicing
     deterministic.
   Revision 1.61  2004/01/19 08:45:20  venku
   - formatting.
   Revision 1.60  2004/01/19 08:30:55  venku
   - Log output formatting.
   Revision 1.59  2004/01/19 08:27:03  venku
   - enabled logging of criteria when they are created in SlicerTool.
   Revision 1.58  2004/01/16 21:18:53  venku
   - renamed setUnitGraphProvider() to setUnitGraphFactory()
     in BasicBlockGraphMgr.
   - ripple effect.
   Revision 1.57  2004/01/13 10:11:04  venku
   - the check for valid combination of slice direction and executability
     of the slice is done in checkConfiguration().
   Revision 1.56  2004/01/13 08:37:55  venku
   - implemented  postProcessSlice().
   Revision 1.55  2004/01/13 04:36:22  venku
   - Now the slicing engine just generates the slice.  The application
     should later on massage the slice for it's needs.  This is triggered
     via postProcessSlice() in SlicerTool.
   Revision 1.54  2004/01/11 00:01:23  venku
   - formatting and coding convention.
   Revision 1.53  2004/01/06 00:17:05  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.52  2004/01/03 19:02:34  venku
   - formatting and coding conventions.
   Revision 1.51  2003/12/31 10:31:19  venku
   - slicer should just slice, not fix the system. FIXED.
   Revision 1.50  2003/12/16 12:44:12  venku
   - exposed the flow analysis tag name.
   Revision 1.49  2003/12/14 16:48:27  venku
   - retrieves sync points only from application classes.
   Revision 1.48  2003/12/13 19:52:41  venku
   - renamed Init2NewExprMapper to NewExpr2InitMapper.
   - ripple effect.
   Revision 1.47  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.46  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.45  2003/12/08 12:20:48  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.44  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.43  2003/12/02 11:32:01  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.42  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.41  2003/12/01 12:15:08  venku
   - optimized populate...() method.
   - used the setConsiderExecution() method in AbstractSliceCriteion.
   Revision 1.40  2003/12/01 04:20:10  venku
   - tag name should be provided for the engine before execution.
   Revision 1.39  2003/11/30 02:13:39  venku
   - incorporated tag based filtering during CG construction.
   Revision 1.38  2003/11/30 01:07:54  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.37  2003/11/30 00:10:20  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.36  2003/11/28 18:16:38  venku
   - formatting.
   Revision 1.35  2003/11/28 16:39:53  venku
   - uses TrapUnitGraphFactory all through.
   - removed unnecessary addition of SlicerConfiguration
     to CompositeConfiguration.
   Revision 1.34  2003/11/26 08:19:10  venku
   - aliasBasedUseDef information analysis was not driven. FIXED.
   Revision 1.33  2003/11/24 22:51:09  venku
   - deleted transformer field as it was not used.
   Revision 1.32  2003/11/24 10:11:32  venku
   - there are no residualizers now.  There is a very precise
     slice collector which will collect the slice via tags.
   - architectural change. The slicer is hard-wired wrt to
     slice collection.  Residualization is outside the slicer.
   Revision 1.31  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.30  2003/11/23 19:54:32  venku
   - used LinkedHashSet instead of HashSet while retrieving DAs
     for the purpose of testing.
   Revision 1.29  2003/11/22 00:44:23  venku
   - ripple effect of splitting initialize() in SliceEngine into many methods.
   Revision 1.28  2003/11/18 21:42:03  venku
   - altered the phase locking logic along with new documentation.
   Revision 1.27  2003/11/17 17:56:21  venku
   - reinstated initialize() method in AbstractTool and SlicerTool.  It provides a neat
     way to intialize the tool independent of how it's dependent
     parts (such as configuration) were instantiated and intialized.
   Revision 1.26  2003/11/17 02:23:52  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.25  2003/11/16 18:33:01  venku
   - fixed an error while returning the DAs.
   Revision 1.24  2003/11/16 18:24:08  venku
   - added methods to retrive active dependencies.
   - documentation and formatting.
   Revision 1.23  2003/11/15 22:06:54  venku
   - added support to extract call graph and environment.
   Revision 1.22  2003/11/15 21:27:03  venku
   - deleted initialize()
   - added a new method to extract call graph
   Revision 1.21  2003/11/09 08:00:39  venku
   - if criteria is empty, slicer will not be run.
   - destringizeConfiguration() now creates a new configuration
     via the factory if the given configuration string is empty or null.
   Revision 1.20  2003/11/07 12:25:48  venku
   - equivalence class-based analysis was preprocessed but
     not executed. FIXED.
   Revision 1.19  2003/11/06 05:15:05  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.18  2003/11/05 03:16:21  venku
   - changes in creating the criteria.
   - coding convention.
   Revision 1.17  2003/11/03 08:14:17  venku
   - fixed processing for equivalence class based escape analysis.
   Revision 1.16  2003/11/03 08:05:34  venku
   - lots of changes
     - changes to get the configuration working with JiBX
     - changes to make configuration amenable to CompositeConfigurator
     - added EquivalenceClassBasedAnalysis
     - added fix for Thread's start method
   Revision 1.15  2003/10/21 08:41:49  venku
   - fixed minor errors regarding missing mandatory
     method calls on the analyses.
   Revision 1.14  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.13  2003/10/20 13:55:25  venku
   - Added a factory to create new configurations.
   - Simplified AbstractToolConfigurator methods.
   - The driver manages the shell.
   - Got all the gui parts running EXCEPT for changing
     the name of the configuration.
   Revision 1.12  2003/10/19 20:04:42  venku
   - configuration should be (un)marshalled not the tool. FIXED.
   Revision 1.11  2003/10/13 01:01:45  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.10  2003/09/28 06:54:17  venku
   - one more small change to the interface.
   Revision 1.9  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
   Revision 1.8  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.7  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.6  2003/09/27 01:27:46  venku
   - documentation.
   Revision 1.5  2003/09/27 01:09:35  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.4  2003/09/26 15:08:02  venku
   - completed support for exposing slicer as a tool
     and configuring it both in Bandera and outside it.
   Revision 1.3  2003/09/26 07:33:29  venku
   - checkpoint commit.
   Revision 1.2  2003/09/26 05:55:28  venku
   - a checkpoint commit. Also a cvs fix commit.
   Revision 1.1  2003/09/24 07:32:23  venku
   - Created an implementation of indus tool api specific to Slicer.
     The GUI needs to be setup and bandera adapter needs to be fixed.
 */
