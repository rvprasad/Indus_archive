package edu.ksu.cis.bandera.bfa;



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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

	private static final Logger logger = LogManager.getLogger(AbstractExprSwitch.class.getName());

	protected final MethodVariant method;

	protected final AbstractStmtSwitch stmt;

	protected final Context context;

	protected final FGNodeConnector connector;

	protected final BFA bfa;

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

	public void caseThreadExpr(ThreadExpr e) {
		defaultCase(e);
	}

	public void defaultCase(Object o) {
		setResult(method.getASTNode((Value)o));
		logger.debug(o + " is not handled.");
	}

	public final WorkList getWorkList() {
		return bfa.worklist;
	}

	public static final boolean isNonVoid(SootMethod sm) {
		return !(sm.getReturnType() instanceof VoidType);
	}

	public void process(ValueBox v) {
		logger.debug(">>>>>>>>>> Processing: " + v.getValue());
		v.getValue().apply(this);
		logger.debug("<<<<<<<<<< Processing: " + v.getValue() + "\n" + getResult());
	}

	public final Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype method is not supported.");
	}

}// AbstractExprSwitch
