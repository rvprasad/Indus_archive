
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This provides the implementation of <code>IToolConfiguration</code> which concrete implementations should extend.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractToolConfiguration
  implements IToolConfiguration {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractToolConfiguration.class);

	/**
	 * This is set of property ids recognized by this configurationCollection.
	 */
	protected final Collection propertyIds = new HashSet();

	/**
	 * This maps properties of this configurationCollection to their values.
	 *
	 * @invariant properties != null
	 */
	protected final Map properties = new HashMap();

	/**
	 * The name of the configuration.
	 */
	private String configName;

	/**
	 * @see IToolConfiguration#setProperty(Object,Object)
	 */
	public final boolean setProperty(final Object propertyID, final Object value) {
		if (!propertyIds.contains(propertyID)) {
			final String _message = "Invalid property identifier specified: " + propertyID;
			LOGGER.error(_message);
			throw new IllegalArgumentException(_message);
		}

		final boolean _result = processProperty(propertyID, value);

		if (_result) {
			properties.put(propertyID, value);
		}
		return _result;
	}

	/**
	 * @see IToolConfiguration#getProperty(Object)
	 */
	public final Object getProperty(final Object id) {
		return properties.get(id);
	}

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfiguration#setConfigName(java.lang.String)
	 */
	public void setConfigName(final String string) {
		configName = string;
	}

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfiguration#getConfigName()
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * Processes the given property.  This should be overriden by subclasses to handle alter configurationCollection.  Only a
	 * return value of <code>true</code> will result in the property being added to the configurationCollection.
	 *
	 * @param propertyID is the identifier of the property.
	 * @param value of the property.
	 *
	 * @return <code>true</code> if the property was successfully processed; <code>false</code>, otherwise.
	 *
	 * @pre propertyID != null
	 */
	protected abstract boolean processProperty(final Object propertyID, final Object value);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.3  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.2  2003/09/27 01:09:36  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.1  2003/09/26 23:46:59  venku
   - Renamed Tool to AbstractTool
   - Renamed ToolConfiguration to AbstractToolConfiguration
   - Renamed ToolConfigurator to AbstractToolConfigurator
   Revision 1.7  2003/09/26 15:35:53  venku
   - finalized methods.
   Revision 1.6  2003/09/26 15:30:39  venku
   - removed PropertyIdentifier class.
   - ripple effect of the above change.
   - formatting
   Revision 1.5  2003/09/26 15:00:01  venku
   - The configuration of tools in Indus has been placed in this package.
   - Formatting.
   Revision 1.4  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
   Revision 1.3  2003/09/26 05:56:10  venku
   - a checkpoint commit.
   Revision 1.2  2003/09/24 07:03:02  venku
   - Renamed ToolConfigurationEditor to AbstractToolConfigurator.
   - Added property id creation support, via factory method, to AbstractToolConfiguration.
   - Changed the interface in AbstractTool.
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
