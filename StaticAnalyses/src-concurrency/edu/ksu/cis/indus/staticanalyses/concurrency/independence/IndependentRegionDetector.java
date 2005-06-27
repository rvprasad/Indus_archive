
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class detects independent region. Independence is the property that ensures the execution of a statement will only affect the
 * state of the thread that executes it and not other threads.
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
	private static final Log LOGGER = LogFactory.getLog(IndependentRegionDetector.class);

	/** 
	 * The statement level detector to be used.
	 */
	private IndependentStmtDetector independenceDetector;

	/** 
	 * The basic block graph manager to be used during region discovery.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/** 
	 * This is a cache variable that records the basic blocks that have an independent leader statement.
	 *
	 * @invariant atomicBeginsBBCache.oclIsKindOf(Sequence(BasicBlock))
	 */
	private final Collection independentBeginsBBCache = new ArrayList();

	/** 
	 * This is a cache variable that records the basic blocks that have an independent trailer statement.
	 *
	 * @invariant independentEndsBBCache.oclIsKindOf(Sequence(BasicBlock))
	 */
	private final Collection independentEndsBBCache = new ArrayList();

	/** 
	 * This is a cache variable that maps basic blocks to statements (in them) after which independent regions begin.
	 *
	 * @invariant bb2beginsAfterCache.oclIsKindOf(Map(BasicBlock, Collection(Stmt)))
	 * @invariant o.getStmtsOf().containsAll(bb2beginsAfterCache.get(o))
	 */
	private final Map bb2beginsAfterCache = new HashMap();

	/** 
	 * This is a cache variable that maps basic blocks to statements (in them) before which independent regions begin.
	 *
	 * @invariant bb2beginsBeforeCache.oclIsKindOf(Map(BasicBlock, Collection(Stmt)))
	 * @invariant o.getStmtsOf().containsAll(bb2beginsBeforeCache.get(o))
	 */
	private final Map bb2beginsBeforeCache = new HashMap();

	/** 
	 * This is a cache variable that maps basic blocks to statements (in them) after which independent regions end.
	 *
	 * @invariant bb2endsAfterCache.oclIsKindOf(Map(BasicBlock, Collection(Stmt)))
	 * @invariant o.getStmtsOf().containsAll(bb2endsAfterCache.get(o))
	 */
	private final Map bb2endsAfterCache = new HashMap();

	/** 
	 * This is a cache variable that maps basic blocks to statements (in them) before which independent regions end.
	 *
	 * @invariant bb2endsAfterCache.oclIsKindOf(Map(BasicBlock, Collection(Stmt)))
	 * @invariant o.getStmtsOf().containsAll(bb2endsAfterCache.get(o))
	 */
	private final Map bb2endsBeforeCache = new HashMap();

	/** 
	 * This maps methods to statements (in the method) after which independent regions begin.
	 *
	 * @invariant method2beginsAfter.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map method2beginsAfter = new HashMap();

	/** 
	 * This maps methods to statements (in the method) before which independent regions begin.
	 *
	 * @invariant method2beginsBefore.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map method2beginsBefore = new HashMap();

	/** 
	 * This maps methods to statements (in the method) after which independent regions end.
	 *
	 * @invariant method2endsAfter.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map method2endsAfter = new HashMap();

	/** 
	 * This maps methods to statements (in the method) before which independent regions end.
	 *
	 * @invariant method2endsBefore.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map method2endsBefore = new HashMap();

	/**
	 * Retrieves a collection of statements after which independent regions begin in the given method.
	 *
	 * @param method of interest.
	 *
	 * @return a collection of statements.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Stmt))
	 */
	public Collection getAtomicRegionBeginAfterBoundariesFor(final SootMethod method) {
		return CollectionsUtilities.getListFromMap(method2beginsAfter, method);
	}

	/**
	 * Retrieves a collection of statements before which independent regions begin in the given method.
	 *
	 * @param method of interest.
	 *
	 * @return a collection of statements.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Stmt))
	 */
	public Collection getAtomicRegionBeginBeforeBoundariesFor(final SootMethod method) {
		return CollectionsUtilities.getListFromMap(method2beginsBefore, method);
	}

	/**
	 * Retrieves a collection of statements after which independent regions end in the given method.
	 *
	 * @param method of interest.
	 *
	 * @return a collection of statements.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Stmt))
	 */
	public Collection getAtomicRegionEndAfterBoundariesFor(final SootMethod method) {
		return CollectionsUtilities.getListFromMap(method2endsAfter, method);
	}

	/**
	 * Retrieves a collection of statements before which independent regions end in the given method.
	 *
	 * @param method of interest.
	 *
	 * @return a collection of statements.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Stmt))
	 */
	public Collection getAtomicRegionEndBeforeBoundariesFor(final SootMethod method) {
		return CollectionsUtilities.getListFromMap(method2endsBefore, method);
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
	 *
	 * @pre manager != null
	 */
	public void setBasicBlockGraphMgr(final BasicBlockGraphMgr manager) {
		bbgMgr = manager;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		super.callback(method);

		if (bbgMgr == null) {
			LOGGER.error("callback(SootMethod) - Please call setBasicBlockGraphMgr() before executing this processor.");
			throw new IllegalStateException("Please call setBasicBlockGraphMgr() before executing this processor.");
		}

		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);
		final List _nodes = _bbg.getNodes();
		final Iterator _i = _nodes.iterator();
		final int _iEnd = _nodes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			final List _stmtsOf = _bb.getStmtsOf();
			boolean _independent = false;
			Stmt _independentBegin = null;
			final Iterator _j = _stmtsOf.iterator();
			final int _jEnd = _stmtsOf.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = (Stmt) _j.next();

				if (independenceDetector.isIndependent(_stmt) && !_independent) {
					_independent = true;
					_independentBegin = _stmt;
				} else if (!independenceDetector.isIndependent(_stmt) && _independent) {
					final int _indexOfAtomicEnd = _stmtsOf.indexOf(_stmt);

					if (_indexOfAtomicEnd - _stmtsOf.indexOf(_independentBegin) >= 1) {
						CollectionsUtilities.putIntoListInMap(bb2beginsBeforeCache, _bb, _independentBegin);
						CollectionsUtilities.putIntoListInMap(bb2endsAfterCache, _bb, _stmtsOf.get(_indexOfAtomicEnd - 1));
					}
					_independent = false;
				}
			}

			if (_independent) {
				final Stmt _trailerStmt = _bb.getTrailerStmt();
				final int _indexOfAtomicEnd = _stmtsOf.indexOf(_trailerStmt);

				if (_indexOfAtomicEnd - _stmtsOf.indexOf(_independentBegin) >= 0) {
					CollectionsUtilities.putIntoListInMap(bb2beginsBeforeCache, _bb, _independentBegin);
					CollectionsUtilities.putIntoListInMap(bb2endsAfterCache, _bb, _trailerStmt);
					independentEndsBBCache.add(_bb);
				}
			}

			final Collection _collection = (Collection) bb2beginsBeforeCache.get(_bb);

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
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#reset()
	 */
	public void reset() {
		super.reset();
		method2beginsBefore.clear();
		method2beginsAfter.clear();
		method2endsAfter.clear();
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
	 *
	 * @pre basicblocks != null and basicblocks.oclIsKindOf(Collection(BasicBlock))
	 */
	private void calculateMultiBBRegions(final Collection basicblocks) {
		final IWorkBag _beginsAfterTrailer = new LIFOWorkBag();
		final IWorkBag _endsBeforeLeader = new LIFOWorkBag();

		final Iterator _i1 = independentEndsBBCache.iterator();
		final int _i1End = independentEndsBBCache.size();

		for (int _i1Index = 0; _i1Index < _i1End; _i1Index++) {
			final BasicBlock _bb = (BasicBlock) _i1.next();
			final Collection _succsOf = _bb.getSuccsOf();

			if (CollectionUtils.containsAny(independentBeginsBBCache, _succsOf)) {
				((Collection) bb2endsAfterCache.get(_bb)).remove(_bb.getTrailerStmt());
				_endsBeforeLeader.addAllWorkNoDuplicates(CollectionUtils.subtract(_succsOf, independentBeginsBBCache));
			} else {
				_i1.remove();
			}
		}

		final Iterator _i2 = independentBeginsBBCache.iterator();
		final int _i2End = independentBeginsBBCache.size();

		for (int _i2Index = 0; _i2Index < _i2End; _i2Index++) {
			final BasicBlock _bb = (BasicBlock) _i2.next();
			final Collection _predsOf = _bb.getPredsOf();

			if (CollectionUtils.containsAny(independentEndsBBCache, _predsOf)) {
				((Collection) bb2beginsBeforeCache.get(_bb)).remove(_bb.getLeaderStmt());
				_beginsAfterTrailer.addAllWorkNoDuplicates(CollectionUtils.subtract(_predsOf, independentEndsBBCache));
			} else {
				_i2.remove();
			}
		}

		final Collection _nonAtomicBegins = new ArrayList(basicblocks);
		final Collection _nonAtomicEnds = new ArrayList(basicblocks);
		_nonAtomicBegins.removeAll(independentBeginsBBCache);
		_nonAtomicEnds.removeAll(independentEndsBBCache);

		do {
			while (_beginsAfterTrailer.hasWork()) {
				final BasicBlock _bb = (BasicBlock) _beginsAfterTrailer.getWork();
				CollectionsUtilities.putIntoListInMap(bb2beginsAfterCache, _bb, _bb.getTrailerStmt());
				_nonAtomicEnds.remove(_bb);
				_endsBeforeLeader.addAllWorkNoDuplicates(CollectionUtils.intersection(_nonAtomicBegins, _bb.getSuccsOf()));
			}

			while (_endsBeforeLeader.hasWork()) {
				final BasicBlock _bb = (BasicBlock) _endsBeforeLeader.getWork();
				CollectionsUtilities.putIntoListInMap(bb2endsBeforeCache, _bb, _bb.getLeaderStmt());
				_nonAtomicBegins.remove(_bb);
				_beginsAfterTrailer.addAllWorkNoDuplicates(CollectionUtils.intersection(_nonAtomicEnds, _bb.getPredsOf()));
			}
		} while (_beginsAfterTrailer.hasWork() || _endsBeforeLeader.hasWork());
	}

	/**
	 * Collects the objects in the collections that are values in the given map.
	 *
	 * @param key2collection is the map from which to collect.
	 *
	 * @return a collection of objects.
	 *
	 * @pre key2collection.oclIsKindOf(Map(Object, Collection(Object))) and key2collection != null
	 * @post result != null and result.oclIsKindOf(Collection(Object))
	 */
	private Collection collectValues(final Map key2collection) {
		final Collection _result = new ArrayList();

		for (final Iterator _i = key2collection.values().iterator(); _i.hasNext();) {
			final Collection _t = (Collection) _i.next();
			_result.addAll(_t);
		}
		return _result;
	}

	/**
	 * Records the beginnings and ends of independent region for the given method.
	 *
	 * @param method for which to record the information.
	 *
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
