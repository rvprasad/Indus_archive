
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
 * This is the interface to be implemented by a configuration instance of a tool.  It provides methods to programmatically
 * configure the tool.  It also provides a method to stringize the configuration if required by the toolkits/IDEs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ToolConfiguration {
	/**
	 * Sets a property of the configuration.
	 *
	 * @param property to be set.
	 * @param value to be assigned to the property.
	 *
	 * @pre property != null and value != null
	 */
	void setProperty(Object property, Object value);

	/**
	 * Retrieves the value of the given property, if it exists.
	 *
	 * @param property for which the value is requested.
	 *
	 * @return the value of the property if it exists; <code>null</code>, otherwise.
	 *
	 * @pre key != null
	 */
	Object getProperty(Object key);

	/**
	 * Returns a stringized representation of the configuration.  This may be required in toolkits/IDEs.
	 *
	 * @return the stringized representation of the configuration.
	 *
	 * @post result != null
	 */
	String stringize();
}

/*
   ChangeLog:
   $Log$
 */
