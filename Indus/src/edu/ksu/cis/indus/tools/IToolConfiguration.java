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
	Object getProperty(final Comparable<?> id);

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
	boolean setProperty(final Comparable<?> propertyID, final Object value);
}

// End of File
