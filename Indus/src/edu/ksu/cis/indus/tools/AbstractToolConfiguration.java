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
	protected final Map<Comparable<?>, Object> properties = new HashMap<Comparable<?>, Object>();

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
	public final Object getProperty(final Comparable<?> id) {
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
	public final boolean setProperty(final Comparable<?> propertyID, final Object value) {
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
	protected abstract boolean processProperty(final Comparable<?> propertyID, final Object value);
}

// End of File
