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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractToolConfiguration.class);

	/**
	 * This maps properties of this configuration to their values.
	 * 
	 * @invariant properties != null
	 */
	protected final Map<Object, Object> properties = new HashMap<Object, Object>();

	/**
	 * This is set of property ids recognized by this configurationCollection.
	 */
	protected final Collection<Object> propertyIds = new HashSet<Object>();

	/**
	 * The name of the configuration.
	 */
	private String configName;

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfiguration#getConfigName()
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @see IToolConfiguration#getProperty(Comparable)
	 */
	public final Object getProperty(final Comparable id) {
		return properties.get(id);
	}

	/**
	 * @see edu.ksu.cis.indus.tools.IToolConfiguration#setConfigName(java.lang.String)
	 */
	public void setConfigName(final String string) {
		configName = string;
	}

	/**
	 * @see IToolConfiguration#setProperty(Comparable,Object)
	 */
	public final boolean setProperty(final Comparable propertyID, final Object value) {
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
	 * Processes the given property. This should be overriden by subclasses to handle alter configuration. Only a return value
	 * of <code>true</code> will result in the property being added to the configuration.
	 * 
	 * @param propertyID is the identifier of the property.
	 * @param value of the property.
	 * @return <code>true</code> if the property was successfully processed; <code>false</code>, otherwise.
	 * @pre propertyID != null
	 */
	protected abstract boolean processProperty(final Comparable propertyID, final Object value);
}

// End of File
