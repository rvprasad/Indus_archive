
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

import java.util.List;


/**
 * The set of views.
 *
 * @author Ganeshan 
 */
public class ViewConfiguration {
	/** 
	 * The list of ViewData.
	 */
	private List list;

	/**
	 * Sets the list of views.
	 *
	 * @param thelist The list to set.
	 */
	public void setList(final List thelist) {
		this.list = thelist;
	}

	/**
	 * Get the set of views.
	 *
	 * @return Returns the list.
	 */
	public List getList() {
		return list;
	}
}
