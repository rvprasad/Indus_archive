
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

import ca.mcgill.sable.soot.ArrayType;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.flow.BFA;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.IFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;


/**
 * This class is the counter part of <code>FieldAccessExprWork</code>.  It encapsulates the logic to instrument the flow
 * values through array components.  Created: Wed Mar  6 12:31:07 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ArrayAccessExprWork
  extends AbstractAccessExprWork {
	/**
	 * An instance of <code>Logger</code> used for logging purpose.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ArrayAccessExprWork.class);

	/**
	 * The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the
	 * primary.
	 */
	protected final IFGNode ast;

	/**
	 * The connector to be used to connect the ast and non-ast node.
	 */
	protected final IFGNodeConnector connector;

	/**
	 * Creates a new <code>ArrayAccessExprWork</code> instance.
	 *
	 * @param caller the method in which the access occurs.
	 * @param accessExprBox the array access expression program point.
	 * @param context the context in which the access occurs.
	 * @param astArg the flow graph node associated with the access expression.
	 * @param connector the connector to use to connect the ast node to the non-ast node.
	 */
	public ArrayAccessExprWork(MethodVariant caller, ValueBox accessExprBox, Context context, IFGNode astArg,
		IFGNodeConnector connector) {
		super(caller, accessExprBox, context);
		this.ast = astArg;
		this.connector = connector;
	}

	/**
	 * Connects non-ast nodes to ast nodes when new values arrive at the primary of the array access expression.
	 */
	public synchronized void execute() {
		ArrayType atype = (ArrayType) ((ArrayRef) accessExprBox.getValue()).getBase().getType();
		BFA bfa = caller._BFA;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(values + " values arrived at base node of " + accessExprBox.getValue() + " of type " + atype
				+ " in " + context);
		}

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value v = (Value) i.next();

			if (v instanceof NullConstant) {
				continue;
			}

			context.setAllocationSite(v);

			IFGNode nonast = bfa.getArrayVariant(atype, context).getFGNode();
			connector.connect(ast, nonast);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(nonast + " " + context);
			}
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
