
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
import edu.ksu.cis.indus.tools.Tool;
import edu.ksu.cis.indus.tools.ToolConfiguration;
import edu.ksu.cis.indus.tools.ToolConfigurator;
import edu.ksu.cis.indus.transformations.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.transformations.slicer.SlicingEngine;
import edu.ksu.cis.indus.transformations.slicer.TagBasedSlicingTransformer;

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
  implements Tool {
	/** 
	 * The configuration of this slicer.
	 */
	SlicerConfiguration configuration;

	/** 
	 * The configurator to configure this slicer. 
	 */
	SlicerConfigurator configurator;

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
		configuration = new SlicerConfiguration();
		configurator = new SlicerConfigurator(configuration);

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
	}

	/**
	 * Provides the configuration object used to configure the slicer.
	 *
	 * @return the configuration object.
	 *
	 * @see edu.ksu.cis.indus.tools.Tool#getConfiguration()
	 */
	public ToolConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Retrieves an GUI-based configurator via which the user can configure the tool.
	 *
	 * @see edu.ksu.cis.indus.tools.Tool#getConfigurationEditor()
	 */
	public ToolConfigurator getConfigurationEditor() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void execute() {
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
 */
