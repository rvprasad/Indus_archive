
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
 * DOCUMENT ME!
 *
 * @author Ganeshan
 */
public class Criteria {
	/** 
	 * The list of criteria associated with the project.
	 */
	private ArrayList criteria;

	/** 
	 * The filename.
	 */
	private String fileName;  // The java fileName

	/** 
	 * Enabled or not.
	 */
	private boolean disabled;

	/**
	 * Sets the criteria.
	 *
	 * @param criterias The criteria to set.
	 */
	public void setCriteria(final ArrayList criterias) {
		this.criteria = criterias;
	}

	/**
	 * Get the criteria set.
	 *
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
	 * Set the filename.
	 *
	 * @param javafileName The fileName to set.
	 */
	public void setFileName(final String javafileName) {
		fileName = javafileName;
	}

	/**
	 * Get the filename.
	 *
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}
}
