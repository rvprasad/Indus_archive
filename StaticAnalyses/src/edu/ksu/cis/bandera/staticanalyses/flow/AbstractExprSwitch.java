package edu.ksu.cis.bandera.staticanalyses.flow;


import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;
import ca.mcgill.sable.soot.jimple.AbstractJimpleValueSwitch;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.jext.BanderaExprSwitch;
import edu.ksu.cis.bandera.jext.ChooseExpr;
import edu.ksu.cis.bandera.jext.ComplementExpr;
import edu.ksu.cis.bandera.jext.InExpr;
import edu.ksu.cis.bandera.jext.LocalExpr;
import edu.ksu.cis.bandera.jext.LocationTestExpr;
import edu.ksu.cis.bandera.jext.LogicalAndExpr;
import edu.ksu.cis.bandera.jext.LogicalOrExpr;
import edu.ksu.cis.bandera.jext.ThreadExpr;

import java.util.Stack;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//AbstractExprSwitch.java
/**
 * <p>The expression visitor class.  This class provides the default method implementations for all the expressions that need
 * to be dealt at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to
 * extend the class as required.  It patches the inheritance hierarchy to inject the new constructs declared in
 * <code>BanderaExprSwitch</code> into the visitor provided in <code>AbstractJimpleValueSwitch</code>.</p>
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractExprSwitch extends AbstractJimpleValueSwitch implements BanderaExprSwitch, Prototype {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractExprSwitch.class);

	/**
	 * <p>This visitor is used to visit the expressions in the <code>method</code> variant. </p>
	 *
	 */
	protected final MethodVariant method;

	/**
	 * <p>This visitor is used by <code>stmt</code> to walk the embedded expressions.</p>
	 *
	 */
	protected final AbstractStmtSwitch stmt;

	/**
	 * <p>This visitor works in the context given by <code>context</code>.</p>
	 *
	 */
	protected final Context context;


	/**
	 * <p>This stores the program points as expressions are recursively processed.</p>
	 *
	 */
	private final Stack programPoints = new Stack();

	/**
	 * <p>The object used to connect flow graph nodes corresponding to AST and non-AST entities.  This provides the
	 * flexibility to use the same implementation of the visitor with different connectors to process LHS and RHS
	 * entities.</p>
	 *
	 */
	protected final FGNodeConnector connector;

	/**
	 * <p>The instance of the framework in which this visitor exists.</p>
	 *
	 */
	protected final BFA bfa;

	/**
	 * <p>Creates a new <code>AbstractExprSwitch</code> instance.</p>
	 *
	 * @param stmt the statement visitor which shall use this expression visitor.
	 * @param connector the connector to be used by this expression visitor to connect flow graph nodes corresponding to AST
	 * and non-AST entities.
	 */
	protected AbstractExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		this.stmt = stmt;
		this.connector = connector;
		if (stmt != null) {
			context = stmt.context;
			method = stmt.method;
			bfa = stmt.method.bfa;
		} else {
			context = null;
			method = null;
			bfa = null;
		} // end of else

	}

	/**
	 * <p>Processes <code>ChooseExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseChooseExpr(ChooseExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseComplementExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseComplementExpr(ComplementExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseInExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseInExpr(InExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseLocalExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLocalExpr(LocalExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseLocationTestExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLocationTestExpr(LocationTestExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseLogicalAndExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLogicalAndExpr(LogicalAndExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseLogicalOrExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLogicalOrExpr(LogicalOrExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Processes <code>caseThreadExpr</code>.</p>
	 *
	 * @param e the expression to be processed.
	 */
	public void caseThreadExpr(ThreadExpr e) {
		defaultCase(e);
	}

	/**
	 * <p>Provides the default implementation when any expression is not handled by the visitor.</p>
	 *
	 * @param o the expression which is not handled by the visitor.
	 */
	public void defaultCase(Object o) {
		setResult(method.getASTNode((Value)o));
		logger.debug(o + " is not handled.");
	}

	/**
	 * <p>Returns the <code>WorkList</code> associated with this visitor.</p>
	 *
	 * @return the <code>WorkList</code> associated with this visitor.
	 */
	public final WorkList getWorkList() {
		return bfa.worklist;
	}

	/**
	 * <p>Checks if the return type of the given method is <code>void</code>. </p>
	 *
	 * @param sm the method whose return type is to be checked for voidness.
	 * @return <code>true</code> if <code>sm</code>'s return type is <code>void</code>; <code>false</code> otherwise.
	 */
	public static final boolean isNonVoid(SootMethod sm) {
		return !(sm.getReturnType() instanceof VoidType);
	}

	/**
	 * <p>Processes the expression at the given program point, <code>v</code>.</p>
	 *
	 * @param v the program point at which the to-be-processed expression occurs.
	 */
	public void process(ValueBox v) {
		logger.debug("Started to process expression: " + v.getValue());
		programPoints.push(v);
		v.getValue().apply(this);
		programPoints.pop();
		logger.debug("Finished processing expression: " + v.getValue() + "\n" + getResult());
	}

	/**
	 * <p>Returns the current program point.</p>
	 *
	 * @return Returns the current program point.
	 */
	public final ValueBox getCurrentProgramPoint() {
		return (ValueBox)programPoints.peek();
	}

	/**
	 * <p>This method will throw <code>UnsupportedOperationException</code>.</p>
	 *
	 * @return (This method raises an exception.)
	 */
	public final Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype method is not supported.");
	}

}// AbstractExprSwitch
