package edu.ksu.cis.bandera.bfa.analysis.ofa;



import ca.mcgill.sable.soot.jimple.Value;
import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.AbstractWork;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * AbstractAccessExprWork.java
 *
 *
 * Created: Tue Jan 22 04:27:47 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractAccessExprWork extends AbstractWork {

	protected final Value accessExpr;

	protected final Context context;

	protected final MethodVariant caller;

	protected final Set installedVariants = new HashSet();

	protected AbstractAccessExprWork (MethodVariant caller, Value accessExpr, Context context) {
		this(null, new ArrayList(), caller, accessExpr, (Context)context.clone());
	}

	protected AbstractAccessExprWork (AbstractFGNode node, Collection values, MethodVariant caller,
									  Value accessExpr, Context context) {
		super(node, values);
		this.accessExpr = accessExpr;
		this.caller = caller;
		this.context = (Context)context.clone();
	}

}// AbstractAccessExprWork
