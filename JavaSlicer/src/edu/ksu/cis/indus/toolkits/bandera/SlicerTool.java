
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

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import edu.ksu.cis.bandera.util.BaseObservable;

import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;

import edu.ksu.cis.indus.tools.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Scene;


/**
 * This class wraps the slicer in the tool interface required by the tool pipeline in Bandera.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerTool
  extends BaseObservable
  implements Tool {
	/**
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "scene";

	/**
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "entryPoints";

	/**
	 * This identifies the slicing criteria in the input arguments.
	 */
	public static final Object CRITERIA = "slicingCriteria";

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
		OUT_ARGUMENTS_IDS = new ArrayList();
		OUT_ARGUMENTS_IDS.add(SCENE);
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerTool.class);

	/**
	 * The default tag name to be used.
	 */
	private static final String TAG_NAME = "Slicer:Bandera";

	/**
	 * The slicer tool that is adapted by this object.
	 */
	private final edu.ksu.cis.indus.tools.slicer.SlicerTool tool;

	/**
	 * The configuration interface provided by this object to configure the slicer tool.
	 */
	private SlicerConfigurationView configurationView;

	/**
	 * Creates a new SlicerTool object.
	 */
	public SlicerTool() {
		tool = new edu.ksu.cis.indus.tools.slicer.SlicerTool();
		tool.setTagName(TAG_NAME);
		configurationView = new SlicerConfigurationView(tool.getConfigurator());
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String configStr)
	  throws Exception {
		tool.destringizeConfiguration(configStr);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() {
		return tool.stringizeConfiguration();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param inputArgs maps the input argument identifiers to the arguments.
	 *
	 * @pre inputArgs.get(SCENE) != null and inputArgs.get(SCENE).oclIsKindOf(Scene)
	 * @pre inputArgs.get(CRITERIA).oclIsKindOf(Collection(edu.ksu.cis.indus.slicer.AbstractSliceCriterion))
	 * @pre inputArgs.get(ROOT_METHODS) != null and inputArgs.get(ROOT_METHODS).oclIsKindOf(Collection(SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map inputArgs) {
		final Scene _theScene = (Scene) inputArgs.get(SCENE);

		if (_theScene == null) {
			LOGGER.error("A scene must be provided for slicing.");
			throw new IllegalArgumentException("A scene must be provided for slicing.");
		}
		tool.setSystem(_theScene);

		final Collection _criteria = (Collection) inputArgs.get(CRITERIA);

		if (_criteria == null || _criteria.isEmpty()) {
			LOGGER.warn("Deadlock criteria will be used.");
		} else {
			tool.setCriteria(_criteria);

			for (final Iterator _i = _criteria.iterator(); _i.hasNext();) {
				final Object _o = _i.next();

				if (!SliceCriteriaFactory.isSlicingCriterion(_o)) {
					LOGGER.error(_o + " is an invalid slicing criterion.  "
						+ "All slicing criterion should be created via SliceCriteriaFactory.");
					throw new IllegalArgumentException("Slicing criteion " + _o
						+ " was not created via SliceCriteriaFactory.");
				}
			}
		}

		final Collection _rootMethods = (Collection) inputArgs.get(ROOT_METHODS);

		if (_rootMethods == null || _rootMethods.isEmpty()) {
			LOGGER.error("Atleast one method should be specified as the entry-point into the system.");
			throw new IllegalArgumentException("Atleast one method should be specified as the entry-point into the system.");
		}
		tool.setRootMethods(_rootMethods);
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
		final Map _outputMap = new HashMap();
		_outputMap.put(SCENE, tool.getSystem());
		return _outputMap;
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
		return configurationView;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.AbstractTool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return configurationView;
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
	public void run()
	  throws Exception {
		tool.run(Phase.STARTING_PHASE, true);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.20  2003/12/09 12:23:48  venku
   - added support to control synchronicity of method runs.
   - ripple effect.
   Revision 1.19  2003/12/02 11:32:01  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.18  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.17  2003/11/24 10:11:32  venku
   - there are no residualizers now.  There is a very precise
     slice collector which will collect the slice via tags.
   - architectural change. The slicer is hard-wired wrt to
     slice collection.  Residualization is outside the slicer.
   Revision 1.16  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.15  2003/11/18 21:43:54  venku
   - fixed code in bandera version of the tool to work with new assumptions.
   - removed TAG_NAME input parameter to make the transition to clone-based transformer
     transparent to bandera.
   Revision 1.14  2003/11/14 21:09:37  venku
   - formatting.
   Revision 1.13  2003/11/14 21:08:17  venku
   - verify the type of criteria if atleast one is specified.
   Revision 1.12  2003/11/13 15:37:47  venku
   - criteria can be null or an empty set to defaul to deadlock
     based criteria.
   Revision 1.11  2003/10/14 02:58:53  venku
   - changed tag name.
   Revision 1.10  2003/10/13 01:01:45  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.9  2003/10/12 19:45:05  venku
    - Changed valus of input/output args Ids as per the suggestion
      of Todd.
   Revision 1.8  2003/09/28 23:16:18  venku
   - documentation
   Revision 1.7  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.6  2003/09/27 01:27:46  venku
   - documentation.
   Revision 1.5  2003/09/27 01:09:35  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.4  2003/09/26 15:07:51  venku
   - completed support for exposing slicer as a tool
     and configuring it both in Bandera and outside it.
   Revision 1.3  2003/09/26 05:55:51  venku
   - a checkpoint commit.
   Revision 1.2  2003/09/24 07:33:24  venku
   - Nightly commit.
   - Need to wrap the indus tool api in ways specific to bandera
     tool api.
   Revision 1.1  2003/09/24 01:43:45  venku
   - Renamed edu.ksu.cis.indus.tools to edu.ksu.cis.indus.toolkits.
     This package is to house adaptation of each tools for each toolkits.
   - Retained edu.ksu.cis.indus.tools to contain API/interface to expose
     the implementation as a tool.
   Revision 1.1  2003/09/15 08:55:23  venku
   - Well, the SlicerTool is still a mess in my opinion as it needs
     to be implemented as required by Bandera.  It needs to be
     much richer than it is to drive the slicer.
   - SlicerConfigurator is supposed to bridge the above gap.
     I doubt it.
 */
