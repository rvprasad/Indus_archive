
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

package edu.ksu.cis.indus.tools.bandera;

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import edu.ksu.cis.bandera.util.BaseObservable;

import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;

import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public static final Object SCENE = "scene";

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(MetricsTool.class);

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
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String arg)
	  throws Exception {
	}

	/**
	 * <i>Does not do anything.</i>
	 *
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration()
	  throws Exception {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param inputArgs maps the input argument identifiers to the arguments.
	 *
	 * @pre inputArgs.get(SCENE) != null and inputArgs.get(SCENE).oclIsKindOf(Scene)
	 */
	public void setInputMap(final Map inputArgs) {
		scene = (Scene) inputArgs.get(SCENE);

		if (scene == null) {
			LOGGER.error("A scene must be provided for slicing.");
			throw new IllegalArgumentException("A scene must be provided for slicing.");
		}
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List getInputParameterList() {
		return Collections.singletonList(SCENE);
	}

	/**
	 * {@inheritDoc}  The map will contain statistics for classes belonging to various categories. Currently, application and
	 * library classes are supported.  The statistics for each category is itself provided as a map. Please refer to {@link
	 * edu.ksu.cis.indus.common.soot.MetricsProcessor#getStatistics() MetricsProcessor.getStatistics()} for details of the
	 * category statistics.
	 */
	public Map getOutputMap() {
		return outputMap;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List getOutputParameterList() {
		final List _result = new ArrayList();
		_result.add(MetricsProcessor.APPLICATION_STATISTICS);
		_result.add(MetricsProcessor.LIBRARY_STATISTICS);
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
	public void quit()
	  throws Exception {
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run()
	  throws Exception {
		final ProcessingController _pc = new ProcessingController();
		final MetricsProcessor _mp = new MetricsProcessor();
        final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
        final IStmtGraphFactory _sgf = new ExceptionFlowSensitiveStmtGraphFactory();
        _ssr.setStmtGraphFactory(_sgf);
        _pc.setStmtSequencesRetriever(_ssr);
		_mp.hookup(_pc);
		_pc.setEnvironment(new Environment(scene));
		_pc.process();
		_mp.unhook(_pc);
		outputMap = _mp.getStatistics();
	}
}

// End of File
