
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.ASTVariantManager;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariantFactory;

import java.util.regex.Pattern;

import soot.SootMethod;


/**
 * This implementation creates instances of <code>MethodVariant</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class MethodVariantFactory
  implements IMethodVariantFactory<OFAFGNode, FlowInsensitiveExprSwitch, FlowInsensitiveExprSwitch, StmtSwitch> {
	/** 
	 * The pattern used to decide if a stub variant or a complete variant needs to be returned during <code>create()</code>
	 * call. 
	 */
	private final Pattern pattern;

	/**
	 * Creates an instance of this class.
	 */
	public MethodVariantFactory() {
		final String _p = Constants.getFAScopePattern();

		if (_p != null) {
			pattern = Pattern.compile(_p);
		} else {
			pattern = null;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IMethodVariantFactory#create(soot.SootMethod,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ASTVariantManager, edu.ksu.cis.indus.staticanalyses.flow.FA)
	 */
	public IMethodVariant<OFAFGNode, FlowInsensitiveExprSwitch, FlowInsensitiveExprSwitch, StmtSwitch> create(final SootMethod sootMethod, final ASTVariantManager<OFAFGNode> astVM, final FA fa) {
		final IMethodVariant<OFAFGNode, FlowInsensitiveExprSwitch, FlowInsensitiveExprSwitch, StmtSwitch> _result;

		if (pattern == null || pattern.matcher(sootMethod.getDeclaringClass().getName()).matches()) {
			_result = new MethodVariant(sootMethod, astVM, fa);
		} else {
			_result = new StubMethodVariant(sootMethod, astVM, fa);
		}
		return _result;
	}
}

// End of File
