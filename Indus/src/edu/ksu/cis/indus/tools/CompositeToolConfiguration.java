
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
import java.util.Iterator;
import java.util.List;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CompositeToolConfiguration
  extends ToolConfiguration {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CompositeToolConfiguration.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final List configurations = new ArrayList();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ToolConfiguration active;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public static List createConfigurations() {
		return new ArrayList();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param tc DOCUMENT ME!
	 */
	public final void setActiveToolConfiguration(final ToolConfiguration tc) {
		if (configurations.contains(tc)) {
			active = tc;
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("The given configuration is not part of this collection.  It was not activated.");
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public final ToolConfiguration getActiveToolConfiguration() {
		if (active == null) {
			active = (ToolConfiguration) configurations.get(0);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Selecting the first configurationCollection as active configurationCollection.");
			}

			if (active == null) {
				throw new RuntimeException("There are no configurations.");
			}
		}
		return active;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param id DOCUMENT ME!
	 */
	public final void setActiveToolConfigurationID(final String id) {
		for (Iterator i = configurations.iterator(); i.hasNext();) {
			ToolConfiguration config = (ToolConfiguration) i.next();

			if (config.NAME.equals(id)) {
				active = config;
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public final String getActiveToolConfigurationID() {
		return getActiveToolConfiguration().NAME;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param tc DOCUMENT ME!
	 */
	public final void addToolConfiguration(final ToolConfiguration tc) {
		if (tc == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Null configurations are not supported.");
			}
		} else {
			if (configurations.contains(tc)) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("The given configurationCollection exists.");
				}
			} else {
				configurations.add(tc);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.tools.ToolConfiguration#initialize()
	 */
	public void initialize() {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param configs DOCUMENT ME!
	 */
	protected void setConfigurations(List configs) {
		configurations.clear();
		configurations.addAll(configs);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param name DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected final ToolConfiguration getToolConfiguration(String name) {
		ToolConfiguration result = null;

		for (Iterator i = configurations.iterator(); i.hasNext();) {
			result = (ToolConfiguration) i.next();

			if (result.NAME.equals(name)) {
				break;
			}
			result = null;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.tools.ToolConfiguration#processProperty(edu.ksu.cis.indus.tools.ToolConfiguration.PropertyIdentifier,
	 * 		java.lang.Object)
	 */
	protected final boolean processProperty(PropertyIdentifier propertyID, Object value) {
		return getActiveToolConfiguration().processProperty(propertyID, value);
	}
}

/*
   ChangeLog:
   $Log$
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
