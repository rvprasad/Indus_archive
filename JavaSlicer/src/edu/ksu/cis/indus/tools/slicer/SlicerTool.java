
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.slicer.SliceCollector;
import edu.ksu.cis.indus.slicer.SliceGotoProcessor;
import edu.ksu.cis.indus.slicer.SlicingEngine;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.SafeLockAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.ThreadEscapeInfoBasedCallingContextRetriever;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationInsensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationSensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.NewExpr2InitMapper;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processors.StaticFieldUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;

import edu.ksu.cis.indus.tools.AbstractTool;
import edu.ksu.cis.indus.tools.CompositeToolConfiguration;
import edu.ksu.cis.indus.tools.CompositeToolConfigurator;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.criteria.generators.ISliceCriteriaGenerator;
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
	 * @invariant criteria != null and criteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	private final Collection criteria;

	/** 
	 * This is used to retrieve any application-level criteria as opposed to those hand-picked by the user.
	 *
	 * @invariant criteriaGenerators.oclIsKindOf(Collection(ISliceCriteriaGenerator))
	 */
	private final Collection criteriaGenerators;

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
	 * This manages the tokens used in flow analysis.
	 */
	private final ITokenManager theTokenMgr;

	/** 
	 * This is the information map used to initialized analyses.
	 */
	private final Map info;

	/** 
	 * This provides monitor information.
	 */
	private final MonitorAnalysis monitorInfo;

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
	 * This provides use-def information based on aliasing.
	 */
	private AliasedUseDefInfov2 aliasUD;

	/** 
	 * This is the instance of equivalence class based escape analysis used by this object.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/** 
	 * This provides mapping from init invocation expression to corresponding new expression.
	 */
	private NewExpr2InitMapper initMapper;

	/** 
	 * This provides pair management.
	 */
	private final PairManager pairMgr;

	/** 
	 * The system to be sliced.
	 */
	private IEnvironment system;

	/** 
	 * This provides safe lock information.
	 */
	private final SafeLockAnalysis safelockAnalysis;

	/** 
	 * This defines the scope of slicing.  If this undefined, then the entire reachbable system is the scope.
	 */
	private SpecificationBasedScopeDefinition sliceScopeDefinition;

	/** 
	 * This provides use def information for static fields.
	 */
	private final StaticFieldUseDefInfo staticFieldUD;

	/**
	 * Creates a new SlicerTool object.  The client should relinquish control/ownership of the arguments as they are provided
	 * to configure the tool.
	 *
	 * @param tokenMgr is the token manager to be used with this instance of slicer tool.
	 * @param stmtGraphFactoryToUse is the statement graph factory to use.
	 *
	 * @pre tokenMgr != null and stmtGraphFactoryToUse != null
	 */
	public SlicerTool(final ITokenManager tokenMgr, final IStmtGraphFactory stmtGraphFactoryToUse) {
		theTokenMgr = tokenMgr;
		phase = Phase.createPhase();

		rootMethods = new HashSet();
		criteria = new HashSet();
		info = new HashMap();
		criteriaGenerators = new HashSet();

		// create the flow analysis.
		ofa = OFAnalyzer.getFSOSAnalyzer(FLOW_ANALYSIS_TAG_NAME, tokenMgr);

		stmtGraphFactory = stmtGraphFactoryToUse;

		// create the pre processor for call graph construction.
		cgPreProcessCtrl = new ValueAnalyzerBasedProcessingController();
		cgPreProcessCtrl.setAnalyzer(ofa);
		cgPreProcessCtrl.setProcessingFilter(new TagBasedProcessingFilter(FLOW_ANALYSIS_TAG_NAME));
		cgPreProcessCtrl.setStmtGraphFactory(getStmtGraphFactory());

		// create pair manager
		pairMgr = new Pair.PairManager(false, true);
		// create the call graph.
		callGraph = new CallGraph(pairMgr);

		// create the pre processor for thread graph construction.
		cgBasedPreProcessCtrl = new ValueAnalyzerBasedProcessingController();
		cgBasedPreProcessCtrl.setProcessingFilter(new CGBasedProcessingFilter(callGraph));
		cgBasedPreProcessCtrl.setAnalyzer(ofa);
		cgBasedPreProcessCtrl.setStmtGraphFactory(getStmtGraphFactory());

		// create basic block graph manager
		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setStmtGraphFactory(getStmtGraphFactory());
		// create the thread graph.
		threadGraph = new ThreadGraph(callGraph, new CFGAnalysis(callGraph, bbgMgr), pairMgr);
		// create equivalence class-based escape analysis.
		ecba = new EquivalenceClassBasedEscapeAnalysis(callGraph, bbgMgr);
		// create monitor analysis
		monitorInfo = new MonitorAnalysis();
		// create safe lock analysis
		safelockAnalysis = new SafeLockAnalysis();
		// create alias use def analysis
		aliasUD = new AliasedUseDefInfov2(ofa, callGraph, threadGraph, bbgMgr, pairMgr);
		// create static field use def analysis
		staticFieldUD = new StaticFieldUseDefInfo();

		// set up data required for dependency analyses.
		info.put(ICallGraphInfo.ID, callGraph);
		info.put(IThreadGraphInfo.ID, threadGraph);
		info.put(IEnvironment.ID, ofa.getEnvironment());
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, aliasUD);
		info.put(IUseDefInfo.GLOBAL_USE_DEF_ID, staticFieldUD);
		info.put(PairManager.ID, pairMgr);
		info.put(IValueAnalyzer.ID, ofa);
		info.put(IEscapeInfo.ID, ecba);
		info.put(IMonitorInfo.ID, monitorInfo);
		info.put(SafeLockAnalysis.ID, safelockAnalysis);

		// create dependency analyses controller 
		daController = new AnalysesController(info, cgBasedPreProcessCtrl, bbgMgr);

		// create the slicing engine.
		engine = new SlicingEngine();

		// create the <init> call to new expr mapper
		initMapper = new NewExpr2InitMapper();
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
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
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
		final SlicerConfiguration _config = (SlicerConfiguration) getActiveConfiguration();
		final List _daNames = new ArrayList(_config.getIDsOfDAsToUse());
		Collections.sort(_daNames);

		for (final Iterator _i = _daNames.iterator(); _i.hasNext();) {
			_result.addAll(_config.getDependenceAnalyses(_i.next()));
		}
		return _result;
	}

	/**
	 * Retrieves the value in <code>ecba</code>.
	 *
	 * @return the value in <code>ecba</code>.
	 */
	public EquivalenceClassBasedEscapeAnalysis getECBA() {
		return ecba;
	}

	/**
	 * Retrieves the value in <code>monitorInfo</code>.
	 *
	 * @return the value in <code>monitorInfo</code>.
	 */
	public MonitorAnalysis getMonitorInfo() {
		return monitorInfo;
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
	 * Sets the scope of the slicing.
	 *
	 * @param scope to be used.
	 */
	public void setSliceScopeDefinition(final SpecificationBasedScopeDefinition scope) {
		sliceScopeDefinition = scope;
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
	 * @param theEnvironment contains the class of the system to be sliced.
	 *
	 * @pre theEnvironment != null
	 */
	public void setSystem(final IEnvironment theEnvironment) {
		system = theEnvironment;
	}

	/**
	 * Retrieves the system being sliced.
	 *
	 * @return the system being sliced.
	 *
	 * @post result != null
	 */
	public IEnvironment getSystem() {
		return system;
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
	 * Adds <code>criteriaGenerator</code> to the collection of criteria generator.
	 *
	 * @param theCriteriaGenerator anothre criteria generator.
	 *
	 * @return <code>true</code> if the given generator was added; <code>false</code>, otherwise.
	 *
	 * @pre theCriteriaGenerator != null
	 */
	public boolean addCriteriaGenerator(final ISliceCriteriaGenerator theCriteriaGenerator) {
		return criteriaGenerators.add(theCriteriaGenerator);
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
	public void execute(final Phase phaseParam)
	  throws InterruptedException {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Execution of the slicer tool");
		}

		Phase _ph = phase;

		if (phaseParam != null && phaseParam.isEarlierThan(phase)) {
			phase = (Phase) phaseParam.clone();
			_ph = phase;
		}

		if (_ph.equalsMajor(Phase.STARTING_PHASE)) {
			fireToolProgressEvent("Performing low level analyses", _ph);
			lowLevelAnalysisPhase();
		}

		movingToNextPhase();

		final SlicerConfiguration _slicerConfig = (SlicerConfiguration) getActiveConfiguration();

		if (_ph.equalsMajor((Phase) DEPENDENCE_MAJOR_PHASE)) {
			fireToolProgressEvent("Performing dependence analyses", _ph);
			dependencyAnalysisPhase(_slicerConfig);
		}

		movingToNextPhase();

		if (_ph.equalsMajor((Phase) SLICE_MAJOR_PHASE)) {
			fireToolProgressEvent("Performing slicing", _ph);
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
	 * Removes <code>criteriaGenerator</code> to the collection of criteria generator.
	 *
	 * @param theCriteriaGenerator anothre criteria generator.
	 *
	 * @return <code>true</code> if the given generator was removed; <code>false</code>, otherwise.
	 *
	 * @pre theCriteriaGenerator != null
	 */
	public boolean removeCriteriaGenerator(final ISliceCriteriaGenerator theCriteriaGenerator) {
		return criteriaGenerators.remove(theCriteriaGenerator);
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		aliasUD.reset();
		bbgMgr.reset();
		callGraph.reset();
		cgBasedPreProcessCtrl.reset();
		cgPreProcessCtrl.reset();
		criteria.clear();
		daController.reset();
		ecba.reset();
		engine.reset();
		initMapper.reset();
		monitorInfo.reset();
		ofa.reset();
		pairMgr.reset();
		phase.reset();
		rootMethods.clear();
		safelockAnalysis.reset();
		stmtGraphFactory.reset();
		theTokenMgr.reset();
		threadGraph.reset();
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

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("checkConfiguration() - " + _slicerConf.getConfigName());
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
			if (slicerConfig.isNonTerminationSensitiveControlDependenceUsed()) {
				_controlDAs.add(new NonTerminationSensitiveEntryControlDA());
			} else {
				_controlDAs.add(new NonTerminationInsensitiveEntryControlDA());
			}
		}
		CollectionsUtilities.getSetFromMap(info, IDependencyAnalysis.CONTROL_DA).addAll(_controlDAs);

		// drive the analyses
		for (final Iterator _i = slicerConfig.getIDsOfDAsToUse().iterator(); _i.hasNext();) {
			final Object _id = _i.next();
			final Collection _c = slicerConfig.getDependenceAnalyses(_id);
			daController.addAnalyses(_id, _c);
		}
		daController.addAnalyses(IMonitorInfo.ID, Collections.singleton(monitorInfo));
		daController.addAnalyses(IEscapeInfo.ID, Collections.singleton(ecba));

		if (slicerConfig.isSafeLockAnalysisUsedForReady()) {
			daController.addAnalyses(SafeLockAnalysis.ID, Collections.singleton(safelockAnalysis));
		}
		daController.initialize();
		daController.execute();

		final String _deadlockCriteriaSelectionStrategy = slicerConfig.getDeadlockCriteriaSelectionStrategy();

		if (!slicerConfig.getPropertyAware()
			  && !_deadlockCriteriaSelectionStrategy.equals(SlicerConfiguration.CONTEXT_SENSITIVE_ESCAPING_SYNC_CONSTRUCTS)) {
			ecba.flushSiteContexts();
		}

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

		fireToolProgressEvent("LOW LEVEL ANALYSES: Performing object flow analysis", phase);

		phase.reset();
		// do the flow analyses
		ofa.reset();
		bbgMgr.reset();
		stmtGraphFactory.reset();
		ofa.analyze(system, rootMethods);
		phase.nextMinorPhase();

		movingToNextPhase();

		fireToolProgressEvent("LOW LEVEL ANALYSES: Constructing call graph", phase);

		// process flow information into a more meaningful call graph
		callGraph.reset();
		cgPreProcessCtrl.reset();
		callGraph.hookup(cgPreProcessCtrl);
		cgPreProcessCtrl.process();
		callGraph.unhook(cgPreProcessCtrl);
		phase.nextMinorPhase();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Call Graph:\n" + callGraph);
		}

		movingToNextPhase();

		fireToolProgressEvent("LOW LEVEL ANALYSES: Constructing thread graph and misc.", phase);

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
			LOGGER.debug("Thread Graph:\n" + threadGraph);
		}

		movingToNextPhase();

		fireToolProgressEvent("LOW LEVEL ANALYSES: Calculating intra-procedural use-def information", phase);

		// process alias use-def analyses.
		cgBasedPreProcessCtrl.reset();
		aliasUD.reset();
		aliasUD.hookup(cgBasedPreProcessCtrl);
		staticFieldUD.reset();
		staticFieldUD.hookup(cgBasedPreProcessCtrl);
		cgBasedPreProcessCtrl.process();
		aliasUD.unhook(cgBasedPreProcessCtrl);
		staticFieldUD.unhook(cgBasedPreProcessCtrl);

		phase.nextMajorPhase();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: low level static analyses phase");
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
			final SliceCollector _collector = engine.getCollector();
			final Collection _methods = _collector.getMethodsInSlice();
			final SliceGotoProcessor _gotoProcessor = new SliceGotoProcessor(_collector);

			fireToolProgressEvent("SLICING: Injecting executability into the slice.", phase);

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

		for (final Iterator _i = criteriaGenerators.iterator(); _i.hasNext();) {
			final ISliceCriteriaGenerator _e = (ISliceCriteriaGenerator) _i.next();
			criteria.addAll(_e.getCriteria(this));
		}

		for (final Iterator _j = slicerConfig.getSliceCriteriaGenerators().iterator(); _j.hasNext();) {
			final ISliceCriteriaGenerator _generator = (ISliceCriteriaGenerator) _j.next();
			criteria.addAll(_generator.getCriteria(this));
		}

		if (!criteria.isEmpty()) {
			fireToolProgressEvent("SLICING: Calculating the slice", phase);

			// setup the slicing engine and slice
			engine.setCgi(callGraph);
			engine.setSliceType(slicerConfig.getProperty(SlicerConfiguration.SLICE_TYPE));
			engine.setInitMapper(initMapper);
			engine.setBasicBlockGraphManager(bbgMgr);
			engine.setAnalysesControllerAndDependenciesToUse(daController, slicerConfig.getIDsOfDAsToUse());
			engine.setSliceCriteria(criteria);
			engine.setSliceScopeDefinition(sliceScopeDefinition);
			engine.setSystem(system);

			if (slicerConfig.getPropertyAware()) {
				final Map _map = new HashMap();
				final ThreadEscapeInfoBasedCallingContextRetriever _t1 = new ThreadEscapeInfoBasedCallingContextRetriever();
				_t1.setECBA(getECBA());
				_t1.setCallGraph(getCallGraph());
				_map.put(IDependencyAnalysis.READY_DA, _t1);
				_map.put(IDependencyAnalysis.INTERFERENCE_DA, _t1);
				engine.setDepID2ContextRetrieverMapping(_map);
			}
			engine.initialize();
			engine.slice();
			phase.nextMinorPhase();

			// post process the slice as required
			postProcessSlice();

			phase.nextMajorPhase();
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No slicing criteria were specified. Hence, no slicing was done.\nIf \"slice for deadlock\" was "
					+ "selected then the system did not have any synchronized methods or synchronized blocks.");
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: slicing phase");
		}
	}
}

// End of File
