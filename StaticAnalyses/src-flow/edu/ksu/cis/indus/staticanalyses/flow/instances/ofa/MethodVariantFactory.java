
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

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.staticanalyses.flow.ASTVariantManager;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariantFactory;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.regex.Pattern;

import soot.SootMethod;
import soot.Type;
import soot.Value;


/**
 * This implementation creates instances of <code>MethodVariant</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T>  is the type of the token set object.
 */
class MethodVariantFactory<T extends ITokens<T, Value>>
  implements IMethodVariantFactory<Value, T, OFAFGNode<T>, Type> {
	/**
	 * The pattern used to decide if a stub variant or a complete variant needs to be returned during <code>create()</code>
	 * call.
	 */
	private final Pattern pattern;

	/**
	 * The statement graph to use to retrieve method bodies.
	 */
	private final IStmtGraphFactory<?> stmtGraphFactory;

	/**
	 * Creates an instance of this class.
	 *
	 * @param actualBodyScopePattern the scope in which the variants are based on actual body.
	 * @param factory provides the statement graphs to be used construct method variants.
	 * @pre factory != null
	 */
	public MethodVariantFactory(final String actualBodyScopePattern, final IStmtGraphFactory<?> factory) {
		if (actualBodyScopePattern != null) {
			pattern = Pattern.compile(actualBodyScopePattern);
		} else {
			pattern = null;
		}
		stmtGraphFactory = factory;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IMethodVariantFactory#create(soot.SootMethod,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ASTVariantManager, edu.ksu.cis.indus.staticanalyses.flow.FA)
	 */
	public IMethodVariant<OFAFGNode<T>> create(final SootMethod sootMethod,
			final ASTVariantManager<Value, T, OFAFGNode<T>, Type> astVM, final FA<Value, T, OFAFGNode<T>, Type> fa) {
		final IMethodVariant<OFAFGNode<T>> _result;

		if (pattern == null || pattern.matcher(sootMethod.getDeclaringClass().getName()).matches()) {
			_result = new MethodVariant<T>(sootMethod, astVM, fa, stmtGraphFactory);
		} else {
			_result = new StubMethodVariant<T>(sootMethod, astVM, fa);
		}
		return _result;
	}
}

// End of File
