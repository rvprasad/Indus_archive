
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

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;


/**
 * An abstract implementation of <code>IMethodVariant</code> with the most general content in place.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractMethodVariant
  implements IMethodVariant {
	/** 
	 * The context which resulted in the creation of this variant.
	 *
	 * @invariant context != null
	 */
	protected final Context context;

	/** 
	 * The instance of <code>FA</code> which was responsible for the creation of this variant.
	 *
	 * @invariant fa != null
	 */
	protected final FA fa;

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
	 * The flow graph node associated with an abstract single exceptional return point of the corresponding method.
	 */
	protected final IFGNode thrownNode;

	/** 
	 * The statement visitor used to process in the statement in the correpsonding method.
	 *
	 * @invariant stmt != null
	 */
	protected final IStmtSwitch stmt;

	/** 
	 * The manager of AST node variants.  This is required as in Jimple, the same AST node instance may occur at different
	 * locations in the AST as it serves the purpose of AST representation.
	 *
	 * @invariant astvm != null
	 */
	protected final IVariantManager astvm;

	/** 
	 * The method represented by this variant.
	 *
	 * @invariant method != null
	 */
	protected final SootMethod method;

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
	 * Creates an instance of this class.
	 *
	 * @param sm is the method be represented.
	 * @param astVariantManager to be used for the AST chunks of the represented method.
	 * @param theFA the instance of the flow framework with which this variant operates.
	 *
	 * @pre sm != null and astVariantManager != null and theFA != null
	 */
	protected AbstractMethodVariant(final SootMethod sm, final IVariantManager astVariantManager, final FA theFA) {
		super();
		method = sm;
		astvm = astVariantManager;
		fa = theFA;
		context = fa.getAnalyzer().getContext().clone();
		context.callNewMethod(sm);

		fa.processClass(sm.getDeclaringClass());
		sm.addTag(fa.getTag());

		final Collection _typesToProcess = new HashSet();
		final RefType _sootType = sm.getDeclaringClass().getType();

		if (!sm.isStatic() && shouldConsider(_sootType)) {
			thisVar = fa.getNewFGNode();
			_typesToProcess.add(_sootType);
		} else {
			thisVar = null;
		}

		final int _pCount = sm.getParameterCount();
		parameters = new IFGNode[_pCount];

		for (int _i = 0; _i < _pCount; _i++) {
			if (shouldConsider(sm.getParameterType(_i))) {
				parameters[_i] = fa.getNewFGNode();
				_typesToProcess.add(sm.getParameterType(_i));
			}
		}

		if (shouldConsider(sm.getReturnType())) {
			returnVar = fa.getNewFGNode();
			_typesToProcess.add(sm.getReturnType());
		} else {
			returnVar = null;
		}

		thrownNode = fa.getNewFGNode();

		stmt = fa.getStmt(this);

		if (method.isConcrete()) {
			final JimpleBody _jb = (JimpleBody) method.retrieveActiveBody();

			for (final Iterator _i = _jb.getLocals().iterator(); _i.hasNext();) {
				final Type _localType = ((Local) _i.next()).getType();

				if (shouldConsider(_localType)) {
					fa.processType(_localType);
				}
			}
		}

		for (final Iterator _i = _typesToProcess.iterator(); _i.hasNext();) {
			fa.processType((Type) _i.next());
		}
	}

	/**
	 * @see IMethodVariant#getASTNode(Value)
	 */
	public final IFGNode getASTNode(final Value v) {
		return getASTVariant(v, context).getFGNode();
	}

	/**
	 * @see IMethodVariant#getASTNode(Value, Context)
	 */
	public final IFGNode getASTNode(final Value v, final Context c) {
		return getASTVariant(v, c).getFGNode();
	}

	/**
	 * @see IMethodVariant#getASTVariant(Value, Context)
	 */
	public final ValuedVariant getASTVariant(final Value v, final Context ctxt) {
		return (ValuedVariant) astvm.select(v, ctxt);
	}

	/**
	 * @see IMethodVariant#getContext()
	 */
	public final Context getContext() {
		return context;
	}

	/**
	 * @see IMethodVariant#getFA()
	 */
	public final FA getFA() {
		return fa;
	}

	/**
	 * @see IMethodVariant#getMethod()
	 */
	public final SootMethod getMethod() {
		return method;
	}

	/**
	 * @see IMethodVariant#queryASTNode(Value)
	 */
	public final IFGNode queryASTNode(final Value v) {
		return queryASTNode(v, context);
	}

	/**
	 * @see IMethodVariant#queryASTNode(Value, Context)
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
	 * @see IMethodVariant#queryASTVariant(Value, Context)
	 */
	public final ValuedVariant queryASTVariant(final Value v, final Context c) {
		return (ValuedVariant) astvm.query(v, c);
	}

	/**
	 * @see IMethodVariant#queryParameterNode(int)
	 */
	public final IFGNode queryParameterNode(final int index) {
		IFGNode _temp = null;

		if (index >= 0 && index <= method.getParameterCount()) {
			_temp = parameters[index];
		}

		return _temp;
	}

	/**
	 * @see IMethodVariant#queryReturnNode()
	 */
	public final IFGNode queryReturnNode() {
		return returnVar;
	}

	/**
	 * @see IMethodVariant#queryThisNode()
	 */
	public final IFGNode queryThisNode() {
		return thisVar;
	}

	/**
	 * @see IMethodVariant#queryThrowNode(InvokeExpr, Context)
	 */
	public final IFGNode queryThrowNode(final InvokeExpr e, final Context c) {
		final InvocationVariant _var = (InvocationVariant) queryASTVariant(e, c);
		IFGNode _temp = null;

		if (_var != null) {
			_temp = _var.getThrowNode();
		}
		return _temp;
	}

	/**
	 * Retrieves the node corresponding to the exceptions thrown by this method variant.
	 *
	 * @return the node for thrown exceptions.
	 *
	 * @post result != null
	 */
	public final IFGNode queryThrownNode() {
		return thrownNode;
	}

	/**
	 * Decides if given type or references of it's type should be considered.
	 *
	 * @param type of interest.
	 *
	 * @return <code>true</code> if it should be considered; <code>false</code>, otherwise.
	 */
	protected abstract boolean shouldConsider(Type type);
}

// End of File
