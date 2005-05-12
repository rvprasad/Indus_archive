
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.TrapManager;

import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

import soot.toolkits.graph.UnitGraph;


/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ThrowAnalysis
  extends AbstractProcessor {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThrowAnalysis.class);

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final ICallGraphInfo cgi;

	/** 
	 * DOCUMENT ME!
	 *
	 * @invariant method2stmt2thrownTypes.oclIsKindOf(Map(SootMethod, Map(Stmt, Collection(SootClass))))
	 */
	private final Map method2stmt2thrownTypes = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IStmtGraphFactory stmtGraphFactory;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IWorkBag workbagCache;

	/**
	 * Creates an instance of this class.
	 *
	 * @param factory
	 * @param callgraph
	 */
	public ThrowAnalysis(final IStmtGraphFactory factory, final ICallGraphInfo callgraph) {
		stmtGraphFactory = factory;
		cgi = callgraph;
		workbagCache = new HistoryAwareFIFOWorkBag(new ArrayList());
	}

	/**
	 * @see AbstractProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final SootClass _thrownType = ((RefType) ((ThrowStmt) stmt).getOp().getType()).getSootClass();
		final SootMethod _method = context.getCurrentMethod();

		if (isThrownExceptionNotCaught(stmt, _method, _thrownType)) {
			workbagCache.addWorkNoDuplicates(new Triple(stmt, _method, _thrownType));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - BEGIN");
		}

		super.consolidate();

		while (workbagCache.hasWork()) {
			final Triple _triple = (Triple) workbagCache.getWork();
			final Stmt _stmt = (Stmt) _triple.getFirst();
			final SootMethod _method = (SootMethod) _triple.getSecond();
			final SootClass _thrownType = (SootClass) _triple.getThird();
			final Collection _callers = cgi.getCallers(_method);
			final Iterator _j = _callers.iterator();
			final int _jEnd = _callers.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _j.next();

				final SootMethod _caller = _ctrp.getMethod();

				if (isThrownExceptionNotCaught(_ctrp.getStmt(), _caller, _thrownType)) {
					workbagCache.addWorkNoDuplicates(new Pair(_caller, _thrownType));
				}
			}
			CollectionsUtilities.putIntoSetInMap(CollectionsUtilities.getMapFromMap(method2stmt2thrownTypes, _method), _stmt,
				_thrownType);
		}

		workbagCache.clear();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - END - "
				+ CollectionsUtilities.prettyPrint("method-to-stmt-to-throwntypes", method2stmt2thrownTypes));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(ThrowStmt.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	public void reset() {
		super.reset();
		method2stmt2thrownTypes.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(ThrowStmt.class, this);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param stmt
	 * @param method
	 * @param thrownType
	 *
	 * @return
	 */
	private boolean isThrownExceptionNotCaught(final Stmt stmt, final SootMethod method, final SootClass thrownType) {
		final UnitGraph _graph = stmtGraphFactory.getStmtGraph(method);
		final List _succs = _graph.getSuccsOf(stmt);
		boolean _result = true;

		if (!_succs.isEmpty()) {
			boolean _isCaught = false;
			final Body _body = _graph.getBody();
			final Iterator _i = _succs.iterator();
			final int _iEnd = _succs.size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_isCaught; _iIndex++) {
				final Stmt _handler = (Stmt) _i.next();
				final Iterator _j = TrapManager.getExceptionTypesOf(_handler, _body).iterator();
				final int _jEnd = TrapManager.getExceptionTypesOf(_handler, _body).size();

				for (int _jIndex = 0; _jIndex < _jEnd && !_isCaught; _jIndex++) {
					final SootClass _caughtType = (SootClass) _j.next();
					_isCaught |= Util.isDescendentOf(thrownType, _caughtType);
				}
			}
			_result = !_isCaught;
		}
		return _result;
	}
}

// End of File
