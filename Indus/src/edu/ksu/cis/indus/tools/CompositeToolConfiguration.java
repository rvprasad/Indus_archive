
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


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
	 */
	final ArrayList configurations = new ArrayList();

	/**
	 * The active constituent configuration.
	 */
	private AbstractToolConfiguration active;

	/**
	 * Create a new container of configurations.  This is primarily used for java-2-xml binding.
	 *
	 * @return a configurations container.
	 *
	 * @post result != null
	 */
	public static ArrayList createConfigurations() {
		return new ArrayList();
	}

	/**
	 * Sets the active constituent configuration.
	 *
	 * @param config is the active configuration.
	 *
	 * @pre config != null and configurations.contains(config)
	 */
	public void setActiveToolConfiguration(final AbstractToolConfiguration config) {
		if (configurations.contains(config)) {
			active = config;
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("The given configuration is not part of this collection.  It was not activated.");
			}
		}
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
	public AbstractToolConfiguration getActiveToolConfiguration() {
		if (active == null) {
			active = (AbstractToolConfiguration) configurations.get(0);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Selecting the first configuration as active configurationCollection.");
			}

			if (active == null) {
				throw new RuntimeException("There are no configurations.");
			}
		}
		return active;
	}

	/**
	 * Sets the configuration with the given id as active.
	 *
	 * @param id of the configuration to be activated.
	 */
	public void setActiveToolConfigurationID(final String id) {
		for (Iterator i = configurations.iterator(); i.hasNext();) {
			AbstractToolConfiguration config = (AbstractToolConfiguration) i.next();

			if (config.configName.equals(id)) {
				active = config;
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
		return getActiveToolConfiguration().configName;
	}

	/**
	 * Add a configuration to this composite.
	 *
	 * @param config is the configuration to be added.
	 *
	 * @pre config != null
	 * @post config != null implies configurations.contains(config)
	 */
	public void addToolConfiguration(final AbstractToolConfiguration config) {
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
	 * Retrieves the tool configuration with the given id.
	 *
	 * @param id of the requested configuration.
	 *
	 * @return the requested configuration.
	 */
	protected AbstractToolConfiguration getToolConfiguration(final String id) {
		AbstractToolConfiguration result = null;

		for (Iterator i = configurations.iterator(); i.hasNext();) {
			result = (AbstractToolConfiguration) i.next();

			if (result.configName.equals(id)) {
				break;
			}
			result = null;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfiguration#processProperty(Object, Object)
	 */
	protected boolean processProperty(final Object propertyID, final Object value) {
		return getActiveToolConfiguration().processProperty(propertyID, value);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2003/10/19 20:16:23  venku
   - jibx binding fixes.

   Revision 1.9  2003/10/19 19:11:57  venku
   *** empty log message ***

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
