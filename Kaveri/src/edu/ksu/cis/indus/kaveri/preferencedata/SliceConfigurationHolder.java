
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

import java.util.List;


/**
 * This class holds the set of configurations used by the plugin. Configurations are stored as a list of Strings
 * corresponding to the configurations.
 *
 * @author Ganeshan
 */
public class SliceConfigurationHolder {
	/** 
	 * The list of configurations.
	 */
	private List list;

	/**
	 * Sets the list of configurations.
	 *
	 * @param lists The configurations to set
	 */
	public void setList(final List lists) {
		this.list = lists;
	}

	/**
	 * Gets the list of configuration.
	 *
	 * @return List Returns the list.
	 */
	public List getList() {
		return list;
	}
}
