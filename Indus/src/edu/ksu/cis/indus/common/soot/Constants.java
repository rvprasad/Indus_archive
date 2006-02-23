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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.AEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class contains a set of constants that are specific to indus.
 * <p>
 * The user can configure the values returned by the getter methods in this class by specifying a properties file via
 * <code>indus.constant.configuration.properties.file</code> property. The name of the properties is the value of the
 * constants in this class that end with "PROPERTY".
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Constants {

	/**
	 * This is the name of the property via which the constants properties file can be specified. It's value is
	 * "indus.constant.configuration.properties.file".
	 */
	public static final String CONSTANTS_CONFIGURATION_FILE_PROPERTY = "indus.constant.configuration.properties.file";

	/**
	 * The name of the property via which the loading method bodies during initialization can be controlled. It's name is
	 * "edu.ksu.cis.indus.common.soot.SootBasedDriver.LoadMethodBodiesDuringInit".
	 */
	public static final String LOAD_METHOD_BODIES_DURING_INITIALIZATION = "edu.ksu.cis.indus.common.soot.SootBasedDriver.LoadMethodBodiesDuringInit";

	/**
	 * This is the name of the property that control the number of classes in the system. It's name is
	 * "edu.ksu.cis.indus.NumOfClassesInApplication".
	 */
	public static final String NUM_OF_CLASSES_IN_APPLICATION_PROPERTY = "edu.ksu.cis.indus.NumOfClassesInApplication";

	/**
	 * This is the name of the property that control the number of fields in the system. It's name is
	 * "edu.ksu.cis.indus.NumOfFieldsInApplication".
	 */
	public static final String NUM_OF_FIELDS_IN_APPLICATION_PROPERTY = "edu.ksu.cis.indus.NumOfFieldsInApplication";

	/**
	 * This is the name of the property that control the number of methods in the system. It's name is
	 * "edu.ksu.cis.indus.NumOfMethodsInApplication".
	 */
	public static final String NUM_OF_METHODS_IN_APPLICATION_PROPERTY = "edu.ksu.cis.indus.NumOfMethodsInApplication";

	/**
	 * The name of the property via which the name of the root method trapper class can be specified. The name is
	 * "edu.ksu.cis.indus.common.soot.RootMethodTrapper.class". The specified class should be a subclass of
	 * <code>edu.ksu.cis.indus.soot.RootMethodTrapper</code>.
	 */
	public static final String ROOT_METHOD_TRAPPER_CLASS_PROPERTY = "edu.ksu.cis.indus.common.soot.RootMethodTrapper.class";

	/**
	 * The name of the property via which the name of the statement graph factory class can be specified. It's name is
	 * "edu.ksu.cis.indus.common.soot.SootBasedDriver.StmtGraphFactory.class".
	 */
	public static final String STMT_GRAPH_FACTORY_CLASS_PROPERTY = "edu.ksu.cis.indus.common.soot.SootBasedDriver.StmtGraphFactory.class";

	/**
	 * This contains the constants.
	 */
	private static final Properties CONFIGURATIONS = new Properties();

	/**
	 * This is the default for the number of classes in the system.
	 */
	private static final int DEFAULT_NUM_OF_CLASSES_IN_APPLICATION = 1000;

	/**
	 * This is the default for the number of fields in the system.
	 */
	private static final int DEFAULT_NUM_OF_FIELDS_IN_APPLICATION = 3000;

	/**
	 * This is the default for the number of classes in the system.
	 */
	private static final int DEFAULT_NUM_OF_METHODS_IN_APPLICATION = 5000;

	static {
		final String _propFileName = System.getProperty(CONSTANTS_CONFIGURATION_FILE_PROPERTY);

		if (_propFileName != null) {
			try {
				final InputStream _stream = ClassLoader.getSystemResourceAsStream(_propFileName);
				CONFIGURATIONS.load(_stream);
			} catch (final IOException _e) {
				System.err.println("Well, error loading property file.  Bailing.");
				throw new RuntimeException(_e);
			}
		}
	}

	// /CLOVER:OFF

	/**
	 * <i>Prevents creation instances of this class.</i>
	 */
	@AEmpty private Constants() {
		// does nothing
	}

	// /CLOVER:ON

	/**
	 * Returns the approximate number of classes that the application may contain. This defaults to 1000.
	 * 
	 * @return the approximate number of classes that the application may contain.
	 */
	public static int getNumOfClassesInApplication() {
		final int _defaultValue = DEFAULT_NUM_OF_CLASSES_IN_APPLICATION;
		final String _key = NUM_OF_CLASSES_IN_APPLICATION_PROPERTY;
		final int _result;
		_result = retrieveIntValue(_defaultValue, _key, CONFIGURATIONS);
		return _result;
	}

	/**
	 * Returns the approximate number of fields that the application may contain. This defaults to 5000.
	 * 
	 * @return the approximate number of fields that the application may contain.
	 */
	public static int getNumOfFieldsInApplication() {
		final int _defaultValue = DEFAULT_NUM_OF_FIELDS_IN_APPLICATION;
		final String _key = NUM_OF_FIELDS_IN_APPLICATION_PROPERTY;
		final int _result;
		_result = retrieveIntValue(_defaultValue, _key, CONFIGURATIONS);
		return _result;
	}

	/**
	 * Returns the approximate number of methods that the application may contain. This defaults to 3000.
	 * 
	 * @return the approximate number of methods that the application may contain.
	 */
	public static int getNumOfMethodsInApplication() {
		final int _defaultValue = DEFAULT_NUM_OF_METHODS_IN_APPLICATION;
		final String _key = NUM_OF_METHODS_IN_APPLICATION_PROPERTY;
		final int _result;
		_result = retrieveIntValue(_defaultValue, _key, CONFIGURATIONS);
		return _result;
	}

	/**
	 * Retrieves the name of the root method trapper class. If not specified, it defaults to
	 * <code>edu.ksu.cis.indus.common.soot.RootMethodTrapper$MainMethodTrapper</code>.
	 * 
	 * @return the class name if specified.
	 * @post result != null
	 */
	public static String getRootMethodTrapperClassName() {
		String _result = CONFIGURATIONS.getProperty(ROOT_METHOD_TRAPPER_CLASS_PROPERTY);

		if (_result == null) {
			_result = "edu.ksu.cis.indus.common.soot.RootMethodTrapper$MainMethodTrapper";
		}
		return _result;
	}

	/**
	 * Retrieves the name of the statement graph factory class. If not specified, it defaults to
	 * <code>edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory</code>.
	 * 
	 * @return the class name if specified.
	 * @post result != null
	 */
	public static String getStmtGraphFactoryClassName() {
		String _result = CONFIGURATIONS.getProperty(STMT_GRAPH_FACTORY_CLASS_PROPERTY);

		if (_result == null) {
			_result = "edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory";
		}
		return _result;
	}

	/**
	 * Retrieves an integer constant. <i>This method is not for public use.</i>
	 * 
	 * @param defaultValue obviously.
	 * @param key of interest.
	 * @param props from which to retrieve the values.
	 * @return the constant.
	 * @throws NumberFormatException when configuration file is syntactically incorrect.
	 */
	public static int retrieveIntValue(final int defaultValue, final String key, final Properties props)
			throws NumberFormatException {
		final int _result;
		final String _temp = props.getProperty(key);

		if (_temp != null) {
			_result = Integer.parseInt(_temp);
		} else {
			_result = defaultValue;
		}
		return _result;
	}

	/**
	 * Checks if method bodies should be loaded during initialization. If not specified, it defaults to <code>false</code>.
	 * 
	 * @return <code>true</code> if the bodies should be loaded; <code>false</code>, otherwise.
	 */
	public static boolean shouldLoadMethodBodiesDuringInitialization() {
		final boolean _defaultValue = false;
		final boolean _result;
		final String _temp = CONFIGURATIONS.getProperty(LOAD_METHOD_BODIES_DURING_INITIALIZATION);

		if (_temp != null) {
			_result = Boolean.parseBoolean(_temp);
		} else {
			_result = _defaultValue;
		}
		return _result;
	}
}

// End of File
