package edu.ksu.cis.bandera.bfa;

import edu.ksu.cis.bandera.jext.BanderaExprSwitch;
import edu.ksu.cis.bandera.jext.ChooseExpr;
import edu.ksu.cis.bandera.jext.ComplementExpr;
import edu.ksu.cis.bandera.jext.InExpr;
import edu.ksu.cis.bandera.jext.LocalExpr;
import edu.ksu.cis.bandera.jext.LocationTestExpr;
import edu.ksu.cis.bandera.jext.LogicalAndExpr;
import edu.ksu.cis.bandera.jext.LogicalOrExpr;

import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;
import ca.mcgill.sable.soot.jimple.AbstractJimpleValueSwitch;
import ca.mcgill.sable.soot.jimple.ValueBox;

import org.apache.log4j.Category;

/**
 * AbstractExprSwitch.java
 *
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractExprSwitch extends AbstractJimpleValueSwitch implements
																			   BanderaExprSwitch,
																			   Prototype {

	private static final Category cat = Category.getInstance(AbstractExprSwitch.class.getName());

	protected final MethodVariant method;

	protected final AbstractStmtSwitch stmt;

	protected final Context context;

	protected final FGNodeConnector connector;

	protected final BFA bfa;

	protected AbstractExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		this.stmt = stmt;
		context = stmt.context;
		this.connector = connector;
		method = stmt.method;
		bfa = stmt.method.bfa;
	}

	public void caseChooseExpr(ChooseExpr e) {
		defaultCase(e);
	}

	public void caseComplementExpr(ComplementExpr e) {
		defaultCase(e);
	}

	public void caseInExpr(InExpr e) {
		defaultCase(e);
	}

	public void caseLocalExpr(LocalExpr e) {
		defaultCase(e);
	}

	public void caseLocationTestExpr(LocationTestExpr e) {
		defaultCase(e);
	}

	public void caseLogicalAndExpr(LogicalAndExpr e) {
		defaultCase(e);
	}

	public void caseLogicalOrExpr(LogicalOrExpr e) {
		defaultCase(e);
	}

	public void defaultCase(Object o) {
		cat.info(o + " is not handled.");
	}

	public final WorkList getWorkList() {
		return bfa.worklist;
	}

	public static final boolean isNonVoid(SootMethod sm) {
		return !(sm.getReturnType() instanceof VoidType);
	}

	public void process(ValueBox v) {
		v.getValue().apply(this);
	}

	public final Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype method is not supported.");
	}

}// AbstractExprSwitch
