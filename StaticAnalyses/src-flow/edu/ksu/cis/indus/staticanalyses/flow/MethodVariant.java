
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;

import java.lang.ref.WeakReference;

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
	 * This is a weak reference to the local def information and it provides the def sites for local variables in the
	 * associated method.  This is used in conjunction with flow-sensitive information calculation.
	 */
	protected WeakReference defs = new WeakReference(null);

	/** 
	 * This indicates if the method variant is unretrievale due to various reasons such as non-concrete body.
	 */
	protected boolean unRetrievable;

	/** 
	 * The context which resulted in the creation of this variant.
	 *
	 * @invariant context != null
	 */
	private final Context context;

	/** 
	 * The instance of <code>FA</code> which was responsible for the creation of this variant.
	 *
	 * @invariant fa != null
	 */
	private final FA fa;

	/** 
	 * The method represented by this variant.
	 *
	 * @invariant method != null
	 */
	private final SootMethod method;

	/**
	 * Creates a new <code>MethodVariant</code> instance.  This will not process the statements of this method.  That is
	 * accomplished via call to <code>process()</code>.  This will also mark the field with the flow analysis tag.
	 *
	 * @param sm the method represented by this variant.  This parameter cannot be <code>null</code>.
	 * @param astVariantManager the manager of flow graph nodes corresponding to the AST nodes of<code>sm</code>.  This
	 * 		  parameter cannot be <code>null</code>.
	 * @param theFA the instance of <code>FA</code> which was responsible for the creation of this variant.  This parameter
	 * 		  cannot be <code>null</code>.
	 *
	 * @pre sm != null and astvm != null and theFA != null
	 */
	protected MethodVariant(final SootMethod sm, final AbstractVariantManager astVariantManager, final FA theFA) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: preprocessing of " + sm);
		}
		method = sm;
		fa = theFA;
		context = (Context) fa.getAnalyzer().context.clone();
		context.callNewMethod(sm);

		final Collection _typesToProcess = new HashSet();
		final int _pCount = sm.getParameterCount();

		if (_pCount > 0) {
			parameters = new AbstractFGNode[_pCount];

			for (int _i = 0; _i < _pCount; _i++) {
				if (sm.getParameterType(_i) instanceof RefLikeType) {
					parameters[_i] = fa.getNewFGNode();
					_typesToProcess.add(sm.getParameterType(_i));
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
			final ITokenManager _tokenMgr = fa.getTokenManager();
			final RefType _sootType = sm.getDeclaringClass().getType();
			thisVar.setFilter(_tokenMgr.getTypeBasedFilter(_tokenMgr.getTypeManager().getTypeForIRType(_sootType)));
		}

		if (sm.getReturnType() instanceof RefLikeType) {
			returnVar = fa.getNewFGNode();
			_typesToProcess.add(sm.getReturnType());
		} else {
			returnVar = null;
		}

		astvm = astVariantManager;
		sm.addTag(fa.getTag());

		// process the types required by the method body        
		fa.processClass(sm.getDeclaringClass());

		for (final Iterator _i = _typesToProcess.iterator(); _i.hasNext();) {
			fa.processType((Type) _i.next());
		}

		if (method.isConcrete()) {
			final JimpleBody _jb = (JimpleBody) method.retrieveActiveBody();

			for (final Iterator _i = _jb.getLocals().iterator(); _i.hasNext();) {
				final Type _localType = ((Local) _i.next()).getType();

				if (_localType instanceof RefLikeType) {
					fa.processType(_localType);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: preprocessed " + sm);
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
		return getASTVariant(v, context).getFGNode();
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
	 * Retrieves the context used by this method variant.
	 *
	 * @return the context used by this method.
	 *
	 * @post result != null
	 */
	public final Context getContext() {
		return context;
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
		List _result = Collections.EMPTY_LIST;

		if (!unRetrievable) {
			final SimpleLocalDefs _temp = (SimpleLocalDefs) defs.get();

			if (_temp != null) {
				_result = _temp.getDefsOfAt(l, s);
			} else {
				if (method.hasActiveBody()) {
					final SimpleLocalDefs _temp2 = new SimpleLocalDefs(new CompleteUnitGraph(method.retrieveActiveBody()));
					defs = new WeakReference(_temp2);
					_result = _temp2.getDefsOfAt(l, s);
				} else {
					unRetrievable = true;
				}
			}
		}
		return _result;
	}

	/**
	 * Retrieves the flow analysis instance used by this method variant.
	 *
	 * @return the flow analysis instance used by this method.
	 *
	 * @post result != null
	 */
	public final FA getFA() {
		return fa;
	}

	/**
	 * Retrieves the method used by this method variant.
	 *
	 * @return the method used by this method.
	 *
	 * @post result != null
	 */
	public final SootMethod getMethod() {
		return method;
	}

	/**
	 * Processes the body of the method implementation associated with this variant.
	 */
	public void process() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing of " + method);
		}

		JimpleBody _jb = null;

		// We assume the user has closed the system.
		if (method.isConcrete()) {
			_jb = (JimpleBody) method.retrieveActiveBody();

			final List _stmtList = new ArrayList(_jb.getUnits());
			stmt = fa.getStmt(this);

			for (final Iterator _i = _stmtList.iterator(); _i.hasNext();) {
				stmt.process((Stmt) _i.next());
			}

			processBody(_jb, _stmtList);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(method + " is not a concrete method. Hence, it's body could not be retrieved.");
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processing of " + method);
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
		return queryASTNode(v, context);
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
		final ValuedVariant _var = queryASTVariant(v, c);
		IFGNode _temp = null;

		if (_var != null) {
			_temp = _var.getFGNode();
		}
		return _temp;
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
		IFGNode _temp = null;

		if (index >= 0 && index <= method.getParameterCount()) {
			_temp = parameters[index];
		}

		return _temp;
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
		return queryThrowNode(e, exception, context);
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
		final InvocationVariant _var = (InvocationVariant) queryASTVariant(e, c);
		IFGNode _temp = null;

		if (_var != null) {
			_temp = _var.queryThrowNode(exception);
		}
		return _temp;
	}

	/**
	 * Returns the variant associated with the given AST node in the given context.  Creates a new one if none exists.
	 *
	 * @param v the AST node whose associated variant is to be returned.
	 * @param ctxt the context in which the variant was associated with <code>v</code>.
	 *
	 * @return the variant associated with <code>v</code> in the context <code>c</code>.
	 *
	 * @pre v != null and ctxt != null
	 */
	final ValuedVariant getASTVariant(final Value v, final Context ctxt) {
		return (ValuedVariant) astvm.select(v, ctxt);
	}

	/**
	 * Process the body.
	 *
	 * @param body to be processed.
	 * @param stmtList is the list of statements that make up the body.
	 *
	 * @pre body != null and stmtList != null
	 */
	private void processBody(final JimpleBody body, final List stmtList) {
		final Collection _caught = new HashSet();
		boolean _flag = false;
		InvokeExpr _expr = null;

		for (final Iterator _i = body.getTraps().iterator(); _i.hasNext();) {
			final Trap _trap = (Trap) _i.next();
			final Stmt _begin = (Stmt) _trap.getBeginUnit();
			final Stmt _end = (Stmt) _trap.getEndUnit();

			// we assume that the first statement in the handling block will be the identity statement that retrieves the 
			// caught expression.
			final CaughtExceptionRef _catchRef = (CaughtExceptionRef) ((IdentityStmt) _trap.getHandlerUnit()).getRightOp();
			final SootClass _exception = _trap.getException();

			final int _k = stmtList.indexOf(_end);

			for (int _j = stmtList.indexOf(_begin); _j < _k; _j++) {
				final Stmt _tmp = (Stmt) stmtList.get(_j);

				if (_tmp instanceof ThrowStmt) {
					final ThrowStmt _ts = (ThrowStmt) _tmp;

					if (!_caught.contains(_ts)) {
						final SootClass _scTemp = fa.getClass(((RefType) _ts.getOp().getType()).getClassName());

						if (Util.isDescendentOf(_scTemp, _exception)) {
							context.setStmt(_ts);

							final IFGNode _throwNode = getASTNode(_ts.getOp(), context);
							_throwNode.addSucc(getASTNode(_catchRef));
							_caught.add(_ts);
						}
					}
				} else if (_tmp.containsInvokeExpr()) {
					_expr = _tmp.getInvokeExpr();
					_flag = true;
				}

				if (_flag) {
					_flag = false;

					if (!_caught.contains(_tmp)) {
						context.setStmt(_tmp);

						final IFGNode _tempNode = queryThrowNode(_expr, _exception);

						if (_tempNode != null) {
							_tempNode.addSucc(getASTNode(_catchRef));
						}
					}
				}
			}
		}
	}
}

// End of File
