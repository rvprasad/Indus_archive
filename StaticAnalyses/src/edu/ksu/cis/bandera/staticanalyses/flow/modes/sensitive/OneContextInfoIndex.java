
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive;

import edu.ksu.cis.bandera.staticanalyses.flow.IIndex;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * <p>
 * This class represents an index which can be differentiated based on a context of unit size.  We consider any peice of
 * information which can be used to divide summary sets, as context information.  So, a context can be made up of many such
 * peices of information.  This class can encapsulate only one such peice of context information.  For example, an instance
 * can encapsulate either the  calling stack or the program point as the context information, but not both.
 * </p>
 * Created: Fri Jan 25 13:11:19 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class OneContextInfoIndex
  implements IIndex {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(OneContextInfoIndex.class);

	/**
	 * <p>
	 * The context in which <code>value</code> needs to be differentiated.
	 * </p>
	 */
	private final Object CONTEXTINFO;

	/**
	 * <p>
	 * This index is used in association with <code>value</code>.  This value is not available for retrieval, but rather adds
	 * to improve the performance of <code>hashCode()</code> and <code>equals(Object)</code>.
	 * </p>
	 */
	private final Object VALUE;

	/**
	 * Cached stringified representation of this object.
	 */
	private final String STR;

	/**
	 * Cached hash code of this object.
	 */
	private final int HASHCODE;

	/**
	 * <p>
	 * Creates a new <code>OneContextInfoIndex</code> instance.
	 * </p>
	 *
	 * @param value the value whose variant is identified by this index.
	 * @param c the context in which <code>value</code>'s variant is identified by this index.
	 */
	public OneContextInfoIndex(Object value, Object c) {
		this.VALUE = value;
		this.CONTEXTINFO = c;
		STR = value + " " + c;
		HASHCODE = STR.hashCode();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Value: " + value + "  Context: " + CONTEXTINFO);
		}
	}

	/**
	 * <p>
	 * Compares this index with a given index.  The objects are equal when the <code>value</code> and <code>context</code>
	 * are equal.
	 * </p>
	 *
	 * @param index the index to be compared with.
	 *
	 * @return <code>true</code> if this index is equal to <code>index</code>; <code>false</code> otherwise.
	 */
	public boolean equals(Object index) {
		boolean temp = false;

		if (index instanceof OneContextInfoIndex) {
			OneContextInfoIndex d = (OneContextInfoIndex) index;
			temp = d.VALUE.equals(VALUE);

			if (CONTEXTINFO != null) {
				temp = temp && CONTEXTINFO.equals(d.CONTEXTINFO);
			}
		}
		return temp;
	}

	/**
	 * <p>
	 * Generates a hash code for this object.
	 * </p>
	 *
	 * @return the hash code for this object.
	 */
	public int hashCode() {
		return HASHCODE;
	}

	/**
	 * <p>
	 * Returns the stringized form of this object.
	 * </p>
	 *
	 * @return returns the stringized form of this object.
	 */
	public String toString() {
		return STR;
	}
}

/*****
 ChangeLog:

$Log$

*****/
