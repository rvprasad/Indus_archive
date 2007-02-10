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

package edu.ksu.cis.indus.processing;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

/**
 * This implementation provides infrastructure to store information pertaining to higher-level structures (stmt graph or basic
 * block graph) based on which concrete implementation can generate statement sequences. <i>The user should call atleast one
 * of <code>setBbgFactory()</code> or <code>setStmtGraphFactory()</code> with a non-null argument before using an instance
 * of this class.</i>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractStmtSequenceRetriever
		implements IStmtSequencesRetriever {

	/**
	 * The basic block graph factory to use.
	 */
	private BasicBlockGraphMgr bbgFactory;

	/**
	 * The statement graph factory to use.
	 */
	private IStmtGraphFactory<?> stmtGraphFactory;

	/**
	 * Sets the value of <code>bbgMgr</code>.
	 * 
	 * @param factory the new value of <code>bbgMgr</code>.
	 */
	public final void setBbgFactory(final BasicBlockGraphMgr factory) {
		this.bbgFactory = factory;
	}

	/**
	 * Sets the value of <code>stmtGraphFactory</code>.
	 * 
	 * @param factory the new value of <code>stmtGraphFactory</code>.
	 */
	public final void setStmtGraphFactory(final IStmtGraphFactory<?> factory) {
		this.stmtGraphFactory = factory;
	}

	/**
	 * Retrieves the factory that provides basic block graphs.
	 * 
	 * @return basic block graph factory.
	 */
	protected final BasicBlockGraphMgr getBbgFactory() {
		return bbgFactory;
	}

	/**
	 * Retrieves the factory that provides statement graph factory.
	 * 
	 * @return statement graph factory.
	 */
	protected final IStmtGraphFactory<?> getStmtGraphFactory() {
		return stmtGraphFactory;
	}
}

// End of File
