package edu.ksu.cis.bandera.bfa.analysis.ofa;


import edu.ksu.cis.bandera.bfa.BFA;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import edu.ksu.cis.bandera.bfa.MethodVariant;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import java.util.Iterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// ArrayAccessExprWork.java
/**
 * <p>This class is the counter part of <code>FieldAccessExprWork</code>.  It encapsulates the logic to instrument the flow
 * values through array components.</p>
 *
 * Created: Wed Mar  6 12:31:07 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class ArrayAccessExprWork extends AbstractAccessExprWork {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ArrayAccessExprWork.class);

	/**
	 * <p>The ast flow graph node which needs to be connected to non-ast nodes depending on the values that occur at the
	 * primary.</p>
	 *
	 */
	protected final FGNode ast;

	/**
	 * <p>The connector to be used to connect the ast and non-ast node.</p>
	 *
	 */
	protected final FGNodeConnector connector;

	/**
	 * <p>Creates a new <code>ArrayAccessExprWork</code> instance.</p>
	 *
	 * @param caller the method in which the access occurs.
	 * @param accessExprBox the array access expression program point.
	 * @param context the context in which the access occurs.
	 * @param ast the flow graph node associated with the access expression.
	 * @param connector the connector to use to connect the ast node to the non-ast node.
	 */
	public ArrayAccessExprWork (MethodVariant caller, ValueBox accessExprBox, Context context, FGNode ast,
								FGNodeConnector connector){
		super(caller, accessExprBox, context);
		this.ast = ast;
		this.connector = connector;
		logger.debug(String.valueOf(hashCode()));
	}

	/**
	 * <p>Connects non-ast nodes to ast nodes when new values arrive at the primary of the array access expression.</p>
	 *
	 */
	public synchronized void execute() {
		ArrayType atype = (ArrayType)((ArrayRef)accessExprBox.getValue()).getBase().getType();
		BFA bfa = caller.bfa;

		logger.debug(values + " values arrived at base node of " + accessExprBox.getValue() + " of type " + atype + " in " +
					 context); 

		for (Iterator i = values.iterator(); i.hasNext();) {
			 Value v = (Value)i.next();

			 if (v instanceof NullConstant) {
				 continue;
			 } // end of if (v instanceof NullConstant)

			 context.setAllocationSite(v);
			 FGNode nonast = bfa.queryArrayVariant(atype, context).getFGNode();
			 connector.connect(ast, nonast);
			 logger.debug(nonast + " " + context);
		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

}// ArrayAccessExprWork
