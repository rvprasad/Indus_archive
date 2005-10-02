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

package edu.ksu.cis.indus.common.soot;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.VoidType;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;

import soot.toolkits.graph.UnitGraph;

/**
 * This class provides the an abstract implementation of <code>IStmtGraphFactory</code> via which unit graphs can be
 * retrieved. The subclasses should provide suitable unit graph implementation. The control flow edges in the provided unit
 * graphs are pruned by matching the thrown exceptions to the enclosing catch blocks. Refer to
 * <code>Util.pruneExceptionBasedControlFlow()</code> for more information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> the type of the manufactored graph.
 */
public abstract class AbstractStmtGraphFactory<T extends UnitGraph>
		implements IStmtGraphFactory<T> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStmtGraphFactory.class);

	/**
	 * This maps methods to unit graphs.
	 * 
	 * @invariant method2UnitGraph != null and method2UnitGraph.oclIsKindOf(Map(SootMethod, UnitGraph))
	 */
	private final Map<SootMethod, Reference<T>> method2UnitGraph = new HashMap<SootMethod, Reference<T>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * Retrieves the unit graph of the given method.
	 * 
	 * @param method for which the unit graph is requested.
	 * @return the requested unit graph.
	 * @post result != null
	 * @post method.isConcrete() implies result.getBody() = method.getBody()
	 * @post 1method.isConcrete() implies result.getBody() != method.getBody()
	 */
	public final T getStmtGraph(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getStmtGraph(method = " + method + ")");
		}

		final Reference<T> _ref = method2UnitGraph.get(method);
		T _result = null;
		boolean _flag = false;

		if (_ref == null) {
			_flag = true;
		} else {
			_result =  _ref.get();

			if (_result == null) {
				_flag = true;
			}
		}

		if (_flag) {
			if (method.isConcrete()) {
				final JimpleBody _body = (JimpleBody) method.retrieveActiveBody();
				_result = getStmtGraphForBody(_body);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Method " + method + " is not concrete.");
			}

			if (_result == null) {
				// stub in an empty graph.
				final Jimple _jimple = Jimple.v();
				final JimpleBody _body = _jimple.newBody();
				_body.setMethod(method);

				@SuppressWarnings("unchecked") final Collection<Object> _units = _body.getUnits();

				if (method.getReturnType() instanceof VoidType) {
					_units.add(_jimple.newReturnVoidStmt());
				} else {
					_units.add(_jimple.newReturnStmt(Util.getDefaultValueFor(method.getReturnType())));
				}
				_result = getStmtGraphForBody(_body);
			}
			method2UnitGraph.put(method, new WeakReference<T>(_result));
		}
		return _result;
	}

	/**
	 * Resets all internal datastructures.
	 */
	public final void reset() {
		method2UnitGraph.clear();
	}

	/**
	 * Retreives the unit graph (of a particular implementation) for the given body.
	 * 
	 * @param body to be represented as a graph.
	 * @return a unit graph.
	 * @pre body != null
	 * @post result != null
	 */
	protected abstract T getStmtGraphForBody(final JimpleBody body);

	/**
	 * Retrieves the body for the given method.  This method split traps based on overlap and subtyping relation
	 * between the exceptions trapped by overlapping regions.
	 * 
	 * @param method of interest.
	 * @return the jimple body of the given method.
	 * @pre method != null
	 */
	/*private JimpleBody getMethodBody(final SootMethod method) {
		final JimpleBody _body = (JimpleBody) method.retrieveActiveBody();
		final List<Trap> _newTraps = new ArrayList<Trap>();
		final Jimple _jimple = Jimple.v();
		@SuppressWarnings("unchecked") final List<Stmt> _stmts = new ArrayList<Stmt>(_body.getUnits());
		final IWorkBag<Trap> _traps = new FIFOWorkBag<Trap>();
		_traps.addAllWork(_body.getTraps());

		while (_traps.hasWork()) {
			final Trap _trap1 = _traps.getWork();
			final SootClass _sc1 = _trap1.getException();
			int _bIndex1 = _stmts.indexOf(_trap1.getBeginUnit());
			int _eIndex1 = _stmts.indexOf(_trap1.getEndUnit());
			boolean _retainTrap = true;
			for (final Iterator<Trap> _i = _newTraps.iterator(); _i.hasNext();) {
				final Trap _trap2 = _i.next();
				final SootClass _sc2 = _trap2.getException();
				if (_sc1.equals(_sc2) || Util.isDescendentOf(_sc1, _sc2)) {
					final int _bIndex2 = _stmts.indexOf(_trap2.getBeginUnit());
					final int _eIndex2 = _stmts.indexOf(_trap2.getEndUnit());
					// A trap's begin boundary is inclusive while the end boundary is exclusive
					if (_bIndex1 < _eIndex2 && _eIndex1 > _bIndex2) {
						if (_eIndex1 <= _eIndex2) {
							if (_bIndex1 >= _bIndex2) {
								// position: _bIndex2 _bIndex1 _eIndex1 _eIndex2
								_retainTrap = false;
							} else {
								 // if (_bIndex1 < _bIndex2) position: _bIndex1 _bIndex2 _eIndex1 _eIndex2
								_eIndex1 = _bIndex2;
							}
						} else {
							// if (_eIndex1 > _eIndex2)
							if (_bIndex1 >= _bIndex2) {
								// position: _bIndex2 _bIndex1 _eIndex2 _eIndex1
								_bIndex1 = _eIndex2;
							} else {
								 // if (_bIndex1 < _bIndex2) position: _bIndex1 _bIndex2 _eIndex2 _eIndex1
								_traps.addWork(_jimple.newTrap(_sc1, _stmts.get(_eIndex2), _trap1.getEndUnit(), _trap1
										.getHandlerUnit()));
								_eIndex1 = _bIndex2;
							}
						}
					}
				}
			}
			if (_retainTrap && _bIndex1 < _eIndex1) {
				_newTraps.add(_trap1);
				_trap1.setBeginUnit(_stmts.get(_bIndex1));
				_trap1.setEndUnit(_stmts.get(_eIndex1));
			}
		}

		for (final Iterator<Trap> _i = _newTraps.iterator(); _i.hasNext();) {
			final Trap _t = _i.next();
			if (_t.getBeginUnit() == _t.getEndUnit()) {
				_i.remove();
			}
		}

		@SuppressWarnings("unchecked") final Collection<Trap> _t = _body.getTraps();
		_t.clear();
		_t.addAll(_newTraps);
		return _body;
	}*/
}

// End of File
