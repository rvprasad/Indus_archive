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

/*
 * Created on Jun 24, 2004
 *
 */
package edu.ksu.cis.indus.kaveri.execute;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message Binder.
 * 
 * @author Ganeshan 
 */
 public final class Messages {
	/**
	 * <p>
	 * Bundle name.
	 * </p>
	 */
	private static final String BUNDLE_NAME = "edu.ksu.cis.indus.kaveri.execute.messages"; //$NON-NLS-1$

	/**
	 * <p>
	 * ResourceBundle.
	 * </p>
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * Creates a new Messages object.
	 */
	private Messages() {
	}

	/**
	 * 
	 * <p>
	 * Returns the correct string.
	 * </p>
	 * 
	 * @param key
	 *            The index
	 * 
	 * @return String The key value
	 */
	public static String getString(final String key) {
		String _retString = null;
		try {
			_retString = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException _mre) {
			_retString =  '!' + key + '!';
		}
		return _retString;
	}
}