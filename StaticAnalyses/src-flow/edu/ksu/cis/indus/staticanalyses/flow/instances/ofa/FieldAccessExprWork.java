
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

import soot.SootField;
import soot.Value;
import soot.ValueBox;

import soot.jimple.FieldRef;
import soot.jimple.NullConstant;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;


/**
 * This class encapsulates the logic to instrument the flow of values corresponding to fields.
 * 
 * <p>
 * Created: Wed Mar  6 03:32:30 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldAccessExprWork
  extends AbstractAccessExprWork {
	/**
	 * An instance of <code>Logger</code> used for logging purpose.
	 */
	private static final Logger LOGGER = LogManager.getLogger(FieldAccessExprWork.class);

	/**
	 * The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the
	 * primary.
	 *
	 * @invariant ast != null
	 */
	protected final IFGNode ast;

	/**
	 * The connector to be used to connect the ast and non-ast node.
	 *
	 * @invariant connector != null
	 */
	protected final IFGNodeConnector connector;

	/**
	 * Creates a new <code>FieldAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access occurs.
	 * @param accessProgramPoint the field access expression program point.
	 * @param accessContext the context in which the access occurs.
	 * @param accessNode the flow graph node associated with the access expression.
	 * @param connectorToBeUsed the connector to use to connect the ast node to the non-ast node.
	 *
	 * @pre callerMethod != null and accessProgramPoint != null and accessContext != null and accessNode != null and
	 * 		connectorToBeUsed != null
	 */
	public FieldAccessExprWork(final MethodVariant callerMethod, final ValueBox accessProgramPoint,
		final Context accessContext, final IFGNode accessNode, final IFGNodeConnector connectorToBeUsed) {
		super(callerMethod, accessProgramPoint, accessContext);
		this.ast = accessNode;
		this.connector = connectorToBeUsed;
	}

	/**
	 * Connects non-ast nodes to ast nodes when new values arrive at the primary of the field access expression.
	 */
	public synchronized void execute() {
		SootField sf = ((FieldRef) accessExprBox.getValue()).getField();
		FA fa = caller._fa;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(values + " values arrived at base node of " + accessExprBox.getValue());
		}

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value v = (Value) i.next();

			if (v instanceof NullConstant) {
				continue;
			}
			context.setAllocationSite(v);

			IFGNode nonast = fa.getFieldVariant(sf, context).getFGNode();
			connector.connect(ast, nonast);
		}
	}
}

/*
   ChangeLog:

   $Log$
   Revision 1.3  2003/08/16 21:55:14  venku
   Ripple effect of changing FA._FA to FA._fa

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
