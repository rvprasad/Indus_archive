package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;


import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;

import ca.mcgill.sable.soot.jimple.ValueBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// AbstractAccessExprWork.java
/**
 * <p>This class encapsulates the logic and data related to work to be done in correspondence to access expressions.</p>
 *
 * Created: Tue Jan 22 04:27:47 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractAccessExprWork extends AbstractWork {

	/**
	 * <p>The program point at which the entity occurs.</p>
	 *
	 */
	protected final ValueBox accessExprBox;

	/**
	 * <p>The context in which the access occurs.</p>
	 *
	 */
	protected final Context context;

	/**
	 * <p>The method in which the access occurs.</p>
	 *
	 */
	protected final MethodVariant caller;

	/**
	 * <p>The collection of variants already processed/installed at the given access expression.  We do not want to process
	 * variants again and again.</p>
	 *
	 */
	protected final Set installedVariants = new HashSet();

	/**
	 * <p>Creates a new <code>AbstractAccessExprWork</code> instance.</p>
	 *
	 * @param caller the method in which the access expression occurs.
	 * @param accessExprBox the access expression program point.  This is usually <code>ValueBox</code> containing
	 * <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param context the context in which the access occurs.
	 */
	protected AbstractAccessExprWork (MethodVariant caller, ValueBox accessExprBox, Context context) {
		this(null, new ArrayList(), caller, accessExprBox, (Context)context.clone());
	}

	/**
	 * <p>Creates a new <code>AbstractAccessExprWork</code> instance.</p>
	 *
	 * @param node the node associated with the access expression.
	 * @param values the values arriving at <code>node</code>.
	 * @param caller the method in which the access expression occurs.
	 * @param accessExprBox  the access expression program point.  This is usually <code>ValueBox</code> containing
	 * <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param context the context in which the access occurs.
	 */
	protected AbstractAccessExprWork (FGNode node, Collection values, MethodVariant caller,
									  ValueBox accessExprBox, Context context) {
		super(node, values);
		this.accessExprBox = accessExprBox;
		this.caller = caller;
		this.context = (Context)context.clone();
	}

}// AbstractAccessExprWork
