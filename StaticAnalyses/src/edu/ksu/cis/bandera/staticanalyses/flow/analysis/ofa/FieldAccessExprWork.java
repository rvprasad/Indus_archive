package edu.ksu.cis.bandera.bfa.analysis.ofa;


import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.Value;
import edu.ksu.cis.bandera.bfa.BFA;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import java.util.Iterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * FieldAccessExprWork.java
 *
 *
 * Created: Wed Mar  6 03:32:30 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FieldAccessExprWork extends  AbstractAccessExprWork {

	private static final Logger logger = LogManager.getLogger(FieldAccessExprWork.class);

	protected final FGNode ast;

	protected final FGNodeConnector connector;

	public FieldAccessExprWork (MethodVariant caller, Value accessExpr, Context context, FGNode ast,
								FGNodeConnector connector) {
		super(caller, accessExpr, context);
		this.ast = ast;
		this.connector = connector;
	}

	public void execute() {
		SootField sf = ((FieldRef)accessExpr).getField();
		BFA bfa = caller.bfa;
		logger.debug(values + " values arrived at base node of " + accessExpr);
		for (Iterator i = values.iterator(); i.hasNext();) {
			 Value v = (Value)i.next();

			 if (v instanceof NullConstant) {
				 continue;
			 } // end of if (v instanceof NullConstant)

			 context.setAllocationSite(v);
			 FGNode nonast = bfa.getFieldVariant(sf, context).getFGNode();
			 connector.connect(ast, nonast);
		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

}// FieldAccessExprWork
