/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import edu.ksu.cis.bandera.util.BaseObservable;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.IConcurrentIndependenceInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.independence.IndependentStmtDetector;
import edu.ksu.cis.indus.staticanalyses.concurrency.independence.IndependentStmtDetectorv2;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Scene;

import soot.util.Chain;

/**
 * This class provides a Tool for Bandera that provides global independence information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author Todd Wallentine tcw AT cis ksu edu
 * @version $Revision$ - $Date$
 */
public final class ConcurrentIndependenceTool
		extends BaseObservable
		implements Tool {

	/**
	 * This key denotes the basic block graph manager.
	 */
	public static final String BASIC_BLOCK_GRAPH_MGR_INPUT_KEY = "basicBlockGraphMgr";

	/**
	 * This key denotes the call graph in the given Scene from the given entry points.
	 */
	public static final String CALL_GRAPH_INPUT_KEY = "callGraph";

	/**
	 * This key denotes the independence info.
	 */
	public static final String INDEPENDENCEE_OUTPUT_KEY = "concurrentIndependenceInfo";

	/**
	 * This key denotes the Scene.
	 */
	public static final String SCENE_INPUT_KEY = "scene";

	/**
	 * The list of input parameters.
	 */
	private static List<String> inputParameterList;

	/**
	 * The list of output parameters.
	 */
	private static List<String> outputParameterList;

	static {
		initInputParameters();
		initOutputParameters();
	}

	/**
	 * The only active part in this tool (analyses controller).
	 */
	private IActivePart activePart;

	/**
	 * This provides basic block graph manager.
	 */
	private BasicBlockGraphMgr basicBlockGraphMgr;

	/**
	 * The call graph.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * The independence info.
	 */
	private IConcurrentIndependenceInfo independenceInfo;

	/**
	 * The collection of names of independent classes.
	 */
	private Collection<String> independentClassNames;

	/**
	 * The Scene that holds the application to be analyzed.
	 */
	private Scene scene;

	/**
	 * Initialize the List of input parameters.
	 */
	private static void initInputParameters() {
		inputParameterList = new ArrayList<String>(3);
		inputParameterList.add(SCENE_INPUT_KEY);
		inputParameterList.add(CALL_GRAPH_INPUT_KEY);
		inputParameterList.add(BASIC_BLOCK_GRAPH_MGR_INPUT_KEY);
	}

	/**
	 * Initialize the List of output parameters.
	 */
	private static void initOutputParameters() {
		outputParameterList = new ArrayList<String>(1);
		outputParameterList.add(INDEPENDENCEE_OUTPUT_KEY);
	}

	/**
	 * Get the configuration String for this tool.
	 *
	 * @return the configuration string.
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() {
		if (independentClassNames == null) {
			return "";
		}

		final StringBuffer _sb = new StringBuffer();
		final Iterator<String> _i = independentClassNames.iterator();
		final int _iEnd = independentClassNames.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final String _s = _i.next();
			_sb.append(_s);

			if (_iIndex != _iEnd - 1) {
				_sb.append(',');
			}
		}
		return _sb.toString();
	}

	/**
	 * Get the list of input parameters for this Tool.
	 *
	 * @return A List of input parameter names for this Tool. *
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List<String> getInputParameterList() {
		return inputParameterList;
	}

	/**
	 * Get the output Map .
	 *
	 * @return The Map of output generated by this Tool.
	 * @post result.get(ATOMICITY_OUTPUT_KEY).oclIsKindOf(IConcurrentIndependenceInfo)
	 * @post result.get(ATOMICITY_OUTPUT_KEY) != null
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputMap()
	 */
	public Map getOutputMap() {
		final Map _m = new HashMap(1);
		_m.put(INDEPENDENCEE_OUTPUT_KEY, independenceInfo);
		return _m;
	}

	/**
	 * Get the list of output parameters for this Tool.
	 *
	 * @return A List of output parameter names for this Tool.
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List<String> getOutputParameterList() {
		return outputParameterList;
	}

	/**
	 * Always return null since there is no ToolConfigurationView at this time.
	 *
	 * @return Always returns null.
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolConfigurationView()
	 */
	public ToolConfigurationView getToolConfigurationView() {
		return null;
	}

	/**
	 * Always return null since there is no ToolIconView at this time.
	 *
	 * @return Always returns null.
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return null;
	}

	/**
	 * Quit the generation of the Set of reachable SootMethods. At this point, we cannot quit and just have to wait for it to
	 * complete.
	 *
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	public void quit() {
		activePart.deactivate();
	}

	/**
	 * Run the call graph generation to create a Set of SootMethod objects that are reachable.
	 *
	 * @exception IllegalStateException when the preconditions are not satisfied (this is usually from not calling with
	 *                setInputMap with proper inputs) or independence info could not be calculated.
	 * @pre scene != null && scene is not empty
	 * @pre entryPoints != null && entryPoints.size() >= 1
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run() throws IllegalStateException {
		final ProcessingController _pc = new ProcessingController();
		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(callgraph, null,
				basicBlockGraphMgr);
		final Map _info = new HashMap();
		final AnalysesController _ac = new AnalysesController(_info, _pc, basicBlockGraphMgr);
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setBbgFactory(basicBlockGraphMgr);
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setProcessingFilter(new CGBasedProcessingFilter(callgraph));
		_pc.setEnvironment(new Environment(scene));
		_info.put(ICallGraphInfo.ID, callgraph);
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(_ecba));
		_ac.initialize();
		_ac.execute();
		activePart = _ac.getActivePart();

		final IndependentStmtDetector _independent = new IndependentStmtDetectorv2();

		if (independentClassNames != null) {
			_independent.setAtomicClassNames(independentClassNames);
		}

		_independent.setEscapeAnalysis(_ecba.getEscapeInfo());
		_independent.hookup(_pc);
		_pc.process();
		_independent.unhook(_pc);

		independenceInfo = _independent;
	}

	/**
	 * Accepts a comma-separated list of FQN of independent classes.
	 *
	 * @param configurationString the list of FQN of independent classes. If <code>null</code> then the default set
	 *            mentioned in <code>edu.ksu.cis.indus.staticanalyses.independence.AtomicStmtDetector</code>.
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String configurationString) {
		if (configurationString != null) {
			final String[] _a = configurationString.split(",");
			independentClassNames = new ArrayList<String>();

			for (int _i = _a.length - 1; _i >= 0; _i--) {
				independentClassNames.add(_a[_i].trim());
			}
		}
	}

	/**
	 * Set the Map of input to use in the next run of this Tool. This will include a Scene (SCENE_INPUT_KEY) and a Set of
	 * SootMethod objects that represent the entry points (ENTRY_POINTS_INPUT_KEY).
	 *
	 * @param inputMap The Map of input values to use in the next run of this tool.
	 * @throws Exception <i>to satisfy interface specification</i>
	 * @throws IllegalArgumentException when the input map does not contain information in the required format.
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map inputMap) throws Exception {
		if (inputMap == null) {
			throw new IllegalArgumentException("The input Map cannot be null.");
		}

		final int _inputCount = inputMap.size();

		if (_inputCount < 3) {
			throw new IllegalArgumentException("The input Map must have at least three values.");
		}

		final Object _sceneObject = inputMap.get(SCENE_INPUT_KEY);

		if (_sceneObject == null) {
			throw new IllegalArgumentException("A scene is required.");
		}

		if (!(_sceneObject instanceof Scene)) {
			throw new IllegalArgumentException("A scene of type soot.Scene is required.");
		}

		final Scene _tempScene = (Scene) _sceneObject;
		final Chain _c = _tempScene.getClasses();

		if (_c == null) {
			throw new IllegalArgumentException("Cannot use an empty scene.");
		}

		if (_c.size() < 1) {
			throw new IllegalArgumentException("Cannot use an empty scene.");
		}

		final Object _bbgMgr = inputMap.get(BASIC_BLOCK_GRAPH_MGR_INPUT_KEY);

		if (_bbgMgr == null) {
			throw new IllegalArgumentException("Basic block graph manager is required.");
		}

		if (!(_bbgMgr instanceof BasicBlockGraphMgr)) {
			throw new IllegalArgumentException("A basic block graph manager of type BasicBlockGraphMgr is required.");
		}

		final Object _cgi = inputMap.get(CALL_GRAPH_INPUT_KEY);

		if (_cgi == null) {
			throw new IllegalArgumentException("call graph info is required.");
		}

		if (!(_cgi instanceof ICallGraphInfo)) {
			throw new IllegalArgumentException("A call graph info of type ICallGraphInfo is required.");
		}

		scene = _tempScene;
		basicBlockGraphMgr = (BasicBlockGraphMgr) _bbgMgr;
		callgraph = (ICallGraphInfo) _cgi;
	}
}

// End of File
