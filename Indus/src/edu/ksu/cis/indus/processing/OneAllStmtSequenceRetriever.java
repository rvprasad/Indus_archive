
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

package edu.ksu.cis.indus.processing;

import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * This implementation provides one sequence containing statements of the method as captured by either the basic block or the
 * statement graph (in that order of preference).  Hence, the user must call <code>setStmtGraphFactory()</code> or
 * <code>setBasicBlockGraphMgr()</code> before using this object.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class OneAllStmtSequenceRetriever
  extends AbstractStmtSequenceRetriever {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(OneAllStmtSequenceRetriever.class);

	/**
	 * @see IStmtSequencesRetriever#retreiveStmtSequences(SootMethod)
	 */
	public Collection retreiveStmtSequences(final SootMethod method) {
        final Collection _temp = new ArrayList();
		final Collection _result = new ArrayList();
        _result.add(_temp);
        
		final BasicBlockGraphMgr _bbgMgr = getBbgFactory();

		if (_bbgMgr != null) {
			final BasicBlockGraph _bbg = _bbgMgr.getBasicBlockGraph(method);
			final Iterator _i = _bbg.getNodes().iterator();
			final int _iEnd = _bbg.getNodes().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final BasicBlock _bb = (BasicBlock) _i.next();
				_temp.addAll(_bb.getStmtsOf());
			}
		} else {
			final IStmtGraphFactory _sgf = getStmtGraphFactory();

			if (_sgf != null) {
				final UnitGraph _sg = _sgf.getStmtGraph(method);
				_temp.addAll(IteratorUtils.toList(_sg.iterator()));
			} else {
				final String _msg = "Please provide a statement graph factory or a basic block graph manager.";
				LOGGER.error(_msg);
				throw new IllegalStateException(_msg);
			}
		}
		return _result;
	}
}

// End of File
