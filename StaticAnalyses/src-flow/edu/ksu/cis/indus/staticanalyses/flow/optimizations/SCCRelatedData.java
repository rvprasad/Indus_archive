
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.optimizations;

/**
 * This is data structure class to represent strongly connected component data used by flow analysis framework.  Each node
 * may be associated with 0 or 1 instance of this class.  Please refer to <code>SCCBasedOptimizer</code> to know how this
 * class is used.
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
	 * Sets the value of <code>componentNum</code>.
	 *
	 * @param num the new value of <code>componentNum</code>.
	 */
	void setComponentNum(final int num) {
		this.componentNum = num;
	}

	/**
	 * Retrieves the value in <code>componentNum</code>.
	 *
	 * @return the value in <code>componentNum</code>.
	 */
	int getComponentNum() {
		return componentNum;
	}

	/**
	 * Sets the value of <code>dfsNum</code>.
	 *
	 * @param num the new value of <code>dfsNum</code>.
	 */
	void setDfsNum(final int num) {
		this.dfsNum = num;
	}

	/**
	 * Retrieves the value in <code>dfsNum</code>.
	 *
	 * @return the value in <code>dfsNum</code>.
	 */
	int getDfsNum() {
		return dfsNum;
	}

	/**
	 * Sets the value of <code>high</code>.
	 *
	 * @param num the new value of <code>high</code>.
	 */
	void setHigh(final int num) {
		this.high = num;
	}

	/**
	 * Retrieves the value in <code>high</code>.
	 *
	 * @return the value in <code>high</code>.
	 */
	int getHigh() {
		return high;
	}
}

// End of File
