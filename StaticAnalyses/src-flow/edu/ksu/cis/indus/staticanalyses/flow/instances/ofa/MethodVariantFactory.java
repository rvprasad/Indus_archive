
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.ASTVariantManager;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariantFactory;

import soot.SootMethod;


/**
 * This implementation creates instances of <code>MethodVariant</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class MethodVariantFactory
  implements IMethodVariantFactory {
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IMethodVariantFactory#create(soot.SootMethod,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ASTVariantManager, edu.ksu.cis.indus.staticanalyses.flow.FA)
	 */
	public IMethodVariant create(final SootMethod sootMethod, final ASTVariantManager astVM, final FA fa) {
		return new MethodVariant(sootMethod, astVM, fa);
	}
}

// End of File
