
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
	 * Processes the given property.  This should be overriden by subclasses to handle alter configuration.  Only a return
	 * value of <code>true</code> will result in the property being added to the configuration.
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

// End of File
