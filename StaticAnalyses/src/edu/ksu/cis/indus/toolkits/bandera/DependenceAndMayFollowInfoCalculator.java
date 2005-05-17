
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

import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.SharedWriteBasedEquivalence;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


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
	 * This provides the thread graph.
	 */
	protected final IThreadGraphInfo tgi;

	/** 
	 * The tool that uses this instance.
	 */
	protected final RelativeDependenceInfoTool tool;

	/** 
	 * The collection of methods invoked (not resolved) in virtual invoke expressions.
	 *
	 * @invariant virtualMethods.oclIsKindOf(Collection(SootMethod))
	 */
	final Collection virtualMethods = new HashSet();

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
	 * This maps methods to their bir signature.
	 *
	 * @invariant method2birsig.oclIsKindOf(Map(SootMethod, String))
	 */
	private final Map method2birsig = new HashMap();

	/** 
	 * This provides shared write based equivalence class information.
	 */
	private final SharedWriteBasedEquivalence sharedwrite;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theTool that uses this instance.
	 * @param ida to be used.
	 * @param lbe to be used.
	 * @param swbe to be used.
	 * @param threadGraph to be used.
	 * @param cfgAnalysis to be used.
	 *
	 * @pre ida != null and lbe != null and swbe != null and threadGraph != null and cfgAnalysis != null and theTool !=
	 * 		null
	 */
	public DependenceAndMayFollowInfoCalculator(final RelativeDependenceInfoTool theTool, final InterferenceDAv1 ida,
		final LockAcquisitionBasedEquivalence lbe, final SharedWriteBasedEquivalence swbe,
		final IThreadGraphInfo threadGraph, final CFGAnalysis cfgAnalysis) {
		tool = theTool;
		interferenceDA = ida;
		locking = lbe;
		sharedwrite = swbe;
		tgi = threadGraph;
		cfg = cfgAnalysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public final void callback(final SootMethod method) {
		if (method.isSynchronized()) {
			final Pair _p = new Pair(null, method);
			final Collection _c = locking.getLockAcquisitionsInEquivalenceClassOf(_p);

			if (!_c.isEmpty()) {
				CollectionsUtilities.putAllIntoSetInMap(this.tool.dependence, _p, _c);
				tool.lockAcquisitions.add(generateBIRRep(_p));
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(final Stmt stmt, final Context context) {
		final Pair _pair = new Pair(stmt, context.getCurrentMethod());
		final String _birRep = generateBIRRep(_pair);
		tool.seenStmts.add(_birRep);

		if (stmt instanceof EnterMonitorStmt || stmt instanceof InvokeStmt) {
			final Collection _c = locking.getLockAcquisitionsInEquivalenceClassOf(_pair);

			if (!_c.isEmpty()) {
				CollectionsUtilities.putAllIntoSetInMap(tool.dependence, _pair, _c);
				tool.lockAcquisitions.add(_birRep);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(final ValueBox vBox, final Context context) {
		if (vBox.getValue() instanceof VirtualInvokeExpr) {
			final VirtualInvokeExpr _v = (VirtualInvokeExpr) vBox.getValue();
			virtualMethods.add(_v.getMethod());
		} else {
			final SootMethod _currentMethod = context.getCurrentMethod();
			final Stmt _stmt = context.getStmt();
			final Pair _pair = new Pair(_stmt, _currentMethod);
			final Collection _dependees = interferenceDA.getDependees(_stmt, _currentMethod);
			CollectionsUtilities.putAllIntoSetInMap(this.tool.dependence, _pair, _dependees);

			final Collection _dependents = interferenceDA.getDependents(_stmt, _currentMethod);
			CollectionsUtilities.putAllIntoSetInMap(this.tool.dependence, _pair, _dependents);

			if (_stmt instanceof AssignStmt) {
				final Collection _c = sharedwrite.getSharedWritesInEquivalenceClassOf(_pair);

				if (!_c.isEmpty()) {
					CollectionsUtilities.putAllIntoSetInMap(tool.dependence, _pair, _c);
				}
			}

			if (_stmt.containsArrayRef()) {
				tool.arrayRefs.add(generateBIRRep(_pair));
			} else if (_stmt.containsFieldRef()) {
				tool.fieldRefs.add(generateBIRRep(_pair));
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public final void consolidate() {
		calculatedMayFollowRelation();

		final Map _result = new HashMap();
		final Iterator _i = this.tool.dependence.entrySet().iterator();
		final int _iEnd = this.tool.dependence.entrySet().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Pair _pair = (Pair) _entry.getKey();
			final Collection _depends = (Collection) _entry.getValue();
			final String _t = generateBIRRep(_pair);
			final Iterator _j = _depends.iterator();
			final int _jEnd = _depends.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Pair _p = (Pair) _j.next();
				final String _t2 = generateBIRRep(_p);
				CollectionsUtilities.putIntoSetInMap(_result, _t, _t2);
			}
		}

		this.tool.dependence.clear();
		this.tool.dependence.putAll(_result);
		method2birsig.clear();
		virtualMethods.clear();
        
        if (LOGGER.isDebugEnabled()) {
            writeDataToFiles();
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
		ppc.register(VirtualInvokeExpr.class, this);
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
		ppc.unregister(VirtualInvokeExpr.class, this);
	}

	/**
	 * Calculates may-follow information.
	 */
	protected void calculatedMayFollowRelation() {
		final Set _keys = this.tool.dependence.keySet();
		final Iterator _i = _keys.iterator();
		final int _iEnd = _keys.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair _pSrc = (Pair) _i.next();
			final Stmt _sSrc = (Stmt) _pSrc.getFirst();
			final SootMethod _mSrc = (SootMethod) _pSrc.getSecond();
			final String _pSrcInBIR = generateBIRRep(_pSrc);
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
					CollectionsUtilities.putIntoSetInMap(tool.mayFollow, _pSrcInBIR, generateBIRRep(_pDest));
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
				LOGGER.debug("consolidate()" + _b1.toString() + "\n" + _b2.toString() + "\n" + "Lock Acquisitions:\n"
					+ CollectionsUtilities.prettyPrint(_temp4) + "\nArray Refs:\n" + CollectionsUtilities.prettyPrint(_temp5)
					+ "Field Refs:\n" + CollectionsUtilities.prettyPrint(_temp6));
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
	 * Generates the bir location for the given statement-method pair.
	 *
	 * @param p of interest.
	 *
	 * @return the bir location.
	 *
	 * @throws IllegalStateException when the given statement does not occur in the system.
	 *
	 * @pre p != null and p.oclIsKindOf(Pair(Stmt, SootMethod)) and p.getSecond() != null
	 * @post result != null
	 */
	private String generateBIRRep(final Pair p) {
		final Stmt _stmt = (Stmt) p.getFirst();
		final SootMethod _method = (SootMethod) p.getSecond();
		final String _sig;

		if (method2birsig.containsKey(_method)) {
			_sig = (String) method2birsig.get(_method);
		} else {
			_sig = RelativeDependenceInfoTool.constructMethodName(virtualMethods.contains(_method), _method);
			method2birsig.put(_method, _sig);
		}

		final List _sl = new ArrayList(_method.retrieveActiveBody().getUnits());
		final int _index = _sl.indexOf(_stmt);
		final String _result;

		if (_index != -1) {
			_result = _sig + " loc" + _index;
		} else if (_stmt == null) {
			_result = _sig + " " + RelativeDependenceInfoTool.SYNC_METHOD_LOCATIONS;
		} else {
			throw new IllegalStateException("Hmm");
		}
		return _result;
	}
}

// End of File
