	
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
 * Created on Jul 26, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.preferencedata;

import org.eclipse.jface.text.Position;


/**
 * This class encapsulates the line number to be annotated.
 *
 * @author ganeshan
 */
public class AnnotationData {
	/** 
	 * <p>
	 * The position of this line in the editor.
	 * </p>
	 */
	private Position position;

	/** 
	 * <p>
	 * Indicates if the statement in the given line number is fully or partially in the slice.
	 * </p>
	 */
	private boolean complete;

	/**
	 * The class is which this line is present.
	 */
	private String className;
	
	/**
	 * The name of the method in which the line is present.
	 */
	private String methodName;
	/** 
	 * <p>
	 * The line number present in the slice.
	 * </p>
	 */
	private int nLineNumber;

	/**
	 * Constructor.
	 */
	public AnnotationData() {
	}

	/**
	 * Sets the statement as completely  or partially in the slice.
	 *
	 * @param bcomplete The value to set.
	 */
	public void setComplete(final boolean bcomplete) {
		this.complete = bcomplete;
	}

	/**
	 * Returns if the statement is completely or partially in the slice.
	 *
	 * @return boolean returns if the line number is completely or
	 * partially present in the slice.
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Sets the line number.
	 *
	 * @param lineNumber The nLineNumber to set.
	 */
	public void setNLineNumber(final int lineNumber) {
		nLineNumber = lineNumber;
	}

	/**
	 * Returns the Line number.
	 *
	 * @return Returns the nLineNumber.
	 */
	public int getNLineNumber() {
		return nLineNumber;
	}

	/**
	 * Sets the position.
	 *
	 * @param theposition The position to set.
	 */
	public void setPosition(final Position theposition) {
		this.position = theposition;
	}

	/**
	 * Returns the position.
	 *
	 * @return Returns the position.
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Determines if the objects are equal.
	 *
	 * @param obj The object to be compared
	 *
	 * @return boolean true if the objects are equal
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj) {
		boolean _result = false;

		if (obj instanceof AnnotationData) {
			final AnnotationData _tData = (AnnotationData) obj;

			if (_tData.getNLineNumber() == nLineNumber) {
				_result = true;
			}
		}
		return _result;
	}

	/**
	 * The hashcode for the object.
	 * 
	 * @return int The hash code.
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return nLineNumber;
	}
	/**
	 * Returns the class name.
	 * @return String The classname
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * Sets the classname.
	 * @param strclassName The className.
	 */
	public void setClassName(final String strclassName) {
		this.className = strclassName;
	}
	/**
	 * Returns the methodname.
	 * @return String The method name
	 */
	public String getMethodName() {
		return methodName;
	}
	/**
	 * Sets the method name.
	 * @param strmethodName The method name
	 */
	public void setMethodName(final String strmethodName) {
		this.methodName = strmethodName;
	}
}
