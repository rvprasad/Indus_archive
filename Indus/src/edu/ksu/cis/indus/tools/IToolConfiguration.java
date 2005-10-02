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

/**
 * This is the interface to a configuration of a tool.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IToolConfiguration {

	/**
	 * Retrieves the name of this configuration.
	 * 
	 * @return the name of this configuration.
	 */
	String getConfigName();

	/**
	 * Retrieves the value of the given property, if it exists.
	 * 
	 * @param id of the property for which the value is requested.
	 * @return the value of the property if it exists; <code>null</code>, otherwise.
	 * @pre id != null
	 */
	Object getProperty(final Object id);

	/**
	 * Initialize the configuration. This is required if the configuration is created programmatically rather than via
	 * java-to-xml binding.
	 */
	void initialize();

	/**
	 * Sets the name of this configuration.
	 * 
	 * @param name of this configuration.
	 * @pre name != null
	 */
	void setConfigName(String name);

	/**
	 * Sets a property in this configuration. The given <code>propertyID</code> should be a valid property id declared in
	 * this class. If not, an exception will be raised. The valid property ids are managed/specified by concrete
	 * implementations.
	 * 
	 * @param propertyID to be set.
	 * @param value to be assigned to the property.
	 * @return <code>true</code> if the property was added; <code>false</code>, otherwise.
	 * @pre propertyID != null and value != null
	 */
	boolean setProperty(final Object propertyID, final Object value);
}

// End of File
