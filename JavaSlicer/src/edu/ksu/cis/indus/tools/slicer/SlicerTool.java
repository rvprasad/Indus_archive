
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IUnitGraphFactory;
import edu.ksu.cis.indus.common.soot.TrapUnitGraphFactory;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.common.structures.Pair;
import edu.ksu.cis.indus.common.structures.Triple;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.slicer.AbstractSliceCriterion;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.SlicingEngine;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.NewExpr2InitMapper;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import edu.ksu.cis.indus.tools.AbstractTool;
import edu.ksu.cis.indus.tools.CompositeToolConfiguration;
import edu.ksu.cis.indus.tools.CompositeToolConfigurator;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.Phase;

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

import soot.toolkits.graph.UnitGraph;


/**
 * This is a facade that exposes the slicer as a tool.  This is recommended interface to interact with the slicer if the
 * slicer is being used as a tool in a tool chain.
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
	private final IUnitGraphFactory unitGraphProvider;

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
	private AliasedUseDefInfo aliasUD;

	/**
	 * This is the instance of equivalence class based escape analysis used by this object.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * This provides mapping from init invocation expression to corresponding new expression.
	 */
	private NewExpr2InitMapper initMapper;

	/**
	 * Creates a new SlicerTool object.
	 */
	public SlicerTool() {
		phase = Phase.createPhase();

		rootMethods = new HashSet();
		criteria = new HashSet();

		// create the flow analysis.
		final String _tagName = "SlicerTool:FA";
		ofa = OFAnalyzer.getFSOSAnalyzer(_tagName);

		// create the pre processor for call graph construction.
		cgPreProcessCtrl = new ValueAnalyzerBasedProcessingController();
		cgPreProcessCtrl.setAnalyzer(ofa);
		cgPreProcessCtrl.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		// create the call graph.
		callGraph = new CallGraph();

		// create the pre processor for thread graph construction.
		cgBasedPreProcessCtrl = new ValueAnalyzerBasedProcessingController();
		cgBasedPreProcessCtrl.setProcessingFilter(new CGBasedProcessingFilter(callGraph));
		cgBasedPreProcessCtrl.setAnalyzer(ofa);

		unitGraphProvider = new TrapUnitGraphFactory();
		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setUnitGraphProvider(unitGraphProvider);
		// create the thread graph.
		threadGraph = new ThreadGraph(callGraph, new CFGAnalysis(callGraph, bbgMgr));
		// create equivalence class-based escape analysis.
		ecba = new EquivalenceClassBasedEscapeAnalysis(callGraph, threadGraph, bbgMgr);

		// set up data required for dependency analyses.
		final Map _info = new HashMap();
		aliasUD = new AliasedUseDefInfo(ofa);
		_info.put(ICallGraphInfo.ID, callGraph);
		_info.put(IThreadGraphInfo.ID, threadGraph);
		_info.put(IEnvironment.ID, ofa.getEnvironment());
		_info.put(IUseDefInfo.ID, aliasUD);
		_info.put(Pair.PairManager.ID, new Pair.PairManager());
		_info.put(IValueAnalyzer.ID, ofa);
		_info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);

		// create dependency analyses controller 
		daController = new AnalysesController(_info, cgBasedPreProcessCtrl, unitGraphProvider);

		// create the slicing engine.
		engine = new SlicingEngine();

		// create the <init> call to new expr mapper
		initMapper = new NewExpr2InitMapper();

		criteriaFactory = new SliceCriteriaFactory();
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
	 * @post result != null and result.oclIsKindOf(Set(DependencyAnalysis))
	 */
	public Collection getDAs() {
		final Collection _result = new LinkedHashSet();
		final SlicerConfiguration _config = ((SlicerConfiguration) getActiveConfiguration());
		final List _daNames = new ArrayList(_config.getNamesOfDAsToUse());
		Collections.sort(_daNames);

		for (final Iterator _i = _daNames.iterator(); _i.hasNext();) {
			_result.addAll(_config.getDependenceAnalysis(_i.next()));
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
		Phase _ph = phase;

		if (phaseParam != null && ((Phase) phaseParam).isEarlierThan(phase)) {
			phase = (Phase) ((Phase) phaseParam).clone();
			_ph = phase;
		}

		if (_ph.equalsMajor(Phase.STARTING_PHASE)) {
			phase.reset();
			// do the flow analyses
			ofa.reset();
			bbgMgr.reset();
			unitGraphProvider.reset();
			Util.fixupThreadStartBody(system);
			ofa.analyze(system, rootMethods);
			phase.nextMinorPhase();

			movingToNextPhase();

			// process flow information into a more meaningful call graph
			callGraph.reset();
			callGraph.hookup(cgPreProcessCtrl);
			cgPreProcessCtrl.process();
			callGraph.unhook(cgPreProcessCtrl);
			phase.nextMinorPhase();

			movingToNextPhase();

			// process flow information into a more meaningful thread graph. Also, initialize <init> call to new expression 
			// mapper.
			threadGraph.reset();
			initMapper.hookup(cgBasedPreProcessCtrl);
			threadGraph.hookup(cgBasedPreProcessCtrl);
			cgBasedPreProcessCtrl.process();
			threadGraph.unhook(cgBasedPreProcessCtrl);
			initMapper.unhook(cgBasedPreProcessCtrl);
			phase.nextMinorPhase();

			movingToNextPhase();

			// process escape analyses.
			ecba.hookup(cgBasedPreProcessCtrl);
			aliasUD.hookup(cgBasedPreProcessCtrl);
			cgBasedPreProcessCtrl.process();
			aliasUD.unhook(cgBasedPreProcessCtrl);
			ecba.unhook(cgBasedPreProcessCtrl);
			ecba.execute();
			phase.nextMajorPhase();
		}

		movingToNextPhase();

		final SlicerConfiguration _slicerConfig = (SlicerConfiguration) getActiveConfiguration();

		if (_ph.equalsMajor((Phase) DEPENDENCE_MAJOR_PHASE)) {
			// perform dependency analyses
			daController.reset();

			for (final Iterator _i = _slicerConfig.getNamesOfDAsToUse().iterator(); _i.hasNext();) {
				final Object _id = _i.next();
				final Collection _c = _slicerConfig.getDependenceAnalysis(_id);
				daController.setAnalyses(_id, _c);

				for (final Iterator _iter = _c.iterator(); _iter.hasNext();) {
					final DependencyAnalysis _da = (DependencyAnalysis) _iter.next();
					_da.setBasicBlockGraphManager(bbgMgr);
				}
			}
			daController.initialize();
			daController.execute();
			phase.nextMajorPhase();
		}

		movingToNextPhase();

		if (_ph.equalsMajor((Phase) SLICE_MAJOR_PHASE)) {
			// perform slicing
			engine.reset();

			if (_slicerConfig.sliceForDeadlock) {
				populateDeadlockCriteria();
			}

			if (!criteria.isEmpty()) {
				engine.setCgi(callGraph);
				engine.setExecutableSlice(_slicerConfig.executableSlice);
				engine.setSliceType(_slicerConfig.getProperty(SlicerConfiguration.SLICE_TYPE));
				engine.setInitMapper(initMapper);
				engine.setSlicedBBGMgr(bbgMgr);
				engine.setAnalysesControllerAndDependenciesToUse(daController, _slicerConfig.getNamesOfDAsToUse());
				engine.setSliceCriteria(criteria);
				engine.initialize();
				engine.slice();
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(
						"No slicing criteria were specified. Hence, no slicing was done.\nIf \"slice for deadlock\" was "
						+ "selected then the system did not have any synchronized methods are blocks.");
				}
			}
		}
		phase.finished();
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
	 * Returns the call graph used by the slicer.
	 *
	 * @return the call graph used by the slicer.
	 */
	ICallGraphInfo getCallGraph() {
		return callGraph;
	}

	/**
	 * Returns the environment in which the slicer works.
	 *
	 * @return the environment in which the slicer ran or will run.
	 */
	IEnvironment getEnvironment() {
		return ofa.getEnvironment();
	}

	/**
	 * Creates criterion based on synchronization constructs and populates <code>criteria</code>.
	 *
	 * @throws IllegalStateException when none of the Synchronization dependency analyses implement <code>IMonitorInfo</code>
	 * 		   interface.
	 */
	private void populateDeadlockCriteria() {
		final SlicerConfiguration _slicerConfig = (SlicerConfiguration) getActiveConfiguration();
		final Collection _das = _slicerConfig.getDependenceAnalysis(DependencyAnalysis.SYNCHRONIZATION_DA);
		IMonitorInfo _im = null;

		for (final Iterator _i = _das.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (_o instanceof IMonitorInfo) {
				_im = (IMonitorInfo) _o;
				break;
			}
		}

		if (_im == null) {
			throw new IllegalStateException(
				"This implementation requires atleast one Synchronization dependence analysis to "
				+ "implement IMonitorInfo interface.");
		}

		final Collection _temp = new HashSet();

		for (final Iterator _i = _im.getMonitorTriples().iterator(); _i.hasNext();) {
			final Triple _mTriple = (Triple) _i.next();
			final SootMethod _method = (SootMethod) _mTriple.getThird();
			_temp.clear();

			if (_mTriple.getFirst() == null) {
				// add all return points (including throws) of the method as the criteria
				final UnitGraph _graph = unitGraphProvider.getUnitGraph(_method);

				if (_graph == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Could not retrieve the unit graph for " + _method.getSignature() + ".  Moving on.");
					}
				} else {
					for (final Iterator _j = _graph.getTails().iterator(); _j.hasNext();) {
						final Stmt _stmt = (Stmt) _j.next();
						_temp.addAll(criteriaFactory.getCriterion(_method, _stmt));
					}
				}
			} else {
				_temp.addAll(criteriaFactory.getCriterion(_method, (Stmt) _mTriple.getFirst()));
				_temp.addAll(criteriaFactory.getCriterion(_method, (Stmt) _mTriple.getSecond()));
			}

			for (final Iterator _k = _temp.iterator(); _k.hasNext();) {
				final ISliceCriterion _criterion = (ISliceCriterion) _k.next();
				((AbstractSliceCriterion) _criterion).setConsiderExecution(true);
			}
			criteria.addAll(_temp);
		}
	}
}

/*
   ChangeLog:
   $Log$
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
