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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import edu.ksu.cis.indus.annotations.AEmpty;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.AbstractStatus;

import edu.ksu.cis.indus.processing.IProcessor;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootMethod;

import soot.jimple.Stmt;
import soot.toolkits.graph.UnitGraph;

/**
 * This class is the skeletal implementation of the interface of analyses used to execute them.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractAnalysis
		extends AbstractStatus
		implements IAnalysis {

	/**
	 * This contains auxiliary information required by the subclasses. It is recommended that this represent
	 * <code>java.util.Properties</code> but map a <code>String</code> to an <code>Object</code>.
	 */
	protected final Map<Comparable, Object> info = new HashMap<Comparable, Object>();

	/**
	 * The pre-processor for this analysis, if one exists.
	 */
	protected IProcessor preprocessor;

	/**
	 * This manages the basic block graphs of methods.
	 */
	private BasicBlockGraphMgr graphManager;

	/**
	 * @see IAnalysis#analyze()
	 */
	public abstract void analyze();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Subclasses need not override this method. Rather they can set <code>preprocessor</code> field to a preprocessor and
	 * this method will use that to provide the correct information to the caller.
	 * </p>
	 */
	public boolean doesPreProcessing() {
		return preprocessor != null;
	}

	/**
	 * @see IAnalysis#getPreProcessor()
	 */
	public IProcessor getPreProcessor() {
		return preprocessor;
	}

	/**
	 * Returns the statistics about this analysis in the form of a <code>String</code>.
	 * 
	 * @return the statistics about this analysis.
	 */
	public String getStatistics() {
		return getClass() + " does not implement this method.";
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Refer to {@link #info info} and subclass documenation for more details.
	 * </p>
	 */
	public final void initialize(final Map<Comparable, Object> infoParam) throws InitializationException {
		info.putAll(infoParam);
		setup();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @post info.size() == 0
	 */
	public void reset() {
		unstable();
		info.clear();
	}

	/**
	 * @see IAnalysis#setBasicBlockGraphManager(BasicBlockGraphMgr)
	 */
	public void setBasicBlockGraphManager(final BasicBlockGraphMgr bbm) {
		graphManager = bbm;
	}

	/**
	 * Returns the basic block graph for the given method, if available. If not it will try to acquire the unit graph from the
	 * application. From that unit graph it will construct a basic block graph and return it.
	 * 
	 * @param method for which the basic block graph is requested.
	 * @return the basic block graph corresponding to <code>method</code>.
	 * @pre method != null
	 */
	protected BasicBlockGraph getBasicBlockGraph(final SootMethod method) {
		return graphManager.getBasicBlockGraph(method);
	}

	/**
	 * Returns a list of statements in the given method, if it exists. This implementation retrieves the statement list from
	 * the basic block graph manager, if it is available. If not, it retrieves the statement list from the method body
	 * directly. It will return an unmodifiable list of statements.
	 * 
	 * @param method of interest.
	 * @return an unmodifiable list of statements.
	 * @pre method != null
	 * @post result != null
	 */
	protected List<Stmt> getStmtList(final SootMethod method) {
		List _result;

		if (graphManager != null) {
			_result = graphManager.getStmtList(method);
		} else {
			final UnitGraph _stmtGraph = graphManager.getStmtGraph(method);

			if (_stmtGraph != null) {
				_result = Collections.unmodifiableList(new ArrayList<Stmt>(_stmtGraph.getBody().getUnits()));
			} else {
				_result = Collections.EMPTY_LIST;
			}
		}
		return _result;
	}

	/**
	 * Retrives the unit graph of the given method.
	 * 
	 * @param method for which the unit graph is requested.
	 * @return the unit graph of the method.
	 * @post result != null
	 */
	protected UnitGraph getUnitGraph(final SootMethod method) {
		return graphManager.getStmtGraph(method);
	}

	/**
	 * Setup data structures after initialization. This is a convenience method for subclasses to do processing after the
	 * calls to <code>initialize</code> and before the call to <code>preprocess</code>.
	 * 
	 * @throws InitializationException is never thrown by this implementation.
	 */
	@AEmpty protected void setup() throws InitializationException {
		// does nothing
	}
}

// End of File
