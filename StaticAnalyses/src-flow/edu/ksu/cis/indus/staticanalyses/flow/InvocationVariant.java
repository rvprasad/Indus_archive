
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

import soot.jimple.InvokeExpr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a variant for a method invocation expression.  It captures node information regarding return value
 * of the expression and the exceptions thrown by the expression.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InvocationVariant
  extends ASTVariant {
	/**
	 * This maps exception classes to nodes.
	 *
	 * @invariant exception2Node != null
	 */
	private final Map exception2node;

	/**
	 * Creates a new InvocationVariant object.
	 *
	 * @param e is the invoke expression being represented by this object.
	 * @param returnNode is the node associated with the return value of <code>e</code>.
	 * @param thrownExceptions2node is the map from class of exceptions thrown by <code>e</code>to nodes.
	 *
	 * @pre e != null and returnNode != null and thrownExceptions2node != null
	 */
	protected InvocationVariant(final InvokeExpr e, final IFGNode returnNode, final Map thrownExceptions2node) {
		super(e, returnNode);

		if (thrownExceptions2node.isEmpty()) {
			this.exception2node = Collections.EMPTY_MAP;
		} else {
			this.exception2node = new HashMap(thrownExceptions2node);
		}
	}

	/**
	 * Returns the node associated with given exception.
	 *
	 * @param exception is the class of the exception thrown by the expression associated with this variant.
	 *
	 * @return the node associated the exception thrown by the expression associated with this variant.
	 *
	 * @pre exception != null
	 */
	public IFGNode queryThrowNode(final SootClass exception) {
		return (IFGNode) exception2node.get(exception);
	}
}

/*
   ChangeLog:

   $Log$

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
