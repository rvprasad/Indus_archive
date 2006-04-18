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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;
import edu.ksu.cis.indus.common.soot.MetricsProcessor.MetricKeys;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;

/**
 * This tool calculates metrics of the given system.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class MetricsTool
		extends BaseObservable
		implements Tool {

	/**
	 * This identifies the scene in the input arguments.
	 */
	public static final String SCENE = "scene";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricsTool.class);

	/**
	 * The map containing the statistics that is provided as the output.
	 */
	private Map outputMap;

	/**
	 * The scene being processed.
	 */
	private Scene scene;

	/**
	 * <i>Does not do anything.</i>
	 * 
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() throws Exception {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List<String> getInputParameterList() {
		return Collections.singletonList(SCENE);
	}

	/**
	 * {@inheritDoc} The map will contain statistics for classes belonging to various categories. Currently, application and
	 * library classes are supported. The statistics for each category is itself provided as a map. Please refer to {@link
	 * edu.ksu.cis.indus.common.soot.MetricsProcessor#getStatistics() MetricsProcessor.getStatistics()} for details of the
	 * category statistics.
	 */
	public Map getOutputMap() {
		return outputMap;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List<String> getOutputParameterList() {
		final List<String> _result = new ArrayList<String>();
		_result.add(MetricKeys.APPLICATION_STATISTICS.toString());
		_result.add(MetricKeys.LIBRARY_STATISTICS.toString());
		return _result;
	}

	/**
	 * <i>Does not do anything.</i>
	 * 
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolConfigurationView()
	 */
	public ToolConfigurationView getToolConfigurationView() {
		return null;
	}

	/**
	 * <i>Does not do anything.</i>
	 * 
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return null;
	}

	/**
	 * <i>Does not do anything.</i>
	 * 
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	@Empty public void quit() throws Exception {
		// does nothing
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run() throws Exception {
		final ProcessingController _pc = new ProcessingController();
		final MetricsProcessor _mp = new MetricsProcessor();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		final IStmtGraphFactory _sgf = new CompleteStmtGraphFactory();
		_ssr.setStmtGraphFactory(_sgf);
		_pc.setStmtSequencesRetriever(_ssr);
		_mp.hookup(_pc);
		_pc.setEnvironment(new Environment(scene));
		_pc.process();
		_mp.unhook(_pc);
		outputMap = _mp.getStatistics();
	}

	/**
	 * <i>Does not do anything.</i>
	 * 
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(@SuppressWarnings("unused") final String arg) throws Exception {
		// does nothing
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param inputArgs maps the input argument identifiers to the arguments.
	 * @pre inputArgs.get(SCENE) != null and inputArgs.get(SCENE).oclIsKindOf(Scene)
	 */
	public void setInputMap(final Map inputArgs) {
		scene = (Scene) inputArgs.get(SCENE);

		if (scene == null) {
			LOGGER.error("A scene must be provided for slicing.");
			throw new IllegalArgumentException("A scene must be provided for slicing.");
		}
	}
}

// End of File
