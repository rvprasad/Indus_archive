/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.SootMethod;

/**
 * This is factory interface to create method variants in the flow analysis framework.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <N> DOCUMENT ME!
 * @param <LE> DOCUMENT ME!
 * @param <RE> DOCUMENT ME!
 * @param <SS> DOCUMENT ME!
 */
public interface IMethodVariantFactory<N extends IFGNode<N, ?>, LE extends IExprSwitch<LE, N>, RE extends IExprSwitch<RE, N>, SS extends IStmtSwitch<SS>> {

	/**
	 * Creates a method variant.
	 * 
	 * @param sootMethod for which the method variant needs to be created.
	 * @param astVM to be used by the created method variant.
	 * @param fa is the flow analysis framework instance in which the created variant operates.
	 * @return a method variant
	 * @pre sootMethod != null and fa != null
	 */
	IMethodVariant<N, LE, RE, SS> create(SootMethod sootMethod, ASTVariantManager<N> astVM, FA fa);
}

// End of File
