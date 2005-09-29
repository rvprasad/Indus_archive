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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This class provides intra-thread aliased use-def information which is based on types, points-to information, and call
 * graph. If the use is reachable from the def via the control flow graph or via the CFG and the call graph, then def and use
 * site are related by use-def relation. This does not consider any intervening definition when calculating the def-use
 * relation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @see AliasedUseDefInfo
 */
public final class AliasedUseDefInfov2
		extends AliasedUseDefInfo {

	/**
	 * This provides the call graph of the system.
	 */
	protected final ICallGraphInfo cgi;

	/**
	 * This provides the thread graph of the system.
	 */
	protected final IThreadGraphInfo tgi;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param iva is the value analyzer to use.
	 * @param cg is the call graph to use.
	 * @param tg is the thread graph to use. If this parameter is <code>null</code> then it is assumed all methods execute
	 *            in the same thread.
	 * @param bbgManager is the basic block graph manager to use.
	 * @param pairManager is the pair object manager to use.
	 * @pre iva != null and cg != null and bbgManager != null and pairManager != null
	 */
	public AliasedUseDefInfov2(final IValueAnalyzer iva, final ICallGraphInfo cg, final IThreadGraphInfo tg,
			final BasicBlockGraphMgr bbgManager, final PairManager pairManager) {
		super(iva, bbgManager, pairManager, new CFGAnalysis(cg, bbgManager));
		cgi = cg;
		tgi = tg;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	@Override public void consolidate() {
		super.consolidate();
		cfgAnalysis.reset();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation actually checks if the use site is reachable from the def site via control flow. However, it does
	 * not check for the existence of any overriding definitions alike strong updates.
	 * </p>
	 * 
	 * @pre defMethod != null and defStmt != null and useMethod != null and useStmt != null
	 */
	@Override public boolean isReachableViaInterProceduralControlFlow(final SootMethod defMethod, final Stmt defStmt,
			final SootMethod useMethod, final Stmt useStmt) {
		return cfgAnalysis.isReachableViaInterProceduralControlFlow(defMethod, defStmt, useMethod, useStmt, tgi);
	}
}

// End of File
