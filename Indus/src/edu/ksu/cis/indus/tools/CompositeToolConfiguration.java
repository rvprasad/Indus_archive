
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

package edu.ksu.cis.indus.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
	private static final Log LOGGER = LogFactory.getLog(CompositeToolConfiguration.class);

	/** 
	 * The list of constituent configuration.
	 *
	 * @invariant configurations->forall(o | o.oclIsKindOf(IToolConfiguration))
	 */
	final List configurations = new ArrayList();

	/** 
	 * The active constituent configuration id.
	 */
	private String activeConfigID;

	/**
	 * Create a new container of configurations.  This is primarily used for java-2-xml binding.
	 *
	 * @return a configurations container.
	 *
	 * @post result != null
	 */
	public static List createConfigurations() {
		return new ArrayList();
	}

	/**
	 * Sets the active constituent configuration.
	 *
	 * @param config is the active configuration.
	 *
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
	 *
	 * @throws RuntimeException when the given configuration id is non-existent.
	 */
	public void setActiveToolConfigurationID(final String id) {
		if (configurations.isEmpty()) {
			activeConfigID = id;
		} else {
			String _temp = null;

			for (final Iterator _i = configurations.iterator(); _i.hasNext();) {
				final IToolConfiguration _config = (IToolConfiguration) _i.next();

				if (_config.getConfigName().equals(id)) {
					_temp = id;
				}
			}

			if (_temp != null) {
				activeConfigID = _temp;
			} else {
				final String _msg =
					"setActiveToolConfigurationID(id = " + id + ") - Configuration with given ID does not exist.";
				LOGGER.error(_msg);
				throw new RuntimeException(_msg);
			}
		}
	}

	/**
	 * Retrieves the id of the active configuration.
	 *
	 * @return id of the configuration.
	 *
	 * @post result != null
	 */
	public String getActiveToolConfigurationID() {
		return getActiveToolConfiguration().getConfigName();
	}

	/**
	 * Add a configuration to this composite.
	 *
	 * @param config is the configuration to be added.
	 *
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
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#initialize()
	 */
	public void initialize() {
	}

	/**
	 * Sets the configurations.
	 *
	 * @param configs to be contained in this composite.
	 *
	 * @pre configs != null and configs.oclIsKindOf(Collection(AbstractToolConfiguration))
	 */
	protected void setConfigurations(final Collection configs) {
		configurations.clear();
		configurations.addAll(configs);
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#processProperty(Object, Object)
	 */
	protected boolean processProperty(final Object propertyID, final Object value) {
		final IToolConfiguration _ac = getActiveToolConfiguration();
		return ((AbstractToolConfiguration) _ac).processProperty(propertyID, value);
	}

	/**
	 * Retrieves the active configuration.
	 *
	 * @return the active configuration.
	 *
	 * @throws RuntimeException when there are no configurations.
	 *
	 * @post result != null
	 */
	IToolConfiguration getActiveToolConfiguration() {
		IToolConfiguration _result = null;

		for (final Iterator _i = configurations.iterator(); _i.hasNext();) {
			final IToolConfiguration _config = (IToolConfiguration) _i.next();

			if (_config.getConfigName().equals(activeConfigID)) {
				_result = _config;
				break;
			}
		}

		if (_result == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Selecting the first configuration as active configurationCollection.");
			}

			_result = ((IToolConfiguration) configurations.get(0));

			if (_result != null) {
				activeConfigID = _result.getConfigName();
			} else {
				throw new RuntimeException("There are no configurations.");
			}
		}

		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.15  2004/08/03 22:28:12  venku
   - active configuration was ignored. FIXED.
   Revision 1.14  2003/12/28 03:18:51  venku
   - jibx supports abstract types during binding.  Whoa!
   Revision 1.13  2003/12/02 11:31:57  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.12  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.11  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.10  2003/10/19 20:16:23  venku
   - jibx binding fixes.
   Revision 1.9  2003/10/19 19:11:57  venku
   - empty message
   Revision 1.8  2003/10/14 05:39:25  venku
   - well, jibx doesnot support abstract types for fields even with
     factories or I do not know how to tell it to use the factory.
     Right now, the fix is to use concrete types.
   Revision 1.7  2003/09/27 01:27:47  venku
   - documentation.
   Revision 1.6  2003/09/27 01:09:36  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.5  2003/09/26 15:30:39  venku
   - removed PropertyIdentifier class.
   - ripple effect of the above change.
   - formatting
   Revision 1.4  2003/09/26 15:16:40  venku
   - coding conventions.
   Revision 1.3  2003/09/26 15:05:01  venku
   - binding related errors fixed.
   Revision 1.2  2003/09/26 15:00:01  venku
   - The configuration of tools in Indus has been placed in this package.
   - Formatting.
   Revision 1.1  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
   Revision 1.1  2003/09/26 05:56:10  venku
   - a checkpoint commit.
 */
