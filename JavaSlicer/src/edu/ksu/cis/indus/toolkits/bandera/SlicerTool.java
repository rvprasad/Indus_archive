
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

package edu.ksu.cis.indus.toolkits.bandera;

import soot.Scene;
import soot.SootMethod;

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;
import edu.ksu.cis.bandera.util.BaseObservable;
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
import edu.ksu.cis.indus.transformations.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.transformations.slicer.SlicingEngine;
import edu.ksu.cis.indus.transformations.slicer.TagBasedSlicingTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class wraps the slicer in the tool interface required by the tool pipeline in Bandera.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerTool
  extends BaseObservable
  implements Tool {
	/**
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "The Scene to be transformed.";

	/**
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "The Entry-point methods.";

	/**
	 * This identifies the slicing criteria in the input arguments.
	 */
	public static final Object CRITERIA = "The Slicing criteria.";

	/**
	 * This identifies the slicing tag name in the input arguments.
	 */
	public static final String TAG_NAME = "The name of the slice tag.";

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
		IN_ARGUMENTS_IDS.add(CRITERIA);
		IN_ARGUMENTS_IDS.add(TAG_NAME);
		OUT_ARGUMENTS_IDS = new ArrayList();
		OUT_ARGUMENTS_IDS.add(SCENE);
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerTool.class);

	/**
	 * This controls dependency analysis.
	 */
	private AnalysesController daController;

	/**
	 * This manages the basic block graphs for the methods being transformed.
	 */
	private final BasicBlockGraphMgr bbgMgr;

	/**
	 * This provides the call graph.
	 */
	private final CallGraph callGraph;

	/**
	 * The slicing criteria.
	 *
	 * @invariant criteria.oclIsKindOf(Collection(AbstractSliceCriterion))
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
	 * This controls the processing of callgraph.
	 */
	private final ProcessingController cgPreProcessCtrl;

	/**
	 * This provides thread graph.
	 */
	private final ThreadGraph threadGraph;

	/**
	 * The scene to be sliced.
	 */
	private Scene theScene;

	/**
	 * This is the slicing engine that identifies the slice.
	 */
	private final SlicingEngine engine;

	/**
	 * The tag name to be used to identify the slicing tags.
	 */
	private String tagName;

	/**
	 * This is the tagging style slice transformer.
	 */
	private final TagBasedSlicingTransformer transformer;

	/**
	 * This is a call-graph based pre processing controller.
	 */
	private ProcessingController cgBasedPreProcessCtrl;

	/**
	 * The configuration view that can be used to configure the slicer.
	 */
	private final SlicerConfigurationView configuration;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private SliceCriteriaFactory criteriaFactory = new SliceCriteriaFactory();

	/**
	 * Creates a new SlicerTool object.
	 */
	public SlicerTool() {
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

		// set up data required for dependency analysis.
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

		// create the configuration object that maintains the configuration info.
		configuration = new SlicerConfigurationView();
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String configStr)
	  throws Exception {
		configuration.set(configStr);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() {
		return configuration.toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param inputArgs maps the input argument identifiers to the arguments.
	 *
	 * @pre inputArgs.get(SCENE) != null and inputArgs.get(SCENE).oclIsKindOf(Scene)
	 * @pre inputArgs.get(CRITERIA) != null and
	 * 		inputArgs.get(CRITERIA).oclIsKindOf(Collection(edu.ksu.cis.indus.transformations.slicer.AbstractSliceCriterion))
	 * @pre inputArgs.get(TAG_NAME) != null and inputArgs.get(TAG_NAME).oclIsKindOf(String)
	 * @pre inputArgs.get(ROOT_METHODS) != null and inputArgs.get(ROOT_METHODS).oclIsKindOf(Collection(SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map inputArgs) {
		theScene = (Scene) inputArgs.get(SCENE);

		if (theScene == null) {
			LOGGER.error("A scene must be provided for slicing.");
			throw new IllegalArgumentException("A scene must be provided for slicing.");
		}

		Collection temp = (Collection) inputArgs.get(CRITERIA);
		criteria.clear();

		if (temp == null) {
			LOGGER.error("Atlease one slicing criteria should be specified.");
			throw new IllegalArgumentException("Atlease one slicing criteria should be specified.");
		} else if (temp.isEmpty()) {
			LOGGER.warn("Deadlock criteria will be used.");
		} else {
			criteria.addAll(temp);
		}

		for (Iterator i = criteria.iterator(); i.hasNext();) {
			Object o = i.next();

			if (!SliceCriteriaFactory.isSlicingCriterion(o)) {
				LOGGER.error(o
					+ " is an invalid slicing criterion.  All slicing criterion should be created via SliceCriteriaFactory.");
				throw new IllegalArgumentException("Slicing criteion " + o + " was not created by SliceCriteriaFactory.");
			}
		}

		tagName = (String) inputArgs.get(TAG_NAME);
		temp = (Collection) inputArgs.get(ROOT_METHODS);

		if (temp == null || temp.isEmpty()) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Atleast one method should be specified as the entry-point into the system.");
			}
		}
		rootMethods.clear();
		rootMethods.addAll(temp);
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
		Map outputMap = new HashMap();
		outputMap.put(SCENE, theScene);
		return outputMap;
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
		return configuration;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return configuration;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	public void quit()
	  throws Exception {
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run() {
		// do the flow analyses
		bbgMgr.reset();
		ofa.reset();
		ofa.analyze(theScene, rootMethods);

		// process flow information into a more meaningful call graph
		callGraph.reset();
		callGraph.hookup(cgPreProcessCtrl);
		cgPreProcessCtrl.process();
		callGraph.unhook(cgPreProcessCtrl);

		// process flow information into a more meaningful thread graph
		threadGraph.reset();
		threadGraph.hookup(cgBasedPreProcessCtrl);
		cgBasedPreProcessCtrl.process();
		threadGraph.unhook(cgBasedPreProcessCtrl);

		// perform dependency analyses
		daController.reset();

		for (Iterator i = configuration.dependencesToUse.iterator(); i.hasNext();) {
			Object id = i.next();
			daController.setAnalysis(id, (DependencyAnalysis) configuration.id2dependencyAnalysis.get(id));
		}
		daController.initialize(callGraph.getReachableMethods());
		daController.execute();

		// perform slicing
		transformer.reset();
		engine.reset();
		transformer.initialize(theScene, tagName);

		if (criteria.isEmpty()) {
			populateDeadlockCriteria();
		}
		engine.setSliceCriteria(criteria, daController, callGraph, transformer, configuration.dependencesToUse);
		engine.slice(SlicingEngine.BACKWARD_SLICE);
	}

	/**
	 * Creates criterion based on synchronization constructs and populates <code>criteria</code>.
	 */
	private void populateDeadlockCriteria() {
		IMonitorInfo im = (IMonitorInfo) configuration.id2dependencyAnalysis.get(DependencyAnalysis.SYNCHRONIZATION_DA);

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
   Revision 1.1  2003/09/15 08:55:23  venku
   - Well, the SlicerTool is still a mess in my opinion as it needs
     to be implemented as required by Bandera.  It needs to be
     much richer than it is to drive the slicer.
   - SlicerConfigurationView is supposed to bridge the above gap.
     I doubt it.

 */
