
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
 * Created on May 27, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.preferencedata;

/**
 * Encapsulates the chosen criteria.
 *
 * @author Ganeshan
 */
public class Criteria {
	/** 
	 * The class name.
	 */
	private String strClassName;

	/** 
	 * The method name.
	 */
	private String strMethodName;

	/** 
	 * Consider the exceution.
	 */
	private boolean bConsiderValue;

	/**
	 * The serialized criteria.
	 */
	private String criteriaSpec;
	
	
	/** 
	 * The index of the Jimple Stmt.
	 */
	private int nJimpleIndex;

	/** 
	 * The Java line number.
	 */
	private int nLineNo;

	/**
	 * Set consider excecution to considerValue.
	 *
	 * @param considerValue The bConsiderValue to set.
	 */
	public void setBConsiderValue(final boolean considerValue) {
		bConsiderValue = considerValue;
	}

	/**
	 * Returns whether consider execution is enabled.
	 *
	 * @return Returns the bConsiderValue.
	 */
	public boolean isBConsiderValue() {
		return bConsiderValue;
	}

	/**
	 * Sets the Jimple index.
	 *
	 * @param jimpleIndex The nJimpleIndex to set.
	 */
	public void setNJimpleIndex(final int jimpleIndex) {
		nJimpleIndex = jimpleIndex;
	}

	/**
	 * Get the chosen Jimple index.
	 *
	 * @return int Returns the index.
	 */
	public int getNJimpleIndex() {
		return nJimpleIndex;
	}

	/**
	 * Sets the line number.
	 *
	 * @param lineNo The line number.
	 */
	public void setNLineNo(final int lineNo) {
		nLineNo = lineNo;
	}

	/**
	 * Returns the line number.
	 *
	 * @return int The line number.
	 */
	public int getNLineNo() {
		return nLineNo;
	}

	/**
	 * Sets the classname to strClass.
	 *
	 * @param strClass The class name
	 */
	public void setStrClassName(final String strClass) {
		this.strClassName = strClass;
	}

	/**
	 * Returns the class name in which the criteria is present.
	 *
	 * @return String The classname
	 */
	public String getStrClassName() {
		return strClassName;
	}

	/**
	 * Sets the method name to strMethod.
	 *
	 * @param strMethod The method name.
	 */
	public void setStrMethodName(final String strMethod) {
		this.strMethodName = strMethod;
	}

	/**
	 * Returns the name of the method in which the criteria is present.
	 *
	 * @return String The method name.
	 */
	public String getStrMethodName() {
		return strMethodName;
	}
	
	/**
	 * Returns the serialized criteria string.
	 * @return Returns the criteriaSpec.
	 */
	public String getCriteriaSpec() {
		return criteriaSpec;
	}
	
	/**
	 * Sets the serialized form of the criteria.
	 * @param criteriaSpec The criteriaSpec to set.
	 */
	public void setCriteriaSpec(String criteriaSpec) {
		this.criteriaSpec = criteriaSpec;
	}
}
