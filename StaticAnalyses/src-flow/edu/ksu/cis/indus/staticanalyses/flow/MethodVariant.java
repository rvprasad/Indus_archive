
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

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.support.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Value;

import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

import soot.toolkits.graph.CompleteUnitGraph;

import soot.toolkits.scalar.SimpleLocalDefs;


//MethodVariant.java

/**
 * The variant that represents a method implementation.  It maintains variant specific information about local variables and
 * the AST nodes in associated method. It also maintains information about the parameters, this variable, and return values,
 * if any are present.    Created: Tue Jan 22 05:27:59 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */
public class MethodVariant
  implements IVariant {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(MethodVariant.class);

	/**
	 * The context which resulted in the creation of this variant.
	 *
	 * @invariant _context != null
	 */
	public final Context _context;

	/**
	 * The instance of <code>FA</code> which was responsible for the creation of this variant.
	 *
	 * @invariant _fa != null
	 */
	public final FA _fa;

	/**
	 * The method represented by this variant.
	 *
	 * @invariant _method != null
	 */
	public final SootMethod _method;

	/**
	 * The manager of AST node variants.  This is required as in Jimple, the same AST node instance may occur at different
	 * locations in the AST as it serves the purpose of AST representation.
	 *
	 * @invariant astvm != null
	 */
	protected final AbstractVariantManager astvm;

	/**
	 * The statement visitor used to process in the statement in the correpsonding method.
	 *
	 * @invariant stmt != null
	 */
	protected AbstractStmtSwitch stmt;

	/**
	 * The flow graph node associated with an abstract single return point of the corresponding method.  This will be
	 * <code>null</code>, if the associated method's return type is any non-ref type.
	 *
	 * @invariant _method.getReturnType().oclIsKindOf(RefLikeType) implies returnVar != null
	 * @invariant not _method.getReturnType().oclIsKindOf(RefLikeType) implies returnVar == null
	 */
	protected final IFGNode returnVar;

	/**
	 * The flow graph nodes associated with the this variable of the corresponding method.  This will be <code>null</code>,
	 * if the associated method is <code>static</code>.
	 *
	 * @invariant _method.isStatic() implies thisVar == null
	 * @invariant not _method.isStatic() implies thisVar != null
	 */
	protected final IFGNode thisVar;

	/**
	 * The array of flow graph nodes associated with the parameters of thec corresponding method.
	 *
	 * @invariant parameters.oclIsKindOf(Sequence(IFGNode))
	 * @invariant _method.getParameterCount() == 0 implies parameters == null
	 * @invariant _method.getParameterTypes()->forall(p | p.oclIsKindOf(RefLikeType) implies
	 * 			  parameters.at(method.getParameterTypes().indexOf(p)) != null)
	 * @invariant _method.getParameterTypes()->forall(p | not p.oclIsKindOf(RefLikeType) implies
	 * 			  parameters.at(method.getParameterTypes().indexOf(p)) == null)
	 */
	protected final IFGNode[] parameters;

	/**
	 * This provides the def sites for local variables in the associated method.  This is used in conjunction with
	 * flow-sensitive information calculation.
	 */
	protected SimpleLocalDefs defs;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected boolean unRetrievable = false;

	/**
	 * Creates a new <code>MethodVariant</code> instance.  This will not process the statements of this method.  That is
	 * accomplished via call to <code>process()</code>.
	 *
	 * @param sm the method represented by this variant.  This parameter cannot be <code>null</code>.
	 * @param astVariantManager the manager of flow graph nodes corresponding to the AST nodes of<code>sm</code>.  This
	 * 		  parameter cannot be <code>null</code>.
	 * @param fa the instance of <code>FA</code> which was responsible for the creation of this variant.  This parameter
	 * 		  cannot be <code>null</code>.
	 *
	 * @pre sm != null and astvm != null and fa != null
	 */
	protected MethodVariant(final SootMethod sm, final AbstractVariantManager astVariantManager, final FA fa) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: preprocessing of " + sm);
		}
		_method = sm;
		_fa = fa;
		_context = (Context) fa._analyzer.context.clone();
		_context.callNewMethod(sm);

		Collection typesToProcess = new HashSet();
		int pCount = sm.getParameterCount();

		if (pCount > 0) {
			parameters = new AbstractFGNode[pCount];

			for (int i = 0; i < pCount; i++) {
				if (sm.getParameterType(i) instanceof RefLikeType) {
					parameters[i] = fa.getNewFGNode();
					typesToProcess.add(sm.getParameterType(i));
				}
			}
		} else {
			parameters = new AbstractFGNode[0];
		}

		if (sm.isStatic()) {
			thisVar = null;
		} else {
			thisVar = fa.getNewFGNode();

			/*
			 * NOTE: This is required to filter out values which are descendents of a higher common type but which are
			 * incompatible.  An example is all objects entering run() site will have a run() method defined.  However, it
			 * is false to assume that all such objects can be considered as receivers for all run() implementations plugged
			 * into the run() site.
			 */
			thisVar.setFilter(new TypeBasedFilter(sm.getDeclaringClass(), fa));
		}

		if (sm.getReturnType() instanceof RefLikeType) {
			returnVar = fa.getNewFGNode();
			typesToProcess.add(sm.getReturnType());
		} else {
			returnVar = null;
		}

		astvm = astVariantManager;
		sm.addTag(_fa.getTag());

		// process the types required by the method body        
		fa.processClass(sm.getDeclaringClass());

		for (final Iterator i = typesToProcess.iterator(); i.hasNext();) {
			fa.processType((Type) i.next());
		}

		if (_method.isConcrete()) {
			JimpleBody jb = (JimpleBody) _method.retrieveActiveBody();

			for (Iterator i = jb.getLocals().iterator(); i.hasNext();) {
				Type localType = ((Local) i.next()).getType();

				if (localType instanceof RefLikeType) {
					_fa.processType(localType);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: preprocessed of " + sm);
		}
	}

	/**
	 * Returns the flow graph node associated with the given AST node in the context defined by <code>this.context</code>.
	 *
	 * @param v the AST node whose associted flow graph node is to be returned.
	 *
	 * @return the flow graph node associated with <code>v</code> in the context <code>this.context</code>.
	 *
	 * @pre v != null
	 */
	public final IFGNode getASTNode(final Value v) {
		return getASTVariant(v, _context).getFGNode();
	}

	/**
	 * Returns the flow graph node associated with the given AST node in the given context.  Creates a new one if none
	 * exists.
	 *
	 * @param v the AST node whose associted flow graph node is to be returned.
	 * @param c the context in which the flow graph node was associated with <code>v</code>.
	 *
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.
	 *
	 * @pre v != null and c != null
	 */
	public final IFGNode getASTNode(final Value v, final Context c) {
		return getASTVariant(v, c).getFGNode();
	}

	/**
	 * Returns the definitions of local variable <code>l</code> that arrive at statement <code>s</code>.
	 *
	 * @param l the local for which the definitions are requested.
	 * @param s the statement at which the definitions are requested.
	 *
	 * @return the list of definitions of <code>l</code> that arrive at statement <code>s</code>.
	 *
	 * @pre l != null and s != null
	 */
	public List getDefsOfAt(final Local l, final Stmt s) {
		if (unRetrievable) {
			return Collections.EMPTY_LIST;
		} else {
			if (defs == null) {
				defs = new SimpleLocalDefs(new CompleteUnitGraph(_method.retrieveActiveBody()));
			}

			if (defs == null) {
				unRetrievable = true;
				return Collections.EMPTY_LIST;
			} else {
				return defs.getDefsOfAt(l, s);
			}
		}
	}

	/**
	 * Processes the body of the method implementation associated with this variant.
	 */
	public void process() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing of " + _method);
		}

		JimpleBody jb = null;

		// We assume the user has closed the system.
		if (_method.isConcrete()) {
			jb = (JimpleBody) _method.retrieveActiveBody();

			List stmtList = new ArrayList(jb.getUnits());
			stmt = _fa.getStmt(this);

			for (Iterator i = stmtList.iterator(); i.hasNext();) {
				Stmt temp = (Stmt) i.next();
				stmt.process(temp);
			}

			Collection caught = new HashSet();
			boolean flag = false;
			InvokeExpr expr = null;

			for (Iterator i = jb.getTraps().iterator(); i.hasNext();) {
				Trap trap = (Trap) i.next();
				Stmt begin = (Stmt) trap.getBeginUnit();
				Stmt end = (Stmt) trap.getEndUnit();

				// we assume that the first statement in the handling block will be the identity statement that retrieves the 
				// caught expression.
				CaughtExceptionRef catchRef = (CaughtExceptionRef) ((IdentityStmt) trap.getHandlerUnit()).getRightOp();
				SootClass exception = trap.getException();

				for (int j = stmtList.indexOf(begin), k = stmtList.indexOf(end); j < k; j++) {
					Stmt tmp = (Stmt) stmtList.get(j);

					if (tmp instanceof ThrowStmt) {
						ThrowStmt ts = (ThrowStmt) tmp;

						if (!caught.contains(ts)) {
							SootClass scTemp = _fa.getClass(((RefType) ts.getOp().getType()).getClassName());

							if (Util.isDescendentOf(scTemp, exception)) {
								_context.setStmt(ts);

								IFGNode throwNode = getASTNode(ts.getOp(), _context);
								throwNode.addSucc(getASTNode(catchRef));
								caught.add(ts);
							}
						}
					} else if (tmp.containsInvokeExpr()) {
						expr = tmp.getInvokeExpr();
						flag = true;
					}

					if (flag) {
						flag = false;

						if (!caught.contains(tmp)) {
							_context.setStmt(tmp);

							IFGNode tempNode = queryThrowNode(expr, exception);

							if (tempNode != null) {
								tempNode.addSucc(getASTNode(catchRef));
							}
						}
					}
				}
			}
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(_method + " is not a concrete method. Hence, it's body could not be retrieved.");
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processing of " + _method);
		}
	}

	/**
	 * Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.
	 *
	 * @param v the AST node whose associted variant is to be returned.
	 *
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.  If none exists,
	 * 		   <code>null</code> is returned.
	 *
	 * @pre v != null
	 */
	public final IFGNode queryASTNode(final Value v) {
		return queryASTNode(v, _context);
	}

	/**
	 * Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param c the context in which the variant was associated with <code>v</code>.
	 *
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.  If none exists,
	 * 		   <code>null</code> is returned.
	 *
	 * @pre v != null and c != null
	 */
	public final IFGNode queryASTNode(final Value v, final Context c) {
		ValuedVariant var = queryASTVariant(v, c);
		IFGNode temp = null;

		if (var != null) {
			temp = var.getFGNode();
		}
		return temp;
	}

	/**
	 * Same as <code>getASTVariant</code>, except <code>null</code> is returned if none exists.
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param c a <code>Context</code> value
	 *
	 * @return the variant associated with <code>v</code> in the context <code>c</code>.  If none exists, <code>null</code>
	 * 		   is returned.
	 *
	 * @pre v != null and c != null
	 */
	public final ValuedVariant queryASTVariant(final Value v, final Context c) {
		return (ValuedVariant) astvm.query(v, c);
	}

	/**
	 * Returns the flow graph node associated with the given parameter.
	 *
	 * @param index the index of the parameter in the parameter list of the associated method.
	 *
	 * @return the flow graph node associated with the <code>index</code>th parameter in the parameter list of the associated
	 * 		   method.  It returns <code>null</code> if the method has no parameters or if mentioned parameter is of non-ref
	 * 		   type.
	 */
	public final IFGNode queryParameterNode(final int index) {
		IFGNode temp = null;

		if (index >= 0 && index <= _method.getParameterCount()) {
			temp = parameters[index];
		}

		return temp;
	}

	/**
	 * Returns the flow graph node that represents an abstract single return point of the associated method.
	 *
	 * @return the flow graph node that represents an abstract single return point of the associated method.
	 * 		   <code>null</code> if the corresponding method does not return a value or if it returns non-ref typed value.
	 */
	public final IFGNode queryReturnNode() {
		return returnVar;
	}

	/**
	 * Returns the flow graph node associated with the <code>this</code> variable of the associated method.
	 *
	 * @return Returns the flow graph node associated with the <code>this</code> variable of the associated method.
	 * 		   <code>null</code> if the corresponding method is <code>static</code>.
	 */
	public final IFGNode queryThisNode() {
		return thisVar;
	}

	/**
	 * Returns the flow graph node associated with <code>exception</code> class at invoke expression <code>e</code>.
	 *
	 * @param e is the method invoke expression.
	 * @param exception is the class of the exception thrown at <code>e</code>.
	 *
	 * @return the node that captures values associated with the <code>exception</code> class at <code>e</code>.
	 *
	 * @pre e != null and exception != null
	 */
	public final IFGNode queryThrowNode(final InvokeExpr e, final SootClass exception) {
		return queryThrowNode(e, exception, _context);
	}

	/**
	 * Returns the flow graph node associated with <code>exception</code> class at invoke expression <code>e</code>.
	 *
	 * @param e is the method invoke expression.
	 * @param exception is the class of the exception thrown at <code>e</code>.
	 * @param c is the context in which the node is requested.
	 *
	 * @return the node that captures values associated with the <code>exception</code> class at <code>e</code>.
	 *
	 * @pre e != null and exception != null and c != null
	 */
	public final IFGNode queryThrowNode(final InvokeExpr e, final SootClass exception, final Context c) {
		InvocationVariant var = (InvocationVariant) queryASTVariant(e, c);
		IFGNode temp = null;

		if (var != null) {
			temp = var.queryThrowNode(exception);
		}
		return temp;
	}

	/**
	 * Returns the variant associated with the given AST node in the given context.  Creates a new one if none exists.
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param context the context in which the variant was associated with <code>v</code>.
	 *
	 * @return the variant associated with <code>v</code> in the context <code>c</code>.
	 *
	 * @pre v != null and context != null
	 */
	final ValuedVariant getASTVariant(final Value v, final Context context) {
		return (ValuedVariant) astvm.select(v, context);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.14  2003/12/07 05:02:18  venku
   - formatting.

   Revision 1.13  2003/12/05 21:22:15  venku
   - delayed construction of local use-def info
   - process all types known at the interface of the method
     at a single location.
   Revision 1.12  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.11  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.10  2003/11/30 01:11:28  venku
   - types associated with locals are processed.
   - methods are tagged with a named tag retrieved
     from the framework instance.
   Revision 1.9  2003/11/25 23:04:51  venku
   - local variable access is faster than fields.  FIXED.
   Revision 1.8  2003/11/25 23:03:54  venku
   - removed a variant of getASTvariant() as it was not being used.
   - added call to _fa.processClass() in getASTVariant().
   - logging and name change to used field variable.
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/16 21:50:51  venku
   Removed ASTVariant as it did not contain any data that was used.
   Concretized AbstractValuedVariant and renamed it to ValuedVariant.
   Ripple effect of the above change in some.
   Spruced up documentation and specification.
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
