/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author ganeshan
 * 
 * Logs any exceptions that are generated.
 */
public final class KaveriErrorLog {

    /**
     * Constructor.
     */
    private KaveriErrorLog() {

    }

    /**
     * Log the given exception.
     * 
     * @param message
     *            The message associated with the exception.
     * @param exception
     *            The exception to be logged.
     */
    public static void logException(final String message,
            final Throwable exception) {
        addToLog(IStatus.ERROR, IStatus.OK, message, exception);
    }

    /**
     * Log the given exception as an information.
     * 
     * @param message
     *            The message associated with the exception.
     * @param exception
     *            The exception to be logged,
     */
    public static void logInformation(final String message,
            final Throwable exception) {
        addToLog(IStatus.INFO, IStatus.OK, message, exception);
    }

    /**
     * Log the given message.
     * 
     * @param message
     *            The message associated with the exception.
     *  
     */
    public static void logInformation(final String message) {
        addToLog(IStatus.INFO, IStatus.OK, message, null);
    }

    /**
     * 
     * Add the message to the log.
     * 
     * @param error
     *            The error.
     * @param errorCode
     *            The error code
     * @param message
     *            The message
     * @param exception
     *            The exception
     */
    private static void addToLog(final int error, final int errorCode,
            final String message, final Throwable exception) {
        final ILog _log = KaveriPlugin.getDefault().getLog();
        final String _pluginid = KaveriPlugin.getDefault().getBundle()
                .getSymbolicName();
        final IStatus _status = new Status(error, _pluginid, errorCode,
                message, exception);
        _log.log(_status);
    }
}
