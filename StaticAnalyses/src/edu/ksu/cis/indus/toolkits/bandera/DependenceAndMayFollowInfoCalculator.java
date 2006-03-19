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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

/**
 * This class calculates the dependence information as discussed in concurrency-theory.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class DependenceAndMayFollowInfoCalculator
		extends AbstractProcessor {

	/**
	 * This filter accepts <code>Pair(Object, SootMethod></code> objects and filters them out if the method is not an
	 * application class.
	 */
	private static final IPredicate<Pair<?, SootMethod>> APPLICATION_CLASS_ONLY_PREDICATE = new IPredicate<Pair<?, SootMethod>>() {

		public boolean evaluate(final Pair<?, SootMethod> object) {
			return object.getSecond().getDeclaringClass().isApplicationClass();
		}
	};

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DependenceAndMayFollowInfoCalculator.class);

	/**
	 * This provides the thread graph.
	 */
	protected final IThreadGraphInfo tgi;

	/**
	 * The tool that uses this instance.
	 */
	protected final RelativeDependenceInfoTool tool;

	/**
	 * This provide the call graph.
	 */
	final ICallGraphInfo cgi;

	/**
	 * This provides interference information.
	 */
	final InterferenceDAv1 interferenceDA;

	/**
	 * This provides locking based equivalence class information.
	 */
	final LockAcquisitionBasedEquivalence locking;

	/**
	 * This indicates if only array reference operation in application classes should be considered.
	 */
	private boolean arrayRefInApplicationClassesOnly;

	/**
	 * This provides CFG based analysis.
	 */
	private final CFGAnalysis cfg;

	/**
	 * This stores dependence information as pair of statement and method.
	 */
	private final Map<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>> dependenceCache = new HashMap<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>>();

	/**
	 * This stores the reachable program points.
	 */
	private final List<Pair<? extends Stmt, SootMethod>> knownTransitions = new ArrayList<Pair<? extends Stmt, SootMethod>>();

	/**
	 * This indicates if only field reference operation in application classes should be considered.
	 */
	private boolean fieldRefInApplicationClassesOnly;

	/**
	 * This indicates if only lock acquisition operation in application classes should be considered.
	 */
	private boolean lockAcqInApplicationClassesOnly;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theTool that uses this instance.
	 * @param ida to be used.
	 * @param lbe to be used.
	 * @param callGraph to be used.
	 * @param threadGraph to be used.
	 * @param cfgAnalysis to be used.
	 * @pre ida != null and lbe != null and swbe != null and callGraph != null and threadGraph != null and cfgAnalysis != null
	 *      and theTool != null
	 */
	public DependenceAndMayFollowInfoCalculator(final RelativeDependenceInfoTool theTool, final InterferenceDAv1 ida,
			final LockAcquisitionBasedEquivalence lbe, final ICallGraphInfo callGraph, final IThreadGraphInfo threadGraph,
			final CFGAnalysis cfgAnalysis) {
		tool = theTool;
		interferenceDA = ida;
		locking = lbe;
		tgi = threadGraph;
		cfg = cfgAnalysis;
		cgi = callGraph;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	@Override public final void callback(final SootMethod method) {
		if (method.isSynchronized()) {
			final Pair<Stmt, SootMethod> _p = new Pair<Stmt, SootMethod>(null, method);
			final Collection<String> _birLocs = tool.generateBIRRep(_p, false);
			final Collection<Pair<? extends Stmt, SootMethod>> _c = locking.getLockAcquisitionsInEquivalenceClassOf(_p);
			addToDependenceCache(_p, _c, tool.lockAcquisitions, _birLocs, lockAcqInApplicationClassesOnly);
			tool.seenStmts.addAll(tool.generateBIRRep(_p, true));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public final void callback(final Stmt stmt, final Context context) {
		final SootMethod _currentMethod = context.getCurrentMethod();
		final Pair<Stmt, SootMethod> _p = new Pair<Stmt, SootMethod>(stmt, _currentMethod);
		final Collection<String> _birLocs = tool.generateBIRRep(_p, false);
		tool.seenStmts.addAll(_birLocs);

		if (stmt instanceof EnterMonitorStmt
				|| (stmt instanceof InvokeStmt && Util.isWaitInvocation((InvokeStmt) stmt, _currentMethod, cgi))) {
			final Collection<Pair<? extends Stmt, SootMethod>> _c = locking.getLockAcquisitionsInEquivalenceClassOf(_p);
			addToDependenceCache(_p, _c, tool.lockAcquisitions, _birLocs, lockAcqInApplicationClassesOnly);
		}

		knownTransitions.add(_p);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public final void callback(@SuppressWarnings("unused") final ValueBox vBox, final Context context) {
		final SootMethod _currentMethod = context.getCurrentMethod();
		final Stmt _stmt = context.getStmt();
		final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _currentMethod);
		final Collection<String> _c;
		final boolean _flag;

		if (_stmt.containsArrayRef()) {
			_c = tool.arrayRefs;
			_flag = arrayRefInApplicationClassesOnly;
		} else if (_stmt.containsFieldRef()) {
			_c = tool.fieldRefs;
			_flag = fieldRefInApplicationClassesOnly;
		} else {
			_c = null;
			_flag = false;
		}

		if (_c != null) {
			final Collection<String> _birLocs = tool.generateBIRRep(_pair, false);
			final Collection<Pair<AssignStmt, SootMethod>> _dependees = interferenceDA.getDependees((AssignStmt) _stmt,
					_currentMethod);
			addToDependenceCache(_pair, _dependees, _c, _birLocs, _flag);

			final Collection<Pair<AssignStmt, SootMethod>> _dependents = interferenceDA.getDependents((AssignStmt) _stmt,
					_currentMethod);
			addToDependenceCache(_pair, _dependents, _c, _birLocs, _flag);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	@Override public final void consolidate() {
		translateAndPopulateDependenceInfo();
		translateAndPopulateMayFollowRelation();

		dependenceCache.clear();

		if (LOGGER.isDebugEnabled()) {
			writeDataToFiles();
			LOGGER.debug("locking dependence info:" + locking);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.registerForAllStmts(this);
		ppc.register(ArrayRef.class, this);
		ppc.register(InstanceFieldRef.class, this);
		ppc.register(StaticFieldRef.class, this);
	}

	/**
	 * Sets application class filtering options for various class of operations.
	 * 
	 * @param lockAcq <code>true</code> indicates that only lock acquisition operation in application classes should be
	 *            considered; <code>false</code>, otherwise.
	 * @param fieldRef <code>true</code> indicates that only field reference operation in application classes should be
	 *            considered; <code>false</code>, otherwise.
	 * @param arrayRef <code>true</code> indicates that only array reference operation in application classes should be
	 *            considered; <code>false</code>, otherwise.
	 */
	public void setApplicationClassFiltering(final boolean lockAcq, final boolean fieldRef, final boolean arrayRef) {
		lockAcqInApplicationClassesOnly = lockAcq;
		arrayRefInApplicationClassesOnly = arrayRef;
		fieldRefInApplicationClassesOnly = fieldRef;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
		ppc.unregister(ArrayRef.class, this);
		ppc.unregister(InstanceFieldRef.class, this);
		ppc.unregister(StaticFieldRef.class, this);
	}

	/**
	 * Calculates may-follow information.
	 */
	protected void translateAndPopulateMayFollowRelation() {
		final Map<String, Collection<String>> _result = tool.mayFollow;
		_result.clear();

		final ListIterator<Pair<? extends Stmt, SootMethod>> _i = knownTransitions.listIterator();
		final int _iEnd = knownTransitions.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair<? extends Stmt, SootMethod> _pSrc = _i.next();
			final Stmt _sSrc = _pSrc.getFirst();
			final SootMethod _mSrc = _pSrc.getSecond();
			final Collection<String> _pSrcInBIR = tool.generateBIRRep(_pSrc, false);
			final Iterator<? extends Pair<? extends Stmt, SootMethod>> _j = dependenceCache.keySet().iterator();

			while (_j.hasNext()) {
				final Pair<? extends Stmt, SootMethod> _pDest = _j.next();
				final Stmt _sDest = _pDest.getFirst();
				final SootMethod _mDest = _pDest.getSecond();

				boolean _flag;

				if (_sSrc != null && _sDest != null) {
					_flag = cfg.isReachableViaInterProceduralControlFlow(_mSrc, _sSrc, _mDest, _sDest, null, false);
				} else if (_sSrc == null && _sDest == null) {
					_flag = cfg.doesControlPathExistFromTo(_mSrc, _mDest);
				} else if (_sSrc == null) {
					_flag = cfg.doesControlFlowPathExistBetween(_mDest, _sDest, _mSrc, false, true);
				} else {
					_flag = cfg.doesControlFlowPathExistBetween(_mSrc, _sSrc, _mDest, true, true);
				}

				if (_flag) {
					final Collection<String> _birLocs = tool.generateBIRRep(_pDest, false);
					final Iterator<String> _k = _pSrcInBIR.iterator();
					final int _kEnd = _pSrcInBIR.size();

					for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
						MapUtils.putAllIntoCollectionInMap(_result, _k.next(), _birLocs);
					}
				} else if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(_sDest + "@" + _mDest + " will not follow " + _sSrc + "@" + _mDest);
				}
			}
		}
	}

	/**
	 * Writes the data to files and reads it to verify the integrity.
	 * 
	 * @throws IllegalStateException when file i/o error occurs or the objects cannot be serialized back.
	 */
	void writeDataToFiles() throws IllegalStateException {
		try {
			final ObjectOutputStream _output1 = new ObjectOutputStream(new FileOutputStream("dependence"));
			_output1.writeObject(this.tool.dependence);
			_output1.close();

			final ObjectInputStream _input1 = new ObjectInputStream(new FileInputStream("dependence"));
			final Map _temp1 = (Map) _input1.readObject();
			_input1.close();

			final ObjectOutputStream _output2 = new ObjectOutputStream(new FileOutputStream("knowntransitions"));
			_output2.writeObject(this.tool.seenStmts);
			_output2.close();

			final ObjectInputStream _input2 = new ObjectInputStream(new FileInputStream("knowntransitions"));
			final Collection _temp2 = (Collection) _input2.readObject();
			_input2.close();

			final ObjectOutputStream _output3 = new ObjectOutputStream(new FileOutputStream("mayfollow"));
			_output3.writeObject(this.tool.mayFollow);
			_output3.close();

			final ObjectInputStream _input3 = new ObjectInputStream(new FileInputStream("mayfollow"));
			final Map _temp3 = (Map) _input3.readObject();
			_input3.close();

			final ObjectOutputStream _output4 = new ObjectOutputStream(new FileOutputStream("lockAcquisitions"));
			_output4.writeObject(this.tool.lockAcquisitions);
			_output4.close();

			final ObjectInputStream _input4 = new ObjectInputStream(new FileInputStream("lockAcquisitions"));
			final Collection _temp4 = (Collection) _input4.readObject();
			_input4.close();

			final ObjectOutputStream _output5 = new ObjectOutputStream(new FileOutputStream("arrayRefs"));
			_output5.writeObject(this.tool.arrayRefs);
			_output5.close();

			final ObjectInputStream _input5 = new ObjectInputStream(new FileInputStream("arrayRefs"));
			final Collection _temp5 = (Collection) _input5.readObject();
			_input5.close();

			final ObjectOutputStream _output6 = new ObjectOutputStream(new FileOutputStream("fieldRefs"));
			_output6.writeObject(this.tool.fieldRefs);
			_output6.close();

			final ObjectInputStream _input6 = new ObjectInputStream(new FileInputStream("fieldRefs"));
			final Collection _temp6 = (Collection) _input6.readObject();
			_input6.close();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate() - Dependence Relation -" + MapUtils.verbosePrint(_temp1)
						+ "\n May Follow Relation - " + MapUtils.verbosePrint(_temp3) + "\n" + "Known Transitions:\n"
						+ CollectionUtils.verbosePrint(_temp2) + "\nLock Acquisitions:\n"
						+ CollectionUtils.prettyPrint(_temp4) + "\nArray Refs:\n" + CollectionUtils.prettyPrint(_temp5)
						+ "\nField Refs:\n" + CollectionUtils.prettyPrint(_temp6));
				LOGGER.debug("consolidate()" + this.tool.dependence.equals(_temp1) + " " + this.tool.seenStmts.equals(_temp2)
						+ " " + this.tool.mayFollow.equals(_temp3) + " " + tool.lockAcquisitions.equals(_temp4) + " "
						+ tool.arrayRefs.equals(_temp5) + " " + tool.fieldRefs.equals(_temp6));
			}
		} catch (final FileNotFoundException _e) {
			final IllegalStateException _r = new IllegalStateException();
			_r.initCause(_e);
			throw _r;
		} catch (final IOException _e) {
			final IllegalStateException _r = new IllegalStateException();
			_r.initCause(_e);
			throw _r;
		} catch (final ClassNotFoundException _e) {
			final IllegalStateException _r = new IllegalStateException();
			_r.initCause(_e);
			throw _r;
		}
	}

	/**
	 * Adds the given pair and it's dependence information to the dependence cache after filtering it according to
	 * <code>applicationClassOnly</code>.
	 * 
	 * @param p is the source of dependence.
	 * @param dependence to be added
	 * @param equivalents is the collection of location of equivalents of an operation class that needs to be updated.
	 * @param birLocs a collection of new equivalent locations to be added.
	 * @param applicationClassesOnly <code>true</code> indicates only dependences in application classes should be captured;
	 *            <code>false</code>, otherwise.
	 * @pre p != null and dependence != null and equivalents != null and birLocs != null
	 * @post equivalents.containsAll(equivalents$pre)
	 */
	private void addToDependenceCache(final Pair<? extends Stmt, SootMethod> p,
			final Collection<? extends Pair<? extends Stmt, SootMethod>> dependence, final Collection<String> equivalents,
			final Collection<String> birLocs, final boolean applicationClassesOnly) {
		if ((!applicationClassesOnly || p.getSecond().getDeclaringClass().isApplicationClass()) && !dependence.isEmpty()) {
			final Collection<Pair<? extends Stmt, SootMethod>> _t = new ArrayList<Pair<? extends Stmt, SootMethod>>(
					dependence);

			if (applicationClassesOnly) {
				CollectionUtils.filter(_t, APPLICATION_CLASS_ONLY_PREDICATE);
			}
			MapUtils.putAllIntoCollectionInMap(dependenceCache, p, _t);
			equivalents.addAll(birLocs);
		}
	}

	/**
	 * Translates dependence info into BIR and populates the object in the associated tool.
	 */
	private void translateAndPopulateDependenceInfo() {
		final Map<String, Collection<String>> _result = tool.dependence;
		_result.clear();

		final Set<Map.Entry<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>>> _entrySet = dependenceCache
				.entrySet();
		final Iterator<Map.Entry<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>>> _i = _entrySet
				.iterator();
		final int _iEnd = _entrySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>> _entry = _i
					.next();
			final Pair<? extends Stmt, SootMethod> _pair = _entry.getKey();
			final Collection<Pair<? extends Stmt, SootMethod>> _depends = _entry.getValue();
			final Collection<String> _t = tool.generateBIRRep(_pair, false);
			final Iterator<Pair<? extends Stmt, SootMethod>> _j = _depends.iterator();
			final int _jEnd = _depends.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Pair<? extends Stmt, SootMethod> _p = _j.next();
				final Collection<String> _t2 = tool.generateBIRRep(_p, false);
				final Iterator<String> _k = _t.iterator();
				final int _kEnd = _t.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					MapUtils.putAllIntoCollectionInMap(_result, _k.next(), _t2);
				}

				knownTransitions.add(_p);
			}
		}
	}
}

// End of File
