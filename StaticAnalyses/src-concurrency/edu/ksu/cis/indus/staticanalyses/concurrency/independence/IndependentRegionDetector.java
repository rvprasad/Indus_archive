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

package edu.ksu.cis.indus.staticanalyses.concurrency.independence;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.Stmt;

/**
 * This class detects independent region. Independence is the property that ensures the execution of a statement will only
 * affect the state of the thread that executes it and not other threads.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class IndependentRegionDetector
		extends AbstractProcessor {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IndependentRegionDetector.class);

	/**
	 * This is a cache variable that maps basic blocks to statements (in them) after which independent regions begin.
	 */
	private final Map<BasicBlock, Collection<Stmt>> bb2beginsAfterCache = new HashMap<BasicBlock, Collection<Stmt>>();

	/**
	 * This is a cache variable that maps basic blocks to statements (in them) before which independent regions begin.
	 */
	private final Map<BasicBlock, Collection<Stmt>> bb2beginsBeforeCache = new HashMap<BasicBlock, Collection<Stmt>>();

	/**
	 * This is a cache variable that maps basic blocks to statements (in them) after which independent regions end.
	 */
	private final Map<BasicBlock, Collection<Stmt>> bb2endsAfterCache = new HashMap<BasicBlock, Collection<Stmt>>();

	/**
	 * This is a cache variable that maps basic blocks to statements (in them) before which independent regions end.
	 */
	private final Map<BasicBlock, Collection<Stmt>> bb2endsBeforeCache = new HashMap<BasicBlock, Collection<Stmt>>();

	/**
	 * The basic block graph manager to be used during region discovery.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/**
	 * The statement level detector to be used.
	 */
	private IndependentStmtDetector independenceDetector;

	/**
	 * This is a cache variable that records the basic blocks that have an independent leader statement.
	 */
	private final Collection<BasicBlock> independentBeginsBBCache = new ArrayList<BasicBlock>();

	/**
	 * This is a cache variable that records the basic blocks that have an independent trailer statement.
	 */
	private final Collection<BasicBlock> independentEndsBBCache = new ArrayList<BasicBlock>();

	/**
	 * This maps methods to statements (in the method) after which independent regions begin.
	 */
	private Map<SootMethod, Collection<Stmt>> method2beginsAfter = new HashMap<SootMethod, Collection<Stmt>>();

	/**
	 * This maps methods to statements (in the method) before which independent regions begin.
	 */
	private Map<SootMethod, Collection<Stmt>> method2beginsBefore = new HashMap<SootMethod, Collection<Stmt>>();

	/**
	 * This maps methods to statements (in the method) after which independent regions end.
	 */
	private Map<SootMethod, Collection<Stmt>> method2endsAfter = new HashMap<SootMethod, Collection<Stmt>>();

	/**
	 * This maps methods to statements (in the method) before which independent regions end.
	 */
	private Map<SootMethod, Collection<Stmt>> method2endsBefore = new HashMap<SootMethod, Collection<Stmt>>();

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	@Override public void callback(final SootMethod method) {
		super.callback(method);

		if (bbgMgr == null) {
			LOGGER.error("callback(SootMethod) - Please call setBasicBlockGraphMgr() before executing this processor.");
			throw new IllegalStateException("Please call setBasicBlockGraphMgr() before executing this processor.");
		}

		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);
		final List<BasicBlock> _nodes = _bbg.getNodes();
		final Iterator<BasicBlock> _i = _nodes.iterator();
		final int _iEnd = _nodes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = _i.next();
			final List<Stmt> _stmtsOf = _bb.getStmtsOf();
			boolean _independent = false;
			Stmt _independentBegin = null;
			final Iterator<Stmt> _j = _stmtsOf.iterator();
			final int _jEnd = _stmtsOf.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = _j.next();

				if (independenceDetector.isIndependent(_stmt) && !_independent) {
					_independent = true;
					_independentBegin = _stmt;
				} else if (!independenceDetector.isIndependent(_stmt) && _independent) {
					final int _indexOfAtomicEnd = _stmtsOf.indexOf(_stmt);

					if (_indexOfAtomicEnd - _stmtsOf.indexOf(_independentBegin) >= 1) {
						MapUtils.putIntoListInMap(bb2beginsBeforeCache, _bb, _independentBegin);
						MapUtils.putIntoListInMap(bb2endsAfterCache, _bb, _stmtsOf.get(_indexOfAtomicEnd - 1));
					}
					_independent = false;
				}
			}

			if (_independent) {
				final Stmt _trailerStmt = _bb.getTrailerStmt();
				final int _indexOfAtomicEnd = _stmtsOf.indexOf(_trailerStmt);

				if (_indexOfAtomicEnd - _stmtsOf.indexOf(_independentBegin) >= 0) {
					MapUtils.putIntoListInMap(bb2beginsBeforeCache, _bb, _independentBegin);
					MapUtils.putIntoListInMap(bb2endsAfterCache, _bb, _trailerStmt);
					independentEndsBBCache.add(_bb);
				}
			}

			final Collection<Stmt> _collection = bb2beginsBeforeCache.get(_bb);

			if (_collection != null && _collection.contains(_bb.getLeaderStmt())) {
				independentBeginsBBCache.add(_bb);
			}
		}

		calculateMultiBBRegions(_nodes);
		recordBeginsAndEndsFor(method);

		bb2beginsBeforeCache.clear();
		bb2beginsAfterCache.clear();
		bb2endsBeforeCache.clear();
		bb2endsAfterCache.clear();
		independentBeginsBBCache.clear();
		independentEndsBBCache.clear();
	}

	/**
	 * Retrieves a collection of statements after which independent regions begin in the given method.
	 *
	 * @param method of interest.
	 * @return a collection of statements.
	 * @post result != null
	 */
	public Collection<Stmt> getAtomicRegionBeginAfterBoundariesFor(final SootMethod method) {
		return MapUtils.queryObject(method2beginsAfter, method, Collections.<Stmt> emptyList());
	}

	/**
	 * Retrieves a collection of statements before which independent regions begin in the given method.
	 *
	 * @param method of interest.
	 * @return a collection of statements.
	 * @post result != null
	 */
	public Collection<Stmt> getAtomicRegionBeginBeforeBoundariesFor(final SootMethod method) {
		return MapUtils.queryObject(method2beginsBefore, method, Collections.<Stmt> emptyList());
	}

	/**
	 * Retrieves a collection of statements after which independent regions end in the given method.
	 *
	 * @param method of interest.
	 * @return a collection of statements.
	 * @post result != null
	 */
	public Collection<Stmt> getAtomicRegionEndAfterBoundariesFor(final SootMethod method) {
		return MapUtils.queryObject(method2endsAfter, method, Collections.<Stmt> emptyList());
	}

	/**
	 * Retrieves a collection of statements before which independent regions end in the given method.
	 *
	 * @param method of interest.
	 * @return a collection of statements.
	 * @post result != null
	 */
	public Collection<Stmt> getAtomicRegionEndBeforeBoundariesFor(final SootMethod method) {
		return MapUtils.queryObject(method2endsBefore, method, Collections.<Stmt> emptyList());
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#reset()
	 */
	@Override public void reset() {
		super.reset();
		method2beginsBefore.clear();
		method2beginsAfter.clear();
		method2endsAfter.clear();
	}

	/**
	 * Sets the value of <code>independenceDetector</code>.
	 *
	 * @param detector the new value of <code>independenceDetector</code>.
	 */
	public void setAtomicityDetector(final IndependentStmtDetector detector) {
		independenceDetector = detector;
	}

	/**
	 * Sets the basic block graph manager to be used for region detection.
	 *
	 * @param manager to be used.
	 * @pre manager != null
	 */
	public void setBasicBlockGraphMgr(final BasicBlockGraphMgr manager) {
		bbgMgr = manager;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
	}

	/**
	 * Calculates independent regions that span across basic blocks.
	 *
	 * @param basicblocks of course.
	 * @pre basicblocks != null
	 */
	private void calculateMultiBBRegions(final Collection<BasicBlock> basicblocks) {
		final IWorkBag<BasicBlock> _beginsAfterTrailer = new LIFOWorkBag<BasicBlock>();
		final IWorkBag<BasicBlock> _endsBeforeLeader = new LIFOWorkBag<BasicBlock>();

		final Iterator<BasicBlock> _i1 = independentEndsBBCache.iterator();
		final int _i1End = independentEndsBBCache.size();

		for (int _i1Index = 0; _i1Index < _i1End; _i1Index++) {
			final BasicBlock _bb = _i1.next();
			final Collection<BasicBlock> _succsOf = _bb.getSuccsOf();

			if (CollectionUtils.containsAny(independentBeginsBBCache, _succsOf)) {
				bb2endsAfterCache.get(_bb).remove(_bb.getTrailerStmt());
				_endsBeforeLeader.addAllWorkNoDuplicates(SetUtils.difference(_succsOf, independentBeginsBBCache));
			} else {
				_i1.remove();
			}
		}

		final Iterator<BasicBlock> _i2 = independentBeginsBBCache.iterator();
		final int _i2End = independentBeginsBBCache.size();

		for (int _i2Index = 0; _i2Index < _i2End; _i2Index++) {
			final BasicBlock _bb = _i2.next();
			final Collection<BasicBlock> _predsOf = _bb.getPredsOf();

			if (CollectionUtils.containsAny(independentEndsBBCache, _predsOf)) {
				bb2beginsBeforeCache.get(_bb).remove(_bb.getLeaderStmt());
				_beginsAfterTrailer.addAllWorkNoDuplicates(SetUtils.difference(_predsOf, independentEndsBBCache));
			} else {
				_i2.remove();
			}
		}

		final Collection<BasicBlock> _nonAtomicBegins = new ArrayList<BasicBlock>(basicblocks);
		final Collection<BasicBlock> _nonAtomicEnds = new ArrayList<BasicBlock>(basicblocks);
		_nonAtomicBegins.removeAll(independentBeginsBBCache);
		_nonAtomicEnds.removeAll(independentEndsBBCache);

		do {
			while (_beginsAfterTrailer.hasWork()) {
				final BasicBlock _bb = _beginsAfterTrailer.getWork();
				MapUtils.putIntoListInMap(bb2beginsAfterCache, _bb, _bb.getTrailerStmt());
				_nonAtomicEnds.remove(_bb);
				_endsBeforeLeader.addAllWorkNoDuplicates(SetUtils.intersection(_nonAtomicBegins, _bb.getSuccsOf()));
			}

			while (_endsBeforeLeader.hasWork()) {
				final BasicBlock _bb = _endsBeforeLeader.getWork();
				MapUtils.putIntoListInMap(bb2endsBeforeCache, _bb, _bb.getLeaderStmt());
				_nonAtomicBegins.remove(_bb);
				_beginsAfterTrailer.addAllWorkNoDuplicates(SetUtils.intersection(_nonAtomicEnds, _bb.getPredsOf()));
			}
		} while (_beginsAfterTrailer.hasWork() || _endsBeforeLeader.hasWork());
	}

	/**
	 * Collects the objects in the collections that are values in the given map.
	 *
	 * @param <T> DOCUMENT ME!
	 * @param key2collection is the map from which to collect.
	 * @return a collection of objects.
	 * @post result != null
	 */
	private <T> Collection<T> collectValues(final Map<BasicBlock, Collection<T>> key2collection) {
		final Collection<T> _result = new ArrayList<T>();

		for (final Iterator<Collection<T>> _i = key2collection.values().iterator(); _i.hasNext();) {
			final Collection<T> _t = _i.next();
			_result.addAll(_t);
		}
		return _result;
	}

	/**
	 * Records the beginnings and ends of independent region for the given method.
	 *
	 * @param method for which to record the information.
	 * @pre method != null
	 */
	private void recordBeginsAndEndsFor(final SootMethod method) {
		method2beginsBefore.put(method, collectValues(bb2beginsBeforeCache));
		method2beginsAfter.put(method, collectValues(bb2beginsAfterCache));
		method2endsBefore.put(method, collectValues(bb2endsBeforeCache));
		method2endsAfter.put(method, collectValues(bb2endsAfterCache));
	}
}

// End of File
