
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

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Triple;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.Tool;
import edu.ksu.cis.indus.tools.ToolConfiguration;
import edu.ksu.cis.indus.tools.ToolConfigurationCollection;
import edu.ksu.cis.indus.transformations.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.transformations.slicer.SlicingEngine;
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
public class SlicerTool
  extends Tool {
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
	 * This controls dependency analysis.
	 *
	 * @invariant daController != null
	 */
	private AnalysesController daController;

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
	 * This controls the processing of callgraph.
	 */
	private final ProcessingController cgPreProcessCtrl;

	/**
	 * This provides thread graph.
	 */
	private final ThreadGraph threadGraph;

	/**
	 * This is a call-graph based pre processing controller.
	 */
	private ProcessingController cgBasedPreProcessCtrl;

	/**
	 * The scene to be sliced.
	 */
	private Scene theScene;

	/**
	 * This is the slicing engine that identifies the slice.
	 */
	private final SlicingEngine engine;

	/**
	 * This is the slice criterion factory that will be used.
	 */
	private SliceCriteriaFactory criteriaFactory = new SliceCriteriaFactory();

	/**
	 * The tag name to be used to identify the slicing tags.
	 */
	private String tagName;

	/**
	 * This is the tagging style slice transformer.
	 */
	private final TagBasedSlicingTransformer transformer;

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

		bbgMgr = new BasicBlockGraphMgr();
		// create the thread graph.
		threadGraph = new ThreadGraph(callGraph, new CFGAnalysis(callGraph, bbgMgr));

		// set up data required for dependency analyses.
		Map info = new HashMap();
		info.put(ICallGraphInfo.ID, callGraph);
		info.put(IThreadGraphInfo.ID, threadGraph);
		info.put(IEnvironment.ID, ofa.getEnvironment());
		info.put(IUseDefInfo.ID, new AliasedUseDefInfo(ofa));
		info.put(Pair.PairManager.ID, new Pair.PairManager());

		// create dependency analyses controller 
		daController = new AnalysesController(info, cgBasedPreProcessCtrl);

		// create the slicing objects.
		transformer = new TagBasedSlicingTransformer();
		engine = new SlicingEngine();
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
	 * {@inheritDoc}
	 */
	public void destringizeConfiguration(final String stringizedForm) {
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(this.getClass());
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			configurationInfo = (ToolConfiguration) uctx.unmarshalDocument(new StringReader(stringizedForm), null);

			if (configurator != null) {
				configurator.hide();
				configurator.dispose();
			}
			configurator = new SlicerConfigurator((SlicerConfigurationCollection) configurationInfo);
		} catch (JiBXException e) {
			LOGGER.error("Error while unmarshalling Slicer configurationCollection.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void execute(final Object phaseParam)
	  throws InterruptedException {
		Phase ph = (Phase) phaseParam;

		if (ph.equalsMajor(Phase.STARTING_PHASE)) {
			// do the flow analyses
			bbgMgr.reset();
			ofa.reset();
			ofa.analyze(theScene, rootMethods);
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
			phase.nextMajorPhase();
		}

		movingToNextPhase();

		SlicerConfiguration slicerConfig = (SlicerConfiguration) getActiveConfiguration();

		if (ph.equalsMajor((Phase) DEPENDENCE_MAJOR_PHASE)) {
			// perform dependency analyses
			daController.reset();

			for (Iterator i = slicerConfig.getNamesOfDAsToUse().iterator(); i.hasNext();) {
				Object id = i.next();
				daController.setAnalysis(id, slicerConfig.getDependenceAnalysis(id));
			}
			daController.initialize(callGraph.getReachableMethods());
			daController.execute();
			phase.nextMajorPhase();
		}

		movingToNextPhase();

		if (ph.equalsMajor((Phase) SLICE_MAJOR_PHASE)) {
			// perform slicing
			transformer.reset();
			engine.reset();
			transformer.initialize(theScene, tagName);

			if (slicerConfig.sliceForDeadlock) {
				populateDeadlockCriteria();
			}
			engine.setSliceCriteria(criteria, daController, callGraph, transformer, slicerConfig.getNamesOfDAsToUse());
			engine.slice(slicerConfig.getProperty(SlicerConfiguration.SLICE_TYPE));
		}
		phase.finished();
	}

	/**
	 * @see edu.ksu.cis.indus.tools.Tool#initialize()
	 */
	public void initialize() {
		SlicerConfiguration config = new SlicerConfiguration();
		config.initialize();
		((ToolConfigurationCollection) configurationInfo).addToolConfiguration(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public String stringizeConfiguration() {
		StringWriter result = new StringWriter();

		try {
			IBindingFactory bfact = BindingDirectory.getFactory(this.getClass());
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(4);
			mctx.marshalDocument(this, "UTF-8", null, result);
		} catch (JiBXException e) {
			LOGGER.error("Error while marshalling Slicer configurationCollection.");
			throw new RuntimeException(e);
		}
		return result.toString();
	}

	/**
	 * Creates criterion based on synchronization constructs and populates <code>criteria</code>.
	 */
	private void populateDeadlockCriteria() {
		SlicerConfiguration slicerConfig = (SlicerConfiguration) getActiveConfiguration();
		IMonitorInfo im = (IMonitorInfo) slicerConfig.getDependenceAnalysis(DependencyAnalysis.SYNCHRONIZATION_DA);

		for (Iterator i = im.getMonitorTriples().iterator(); i.hasNext();) {
			Triple mTriple = (Triple) i.next();
			SootMethod method = (SootMethod) mTriple.getThird();

			if (mTriple.getFirst() == null) {
				// add all return points (including throws) of the method as the criteria
				UnitGraph graph = daController.getStmtGraph(method);

				for (Iterator j = graph.getTails().iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();
					criteria.add(criteriaFactory.getCriterion(method, stmt, true, true));
				}
			} else {
				criteria.add(criteriaFactory.getCriterion(method, (Stmt) mTriple.getFirst(), true, true));
				criteria.add(criteriaFactory.getCriterion(method, (Stmt) mTriple.getSecond(), true, true));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/09/26 05:55:28  venku
   - a checkpoint commit. Also a cvs fix commit.
   Revision 1.1  2003/09/24 07:32:23  venku
   - Created an implementation of indus tool api specific to Slicer.
     The GUI needs to be setup and bandera adapter needs to be fixed.
 */
