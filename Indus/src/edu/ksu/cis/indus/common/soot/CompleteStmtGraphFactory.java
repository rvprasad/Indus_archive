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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;

import soot.jimple.JimpleBody;
import soot.toolkits.graph.CompleteUnitGraph;

/**
 * This class provides <code>soot.toolkits.graph.CompleteUnitGraph</code>s.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompleteStmtGraphFactory
		extends AbstractStmtGraphFactory<CompleteUnitGraph> {

	/**
	 * Creates an instance of this class.
	 */
	@Empty public CompleteStmtGraphFactory() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override protected CompleteUnitGraph getStmtGraphForBody(@Immutable final JimpleBody body) {
		return new CompleteUnitGraph(body);
	}
}

// End of File
