
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
 * This class should be extended by a configuration instance of a tool.  It provides methods to programmatically configure
 * the tool.  It also provides a method to stringize the configuration if required by the toolkits/IDEs.  Some toolkits/IDE
 * may support persistence of only string-based configurations.  For sake of usability, we recommend the implementors of
 * this interface to support a non-null returning <code>java.lang.toString()</code> method always.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class ToolConfiguration {
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
	 * A factory method to create a property identifier. 
	 *
	 * @param id of the property.
	 *
	 * @return a property identifier.
	 */
	protected static PropertyIdentifier createPropertyIdentifier(final Object id) {
		return new PropertyIdentifier(id);
	}

	/**
	 * Sets a property of the configuration.
	 *
	 * @param property to be set.
	 * @param value to be assigned to the property.
	 *
	 * @pre property != null and value != null
	 */
	public abstract void setProperty(final PropertyIdentifier property, final Object value);

	/**
	 * Retrieves the value of the given property, if it exists.
	 *
	 * @param id of the property for which the value is requested.
	 *
	 * @return the value of the property if it exists; <code>null</code>, otherwise.
	 *
	 * @pre key != null
	 */
	public abstract Object getProperty(final PropertyIdentifier id);
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
