
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;

import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.IFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


// AbstractAccessExprWork.java

/**
 * <p>
 * This class encapsulates the logic and data related to work to be done in correspondence to access expressions.
 * </p>
 * Created: Tue Jan 22 04:27:47 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractAccessExprWork
  extends AbstractWork {
	/**
	 * <p>
	 * The context in which the access occurs.
	 * </p>
	 */
	protected final AllocationContext context;

	/**
	 * <p>
	 * The method in which the access occurs.
	 * </p>
	 */
	protected final MethodVariant caller;

	/**
	 * <p>
	 * The collection of variants already processed/installed at the given access expression.  We do not want to process
	 * variants again and again.
	 * </p>
	 */
	protected final Set installedVariants = new HashSet();

	/**
	 * <p>
	 * The program point at which the entity occurs.
	 * </p>
	 */
	protected final ValueBox accessExprBox;

	/**
	 * <p>
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 * </p>
	 *
	 * @param callerMethod the method in which the access expression occurs.
	 * @param accessExpr the access expression program point.  This is usually <code>ValueBox</code> containing
	 *           <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param accessContext the context in which the access occurs.
	 */
	protected AbstractAccessExprWork(MethodVariant callerMethod, ValueBox accessExpr, Context accessContext) {
		this(null, new ArrayList(), callerMethod, accessExpr, (Context) accessContext.clone());
	}

	/**
	 * <p>
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 * </p>
	 *
	 * @param node the node associated with the access expression.
	 * @param values the values arriving at <code>node</code>.
	 * @param callerMethod the method in which the access expression occurs.
	 * @param accessExpr the access expression program point.  This is usually <code>ValueBox</code> containing
	 *           <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param accessContext the context in which the access occurs.
	 */
	protected AbstractAccessExprWork(IFGNode node, Collection values, MethodVariant callerMethod, ValueBox accessExpr,
		Context accessContext) {
		super(node, values);
		this.accessExprBox = accessExpr;
		this.caller = callerMethod;
		this.context = (AllocationContext) accessContext.clone();
	}
}

/*****
 ChangeLog:

$Log$

*****/
