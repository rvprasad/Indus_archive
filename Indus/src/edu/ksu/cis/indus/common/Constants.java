
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

package edu.ksu.cis.indus.common;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;


/**
 * This class contains a set of constants that are usually dependent on the usage or application scenario.  Usually, at a
 * time, people work on applications of comparable sizes, i.e., number of methods, number of classes, etc. Hence, definition
 * of common threshold depending on the scenario can optimize performance.  This class caters such thresholds.
 * 
 * <p>
 * The user can configure the values returned by the getter methods in this class can configured by specifying a properties
 * file via <code>indus.constant.configuration.properties.file</code> property.  The name of the properties are governed by
 * the available getter methods.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Constants {
	/** 
	 * This is the name of the property via which the constants properties file can be specified.
	 */
	public static final String CONSTANTS_CONFIGURATION_FILE = "indus.constant.configuration.properties.file";

	///CLOVER:ON

	/** 
	 * This contains the constants.
	 */
	private static Properties configuration = new Properties();

	static {
		final String _propFileName = System.getProperty(CONSTANTS_CONFIGURATION_FILE);

		if (_propFileName != null) {
			try {
				final InputStream _stream = ClassLoader.getSystemResourceAsStream(_propFileName);
				configuration.load(_stream);
			} catch (IOException _e) {
				System.err.println("Well, error loading property file.  Bailing.");
				throw new RuntimeException(_e);
			}
		}
	}

	/** 
	 * This constant serves as the key for the value corresponding to the number of methods in the system.
	 */
	private static final String NUM_OF_METHODS_IN_APPLICATION = "NumOfMethodsInApplication";

	/** 
	 * This is the default for the number of classes in the system.
	 */
	private static final int DEFAULT_NUM_OF_METHODS_IN_APPLICATION = 5000;

	/** 
	 * This constant serves as the key for the value corresponding to the number of classes in the system.
	 */
	private static final String NUM_OF_CLASSES_IN_APPLICATION = "NumOfClassesInApplication";

	/** 
	 * This is the default for the number of classes in the system.
	 */
	private static final int DEFAULT_NUM_OF_CLASSES_IN_APPLICATION = 1000;

	/** 
	 * This constant serves as the key for the value corresponding to the number of fields in the system.
	 */
	private static final String NUM_OF_FIELDS_IN_APPLICATION = "NumOfFieldsInApplication";

	/** 
	 * This is the default for the number of fields in the system.
	 */
	private static final int DEFAULT_NUM_OF_FIELDS_IN_APPLICATION = 3000;

	///CLOVER:OFF

	/**
	 * <i>Prevents creation instances of this class.</i>
	 */
	private Constants() {
	}

	///CLOVER:ON
	 
	/**
	 * Returns the approximate number of classes that the application may contain.  This defaults to 1000.
	 *
	 * @return the approximate number of classes that the application may contain.
	 */
	public static int getNumOfClassesInApplication() {
		final int _defaultValue = DEFAULT_NUM_OF_CLASSES_IN_APPLICATION;
		final String _key = NUM_OF_CLASSES_IN_APPLICATION;
		final int _result;
		_result = retrieveIntValue(_defaultValue, _key);
		return _result;
	}

	/**
	 * Returns the approximate number of fields that the application may contain.  This defaults to 5000.
	 *
	 * @return the approximate number of fields that the application may contain.
	 */
	public static int getNumOfFieldsInApplication() {
		final int _defaultValue = DEFAULT_NUM_OF_FIELDS_IN_APPLICATION;
		final String _key = NUM_OF_FIELDS_IN_APPLICATION;
		final int _result;
		_result = retrieveIntValue(_defaultValue, _key);
		return _result;
	}

	/**
	 * Returns the approximate number of methods that the application may contain.  This defaults to 3000.
	 *
	 * @return the approximate number of methods that the application may contain.
	 */
	public static int getNumOfMethodsInApplication() {
		final int _defaultValue = DEFAULT_NUM_OF_METHODS_IN_APPLICATION;
		final String _key = NUM_OF_METHODS_IN_APPLICATION;
		final int _result;
		_result = retrieveIntValue(_defaultValue, _key);
		return _result;
	}

	/**
	 * Retrieves an integer constant.
	 *
	 * @param defaultValue obviously.
	 * @param key of interest.
	 *
	 * @return the constant.
	 *
	 * @throws NumberFormatException when configuration file is syntactically incorrect.
	 */
	private static int retrieveIntValue(final int defaultValue, final String key)
	  throws NumberFormatException {
		final int _result;
		final String _temp = configuration.getProperty(key);

		if (_temp != null) {
			_result = Integer.parseInt(_temp);
		} else {
			_result = defaultValue;
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/08/08 10:11:39  venku
   - added a new class to configure constants used when creating data structures.
   - ripple effect.
 */
