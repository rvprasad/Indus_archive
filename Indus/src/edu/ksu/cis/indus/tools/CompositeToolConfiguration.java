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

package edu.ksu.cis.indus.tools;

import edu.ksu.cis.indus.annotations.Empty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a composite of configurations. The idea is to use composite to pattern to enable hierarchical
 * configuration information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompositeToolConfiguration
		extends AbstractToolConfiguration {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeToolConfiguration.class);

	/**
	 * The list of constituent configuration.
	 */
	final List<IToolConfiguration> configurations = new ArrayList<IToolConfiguration>();

	/**
	 * The active constituent configuration id.
	 */
	private String activeConfigID;

	/**
	 * Creates an instance of this class.
	 */
	public CompositeToolConfiguration() {
		super();
	}

	/**
	 * Create a new container of configurations. This is primarily used for java-2-xml binding.
	 * 
	 * @return a configurations container.
	 * @post result != null
	 */
	public static List createConfigurations() {
		return new ArrayList();
	}

	/**
	 * Add a configuration to this composite.
	 * 
	 * @param config is the configuration to be added.
	 * @pre config != null
	 * @post config != null implies configurations.contains(config)
	 */
	public void addToolConfiguration(final IToolConfiguration config) {
		if (config == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Null configurations are not supported.");
			}
		} else {
			if (configurations.contains(config)) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("The given configurationCollection exists.");
				}
			} else {
				configurations.add(config);
			}
		}
	}

	/**
	 * Retrieves the id of the active configuration.
	 * 
	 * @return id of the configuration.
	 * @post result != null
	 */
	public String getActiveToolConfigurationID() {
		return getActiveToolConfiguration().getConfigName();
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#initialize()
	 */
	@Empty public void initialize() {
		// Does nothing
	}

	/**
	 * Sets the active constituent configuration.
	 * 
	 * @param config is the active configuration.
	 * @pre config != null and configurations.contains(config)
	 */
	public void setActiveToolConfiguration(final IToolConfiguration config) {
		if (configurations.contains(config)) {
			activeConfigID = config.getConfigName();
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("The given configuration is not part of this collection.  It was not activated.");
			}
		}
	}

	/**
	 * Sets the configuration with the given id as active.
	 * 
	 * @param id of the configuration to be activated.
	 * @throws RuntimeException when the given configuration id is non-existent.
	 */
	public void setActiveToolConfigurationID(final String id) {
		if (configurations.isEmpty()) {
			activeConfigID = id;
		} else {
			String _temp = null;

			for (final Iterator<IToolConfiguration> _i = configurations.iterator(); _i.hasNext();) {
				final IToolConfiguration _config = _i.next();

				if (_config.getConfigName().equals(id)) {
					_temp = id;
				}
			}

			if (_temp != null) {
				activeConfigID = _temp;
			} else {
				final String _msg = "setActiveToolConfigurationID(id = " + id
						+ ") - Configuration with given ID does not exist.";
				LOGGER.error(_msg);
				throw new RuntimeException(_msg);
			}
		}
	}

	/**
	 * Sets the configurations.
	 * 
	 * @param configs to be contained in this composite.
	 * @pre configs != null and configs.oclIsKindOf(Collection(AbstractToolConfiguration))
	 */
	public void setConfigurations(final List<IToolConfiguration> configs) {
		configurations.clear();
		configurations.addAll(configs);
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#processProperty(Comparable, Object)
	 */
	@Override protected boolean processProperty(final Comparable propertyID, final Object value) {
		final IToolConfiguration _ac = getActiveToolConfiguration();
		return ((AbstractToolConfiguration) _ac).processProperty(propertyID, value);
	}

	/**
	 * Retrieves the active configuration.
	 * 
	 * @return the active configuration.
	 * @throws RuntimeException when there are no configurations.
	 * @post result != null
	 */
	IToolConfiguration getActiveToolConfiguration() {
		IToolConfiguration _result = null;

		for (final Iterator<IToolConfiguration> _i = configurations.iterator(); _i.hasNext();) {
			final IToolConfiguration _config = _i.next();

			if (_config.getConfigName().equals(activeConfigID)) {
				_result = _config;
				break;
			}
		}

		if (_result == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Selecting the first configuration as active configurationCollection.");
			}

			_result = configurations.get(0);

			if (_result != null) {
				activeConfigID = _result.getConfigName();
			} else {
				throw new RuntimeException("There are no configurations.");
			}
		}

		return _result;
	}
}

// End of File
