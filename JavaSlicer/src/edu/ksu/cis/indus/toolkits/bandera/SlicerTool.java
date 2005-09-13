
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
import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.transformations.TagBasedDestructiveSliceResidualizer;
import edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.tools.slicer.SlicerToolHelper;
import edu.ksu.cis.indus.tools.slicer.criteria.specification.SliceCriteriaParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * This identifies the slicing criteria in the input arguments.
	 */
	public static final Object CRITERIA = "slicingCriteria";

	/** 
	 * This identifies the slicing criteria specification in the input arguments.
	 */
	public static final Object CRITERIA_SPECIFICATION = "slicingCriteriaSpecification";

	/** 
	 * This identifies the slicer configuration to be used.
	 */
	public static final Object ID_OF_CONFIGURATION_TO_USE = "idOfConfigurationToUse";

	/** 
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "entryPoints";

	/** 
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "scene";

	/** 
	 * The collection of input argument identifiers.
	 */
	private static final List<Object> IN_ARGUMENTS_IDS;

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SlicerTool.class);

	/** 
	 * The collection of output argument identifiers.
	 */
	private static final List<Object> OUT_ARGUMENTS_IDS;

	/** 
	 * The default tag name to be used.
	 */
	private static final String TAG_NAME = "Slicer:Bandera";

	static {
		IN_ARGUMENTS_IDS = new ArrayList<Object>();
		IN_ARGUMENTS_IDS.add(SCENE);
		IN_ARGUMENTS_IDS.add(ROOT_METHODS);
		IN_ARGUMENTS_IDS.add(CRITERIA);
		IN_ARGUMENTS_IDS.add(CRITERIA_SPECIFICATION);
		OUT_ARGUMENTS_IDS = new ArrayList<Object>();
		OUT_ARGUMENTS_IDS.add(SCENE);
	}

	/**
	 * This indicates if the tool chain decided to abort the current run.
	 */
	private boolean abortFlag;

	/** 
	 * This is the configuration.
	 */
	private SlicerConfiguration configuration;

	/** 
	 * The configuration interface provided by this object to configure the slicer tool.
	 */
	private SlicerConfigurationView configurationView;

	/** 
	 * This indicates if the configuration was provided.
	 */
	private boolean configurationWasProvided;

	/** 
	 * The scene being processed.
	 */
	private Scene scene;

	/** 
	 * The slicer tool that is adapted by this object.
	 */
	private final edu.ksu.cis.indus.tools.slicer.SlicerTool tool;

	/**
	 * Creates a new SlicerTool object.
	 */
	public SlicerTool() {
		tool =
			new edu.ksu.cis.indus.tools.slicer.SlicerTool(new BitSetTokenManager(new SootValueTypeManager()),
				new CompleteStmtGraphFactory());
		tool.setTagName(TAG_NAME);
		configurationView = new SlicerConfigurationView(tool.getConfigurator());
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() {
		try {
			configuration.slicerConfigurationStr = tool.stringizeConfiguration();

			final IBindingFactory _bfact = BindingDirectory.getFactory(SlicerConfiguration.class);
			final IMarshallingContext _mctx = _bfact.createMarshallingContext();
			_mctx.setIndent(4);

			final ByteArrayOutputStream _b = new ByteArrayOutputStream();
			_mctx.marshalDocument(configuration, "UTF-8", null, _b);
			return _b.toString();
		} catch (final JiBXException _e) {
			final UnknownError _r = new UnknownError();
			_r.initCause(_e);
			throw _r;
		}
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
		final Map<Object, Object> _outputMap = new HashMap<Object, Object>();
		_outputMap.put(SCENE, scene);
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
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return configurationView;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	public void quit()
	  throws Exception {
		tool.abort();
		abortFlag = true;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run()
	  throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: bandera slicer tool");
		}
		abortFlag = false;

		if (!configurationWasProvided) {
			final String _msg = "No configuration was provided.  Aborting!!!";
			LOGGER.error(_msg);
			throw new IllegalArgumentException(_msg);
		}

		tool.run(Phase.STARTING_PHASE, null, true);

		SlicerToolHelper.optimizeForSpaceBeforeResidualization(tool, configuration.retentionList);

		if (!abortFlag) {
			final TagBasedDestructiveSliceResidualizer _residualizer = new TagBasedDestructiveSliceResidualizer();
			_residualizer.setTagToResidualize(TAG_NAME);
			_residualizer.setBasicBlockGraphMgr(tool.getBasicBlockGraphManager());
			_residualizer.residualizeSystem(tool.getSystem());
		}

		if (configuration.eraseUnnecessaryClasses) {
			SlicerToolHelper.optimizeForSpaceAfterResidualization(tool, configuration.retentionList);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: bandera slicer tool");
		}
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String configStr)
	  throws Exception {
		configurationWasProvided = configStr != null && configStr.length() > 0;

		final IBindingFactory _bfact = BindingDirectory.getFactory(SlicerConfiguration.class);
		final IUnmarshallingContext _uctx = _bfact.createUnmarshallingContext();
		configuration =
			(SlicerConfiguration) _uctx.unmarshalDocument(new BufferedInputStream(
					new ByteArrayInputStream(configStr.getBytes())), null);
		tool.destringizeConfiguration(configuration.slicerConfigurationStr);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param inputArgs maps the input argument identifiers to the arguments.
	 *
	 * @pre inputArgs.get(SCENE) != null and inputArgs.get(SCENE).oclIsKindOf(Scene)
	 * @pre inputArgs.get(CRITERIA_SPECIFICATION).oclIsKindOf(String)
	 * @pre inputArgs.get(CRITERIA).oclIsKindOf(Collection(edu.ksu.cis.indus.slicer.ISliceCriterion))
	 * @pre inputArgs.get(ROOT_METHODS) != null and inputArgs.get(ROOT_METHODS).oclIsKindOf(Collection(SootMethod))
	 * @pre inputArgs.get(ID_OF_CONFIGURATION_TO_USE).oclIsKindOf(String)
	 *
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map inputArgs) {
		scene = (Scene) inputArgs.get(SCENE);

		if (scene == null) {
			final String _msg = "A scene must be provided for slicing.";
			LOGGER.error(_msg);
			throw new IllegalArgumentException(_msg);
		}
		tool.setSystem(new Environment(scene));

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

		final String _criteriaSpec = (String) inputArgs.get(CRITERIA_SPECIFICATION);

		if (_criteriaSpec == null) {
			LOGGER.info("No criteria specification provided.");
		} else {
			try {
				tool.setCriteria(SliceCriteriaParser.deserialize(_criteriaSpec, scene));
			} catch (final JiBXException _e) {
				final String _msg = "Error occurred while deserializing the provided criteria specification.";
				LOGGER.error(_msg);

				final IllegalArgumentException _t = new IllegalArgumentException(_msg);
				_t.initCause(_e);
				throw _t;
			}
		}

		final Collection _rootMethods = (Collection) inputArgs.get(ROOT_METHODS);

		if (_rootMethods == null || _rootMethods.isEmpty()) {
			final String _msg = "Atleast one method should be specified as the entry-point into the system.";
			LOGGER.error(_msg);
			throw new IllegalArgumentException(_msg);
		}
		tool.setRootMethods(_rootMethods);

		final String _activeConfID = (String) inputArgs.get(ID_OF_CONFIGURATION_TO_USE);

		if (_activeConfID == null) {
			LOGGER.info("No active configuration was specified.  Using the default in the provided configuration.");
		} else {
			tool.setActiveConfiguration(_activeConfID);
		}
	}
}

// End of File
