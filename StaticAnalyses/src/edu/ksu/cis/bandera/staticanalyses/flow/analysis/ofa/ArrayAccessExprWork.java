package edu.ksu.cis.bandera.bfa.analysis.ofa;


import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.Value;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.BFA;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import java.util.Iterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * ArrayAccessExprWork.java
 *
 *
 * Created: Wed Mar  6 12:31:07 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ArrayAccessExprWork extends AbstractAccessExprWork {

	private static final Logger logger = LogManager.getLogger(ArrayAccessExprWork.class);

	protected final FGNode ast;

	protected final FGNodeConnector connector;

	public ArrayAccessExprWork (MethodVariant caller, Value accessExpr, Context context, FGNode ast,
								FGNodeConnector connector){
		super(caller, accessExpr, context);
		this.ast = ast;
		this.connector = connector;
		logger.debug(String.valueOf(hashCode()));
	}

	public void execute() {
		ArrayType atype = (ArrayType)((ArrayRef)accessExpr).getBase().getType();
		BFA bfa = caller.bfa;

		logger.debug(values + " values arrived at base node of " + accessExpr + " of type " + atype + " in " + context);

		for (Iterator i = values.iterator(); i.hasNext();) {
			 Value v = (Value)i.next();
			 context.setAllocationSite(v);
			 FGNode nonast = bfa.getArrayVariant(atype, context).getFGNode();
			 connector.connect(ast, nonast);
			 logger.debug(nonast + " " + context);
		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

}// ArrayAccessExprWork
