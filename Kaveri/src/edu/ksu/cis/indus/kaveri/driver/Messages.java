/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

/*
 * Created on Jun 3, 2004
 *
 */
package edu.ksu.cis.indus.kaveri.driver;

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
    private static final String BUNDLE_NAME = "edu.ksu.cis.indus.kaveri.driver.messages"; //$NON-NLS-1$

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
            _retString = '!' + key + '!';
        }
        return _retString;
    }
}
