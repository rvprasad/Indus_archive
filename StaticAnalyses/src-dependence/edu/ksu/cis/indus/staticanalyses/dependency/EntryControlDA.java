
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.IDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;


/**
 * This class provides intraprocedural control dependency information. This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod, Sequence(Stmt)))
 * @invariant dependent2dependee.entrySet()->forall(o | o.getValue().size() = o.getKey().getActiveBody().getUnits().size())
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod, Sequence(Set(Stmt))))
 * @invariant dependee2dependent.entrySet()->forall(o | o.getValue().size() = o.getKey().getActiveBody().getUnits().size())
 */
public class EntryControlDA
  extends AbstractDependencyAnalysis {
	/*
	 * The dependence information is stored as follows: For each method, a list of collection is maintained.  Each location in
	 * the list corresponds to the statement at the same location in the statement list of the method.  The collection is the
	 * statements to which the statement at the location of the collection is related via control dependence.
	 */

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(EntryControlDA.class);

	/**
	 * This provides the call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * Returns the statements on which <code>dependentStmt</code> depends on in the given <code>method</code>.
	 *
	 * @param dependentStmt is the dependent of interest.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre dependentStmt.oclIsKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final List _list = (List) dependent2dependee.get(method);

		if (_list != null) {
			final int _index = getStmtList((SootMethod) method).indexOf(dependentStmt);

			if (_list.get(_index) != null) {
				_result = Collections.unmodifiableCollection((Collection) _list.get(_index));
			}
		}
		return _result;
	}

	/**
	 * Returns the statements which depend on <code>dependeeStmt</code> in the given <code>method</code>.
	 *
	 * @param dependeeStmt is the dependee of interest.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre dependeeStmt.isOclKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final List _list = (List) dependee2dependent.get(method);

		if (_list != null) {
			final int _index = getStmtList((SootMethod) method).indexOf(dependeeStmt);

			if (_list.get(_index) != null) {
				_result = Collections.unmodifiableCollection((Collection) _list.get(_index));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return IDependencyAnalysis.CONTROL_DA;
	}

	/**
	 * Calculates the control dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		analyze(callgraph.getReachableMethods());
	}

	/**
	 * Calculates the control dependency information for the provided methods.  The use of this method does not require a
	 * prior call to <code>setup</code>.
	 *
	 * @param methods to be analyzed.
	 *
	 * @pre methods != null and methods.oclIsKindOf(Collection(SootMethod)) and not method->includes(null)
	 */
	public void analyze(final Collection methods) {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Control Dependence processing");
		}

		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _currMethod = (SootMethod) _i.next();
			final BasicBlockGraph _bbGraph = getBasicBlockGraph(_currMethod);

			if (_bbGraph == null) {
				LOGGER.error("Method " + _currMethod.getSignature() + " did not have a basic block graph.");
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing method: " + _currMethod.getSignature());
			}

			final BitSet[] _bbCDBitSets = computeControlDependency(_bbGraph);
			fixupMaps(_bbGraph, _bbCDBitSets, _currMethod);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Control Dependence processing");
		}
		stable = true;
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for control dependence as calculated by " + getClass().getName() + "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependent2dependee.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _method = (SootMethod) _entry.getKey();
			_localEdgeCount = 0;

			final List _stmts = getStmtList(_method);
			final List _cd = (List) _entry.getValue();

			for (int _j = 0; _j < _stmts.size(); _j++) {
				if (_cd == null) {
					continue;
				}

				final Collection _dees = (Collection) _cd.get(_j);

				if (_dees != null) {
					_temp.append("\t\t" + _stmts.get(_j) + " --> " + _dees + "\n");
					_localEdgeCount += _dees.size();
				} else {
					_temp.append("\t\t" + _stmts.get(_j) + " --> METHOD_ENTRY\n");
				}
			}

			_result.append("\tFor " + _entry.getKey() + " there are " + _localEdgeCount + " control dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " control dependence edges exist.");
		return _result.toString();
	}

	/**
	 * Calculates the control dependency from a directed graph.  This calculates the dependence information in terms of nodes
	 * in the graph.  This is later translated to statement level information by {@link
	 * EntryControlDA#fixupMaps(BasicBlockGraph, BitSet[], SootMethod) fixupMaps}.
	 *
	 * @param graph for which dependence info needs to be calculated.  Each node in the graph should have an unique index and
	 * 		  the indices should start from 0.
	 *
	 * @return an array of bitsets.  The length of the array and each of the bitset in it is equal to the number of nodes in
	 * 		   the graph.  The nth bitset captures the dependence information via set bits.  The BitSets capture
	 * 		   dependent->dependee information.
	 *
	 * @post result.oclIsTypeOf(Sequence(BitSet)) and result->size() == graph.getNodes().size()
	 * @post result->forall(o | o.size() == graph.getNodes().size())
	 */
	protected BitSet[] computeControlDependency(final IDirectedGraph graph) {
		final Map _dag = graph.getDAG();
		final List _nodes = graph.getNodes();
		final int _noOfNodes = _nodes.size();
		final int[] _succsSize = new int[_noOfNodes];
		final BitSet[][] _cd = new BitSet[_noOfNodes][_noOfNodes];
		final BitSet[] _result = new BitSet[_noOfNodes];
		final Collection _processed = new ArrayList();
		BitSet _currResult = new BitSet();
		final BitSet _temp1 = new BitSet();
		final IWorkBag _wb = new FIFOWorkBag();
		final Collection _roots = graph.getHeads();
		
		_wb.addAllWorkNoDuplicates(_roots);

		while (_wb.hasWork()) {
			final BasicBlock _bb = (BasicBlock) _wb.getWork();
			final Pair _dagBlock = (Pair) _dag.get(_bb);
			final Collection _preds = (Collection) _dagBlock.getFirst();

			if (!_processed.containsAll(_preds)) {
				_wb.addWorkNoDuplicates(_bb);
				continue;
			}

			// propogate data to the successors   
			final int _currIndex = _nodes.indexOf(_bb);
			final Collection _succs = (Collection) _dagBlock.getSecond();

			final BitSet[] _currCD = _cd[_currIndex];
			_succsSize[_currIndex] = _succs.size();

			for (final Iterator _j = _processed.iterator(); _j.hasNext();) {
				final int _pIndex = _nodes.indexOf(_j.next());
				final BitSet _pCD = _currCD[_pIndex];

				if (_pCD != null) {
					final boolean _assignFlag = _pCD.cardinality() == _succsSize[_pIndex];

					if (!_assignFlag) {
						_currResult.set(_pIndex);
					}

					for (final Iterator _i = _succs.iterator(); _i.hasNext();) {
						final int _succIndex = _nodes.indexOf(_i.next());
						final BitSet[] _succCDs = _cd[_succIndex];

						if (_assignFlag) {
							_succCDs[_pIndex] = _pCD;
						} else {
							BitSet _succCD = _succCDs[_pIndex];

							if (_succCD == null) {
								_succCD = new BitSet();
								_succCDs[_pIndex] = _succCD;
							}
							_succCD.or(_pCD);
						}
					}
				}
			}

			if (_succsSize[_currIndex] > 1) {
				int _count = 0;

				for (final Iterator _i = _succs.iterator(); _i.hasNext();) {
					final int _succIndex = _nodes.indexOf(_i.next());
					BitSet _succCD = _cd[_succIndex][_currIndex];

					if (_succCD == null) {
						_succCD = new BitSet();
						_cd[_succIndex][_currIndex] = _succCD;
					}

					_succCD.set(_count++);
				}
			}

			if (!_currResult.isEmpty()) {
				if (_currResult.length() > 1) {
					// prune the dom set to a mere control-dom set.
					_temp1.clear();

					for (final Iterator _i = _preds.iterator(); _i.hasNext();) {
						final BitSet _t = _result[_nodes.indexOf(_i.next())];

						if (_t != null) {
							_temp1.and(_t);
						}
					}

					for (int _j = _currResult.nextSetBit(0); _j >= 0; _j = _currResult.nextSetBit(_j + 1)) {
						if (!_preds.contains(_nodes.get(_j)) && (_preds.size() == 1 || !_temp1.get(_j))) {
							_currResult.clear(_j);
						}
					}
				}

				_result[_currIndex] = _currResult;
				_currResult = new BitSet();
			}

			// Add the successors of the node 
			_wb.addAllWorkNoDuplicates(_succs);
			_processed.add(_bb);
		}

		return _result;
	}

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Translates the dependence information as captured in <code>bbCDBitSets</code> to statement level info and populates
	 * the dependeXXMap fields.
	 *
	 * @param graph is the basic block graph corresponding to <code>method</code>.
	 * @param bbCDBitSets is the array that contains the basic block level dependence information as calculated by {@link
	 * 		  #computeControlDependency(IDirectedGraph) computeControlDependency}.
	 * @param method for which the maps are being populated.
	 *
	 * @pre graph != null and bbCDBitSets != null and method != null
	 * @post dependee2dependent.get(method) != null
	 * @post dependee2dependent.values()->forall(o | o->forall(p | p != null()))
	 * @post dependent2dependee.get(method) != null
	 * @post dependent2dependee.values()->forall(o | o->forall(p | p != null()))
	 */
	private void fixupMaps(final BasicBlockGraph graph, final BitSet[] bbCDBitSets, final SootMethod method) {
		final List _nodes = graph.getNodes();
		final List _sl = getStmtList(method);
		final List _mDependee = new ArrayList();
		final List _mDependent = new ArrayList();

		for (int _i = _sl.size(); _i > 0; _i--) {
			_mDependee.add(null);
			_mDependent.add(null);
		}

		boolean _flag = false;

		for (int _i = bbCDBitSets.length - 1; _i >= 0; _i--) {
			final BitSet _cd = bbCDBitSets[_i];
			_flag |= _cd != null;

			if (_cd != null) {
				final Collection _cdp = new ArrayList();
				final BasicBlock _bb = (BasicBlock) _nodes.get(_i);

				for (final Iterator _j = _bb.getStmtsOf().iterator(); _j.hasNext();) {
					_mDependee.set(_sl.indexOf(_j.next()), _cdp);
				}

				for (int _j = _cd.nextSetBit(0); _j != -1; _j = _cd.nextSetBit(_j + 1)) {
					final BasicBlock _cdbb = (BasicBlock) _nodes.get(_j);
					final Object _cdStmt = _cdbb.getTrailerStmt();
					_cdp.add(_cdStmt);

					final int _deIndex = _sl.indexOf(_cdStmt);
					Collection _dees = (Collection) _mDependent.get(_deIndex);

					if (_dees == null) {
						_dees = new ArrayList();
						_mDependent.set(_deIndex, _dees);
					}
					_dees.addAll(_bb.getStmtsOf());
				}
			}
		}

		if (_flag) {
			dependee2dependent.put(method, new ArrayList(_mDependent));
			dependent2dependee.put(method, new ArrayList(_mDependee));
		} else {
			dependee2dependent.put(method, null);
			dependent2dependee.put(method, null);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.17  2004/05/31 21:38:08  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.

   Revision 1.16  2004/05/14 06:27:24  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.15  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.14  2004/03/03 10:07:24  venku
   - renamed dependeeMap as dependent2dependee
   - renamed dependentmap as dependee2dependent
   Revision 1.13  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.12  2004/02/23 08:25:58  venku
   - logging.
   Revision 1.11  2004/02/06 00:19:12  venku
   - logging.
   Revision 1.10  2004/01/30 23:55:18  venku
   - added a new analyze method to analyze only the given
     collection of methods.
   Revision 1.9  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.8  2003/12/16 07:37:52  venku
   - incorrect add method used on container.
   Revision 1.7  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.6  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.5  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.4  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.3  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/12/01 11:28:48  venku
   - control da calculation was erroneous when the support
     for direction switch was removed.  FIXED.
   Revision 1.1  2003/11/25 17:51:23  venku
   - split control dependence into 2 classes.
     EntryControlDA handled control DA as required for backward slicing.
     ExitControlDA handles control DA as required for forward slicing.
   - ripple effect.
   Revision 1.26  2003/11/25 17:17:27  venku
   - logging.
   Revision 1.25  2003/11/25 17:08:50  venku
   - added logging statement.
   Revision 1.24  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.23  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.22  2003/11/05 09:29:51  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.21  2003/11/05 04:25:34  venku
   - return value of getDependees() was type incorrect.  FIXED.
   Revision 1.20  2003/11/05 04:20:05  venku
   - formatting.
   Revision 1.19  2003/11/05 04:17:28  venku
   - subtle bug caused when enabled bi-directional support. FIXED.
   Revision 1.18  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.17  2003/11/03 07:56:04  venku
   - added logging.
   Revision 1.16  2003/10/31 01:00:58  venku
   - added support to switch direction.  However, forward
     slicing can be viewed in two interesting ways and
     our implementation handles the most interesting
     direction.
   Revision 1.15  2003/09/28 12:27:31  venku
   -  The control dep was buggy. FIXED.
   Revision 1.14  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.13  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.12  2003/09/16 08:27:35  venku
   - Well, we calculated doms, not idoms.  FIXED.
   Revision 1.11  2003/09/16 05:54:56  venku
   - changed access specifiers of methods from protected to private
     as they were not being called in the package or subclasses.
   Revision 1.10  2003/09/15 01:22:06  venku
   - fixupMaps() was screwed. FIXED.
   Revision 1.9  2003/09/15 00:58:25  venku
   - well, things were fine I guess. Nevertheless, they are more
     streamlined now.
   Revision 1.8  2003/09/14 23:24:26  venku
   - alas a working control DA. However, I have not been able
     to compile a program such that the basic block has two CD points.
     This is possible when the else branch of the enclosed and enclosing
     if's are identical.
   Revision 1.7  2003/09/13 05:56:34  venku
   - an early commit to a (hopefully) working solution.
   - need to document it still.
   Revision 1.6  2003/09/12 23:49:46  venku
   - another one of those unsuccessful solutions.  Checking in to start over.
   Revision 1.5  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
