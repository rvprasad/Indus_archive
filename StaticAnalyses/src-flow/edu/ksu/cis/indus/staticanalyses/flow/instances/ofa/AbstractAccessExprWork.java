
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

import soot.ValueBox;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * This class encapsulates the logic and data related to work to be done in correspondence to access expressions.
 * 
 * <p>
 * Created: Tue Jan 22 04:27:47 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractAccessExprWork
  extends AbstractWork {
	/**
	 * The context in which the access occurs.
	 *
	 * @invariant context != null
	 */
	protected final AllocationContext context;

	/**
	 * The method in which the access occurs.
	 *
	 * @invariant caller != null
	 */
	protected final MethodVariant caller;

	/**
	 * The collection of variants already processed/installed at the given access expression.  We do not want to process
	 * variants again and again.
	 *
	 * @invariant installedVariants != null
	 */
	protected final Set installedVariants = new HashSet();

	/**
	 * The program point at which the entity occurs.
	 *
	 * @invariant accessExprBox != null
	 */
	protected final ValueBox accessExprBox;

	/**
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access expression occurs.
	 * @param expr the access expression program point.  This is usually <code>ValueBox</code> containing
	 * 		  <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param accessContext the context in which the access occurs.
	 *
	 * @pre callerMethod != null and accessExpr != null and accessContext != null
	 */
	protected AbstractAccessExprWork(final MethodVariant callerMethod, final ValueBox expr, final Context accessContext) {
		this(null, new ArrayList(), callerMethod, expr, (Context) accessContext.clone());
	}

	/**
	 * Creates a new <code>AbstractAccessExprWork</code> instance.
	 *
	 * @param accessNode the node associated with the access expression.
	 * @param arrivingValues the values arriving at <code>node</code>.
	 * @param callerMethod the method in which the access expression occurs.
	 * @param accessExpr the access expression program point.  This is usually <code>ValueBox</code> containing
	 * 		  <code>FieldRef</code>, <code>ArrayRef</code>, or <code>NonStaticInvokeExpr</code>.
	 * @param accessContext the context in which the access occurs.
	 *
	 * @pre accessNode != null and arrivingValues != null and callerMethod != null and accessExpr != null and accessContext
	 * 		!= null
	 */
	protected AbstractAccessExprWork(final IFGNode accessNode, final Collection arrivingValues,
		final MethodVariant callerMethod, final ValueBox accessExpr, final Context accessContext) {
		this.accessExprBox = accessExpr;
		this.caller = callerMethod;
		this.context = (AllocationContext) accessContext.clone();
		setFGNode(accessNode);
		addValues(arrivingValues);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.

   Revision 1.3  2003/08/17 10:33:03  venku
   WorkList does not inherit from IWorkBag rather contains an instance of IWorkBag.
   Ripple effect of the above change.
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
