
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

import ca.mcgill.sable.soot.SootField;

import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.flow.BFA;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNodeConnector;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;


// FieldAccessExprWork.java

/**
 * <p>
 * This class encapsulates the logic to instrument the flow of values corresponding to fields.
 * </p>
 * Created: Wed Mar  6 03:32:30 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldAccessExprWork
  extends AbstractAccessExprWork {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger logger = LogManager.getLogger(FieldAccessExprWork.class);

	/**
	 * <p>
	 * The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the
	 * primary.
	 * </p>
	 */
	protected final FGNode ast;

	/**
	 * <p>
	 * The connector to be used to connect the ast and non-ast node.
	 * </p>
	 */
	protected final FGNodeConnector connector;

	/**
	 * <p>
	 * Creates a new <code>FieldAccessExprWork</code> instance.
	 * </p>
	 *
	 * @param caller the method in which the access occurs.
	 * @param accessExprBox the field access expression program point.
	 * @param context the context in which the access occurs.
	 * @param ast the flow graph node associated with the access expression.
	 * @param connector the connector to use to connect the ast node to the non-ast node.
	 */
	public FieldAccessExprWork(MethodVariant caller, ValueBox accessExprBox, Context context, FGNode ast,
		FGNodeConnector connector) {
		super(caller, accessExprBox, context);
		this.ast = ast;
		this.connector = connector;
	}

	/**
	 * <p>
	 * Connects non-ast nodes to ast nodes when new values arrive at the primary of the field access expression.
	 * </p>
	 */
	public synchronized void execute() {
		SootField sf = ((FieldRef) accessExprBox.getValue()).getField();
		BFA bfa = caller.bfa;
		logger.debug(values + " values arrived at base node of " + accessExprBox.getValue());

		for(Iterator i = values.iterator(); i.hasNext();) {
			Value v = (Value) i.next();

			if(v instanceof NullConstant) {
				continue;
			}
			context.setAllocationSite(v);

			FGNode nonast = bfa.getFieldVariant(sf, context).getFGNode();
			connector.connect(ast, nonast);
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
