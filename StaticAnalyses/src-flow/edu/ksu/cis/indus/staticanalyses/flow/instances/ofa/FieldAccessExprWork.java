
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import soot.SootField;
import soot.Value;
import soot.ValueBox;

import soot.jimple.FieldRef;
import soot.jimple.NullConstant;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FieldAccessExprWork.class);

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
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.

   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
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
