/*
 *
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