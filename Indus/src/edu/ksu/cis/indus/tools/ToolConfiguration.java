
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * This class should be extended by a configurationCollection instance of a tool.  It provides methods to programmatically
 * configure the tool.  It also provides a method to stringize the configurationCollection if required by the toolkits/IDEs.
 * Some toolkits/IDE may support persistence of only string-based properties.  To this end, we explicitly provide a
 * <code>stringizeConfiguration</code> and <code>destringizeConfiguration</code> methods in <code>Tool</code> interface that
 * should be used to set and get the information in an instance of this class as a string suitable for serialization.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class ToolConfiguration {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ToolConfiguration.class);

	/**
	 * This is set of property ids recognized by this configurationCollection.
	 */
	protected final Collection PROPERTY_IDS = new HashSet();

	/**
	 * This maps properties of this configurationCollection to their values.
	 *
	 * @invariant properties != null
	 */
	protected final Map properties = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected String NAME;

	/**
	 * This identifies a property.  It does not represent a property.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class PropertyIdentifier {
		/**
		 * The id of the property.
		 */
		public final Object _id;

		/**
		 * Creates a new Property object.
		 *
		 * @param idParam is the name of the parameter.
		 */
		PropertyIdentifier(final Object idParam) {
			_id = idParam;
		}
	}

	/**
	 * Sets a property of the configurationCollection. The given <code>propertyID</code> should be a valid property id
	 * declared in this class.  If not, an exception will be raised.
	 *
	 * @param propertyID to be set.
	 * @param value to be assigned to the property.
	 *
	 * @return <code>true</code> if the property was added; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException when an invalid property identifier is specified.
	 *
	 * @pre property != null and value != null
	 */
	public boolean setProperty(final PropertyIdentifier propertyID, final Object value) {
		if (!PROPERTY_IDS.contains(propertyID)) {
			String message = "Invalid property identifier specified: " + propertyID;
			LOGGER.error(message);
			throw new IllegalArgumentException(message);
		}

		boolean result = processProperty(propertyID, value);

		if (result) {
			properties.put(propertyID, value);
		}
		return result;
	}

	/**
	 * Retrieves the value of the given property, if it exists.
	 *
	 * @param id of the property for which the value is requested.
	 *
	 * @return the value of the property if it exists; <code>null</code>, otherwise.
	 *
	 * @pre key != null
	 */
	public Object getProperty(final PropertyIdentifier id) {
		return properties.get(id);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public abstract void initialize();

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
	protected abstract boolean processProperty(final PropertyIdentifier propertyID, final Object value);

	/**
	 * A factory method to create a property identifier.
	 *
	 * @param id of the property.
	 *
	 * @return a property identifier.
	 */
	protected static PropertyIdentifier createPropertyIdentifier(final Object id) {
		return new PropertyIdentifier(id);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
   Revision 1.3  2003/09/26 05:56:10  venku
   - a checkpoint commit.
   Revision 1.2  2003/09/24 07:03:02  venku
   - Renamed ToolConfigurationEditor to ToolConfigurator.
   - Added property id creation support, via factory method, to ToolConfiguration.
   - Changed the interface in Tool.
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
