
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

/**
 * This is the interface to a configuration of a tool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IToolConfiguration {
	/**
	 * DOCUMENT ME!
	 *
	 * @param string
	 */
	void setConfigName(String string);

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	String getConfigName();

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
	boolean setProperty(final Object propertyID, final Object value);

	/**
	 * Retrieves the value of the given property, if it exists.
	 *
	 * @param id of the property for which the value is requested.
	 *
	 * @return the value of the property if it exists; <code>null</code>, otherwise.
	 *
	 * @pre key != null
	 */
	Object getProperty(final Object id);

	/**
	 * Initialize the configuration.  This is required if the configuration is created programmatically rather than
	 * java-to-xml binding.
	 */
	void initialize();
}

/*
   ChangeLog:
   $Log$
 */
