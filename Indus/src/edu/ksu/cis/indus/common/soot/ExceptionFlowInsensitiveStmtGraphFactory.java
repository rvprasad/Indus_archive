/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.ksu.cis.indus.common.soot;

import soot.jimple.JimpleBody;
import soot.toolkits.graph.BriefUnitGraph;

/**
 * This class provides <code>BriefUnitGraph</code>s. These graphs do not capture control flow due to exceptions.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ExceptionFlowInsensitiveStmtGraphFactory
		extends AbstractStmtGraphFactory<BriefUnitGraph> {

	/**
	 * Creates an instance of this class.
	 */
	public ExceptionFlowInsensitiveStmtGraphFactory() {
		super();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractStmtGraphFactory#getStmtGraphForBody(soot.jimple.JimpleBody)
	 */
	@Override protected BriefUnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new BriefUnitGraph(body);
	}

}

// End of File
