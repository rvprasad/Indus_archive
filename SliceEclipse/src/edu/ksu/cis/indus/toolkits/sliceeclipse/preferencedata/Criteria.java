
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
package edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata;

import java.util.ArrayList;


/**
 * The criteria.
 *
 * @author Ganeshan
 */
public class Criteria {
	/** 
	 * The list of classname, method name, line number, index and value consideration.
	 */
	private ArrayList criteria;
	
	/**
	 * The class name.
	 */
	private String strClassName;
	
	/**
	 * The method name.
	 */
	private String strMethodName;
	
	/**
	 * The Java line number.
	 */
	private int nLineNo;
	
	/**
	 * The index of the Jimple Stmt.
	 */
	private int nJimpleIndex;
	
	/**
	 * Consider the value.
	 */
	private boolean bConsiderValue;
	
	
	/** 
	 * Enabled or not.
	 */
	private boolean disabled;

	/**
	 * Sets the criteria.
	 * @deprecated
	 * @param criterias The criteria to set.
	 */
	public void setCriteria(final ArrayList criterias) {
		this.criteria = criterias;
	}

	/**
	 * Get the criteria set.
	 * @deprecated
	 * @return Returns the criteria.
	 */
	public ArrayList getCriteria() {
		return criteria;
	}

	/**
	 * Criteria Disabled.
	 *
	 * @param setdisable The disabled to set.
	 */
	public void setDisabled(final boolean setdisable) {
		this.disabled = setdisable;
	}

	/**
	 * Is criteria disabled?
	 *
	 * @return Returns the disabled.
	 */
	public boolean isDisabled() {
		return disabled;
	}

	
	/**
	 * @return Returns the bConsiderValue.
	 */
	public boolean isBConsiderValue() {
		return bConsiderValue;
	}
	/**
	 * @param considerValue The bConsiderValue to set.
	 */
	public void setBConsiderValue(final boolean considerValue) {
		bConsiderValue = considerValue;
	}
	/**
	 * @return Returns the nJimpleIndex.
	 */
	public int getNJimpleIndex() {
		return nJimpleIndex;
	}
	/**
	 * @param jimpleIndex The nJimpleIndex to set.
	 */
	public void setNJimpleIndex(final int jimpleIndex) {
		nJimpleIndex = jimpleIndex;
	}
	/**
	 * @return Returns the nLineNo.
	 */
	public int getNLineNo() {
		return nLineNo;
	}
	/**
	 * @param lineNo The nLineNo to set.
	 */
	public void setNLineNo(final int lineNo) {
		nLineNo = lineNo;
	}
	/**
	 * @return Returns the strClassName.
	 */
	public String getStrClassName() {
		return strClassName;
	}
	/**
	 * @param strClass The strClassName to set.
	 */
	public void setStrClassName(final String strClass) {
		this.strClassName = strClass;
	}
	/**
	 * @return Returns the strMethodName.
	 */
	public String getStrMethodName() {
		return strMethodName;
	}
	/**
	 * @param strMethod The strMethodName to set.
	 */
	public void setStrMethodName(final String strMethod) {
		this.strMethodName = strMethod;
	}
}
