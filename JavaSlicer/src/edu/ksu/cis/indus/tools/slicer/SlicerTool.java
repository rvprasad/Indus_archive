
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

import soot.Scene;
import soot.SootMethod;

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.indus.common.CompleteUnitGraphFactory;
import edu.ksu.cis.indus.common.TrapUnitGraphFactory;
import edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory;
import edu.ksu.cis.indus.slicer.ISlicingBasedTransformer;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.SlicingEngine;
import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Triple;
import edu.ksu.cis.indus.staticanalyses.support.Util;
import edu.ksu.cis.indus.tools.AbstractTool;
import edu.ksu.cis.indus.tools.AbstractToolConfiguration;
import edu.ksu.cis.indus.tools.CompositeToolConfiguration;
import edu.ksu.cis.indus.tools.CompositeToolConfigurator;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.transformations.slicer.TagBasedSlicingTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


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
		Phase i = Phase.createPhase();
		i.nextMajorPhase();
		DEPENDENCE_MAJOR_PHASE = (Phase) i.clone();
		i.nextMajorPhase();
		SLICE_MAJOR_PHASE = (Phase) i.clone();
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
	 * This provides <code>UnitGraph</code>s for the analyses.
	 */
	private final AbstractUnitGraphFactory unitGraphProvider;

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
	 * This provides object flow anlaysis.
	 */
	private final OFAnalyzer ofa;

	/**
	 * The phase in which the tool's execution is in.
	 */
	private final Phase phase;

	/**
	 * This is a call-graph based pre processing controller.
	 */
	private final ProcessingController cgBasedPreProcessCtrl;

	/**
	 * This controls the processing of callgraph.
	 */
	private final ProcessingController cgPreProcessCtrl;

	/**
	 * This is the slicing engine that identifies the slice.
	 */
	private final SlicingEngine engine;

	/**
	 * This provides thread graph.
	 */
	private final ThreadGraph threadGraph;

	/**
	 * This is the slice transformer.
	 */
	private ISlicingBasedTransformer transformer;

	/**
	 * The system to be sliced.
	 */
	private Scene system;

	/**
	 * This is the slice criterion factory that will be used.
	 */
	private final SliceCriteriaFactory criteriaFactory;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Creates a new SlicerTool object.
	 */
	public SlicerTool() {
		phase = Phase.createPhase();

		rootMethods = new HashSet();
		criteria = new HashSet();

		// create the flow analysis.
		ofa = OFAnalyzer.getFSOSAnalyzer();

		// create the pre processor for call graph construction.
		cgPreProcessCtrl = new ProcessingController();
		cgPreProcessCtrl.setAnalyzer(ofa);

		// create the call graph.
		callGraph = new CallGraph();

		// create the pre processor for thread graph construction.
		cgBasedPreProcessCtrl = new CGBasedProcessingController(callGraph);
		cgBasedPreProcessCtrl.setAnalyzer(ofa);

		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setUnitGraphProvider(new TrapUnitGraphFactory());
		// create the thread graph.
		threadGraph = new ThreadGraph(callGraph, new CFGAnalysis(callGraph, bbgMgr));
		// create equivalence class-based escape analysis.
		ecba = new EquivalenceClassBasedEscapeAnalysis(callGraph, threadGraph, bbgMgr);

		// set up data required for dependency analyses.
		Map info = new HashMap();
		info.put(ICallGraphInfo.ID, callGraph);
		info.put(IThreadGraphInfo.ID, threadGraph);
		info.put(IEnvironment.ID, ofa.getEnvironment());
		info.put(IUseDefInfo.ID, new AliasedUseDefInfo(ofa));
		info.put(Pair.PairManager.ID, new Pair.PairManager());
		info.put(IValueAnalyzer.ID, ofa);
		info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		unitGraphProvider = new CompleteUnitGraphFactory();

		// create dependency analyses controller 
		daController = new AnalysesController(info, cgBasedPreProcessCtrl, unitGraphProvider);

		// create the slicing engine.
		engine = new SlicingEngine();

		// create the slicing transformer.
		transformer = new TagBasedSlicingTransformer();

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
	 * Set the transformer to be used during slicing.
	 *
	 * @param theTransformer is the transformer driven by slicing.
	 *
	 * @pre theTransformer != null
	 */
	public void setTransformer(final ISlicingBasedTransformer theTransformer) {
		transformer = theTransformer;
	}

	/**
	 * Retrieves the transformer.
	 *
	 * @return the transformer driven by slicing.
	 *
	 * @post result != null
	 */
	public ISlicingBasedTransformer getTransformer() {
		return this.transformer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void destringizeConfiguration(final String stringizedForm) {
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(CompositeToolConfiguration.class);
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			configurationInfo = (AbstractToolConfiguration) uctx.unmarshalDocument(new StringReader(stringizedForm), null);
			configurator =
				new CompositeToolConfigurator((CompositeToolConfiguration) configurationInfo, new SlicerConfigurator(),
					SlicerConfiguration.getFactory());
		} catch (JiBXException e) {
			LOGGER.error("Error while unmarshalling Slicer configurationCollection.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute(final Object phaseParam)
	  throws InterruptedException {
		Phase ph = (Phase) phaseParam;

		if (ph.equalsMajor(Phase.STARTING_PHASE)) {
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

			// process flow information into a more meaningful thread graph
			threadGraph.reset();
			threadGraph.hookup(cgBasedPreProcessCtrl);
			cgBasedPreProcessCtrl.process();
			threadGraph.unhook(cgBasedPreProcessCtrl);
			phase.nextMinorPhase();

			movingToNextPhase();

			// process escape analyses.
			ecba.hookup(cgBasedPreProcessCtrl);
			cgBasedPreProcessCtrl.process();
			ecba.unhook(cgBasedPreProcessCtrl);
			phase.nextMajorPhase();
			ph = phase;
		}

		movingToNextPhase();

		SlicerConfiguration slicerConfig = (SlicerConfiguration) getActiveConfiguration();

		if (ph.equalsMajor((Phase) DEPENDENCE_MAJOR_PHASE)) {
			// perform dependency analyses
			daController.reset();

			for (Iterator i = slicerConfig.getNamesOfDAsToUse().iterator(); i.hasNext();) {
				Object id = i.next();
				Collection c = slicerConfig.getDependenceAnalysis(id);
				daController.setAnalyses(id, c);

				for (Iterator iter = c.iterator(); iter.hasNext();) {
					DependencyAnalysis da = (DependencyAnalysis) iter.next();
					da.setBasicBlockGraphManager(bbgMgr);
				}
			}
			daController.initialize();
			daController.execute();
			phase.nextMajorPhase();
			ph = phase;
		}

		movingToNextPhase();

		if (ph.equalsMajor((Phase) SLICE_MAJOR_PHASE)) {
			// perform slicing
			transformer.reset();
			engine.reset();

			if (slicerConfig.sliceForDeadlock) {
				populateDeadlockCriteria();
			}
			transformer.initialize(system);
			engine.initialize(slicerConfig.getProperty(SlicerConfiguration.SLICE_TYPE), slicerConfig.executableSlice,
				daController, callGraph, transformer, slicerConfig.getNamesOfDAsToUse(), bbgMgr);
			engine.setSliceCriteria(criteria);
			engine.slice();
		}
		phase.finished();
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractTool#initialize()
	 */
	public void initialize() {
		SlicerConfiguration config = new SlicerConfiguration();
		((CompositeToolConfiguration) configurationInfo).addToolConfiguration(config);
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
		StringWriter result = new StringWriter();

		try {
			IBindingFactory bfact = BindingDirectory.getFactory(CompositeToolConfiguration.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(4);
			mctx.marshalDocument(configurationInfo, "UTF-8", null, result);
		} catch (JiBXException e) {
			LOGGER.error("Error while marshalling Slicer configurationCollection.");
			throw new RuntimeException(e);
		}
		return result.toString();
	}

	/**
	 * Creates criterion based on synchronization constructs and populates <code>criteria</code>.
	 *
	 * @throws IllegalStateException DOCUMENT ME!
	 */
	private void populateDeadlockCriteria() {
		SlicerConfiguration slicerConfig = (SlicerConfiguration) getActiveConfiguration();
		Collection das = slicerConfig.getDependenceAnalysis(DependencyAnalysis.SYNCHRONIZATION_DA);
		IMonitorInfo im = null;

		for (Iterator i = das.iterator(); i.hasNext();) {
			Object o = i.next();

			if (o instanceof IMonitorInfo) {
				im = (IMonitorInfo) o;
				break;
			}
		}

		if (im == null) {
			throw new IllegalStateException(
				"This implementation requires atleast one Synchronization dependence analysis to "
				+ "implement IMonitorInfo interface.");
		}

		for (Iterator i = im.getMonitorTriples().iterator(); i.hasNext();) {
			Triple mTriple = (Triple) i.next();
			SootMethod method = (SootMethod) mTriple.getThird();

			if (mTriple.getFirst() == null) {
				// add all return points (including throws) of the method as the criteria
				UnitGraph graph = unitGraphProvider.getUnitGraph(method);

				if (graph == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Could not retrieve the unit graph for " + method.getSignature() + ".  Moving on.");
					}
				} else {
					for (Iterator j = graph.getTails().iterator(); j.hasNext();) {
						Stmt stmt = (Stmt) j.next();
						criteria.addAll(criteriaFactory.getCriterion(method, stmt, true, true));
					}
				}
			} else {
				criteria.addAll(criteriaFactory.getCriterion(method, (Stmt) mTriple.getFirst(), true, true));
				criteria.addAll(criteriaFactory.getCriterion(method, (Stmt) mTriple.getSecond(), true, true));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
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
