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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IVariantManager;
import edu.ksu.cis.indus.staticanalyses.flow.ValuedVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Type;
import soot.Value;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

/**
 * The variant that represents a method implementation. It maintains variant specific information about local variables and
 * the AST nodes in associated method. It also maintains information about the parameters, this variable, and return values,
 * if any are present.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 * @param <T>  is the type of the token set object.
 */
class MethodVariant<T extends ITokens<T, Value>>
		extends AbstractMethodVariant<Value, T, OFAFGNode<T>, Type> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodVariant.class);

	/**
	 * The statement graph to use to retrieve method bodies.
	 */
	private IStmtGraphFactory<?> stmtGraphFactory;

	/**
	 * Creates a new <code>MethodVariant</code> instance. This will not process the statements of this method. That is
	 * accomplished via call to <code>process()</code>. This will also mark the field with the flow analysis tag.
	 * 
	 * @param sm the method represented by this variant. This parameter cannot be <code>null</code>.
	 * @param astVariantManager the manager of flow graph nodes corresponding to the AST nodes of<code>sm</code>. This
	 *            parameter cannot be <code>null</code>.
	 * @param theFA the instance of <code>FA</code> which was responsible for the creation of this variant. This parameter
	 *            cannot be <code>null</code>.
	 * @param factory provides the CFGs.
	 * @pre sm != null and astVariantManager != null and theFA != null and factory != null
	 */
	protected MethodVariant(final SootMethod sm, final IVariantManager<ValuedVariant<OFAFGNode<T>>, Value> astVariantManager,
			final FA<Value, T, OFAFGNode<T>, Type> theFA, final IStmtGraphFactory<?> factory) {
		super(sm, astVariantManager, theFA);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: preprocessing of " + sm);
		}

		/*
		 * NOTE: This is required to filter out values which are descendents of a higher common type but which are
		 * incompatible. An example is all objects entering run() site will have a run() method defined. However, it is false
		 * to assume that all such objects can be considered as receivers for all run() implementations plugged into the run()
		 * site.
		 */
		final ITokenManager<T, Value, Type> _tokenMgr = fa.getTokenManager();

		if (thisVar != null) {
			final RefType _sootType = sm.getDeclaringClass().getType();
			setFilterOfBasedOn(thisVar, _sootType, _tokenMgr);
		}

		// We also want to use retrieve acceptable values from other interfacial data entities.
		if (returnVar != null) {
			setFilterOfBasedOn(returnVar, sm.getReturnType(), _tokenMgr);
		}

		for (int _i = parameters.size() - 1; _i >= 0; _i--) {
			final OFAFGNode<T> _pNode = parameters.get(_i);

			if (_pNode != null) {
				setFilterOfBasedOn(_pNode, sm.getParameterType(_i), _tokenMgr);
			}
		}

		setFilterOfBasedOn(thrownNode, this.fa.getClass("java.lang.Throwable").getType(), _tokenMgr);

		stmtGraphFactory = factory;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: preprocessed " + sm);
		}
	}

	/**
	 * Sets the out filter based on the given type for the given node.
	 * 
	 * @param <T>  is the type of the token set object.
	 * @param node of interest.
	 * @param type for the filter.
	 * @param tokenMgr used in the creation of the type-based filter.
	 * @pre node != null and type != null and tokenMgr != null
	 */
	static <T extends ITokens<T, Value>> void setFilterOfBasedOn(final OFAFGNode<T> node, final Type type,
			final ITokenManager<T, Value, Type> tokenMgr) {
		if (node != null) {
			final IType _baseType = tokenMgr.getTypeManager().getTokenTypeForRepType(type);
			final ITokenFilter<T, Value> _baseFilter = tokenMgr.getTypeBasedFilter(_baseType);
			node.setFilter(_baseFilter);
		}
	}

	/**
	 * Processes the body of the method implementation associated with this variant.
	 */
	public void process() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing of " + method);
		}

		// We assume the user has closed the system.
		if (method.isConcrete()) {
			final JimpleBody _jb = (JimpleBody) stmtGraphFactory.getStmtGraph(method).getBody();
			final List<Stmt> _stmtList = new ArrayList<Stmt>(_jb.getUnits());

			for (final Iterator<Stmt> _i = _stmtList.iterator(); _i.hasNext();) {
				stmt.process(_i.next());
			}

			processBody(_jb, _stmtList);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(method + " is not a concrete method. Hence, it's body could not be retrieved.");
			}
		}

		stmtGraphFactory = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processing of " + method);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.AbstractMethodVariant#shouldConsider(soot.Type)
	 */
	@Override protected boolean shouldConsider(final Type type) {
		return Util.isReferenceType(type);
	}

	/**
	 * Connects the nodes corresponding the exceptions thrown (throw and method invocations) in the body to the nodd
	 * corresponding to the expression thrown by the method.
	 * 
	 * @param body of the method.
	 * @param stmtList is the list of statements that make up the body.
	 * @pre body != null and stmtList != null
	 */
	private void connectThrowNodesToThrownNode(final JimpleBody body, final List<Stmt> stmtList) {
		final Context _ctxt = new Context();
		_ctxt.setRootMethod(method);

		final Iterator<Stmt> _j = stmtList.iterator();
		final int _jEnd = stmtList.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Stmt _stmt = _j.next();
			_ctxt.setStmt(_stmt);

			if (_stmt instanceof ThrowStmt) {
				final Collection<Trap> _t = TrapManager.getTrapsAt(_stmt, body);
				final ThrowStmt _throwStmt = (ThrowStmt) _stmt;
				final SootClass _exp = ((RefType) _throwStmt.getOp().getType()).getSootClass();
				boolean _exceptionIsUncaught = true;
				for (final Iterator<Trap> _i = _t.iterator(); _i.hasNext() && _exceptionIsUncaught;) {
					final Trap _trap = _i.next();
					final SootClass _sc = _trap.getException();
					_exceptionIsUncaught = !_exp.equals(_sc) && !Util.isDescendentOf(_exp, _sc);
				}

				if (_t.isEmpty() || _exceptionIsUncaught) {
					_ctxt.setProgramPoint(_throwStmt.getOpBox());
					queryASTNode(_throwStmt.getOp(), _ctxt).addSucc(thrownNode);
				}
			} else if (_stmt.containsInvokeExpr()) {
				_ctxt.setProgramPoint(_stmt.getInvokeExprBox());
				queryThrowNode(_stmt.getInvokeExpr(), _ctxt).addSucc(thrownNode);
			}
		}
	}

	/**
	 * Process the body.
	 * 
	 * @param body to be processed.
	 * @param stmtList is the list of statements that make up the body.
	 * @pre body != null and stmtList != null
	 */
	private void processBody(final JimpleBody body, final List<Stmt> stmtList) {
		final Collection<Stmt> _caught = new HashSet<Stmt>();
		final Context _exprCtxt = new Context();
		final Context _catchCtxt = new Context();

		_exprCtxt.setRootMethod(method);
		_catchCtxt.setRootMethod(method);

		for (final Iterator<Trap> _i = body.getTraps().iterator(); _i.hasNext();) {
			final Trap _trap = _i.next();
			final Stmt _begin = (Stmt) _trap.getBeginUnit();
			final Stmt _end = (Stmt) _trap.getEndUnit();

			// we assume that the first statement in the handling block will be the identity statement that retrieves the
			// caught expression.
			final IdentityStmt _handlerStmt = (IdentityStmt) _trap.getHandlerUnit();
			final CaughtExceptionRef _catchRef = (CaughtExceptionRef) _handlerStmt.getRightOp();
			final SootClass _exception = _trap.getException();

			_catchCtxt.setStmt(_handlerStmt);
			_catchCtxt.setProgramPoint(_handlerStmt.getRightOpBox());

			final int _k = stmtList.indexOf(_end);

			for (int _j = stmtList.indexOf(_begin); _j < _k; _j++) {
				final Stmt _tmp = stmtList.get(_j);

				if (!_caught.contains(_tmp)) {
					_exprCtxt.setStmt(_tmp);

					if (_tmp instanceof ThrowStmt) {
						final ThrowStmt _ts = (ThrowStmt) _tmp;
						final Value _op = _ts.getOp();
						final SootClass _scTemp = fa.getClass(((RefType) _op.getType()).getClassName());

						if (Util.isDescendentOf(_scTemp, _exception)) {
							_exprCtxt.setProgramPoint(_ts.getOpBox());
							final OFAFGNode<T> _throwNode = getASTNode(_op, _exprCtxt);
							_throwNode.addSucc(queryASTNode(_catchRef, _catchCtxt));
							_caught.add(_ts);
						}
					} else if (_tmp.containsInvokeExpr()) {
						_exprCtxt.setProgramPoint(_tmp.getInvokeExprBox());
						final OFAFGNode<T> _tempNode = queryThrowNode(_tmp.getInvokeExpr(), _exprCtxt);

						if (_tempNode != null) {
							_tempNode.addSucc(queryASTNode(_catchRef, _catchCtxt));
						}
					}
				}
			}
		}

		connectThrowNodesToThrownNode(body, stmtList);
	}
}

// End of File
