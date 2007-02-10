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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Empty;

import soot.jimple.JimpleBody;
import soot.toolkits.graph.TrapUnitGraph;

/**
 * This class provides <code>soot.toolkits.graph.TrapUnitGraph</code>s.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TrapStmtGraphFactory
		extends AbstractStmtGraphFactory<TrapUnitGraph> {

	/**
	 * Creates an instance of this class.
	 */
	@Empty public TrapStmtGraphFactory() {
		super();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected TrapUnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new TrapUnitGraph(body);
	}
}

// End of File
