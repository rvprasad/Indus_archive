
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

package edu.ksu.cis.indus.staticanalyses.flow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.InvokeExpr;


/**
 * This class provides the logic to create new variants of AST nodes.
 * 
 * <p>
 * Created: Tue Jan 22 12:46:24 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ASTVariantManager
  extends AbstractVariantManager {
	/**
	 * Creates a new <code>ASTVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this instance exists.
	 * @param indexManager the manager that shall provide the indices to lookup the variants.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	ASTVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new variant representing the given AST node.
	 *
	 * @param o the AST node, <code>Value</code> object, to be represented by the returned variant.
	 *
	 * @return the variant representing the AST node, <code>o</code>.
	 *
	 * @pre o != null
	 * @post o.oclIsKindOf(InvokeExpr) implies result.oclType = InvocationVariant
	 * @post (not o.oclIsKindOf(InvokeExpr)) implies result.oclType = ASTVariant
	 */
	protected IVariant getNewVariant(final Object o) {
		final IVariant _result;

		if (o instanceof InvokeExpr) {
			final InvokeExpr _expr = (InvokeExpr) o;
			final SootMethod _sm = _expr.getMethod();
			final Map _exception2node = new HashMap();

			/*
			 * for an invoke expression the exceptions thrown by the methods at run-time has to be a subset of those thrown
			 * by the static method mentioned in the invoke expression.  So, it suffices to create nodes for only the
			 *  exception classes mentioned at the invoke expression.
			 */
			for (final Iterator _i = _sm.getExceptions().iterator(); _i.hasNext();) {
				final SootClass _exception = (SootClass) _i.next();
				_exception2node.put(_exception, fa.getNewFGNode());
			}
			_result = new InvocationVariant(fa.getNewFGNode(), _exception2node);
		} else {
			_result = new ValuedVariant(fa.getNewFGNode());
		}
		return _result;
	}
}

// End of File
