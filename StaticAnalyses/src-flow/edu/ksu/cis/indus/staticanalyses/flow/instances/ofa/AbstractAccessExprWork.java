
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import soot.ValueBox;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * This class encapsulates the logic and data related to work to be done in correspondence to access expressions.
 * 
 * <p>
 * Created: Tue Jan 22 04:27:47 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractAccessExprWork
  extends AbstractWork {
	/**
	 * The context in which the access occurs.
	 *
	 * @invariant context != null
	 */
	protected final AllocationContext context;

	/**
	 * The method in which the access occurs.
	 *
	 * @invariant caller != null
	 */
	protected final MethodVariant caller;

	/**
	 * The collection of variants already processed/installed at the given access expression.  We do not want to process
	 * variants again and again.
	 *
	 * @invariant installedVariants != null
	 */
	protected final Set installedVariants = new HashSet();

	/**
	 * The program point at which the entity occurs.
	 *
	 * @invariant accessExprBox != null
	 */
	protected final ValueBox accessExprBox;

	/**
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access expression occurs.
	 * @param expr the access expression program point.  This is usually <code>ValueBox</code> containing
	 * 		  <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param accessContext the context in which the access occurs.
	 *
	 * @pre callerMethod != null and accessExpr != null and accessContext != null
	 */
	protected AbstractAccessExprWork(final MethodVariant callerMethod, final ValueBox expr, final Context accessContext) {
		this(null, new ArrayList(), callerMethod, expr, (Context) accessContext.clone());
	}

	/**
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 *
	 * @param accessNode the node associated with the access expression.
	 * @param arrivingValues the values arriving at <code>node</code>.
	 * @param callerMethod the method in which the access expression occurs.
	 * @param accessExpr the access expression program point.  This is usually <code>ValueBox</code> containing
	 * 		  <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param accessContext the context in which the access occurs.
	 *
	 * @pre accessNode != null and arrivingValues != null and callerMethod != null and accessExpr != null and accessContext
	 * 		!= null
	 */
	protected AbstractAccessExprWork(final IFGNode accessNode, final Collection arrivingValues,
		final MethodVariant callerMethod, final ValueBox accessExpr, final Context accessContext) {
		this.accessExprBox = accessExpr;
		this.caller = callerMethod;
		this.context = (AllocationContext) accessContext.clone();
        setFGNode(accessNode);
        addValues(arrivingValues);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
