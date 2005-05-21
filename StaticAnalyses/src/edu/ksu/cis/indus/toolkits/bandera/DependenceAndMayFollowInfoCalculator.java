
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.ArrayRef;
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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependenceAndMayFollowInfoCalculator.class);

	/** 
	 * This filter accepts <code>Pair(Object, SootMethod></code> objects and filters them out if the method is not an
	 * application class.
	 */
	private static final Predicate APPLICATION_CLASS_ONLY_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object object) {
				return ((SootMethod) ((Pair) object).getSecond()).getDeclaringClass().isApplicationClass();
			}
		};

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
	 * This provides CFG based analysis.
	 */
	private final CFGAnalysis cfg;

	/** 
	 * This caches dependence information as pair of statement and method.
	 *
	 * @invariant dependenceCache.oclIsKindOf(Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethod))))
	 */
	private final Map dependenceCache = new HashMap();

	/** 
	 * This indicates if only array reference operation in application classes should be considered.
	 */
	private boolean arrayRefInApplicationClassesOnly;

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
	 *
	 * @pre ida != null and lbe != null and swbe != null and callGraph != null and threadGraph != null and cfgAnalysis !=
	 * 		null and theTool !=     null
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
	public final void callback(final SootMethod method) {
		if (method.isSynchronized()) {
			final Pair _p = new Pair(null, method);
			final Collection _birLocs = tool.generateBIRRep(_p, false);
			final Collection _c = locking.getLockAcquisitionsInEquivalenceClassOf(_p);
			addToDependenceCache(_p, _c, tool.lockAcquisitions, _birLocs, lockAcqInApplicationClassesOnly);
			tool.seenStmts.addAll(tool.generateBIRRep(_p, true));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(final Stmt stmt, final Context context) {
		final SootMethod _currentMethod = context.getCurrentMethod();
		final Pair _p = new Pair(stmt, _currentMethod);
		final Collection _birLocs = tool.generateBIRRep(_p, false);
		tool.seenStmts.addAll(_birLocs);

		if (stmt instanceof EnterMonitorStmt
			  || (stmt instanceof InvokeStmt && Util.isWaitInvocation((InvokeStmt) stmt, _currentMethod, cgi))) {
			final Collection _c = locking.getLockAcquisitionsInEquivalenceClassOf(_p);
			addToDependenceCache(_p, _c, tool.lockAcquisitions, _birLocs, lockAcqInApplicationClassesOnly);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(final ValueBox vBox, final Context context) {
		final SootMethod _currentMethod = context.getCurrentMethod();
		final Stmt _stmt = context.getStmt();
		final Pair _pair = new Pair(_stmt, _currentMethod);
		final Collection _c;
		final boolean flag;

		if (_stmt.containsArrayRef()) {
			_c = tool.arrayRefs;
			flag = arrayRefInApplicationClassesOnly;
		} else if (_stmt.containsFieldRef()) {
			_c = tool.fieldRefs;
			flag = fieldRefInApplicationClassesOnly;
		} else {
			_c = null;
			flag = false;
		}

		if (_c != null) {
			final Collection _birLocs = tool.generateBIRRep(_pair, false);
			final Collection _dependees = interferenceDA.getDependees(_stmt, _currentMethod);
			addToDependenceCache(_pair, _dependees, _c, _birLocs, flag);

			final Collection _dependents = interferenceDA.getDependents(_stmt, _currentMethod);
			addToDependenceCache(_pair, _dependents, _c, _birLocs, flag);
			_c.addAll(_birLocs);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public final void consolidate() {
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
	 * Sets application class filtering options for various class of operations.
	 *
	 * @param lockAcq <code>true</code> indicates that only lock acquisition operation in application classes should be
	 * 		  considered; <code>false</code>, otherwise.
	 * @param fieldRef <code>true</code> indicates that only field reference operation in application classes should be
	 * 		  considered; <code>false</code>, otherwise.
	 * @param arrayRef <code>true</code> indicates that only array reference operation in application classes should be
	 * 		  considered; <code>false</code>, otherwise.
	 */
	public void setApplicationClassFiltering(boolean lockAcq, boolean fieldRef, boolean arrayRef) {
		lockAcqInApplicationClassesOnly = lockAcq;
		arrayRefInApplicationClassesOnly = arrayRef;
		fieldRefInApplicationClassesOnly = fieldRef;
	}

	/**
	 * Calculates may-follow information.
	 */
	protected void translateAndPopulateMayFollowRelation() {
		final Map _result = tool.mayFollow;
		_result.clear();

		final Set _keys = dependenceCache.keySet();
		final Iterator _i = _keys.iterator();
		final int _iEnd = _keys.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair _pSrc = (Pair) _i.next();
			final Stmt _sSrc = (Stmt) _pSrc.getFirst();
			final SootMethod _mSrc = (SootMethod) _pSrc.getSecond();
			final Collection _pSrcInBIR = tool.generateBIRRep(_pSrc, false);
			final Iterator _j = _keys.iterator();

			for (int _jIndex = 0; _jIndex < _iEnd; _jIndex++) {
				final Pair _pDest = (Pair) _j.next();
				final Stmt _sDest = (Stmt) _pDest.getFirst();
				final SootMethod _mDest = (SootMethod) _pDest.getSecond();

				boolean _flag;

				if (_sSrc != null && _sDest != null) {
					_flag = cfg.isReachableViaInterProceduralControlFlow(_mSrc, _sSrc, _mDest, _sDest, tgi);
				} else if (_sSrc == null && _sDest == null) {
					_flag = cfg.doesControlPathExistsFromTo(_mSrc, _mDest);
				} else if (_sSrc == null) {
					_flag = cfg.doesControlFlowPathExistsBetween(_mDest, _sDest, _mSrc, false, true);
				} else {
					_flag = cfg.doesControlFlowPathExistsBetween(_mSrc, _sSrc, _mDest, true, true);
				}

				if (_flag) {
					final Collection _birLocs = tool.generateBIRRep(_pDest, false);
					final Iterator _k = _pSrcInBIR.iterator();
					final int _kEnd = _pSrcInBIR.size();

					for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
						CollectionsUtilities.putAllIntoSetInMap(_result, _k.next(), _birLocs);
					}
				}
			}
		}
	}

	/**
	 * Writes the data to files and reads it to verify the integrity.
	 *
	 * @throws IllegalStateException when file i/o error occurs or the objects cannot be serialized back.
	 */
	void writeDataToFiles()
	  throws IllegalStateException {
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

			final ByteArrayOutputStream _b1 = new ByteArrayOutputStream();
			MapUtils.verbosePrint(new PrintStream(_b1), "Dependence Relation", _temp1);

			final ByteArrayOutputStream _b2 = new ByteArrayOutputStream();
			MapUtils.verbosePrint(new PrintStream(_b2), "May Follow Relation", _temp3);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate()" + _b1.toString() + "\n" + _b2.toString() + "\n" + "Known Transitions:\n"
					+ CollectionsUtilities.prettyPrint(_temp2) + "\nLock Acquisitions:\n"
					+ CollectionsUtilities.prettyPrint(_temp4) + "\nArray Refs:\n" + CollectionsUtilities.prettyPrint(_temp5)
					+ "\nField Refs:\n" + CollectionsUtilities.prettyPrint(_temp6));
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
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre p != null and dependence != null and equivalents != null and birLocs != null
	 * @pre p.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @pre dependence.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 * @pre equivalents.oclIsKindOf(Collection(String))
	 * @pre birLocs.oclIsKindOf(Collection(String))
	 * @post equivalents.containsAll(equivalents$pre)
	 */
	private void addToDependenceCache(final Pair p, final Collection dependence, final Collection equivalents,
		final Collection birLocs, boolean applicationClassesOnly) {
		if (!applicationClassesOnly || ((SootMethod) p.getSecond()).getDeclaringClass().isApplicationClass()) {
			if (!dependence.isEmpty()) {
				final Collection _t = new ArrayList(dependence);

				if (applicationClassesOnly) {
					CollectionUtils.filter(dependence, APPLICATION_CLASS_ONLY_PREDICATE);
				}
				CollectionsUtilities.putAllIntoSetInMap(dependenceCache, p, _t);
				equivalents.addAll(birLocs);
			}
		}
	}

	/**
	 * Translates dependence info into BIR and populates the object in the associated tool.
	 */
	private void translateAndPopulateDependenceInfo() {
		final Map _result = tool.dependence;
		_result.clear();

		final Set _entrySet = dependenceCache.entrySet();
		final Iterator _i = _entrySet.iterator();
		final int _iEnd = _entrySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Pair _pair = (Pair) _entry.getKey();
			final Collection _depends = (Collection) _entry.getValue();
			final Collection _t = tool.generateBIRRep(_pair, false);
			final Iterator _j = _depends.iterator();
			final int _jEnd = _depends.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Pair _p = (Pair) _j.next();
				final Collection _t2 = tool.generateBIRRep(_p, false);
				final Iterator _k = _t.iterator();
				final int _kEnd = _t.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					CollectionsUtilities.putAllIntoSetInMap(_result, _k.next(), _t2);
				}
			}
		}
	}
}

// End of File
