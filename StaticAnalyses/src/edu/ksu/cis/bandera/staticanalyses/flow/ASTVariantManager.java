
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.Value;

import ca.mcgill.sable.util.Iterator;

import java.util.HashMap;
import java.util.Map;


/**
 * This class provides the logic to create new variants of AST nodes.  Created: Tue Jan 22 12:46:24 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ASTVariantManager
  extends AbstractVariantManager {
	/**
	 * Creates a new <code>ASTVariantManager</code> instance.
	 *
	 * @param bfa the instance of the framework in which this instance exists.
	 * @param indexManager the manager that shall provide the indices to lookup the variants.
	 */
	ASTVariantManager(BFA bfa, AbstractIndexManager indexManager) {
		super(bfa, indexManager);
	}

	/**
	 * Returns a new variant representing the given AST node.
	 *
	 * @param o the AST node, <code>Value</code> object, to be represented by the returned variant.
	 *
	 * @return the variant representing the AST node, <code>o</code>.
	 *
	 * @post o.oclIsKindOf(InvokeExpr) implies result.oclType = InvocationVariant
	 * @post (not o.oclIsKindOf(InvokeExpr)) implies result.oclType = ASTVariant
	 */
	protected Variant getNewVariant(Object o) {
		Variant result;

		if(o instanceof InvokeExpr) {
			InvokeExpr expr = (InvokeExpr) o;
			SootMethod sm = expr.getMethod();
			Map exception2node = new HashMap();
			// for an invoke expression the exceptions thrown by the methods at run-time has to be a subset of those thrown 
			// by the static method mentioned in the invoke expression.  So, it suffices to create nodes for only the 
			// exception classes mentioned at the invoke expression.
			for(Iterator i = sm.getExceptions().iterator(); i.hasNext();) {
				SootClass exception = (SootClass) i.next();
				exception2node.put(exception, bfa.getNewFGNode());
			}
			result = new InvocationVariant(expr, bfa.getNewFGNode(), exception2node);
		} else {
			result = new ASTVariant((Value) o, bfa.getNewFGNode());
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
