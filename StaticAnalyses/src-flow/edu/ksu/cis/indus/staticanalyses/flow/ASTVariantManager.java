
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.InvokeExpr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
