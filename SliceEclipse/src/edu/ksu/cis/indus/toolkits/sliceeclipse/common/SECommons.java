
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

package edu.ksu.cis.indus.toolkits.sliceeclipse.common;

import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.ExceptionDialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.swt.widgets.Display;


/**
 * Common functions to be used throughout. Makes for centralized management of parameter validation.
 *
 * @author ganeshan
 */
public final class SECommons {
	/**
	 * Constructor.
	 */
	private SECommons() {
	}

	/**
	 * Checks if the object is an instance of the specified class.
	 *
	 * @param objChk The object to be checked.
	 * @param classChk The class to be checked against.
	 *
	 * @return boolean True if the object is of the specified class.
	 */
	public static boolean checkForClassEquality(final Object objChk, final Class classChk) {
		boolean _returnVal = false;

		if (!(checkForNull(objChk) && checkForNull(classChk))) {
			_returnVal = classChk.isInstance(objChk);
		}
		return _returnVal;
	}

	/**
	 * Returns true if the object is null.
	 *
	 * @param objChk The object to be checked for nullness.
	 *
	 * @return boolean True if the object is null.
	 */
	public static boolean checkForNull(final Object objChk) {
		boolean _retVal = false;

		if (objChk == null) {
			_retVal = true;
		} else {
			_retVal = false;
		}
		return _retVal;
	}
	
	/**
	 * Handles Exceptions of all sorts.
	 * @param exception The exception to be handled.
	 */
	public static void handleException(final Exception exception) {
		final StringWriter _sw = new StringWriter();
		final PrintWriter _pw = new PrintWriter(_sw);
		exception.printStackTrace(_pw);

		final ExceptionDialog _ed = new ExceptionDialog(Display
				.getDefault().getActiveShell(), _sw.getBuffer()
				.toString());
		_ed.open();		
	}

}
