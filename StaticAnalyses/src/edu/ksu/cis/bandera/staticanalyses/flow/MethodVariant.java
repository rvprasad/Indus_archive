
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.BodyRepresentation;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;

import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.CaughtExceptionRef;
import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.IdentityStmt;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.SimpleLocalDefs;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.ThrowStmt;
import ca.mcgill.sable.soot.jimple.Trap;
import ca.mcgill.sable.soot.jimple.Value;

import ca.mcgill.sable.util.ArrayList;
import ca.mcgill.sable.util.Iterator;
import ca.mcgill.sable.util.List;

import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.TypeBasedFilter;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;


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
	 * An instance of <code>Logger</code> used for logging purposes.
	 */
	private static final Logger LOGGER = LogManager.getLogger(MethodVariant.class.getName());

	/**
	 * This object is used to create <code>Jimple</code> representation of the associated method.  This is required to
	 * extract the list of statement corresponding to the method body and walk over it.
	 */
	public static final BodyRepresentation JIMPLEBODYREP = Jimple.v();

	/**
	 * The instance of <code>BFA</code> which was responsible for the creation of this variant.
	 */
	public final BFA _BFA;

	/**
	 * The context which resulted in the creation of this variant.
	 */
	public final Context _CONTEXT;

	/**
	 * The method represented by this variant.
	 */
	public final SootMethod _METHOD;

	/**
	 * The manager of AST node variants.  This is required as in Jimple, the same AST node instance may occur at different
	 * locations in the AST as it serves the purpose of AST representation.
	 */
	protected final ASTVariantManager astvm;

	/**
	 * The statement visitor used to process in the statement in the correpsonding method.
	 */
	protected AbstractStmtSwitch stmt;

	/**
	 * The flow graph node associated with an abstract single return point of the corresponding method.  This will be
	 * <code>null</code>, if the associated method's return type is <code>void</code>.
	 */
	protected final IFGNode returnVar;

	/**
	 * The flow graph nodes associated with the this variable of the corresponding method.  This will be <code>null</code>,
	 * if the associated method is <code>static</code>.
	 */
	protected final IFGNode thisVar;

	/**
	 * The array of flow graph nodes associated with the parameters of thec corresponding method.  This will be
	 * <code>null</code>, if the associated method has not parameters..
	 */
	protected final IFGNode parameters[];

	/**
	 * This provides the def sites for local variables in the associated method.  This is used in conjunction with
	 * flow-sensitive information calculation.
	 */
	protected SimpleLocalDefs defs;

	/**
	 * Creates a new <code>MethodVariant</code> instance.
	 *
	 * @param sm the method represented by this variant.  This parameter cannot be <code>null</code>.
	 * @param astvm the manager of flow graph nodes corresponding to the AST nodes of <code>sm</code>.  This parameter cannot
	 *           be <code>null</code>.
	 * @param bfa the instance of <code>BFA</code> which was responsible for the creation of this variant.  This parameter
	 *           cannot be <code>null</code>.
	 */
	protected MethodVariant(SootMethod sm, ASTVariantManager astvm, BFA bfa) {
		_METHOD = sm;
		_BFA = bfa;
		_CONTEXT = (Context) bfa._ANALYZER.context.clone();
		bfa._ANALYZER.context.callNewMethod(sm);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: preprocessing of " + sm);
		}

		int pCount = sm.getParameterCount();

		if (pCount > 0) {
			parameters = new AbstractFGNode[pCount];

			for (int i = 0; i < pCount; i++) {
				parameters[i] = bfa.getNewFGNode();
				bfa.processType(sm.getParameterType(i));
			}
		} else {
			parameters = new AbstractFGNode[0];
		}

		if (sm.isStatic()) {
			thisVar = null;
		} else {
			thisVar = bfa.getNewFGNode();

			/*
			 * NOTE: This is required to filter out values which are descendents of a higher common type but which are
			 * incompatible.  An examples is all objects entering run() site will have a run() method defined.  However, it
			 * is false to assume that all such objects can be considered as receivers for all run() implementations plugged
			 * into the run() site.
			 */
			thisVar.setFilter(new TypeBasedFilter(sm.getDeclaringClass(), bfa));
		}
		bfa.processClass(sm.getDeclaringClass());

		if (sm.getReturnType() instanceof VoidType) {
			returnVar = null;
		} else {
			returnVar = bfa.getNewFGNode();
			bfa.processType(sm.getReturnType());
		}

		this.astvm = astvm;
		bfa._ANALYZER.context.returnFromCurrentMethod();

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
	 */
	public final IFGNode getASTNode(Value v) {
		return getASTVariant(v, _CONTEXT).getFGNode();
	}

	/**
	 * Returns the flow graph node associated with the given AST node in the given context.  Creates a new one if none
	 * exists.
	 *
	 * @param v the AST node whose associted flow graph node is to be returned.
	 * @param c the context in which the flow graph node was associated with <code>v</code>.
	 *
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.
	 */
	public final IFGNode getASTNode(Value v, Context c) {
		return getASTVariant(v, c).getFGNode();
	}

	/**
	 * Returns the variant associated with the given AST node in the context defined by <code>this.context</code>.  Creates a
	 * new one if none exists.
	 *
	 * @param v the AST node whose associted variant is to be returned.
	 *
	 * @return the variant associated with <code>v</code> in the context <code>this.context</code>.
	 */
	public final ASTVariant getASTVariant(Value v) {
		_BFA.processType(v.getType());
		return (ASTVariant) astvm.select(v, _CONTEXT);
	}

	/**
	 * Returns the variant associated with the given AST node in the given context.  Creates a new one if none exists.
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param context the context in which the variant was associated with <code>v</code>.
	 *
	 * @return the variant associated with <code>v</code> in the context <code>c</code>.
	 */
	public final ASTVariant getASTVariant(Value v, Context context) {
		return (ASTVariant) astvm.select(v, context);
	}

	/**
	 * Returns the definitions of local variable <code>l</code> that arrive at statement <code>s</code>.
	 *
	 * @param l the local for which the definitions are requested.
	 * @param s the statement at which the definitions are requested.
	 *
	 * @return the list of definitions of <code>l</code> that arrive at statement <code>s</code>.
	 */
	public List getDefsOfAt(Local l, Stmt s) {
		if (defs == null) {
			return new ArrayList();
		} else {
			return defs.getDefsOfAt(l, s);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param v DOCUMENT ME!
	 * @param c DOCUMENT ME!
	 * @param filter DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public IFGNode getFilterEnabledNode(Value v, Context c, ValueFilter filter) {
		IFGNode result = getASTNode(v, c);
		result.setFilter(filter);
		return result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param v DOCUMENT ME!
	 * @param filter DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public IFGNode getFilterEnabledNode(Value v, ValueFilter filter) {
		IFGNode result = getASTNode(v, _CONTEXT);
		result.setFilter(filter);
		return result;
	}

	/**
	 * Processes the body of the method implementation associated with this variant.
	 */
	public void process() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing of " + _METHOD);
		}

		JimpleBody jb = null;

		/*
		   The question is how does one close a system.  If we try to load up bodies that are not loaded then this will lead
		   to explosion of methods to be processed.  So, a better bet is that the user provides those classes crucial to start
		   with and then we load up methods in a restricted way.  Say only, for methods occurring in classes defined in
		   package java.lang.
		 */
		String className = _METHOD.getDeclaringClass().getName();

		if (className.indexOf("java") == 0 && (className.indexOf("java.lang.") == 0 || className.indexOf("java.util") == 0)) {
			try {
				jb = Util.getJimpleBody(_METHOD);
			} catch (RuntimeException e) {
				LOGGER.warn("Could not get body for " + _METHOD.getSignature(), e);
			}
		} else if (_METHOD.isBodyStored(JIMPLEBODYREP)) {
			jb = Util.getJimpleBody(_METHOD);
		}

		if (jb != null) {
			StmtList list = ((StmtBody) _METHOD.getBody(JIMPLEBODYREP)).getStmtList();
			defs = new SimpleLocalDefs(new CompleteStmtGraph(list));
			stmt = _BFA.getStmt(this);

			for (Iterator i = list.iterator(); i.hasNext();) {
				stmt.process((Stmt) i.next());
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

				for (int j = list.indexOf(begin), k = list.indexOf(end); j < k; j++) {
					Stmt tmp = (Stmt) list.get(j);

					if (tmp instanceof ThrowStmt) {
						ThrowStmt ts = (ThrowStmt) tmp;

						if (!caught.contains(ts)
							  && Util.isDescendentOf(_BFA.getClass(((RefType) ts.getOp().getType()).className), exception)) {
							_CONTEXT.setStmt(ts);

							IFGNode throwNode = getASTNode(ts.getOp(), _CONTEXT);
							throwNode.addSucc(getASTNode(catchRef));
							caught.add(ts);
						}
					} else if (tmp instanceof InvokeStmt) {
						expr = (InvokeExpr) ((InvokeStmt) tmp).getInvokeExpr();
						flag = true;
					} else if (tmp instanceof AssignStmt && ((AssignStmt) tmp).getRightOp() instanceof InvokeExpr) {
						expr = (InvokeExpr) ((AssignStmt) tmp).getRightOp();
						flag = true;
					}

					if (flag) {
						flag = false;

						if (!caught.contains(tmp)) {
							_CONTEXT.setStmt(tmp);

							IFGNode tempNode = queryThrowNode(expr, exception);

							if (tempNode != null) {
								tempNode.addSucc(getASTNode(catchRef));
							}
						}
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processing of " + _METHOD);
		}
	}

	/**
	 * Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.
	 *
	 * @param v the AST node whose associted variant is to be returned.
	 *
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.  If none exists,
	 *            <code>null</code> is returned.
	 */
	public final IFGNode queryASTNode(Value v) {
		return queryASTNode(v, _CONTEXT);
	}

	/**
	 * Same as <code>getASTNode</code>, except <code>null</code> is returned if none exists.
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param c the context in which the variant was associated with <code>v</code>.
	 *
	 * @return the flow graph node associated with <code>v</code> in context <code>c</code>.  If none exists,
	 *            <code>null</code> is returned.
	 */
	public final IFGNode queryASTNode(Value v, Context c) {
		ASTVariant var = queryASTVariant(v, c);
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
	 *            is returned.
	 */
	public final ASTVariant queryASTVariant(Value v, Context c) {
		return (ASTVariant) astvm.query(v, c);
	}

	/**
	 * Returns the flow graph node associated with the given parameter.
	 *
	 * @param index the index of the parameter in the parameter list of the associated method.
	 *
	 * @return the flow graph node associated with the <code>index</code>th parameter in the parameter list of the associated
	 *            method.  It returns <code>null</code> if the method has no parameters.
	 */
	public final IFGNode queryParameterNode(int index) {
		IFGNode temp = null;

		if (index >= 0 && index <= _METHOD.getParameterCount()) {
			temp = parameters[index];
		}

		return temp;
	}

	/**
	 * Returns the flow graph node that represents an abstract single return point of the associated method.
	 *
	 * @return the flow graph node that represents an abstract single return point of the associated method.
	 *            <code>null</code> if the corresponding method does not return a value.
	 */
	public final IFGNode queryReturnNode() {
		return returnVar;
	}

	/**
	 * Returns the flow graph node associated with the <code>this</code> variable of the associated method.
	 *
	 * @return Returns the flow graph node associated with the <code>this</code> variable of the associated method.
	 *            <code>null</code> if the corresponding method is <code>static</code>.
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
	 */
	public final IFGNode queryThrowNode(InvokeExpr e, SootClass exception) {
		return queryThrowNode(e, exception, _CONTEXT);
	}

	/**
	 * Returns the flow graph node associated with <code>exception</code> class at invoke expression <code>e</code>.
	 *
	 * @param e is the method invoke expression.
	 * @param exception is the class of the exception thrown at <code>e</code>.
	 * @param c is the context in which the node is requested.
	 *
	 * @return the node that captures values associated with the <code>exception</code> class at <code>e</code>.
	 */
	public final IFGNode queryThrowNode(InvokeExpr e, SootClass exception, Context c) {
		InvocationVariant var = (InvocationVariant) queryASTVariant(e, c);
		IFGNode temp = null;

		if (var != null) {
			temp = var.queryThrowNode(exception);
		}
		return temp;
	}
}

/*****
 ChangeLog:

$Log$

*****/
