
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

import edu.ksu.cis.indus.interfaces.IExceptionThrowInfo;

import java.lang.ref.SoftReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * This class manages a set of basic block graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class BasicBlockGraphMgr {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(BasicBlockGraphMgr.class);

	/** 
	 * This provides exception throwing information used to calculate basic block boundaries.
	 */
	private final IExceptionThrowInfo eti;

	/** 
	 * This maps methods to basic block graphs.
	 *
	 * @invariant method2graph.oclIsKindOf(Map(SootMethod, BasicBlockGraph))
	 */
	private final Map method2graph = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This provides <code>UnitGraph</code>s required to construct the basic block graphs.
	 */
	private IStmtGraphFactory stmtGraphProvider;

	/** 
	 * This maps methods to their statement list.
	 */
	private final Map method2stmtlist = new HashMap();

	/**
	 * Creates a new BasicBlockGraphMgr object.
	 */
	public BasicBlockGraphMgr() {
		this(null);
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param info provides excpetion throwing information.  If this is not provided then implicit exceptional exits are not
     * considered for graph construction.  
	 */
	public BasicBlockGraphMgr(final IExceptionThrowInfo info) {
		super();
		eti = info;
	}

	/**
	 * Retrieves the basic block graph corresponding to the given method.  Returns an empty basic block graph if the method
	 * is abstract or has no available implementation.
	 *
	 * @param sm is the method for which the graph is requested.
	 *
	 * @return the basic block graph corresponding to <code>sm</code>.
	 *
	 * @throws IllegalStateException when a statement graph factory was not set before calling this method.
	 *
	 * @pre sm != null
	 */
	public BasicBlockGraph getBasicBlockGraph(final SootMethod sm)
	  throws IllegalStateException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getBasicBlockGraph(SootMethod sm = " + sm + ") - BEGIN");
		}

		if (stmtGraphProvider == null) {
			throw new IllegalStateException("You need to set the unit graph provider via setStmtGraphFactory() before "
				+ "calling this method.");
		}

		final SoftReference _ref = (SoftReference) method2graph.get(sm);
		BasicBlockGraph _result = null;
		boolean _flag = false;

		if (_ref == null) {
			_flag = true;
		} else {
			_result = (BasicBlockGraph) _ref.get();

			if (_result == null) {
				_flag = true;
			}
		}

		if (_flag) {
			final UnitGraph _graph = stmtGraphProvider.getStmtGraph(sm);
			_result = new BasicBlockGraph(_graph, sm, eti);
			method2graph.put(sm, new SoftReference(_result));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getBasicBlockGraph() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * Provides the unit graph for the given method.  This is retrieved from the unit graph provider set via
	 * <code>setUnitGraphProvider</code>.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the unit graph for the method.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	public UnitGraph getStmtGraph(final SootMethod method) {
		return stmtGraphProvider.getStmtGraph(method);
	}

	/**
	 * Sets the unit graph provider.
	 *
	 * @param cfgProvider provides <code>UnitGraph</code>s required to construct the basic block graphs.
	 */
	public void setStmtGraphFactory(final IStmtGraphFactory cfgProvider) {
		stmtGraphProvider = cfgProvider;
	}

	/**
	 * Returns an unmodifiable list of statements of the given method represented in this graph.
	 *
	 * @param method of interest.
	 *
	 * @return an unmodifiable list of statements.
	 *
	 * @pre method != null
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	public List getStmtList(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getStmtList(method = " + method + ")");
		}

		List _result = (List) method2stmtlist.get(method);

		if (_result == null) {
			final UnitGraph _stmtGraph = getStmtGraph(method);

			if (_stmtGraph != null) {
				_result = Collections.unmodifiableList(IteratorUtils.toList(_stmtGraph.iterator()));
			} else {
				_result = Collections.EMPTY_LIST;
			}
			method2stmtlist.put(method, _result);
		}
		return _result;
	}

	/**
	 * Resets the internal data structures.
	 */
	public void reset() {
		method2graph.clear();
		method2stmtlist.clear();
	}
}

// End of File
