
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

import edu.ksu.cis.indus.interfaces.IPrototype;

import edu.ksu.cis.indus.processing.Context;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.AbstractJimpleValueSwitch;


/**
 * The expression visitor class.  This class provides the default method implementations for all the expressions that need to
 * be dealt at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to extend
 * the class as required.  It patches the inheritance hierarchy to inject the new constructs declared in
 * <code>BanderaExprSwitch</code> into the visitor provided in <code>AbstractJimpleValueSwitch</code>.
 * 
 * <p>
 * Created: Sun Jan 27 14:29:14 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractExprSwitch
  extends AbstractJimpleValueSwitch
  implements IPrototype {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractExprSwitch.class);

	/**
	 * This visitor is used by <code>stmt</code> to walk the embedded expressions.
	 */
	protected final AbstractStmtSwitch stmtSwitch;

	/**
	 * This visitor works in the context given by <code>context</code>.
	 */
	protected final Context context;

	/**
	 * The instance of the framework in which this visitor exists.
	 */
	protected final FA fa;

	/**
	 * The object used to connect flow graph nodes corresponding to AST and non-AST entities.  This provides the flexibility
	 * to use the same implementation of the visitor with different connectors to process LHS and RHS entities.
	 */
	protected final IFGNodeConnector connector;

	/**
	 * This visitor is used to visit the expressions in the <code>method</code> variant.
	 */
	protected final MethodVariant method;

	/**
	 * This stores the program points as expressions are recursively descended into and processed.
	 */
	private final Stack programPoints = new Stack();

	/**
	 * Creates a new <code>AbstractExprSwitch</code> instance.
	 *
	 * @param stmtVisitor the statement visitor which shall use this expression visitor.
	 * @param connectorToUse the connector to be used by this expression visitor to connect flow graph nodes corresponding to
	 * 		  AST and non-AST entities.
	 *
	 * @pre connectorToUse != null
	 */
	protected AbstractExprSwitch(final AbstractStmtSwitch stmtVisitor, final IFGNodeConnector connectorToUse) {
		this.stmtSwitch = stmtVisitor;
		this.connector = connectorToUse;

		if (stmtSwitch != null) {
			context = stmtSwitch.context;
			method = stmtSwitch.method;
			fa = stmtSwitch.method._fa;
		} else {
			context = null;
			method = null;
			fa = null;
		}
	}

	/**
	 * Checks if the return type of the given method is <code>void</code>.
	 *
	 * @param sm the method whose return type is to be checked for voidness.
	 *
	 * @return <code>true</code> if <code>sm</code>'s return type is <code>void</code>; <code>false</code> otherwise.
	 *
	 * @pre sm != null
	 */
	public static final boolean isNonVoid(final SootMethod sm) {
		return !(sm.getReturnType() instanceof VoidType);
	}

	/**
	 * This method will throw <code>UnsupportedOperationException</code>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported.
	 */
	public final Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype method is not supported.");
	}

	/**
	 * Returns the <code>WorkList</code> associated with this visitor.
	 *
	 * @return the <code>WorkList</code> associated with this visitor.
	 *
	 * @post result != null
	 */
	public final WorkList getWorkList() {
		return fa.worklist;
	}

	/**
	 * Provides the default implementation when any expression is not handled by the visitor.  It sets the flow node
	 * associated with the AST as the result.
	 *
	 * @param o the expression which is not handled by the visitor.
	 */
	public void defaultCase(final Object o) {
		setResult(method.getASTNode((Value) o));

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(o + "(" + o.getClass() + ") is not handled.");
		}
	}

	/**
	 * Processes the expression at the given program point, <code>v</code>.
	 *
	 * @param v the program point at which the to-be-processed expression occurs.
	 *
	 * @pre v != null
	 */
	public void process(final ValueBox v) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Started to process expression: " + v.getValue());
		}
		programPoints.push(v);
		v.getValue().apply(this);
		programPoints.pop();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Finished processing expression: " + v.getValue() + "\n" + getResult());
		}
	}

	/**
	 * Returns the current program point.
	 *
	 * @return Returns the current program point.
	 *
	 * @post result != null
	 */
	protected final ValueBox getCurrentProgramPoint() {
		return (ValueBox) programPoints.peek();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.4  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.3  2003/08/15 02:54:06  venku
   Spruced up specification and documentation for flow-insensitive classes.
   Changed names in AbstractExprSwitch.
   Ripple effect of above change.
   Formatting changes to IPrototype.
   Revision 1.2  2003/08/12 18:39:56  venku
   Ripple effect of moving IPrototype to Indus.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.12  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
