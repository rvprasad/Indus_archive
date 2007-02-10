/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;

/**
 * This is data structure class to represent strongly connected component data. Each node may be associated with 0 or 1
 * instance of this class. Please refer to <code>AbstractDirectedGraph</code> to understand how this class is used.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SCCRelatedData {

	/**
	 * The number of the component to which the associated node belongs to.
	 */
	private int componentNum;

	/**
	 * The DFS number of the node associated with this data.
	 */
	private int dfsNum;

	/**
	 * The high number of the node associated with this data.
	 */
	private int high;

	/**
	 * Creates an instance of this class.
	 */
	@Empty public SCCRelatedData() {
		super();
	}

	/**
	 * Retrieves the value in <code>componentNum</code>.
	 * 
	 * @return the value in <code>componentNum</code>.
	 */
	@Functional public int getComponentNum() {
		return componentNum;
	}

	/**
	 * Retrieves the value in <code>dfsNum</code>.
	 * 
	 * @return the value in <code>dfsNum</code>.
	 */
	@Functional public int getDfsNum() {
		return dfsNum;
	}

	/**
	 * Retrieves the value in <code>high</code>.
	 * 
	 * @return the value in <code>high</code>.
	 */
	@Functional public int getHigh() {
		return high;
	}

	/**
	 * Sets the value of <code>componentNum</code>.
	 * 
	 * @param num the new value of <code>componentNum</code>.
	 */
	public void setComponentNum(final int num) {
		this.componentNum = num;
	}

	/**
	 * Sets the value of <code>dfsNum</code>.
	 * 
	 * @param num the new value of <code>dfsNum</code>.
	 */
	public void setDfsNum(final int num) {
		this.dfsNum = num;
	}

	/**
	 * Sets the value of <code>high</code>.
	 * 
	 * @param num the new value of <code>high</code>.
	 */
	public void setHigh(final int num) {
		this.high = num;
	}
}

// End of File
