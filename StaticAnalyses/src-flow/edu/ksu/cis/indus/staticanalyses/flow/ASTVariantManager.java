
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
		IVariant result;

		if (o instanceof InvokeExpr) {
			InvokeExpr expr = (InvokeExpr) o;
			SootMethod sm = expr.getMethod();
			Map exception2node = new HashMap();

			/*
			 * for an invoke expression the exceptions thrown by the methods at run-time has to be a subset of those thrown
			 * by the static method mentioned in the invoke expression.  So, it suffices to create nodes for only the
			 *  exception classes mentioned at the invoke expression.
			 */
			for (Iterator i = sm.getExceptions().iterator(); i.hasNext();) {
				SootClass exception = (SootClass) i.next();
				exception2node.put(exception, fa.getNewFGNode());
			}
			result = new InvocationVariant(fa.getNewFGNode(), exception2node);
		} else {
			result = new ValuedVariant(fa.getNewFGNode());
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/16 21:50:51  venku
   Removed ASTVariant as it did not contain any data that was used.
   Concretized AbstractValuedVariant and renamed it to ValuedVariant.
   Ripple effect of the above change in some.
   Spruced up documentation and specification.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.7  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
