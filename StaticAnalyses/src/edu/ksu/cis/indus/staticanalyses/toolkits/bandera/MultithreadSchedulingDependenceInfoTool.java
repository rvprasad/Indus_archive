
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

package edu.ksu.cis.indus.staticanalyses.toolkits.bandera;

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import edu.ksu.cis.bandera.util.BaseObservable;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.MetricsProcessor;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.callgraphs.CGBasedXMLizingProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv3;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.impl.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class provides run-time dependence information and may-follow information required schedule instructions correctly for
 * in a concurrent setting.  This is used for POR in Bogor.
 * 
 * <p>
 * PUT A LINK TO THE TECH REPORT FIX_ME
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class MultithreadSchedulingDependenceInfoTool
  extends BaseObservable
  implements Tool {
	/** 
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "scene";

	/** 
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "entryPoints";

	/** 
	 * This identifies the dependence info in the output arguments.
	 */
	public static final Object DEPENDENCE = "dependence information";

	/** 
	 * This identifies the known transitions info in the output arguments.
	 */
	public static final Object KNOWN_TRANSITIONS = "known transitions information";

	/** 
	 * This identifies the may-flow relation in the output arguments.
	 */
	public static final Object MAY_FOLLOW_RELATION = "may follow relation";

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(MultithreadSchedulingDependenceInfoTool.class);

	/** 
	 * This is the special location associated with lock acquisition while entering synchronized methods in bir models
	 * generated by j2b.
	 */
	static final String SYNC_METHOD_LOCATIONS = "sync";

	/** 
	 * This is the method name prefix in bir model from j2b.
	 */
	private static final String METHOD_PREFIX = "{|";

	/** 
	 * This is the method name suffix in bir model from j2b.
	 */
	private static final String METHOD_SUFFIX = "|}";

	/** 
	 * This is the virtual method name prefix in bir model from j2b.
	 */
	private static final String VIRTUAL_PREFIX = "+|";

	/** 
	 * This is the virtual method name suffix in bir model from j2b.
	 */
	private static final String VIRTUAL_SUFFIX = "|+";

	/** 
	 * The collection of input argument identifiers.
	 */
	private static final List IN_ARGUMENTS_IDS;

	/** 
	 * The collection of output argument identifiers.
	 */
	private static final List OUT_ARGUMENTS_IDS;

	static {
		IN_ARGUMENTS_IDS = new ArrayList();
		IN_ARGUMENTS_IDS.add(SCENE);
		IN_ARGUMENTS_IDS.add(ROOT_METHODS);
		OUT_ARGUMENTS_IDS = new ArrayList();
		OUT_ARGUMENTS_IDS.add(DEPENDENCE);
		OUT_ARGUMENTS_IDS.add(KNOWN_TRANSITIONS);
		OUT_ARGUMENTS_IDS.add(MAY_FOLLOW_RELATION);
	}

	/** 
	 * This is the collection of bir location corresponding to the statements seen by the processor.
	 *
	 * @invariant seenStmts.oclIsKindOf(Collection(Stmt))
	 */
	final Collection seenStmts = new HashSet();

	/** 
	 * This is dependence info in terms of bir locations.
	 *
	 * @invariant dependence.oclIsKindOf(Map(Stmt, Collection(Stmt)))
	 */
	final Map dependence = new HashMap();

	/** 
	 * This is may-follow info in terms of bir locations.
	 *
	 * @invariant dependence.oclIsKindOf(Map(Stmt, Collection(Stmt)))
	 */
	final Map mayFollow = new HashMap();

	/** 
	 * This is entry points to the system.
	 *
	 * @invariant rootMethods.oclIsKindOf(Collection(SootMethod))
	 */
	private Collection rootMethods;

	/** 
	 * This is the environment to be analyzed.
	 */
	private IEnvironment env;

	/**
	 * This class calculates the information.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class DependenceAndMayFollowInfoCalculator
	  extends AbstractProcessor {
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
		 * This is the collection of definition statements involving array reference or field reference.
		 *
		 * @invariant defStmts.oclIsKindOf(Collection(Stmt))
		 * @invariant defStmts->forall(o | o.containsArrayRef() or o.containsFieldRef())
		 */
		private final Collection defStmts = new HashSet();

		/** 
		 * This provides escape information.
		 */
		private final IEscapeInfo einfo;

		/** 
		 * This provides the thread graph.
		 */
		private final IThreadGraphInfo tgi;

		/** 
		 * This maps methods to their bir signature.
		 *
		 * @invariant method2birsig.oclIsKindOf(Map(SootMethod, String))
		 */
		private final Map method2birsig = new HashMap();

		/**
		 * Creates an instance of this class.
		 *
		 * @param ida to be used.
		 * @param lbe to be used.
		 * @param escapeInfo to be used.
		 * @param threadGraph to be used.
		 * @param cfgAnalysis to be used.
		 *
		 * @pre ida != null and lbe != null and escapeInfo != null and threadGraph != null and cfgAnalysis != null
		 */
		public DependenceAndMayFollowInfoCalculator(final InterferenceDAv1 ida, final LockAcquisitionBasedEquivalence lbe,
			final IEscapeInfo escapeInfo, final IThreadGraphInfo threadGraph, final CFGAnalysis cfgAnalysis) {
			interferenceDA = ida;
			locking = lbe;
			einfo = escapeInfo;
			tgi = threadGraph;
			cfg = cfgAnalysis;
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				final Pair _p = new Pair(null, method);
				CollectionsUtilities.putAllIntoSetInMap(dependence, _p, locking.getLockAcquisitionsInEquivalenceClassOf(_p));
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			seenStmts.add(generateBIRRep(new Pair(stmt, context.getCurrentMethod())));
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
		 */
		public void callback(final ValueBox vBox, final Context context) {
			if (vBox.getValue() instanceof VirtualInvokeExpr) {
				final VirtualInvokeExpr _v = (VirtualInvokeExpr) vBox.getValue();
				virtualMethods.add(_v.getMethod());
			} else {
				final SootMethod _currentMethod = context.getCurrentMethod();
				final Stmt _stmt = context.getStmt();
				final Pair _pair = new Pair(_stmt, _currentMethod);
				final Collection _dependees = interferenceDA.getDependees(_stmt, _currentMethod);
				CollectionsUtilities.putAllIntoSetInMap(dependence, _pair, _dependees);

				final Collection _dependents = interferenceDA.getDependents(_stmt, _currentMethod);
				CollectionsUtilities.putAllIntoSetInMap(dependence, _pair, _dependents);

				if (_stmt instanceof DefinitionStmt && ((DefinitionStmt) _stmt).getLeftOpBox() == vBox) {
					defStmts.add(_pair);
				}
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
		 */
		public void consolidate() {
			calculateWriteWriteDependence();
			calculateLockAcquisitionDependence();
			calculatedMayFollowRelation();

			final Map _result = new HashMap();
			final Iterator _i = dependence.entrySet().iterator();
			final int _iEnd = dependence.entrySet().size();

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

			dependence.clear();
			dependence.putAll(_result);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate()");
				writeDataToFiles();
			}

			method2birsig.clear();
			defStmts.clear();
			virtualMethods.clear();
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
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
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(this);
			ppc.unregisterForAllStmts(this);
			ppc.unregister(ArrayRef.class, this);
			ppc.unregister(InstanceFieldRef.class, this);
			ppc.unregister(StaticFieldRef.class, this);
			ppc.unregister(VirtualInvokeExpr.class, this);
		}

		/**
		 * Calcualtes lock acquisition based dependence.
		 */
		private void calculateLockAcquisitionDependence() {
			final Collection _t = locking.getLockAcquisitionsInNonSingletonEquivalenceClass();
			final Iterator _i = _t.iterator();
			final int _iEnd = _t.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Pair _p = (Pair) _i.next();
				CollectionsUtilities.putAllIntoSetInMap(dependence, _p, locking.getLockAcquisitionsInEquivalenceClassOf(_p));
			}
		}

		/**
		 * Calculates write-write based dependence.
		 */
		private void calculateWriteWriteDependence() {
			final Iterator _i = defStmts.iterator();
			final int _iEnd = defStmts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Pair _p1 = (Pair) _i.next();
				final DefinitionStmt _s1 = (DefinitionStmt) _p1.getFirst();
				final SootMethod _m1 = (SootMethod) _p1.getSecond();
				final Iterator _j = defStmts.iterator();

				for (int _jIndex = 0; _jIndex < _iEnd; _jIndex++) {
					final Pair _p2 = (Pair) _j.next();
					final DefinitionStmt _s2 = (DefinitionStmt) _p2.getFirst();
					final SootMethod _m2 = (SootMethod) _p2.getSecond();

					if (writeWriteExecutionDependence(_s1, _m1, _s2, _m2)) {
						CollectionsUtilities.putIntoSetInMap(dependence, _p1, _p2);
						CollectionsUtilities.putIntoSetInMap(dependence, _p2, _p1);
					}
				}
			}
		}

		/**
		 * Calculates may-follow information.
		 */
		private void calculatedMayFollowRelation() {
			final Set _keys = dependence.keySet();
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
						CollectionsUtilities.putIntoSetInMap(mayFollow, _pSrcInBIR, generateBIRRep(_pDest));
					}
				}
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
				_sig = constructMethodName(virtualMethods.contains(_method), _method);
				method2birsig.put(_method, _sig);
			}

			final List _sl = new ArrayList(_method.retrieveActiveBody().getUnits());
			final int _index = _sl.indexOf(_stmt);
			final String _result;

			if (_index != -1) {
				_result = _sig + " loc" + _index;
			} else if (_stmt == null) {
				_result = _sig + " " + SYNC_METHOD_LOCATIONS;
			} else {
				throw new IllegalStateException("Hmm");
			}
			return _result;
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @throws IllegalStateException
		 */
		private void writeDataToFiles()
		  throws IllegalStateException {
			try {
				final ObjectOutputStream _output1 = new ObjectOutputStream(new FileOutputStream("dependence"));
				_output1.writeObject(dependence);
				_output1.close();

				final ObjectInputStream _input1 = new ObjectInputStream(new FileInputStream("dependence"));
				final Map _temp1 = (Map) _input1.readObject();
				_input1.close();

				final ObjectOutputStream _output2 = new ObjectOutputStream(new FileOutputStream("knowntransitions"));
				_output2.writeObject(seenStmts);
				_output2.close();

				final ObjectInputStream _input2 = new ObjectInputStream(new FileInputStream("knowntransitions"));
				final Collection _temp2 = (Collection) _input2.readObject();
				_input2.close();

				final ObjectOutputStream _output3 = new ObjectOutputStream(new FileOutputStream("mayfollow"));
				_output3.writeObject(mayFollow);
				_output3.close();

				final ObjectInputStream _input3 = new ObjectInputStream(new FileInputStream("mayfollow"));
				final Map _temp3 = (Map) _input3.readObject();
				_input3.close();

				final ByteArrayOutputStream _b1 = new ByteArrayOutputStream();
				MapUtils.verbosePrint(new PrintStream(_b1), "Dependence Relation", _temp1);

				final ByteArrayOutputStream _b3 = new ByteArrayOutputStream();
				MapUtils.verbosePrint(new PrintStream(_b3), "May Follow Relation", _temp3);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("consolidate()" + dependence.equals(_temp1) + " " + seenStmts.equals(_temp2) + " "
						+ mayFollow.equals(_temp3));
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
		 * Checks if the given definition statements are dependent based on the fact that they may update the same field on
		 * an object or the same cell of an array.
		 *
		 * @param s1 is one statement of interest.
		 * @param m1 is the method containing <code>s1</code>.
		 * @param s2 is the second statement of interest.
		 * @param m2 is the method containing <code>s2</code>.
		 *
		 * @return <code>true</code> if both <code>s1</code> and <code>s2</code> contain either array/field ref in the
		 * 		   l-position; <code>false</code>, otherwise.
		 *
		 * @pre s1 != null and m1 != null and s2 != null and m2 != null
		 */
		private boolean writeWriteExecutionDependence(final DefinitionStmt s1, final SootMethod m1, final DefinitionStmt s2,
			final SootMethod m2) {
			boolean _result = false;

			if (s1.containsArrayRef() && s2.containsArrayRef()) {
				_result = einfo.shared(s1.getArrayRef().getBase(), m1, s2.getArrayRef().getBase(), m2);
			} else if (s1.containsFieldRef() && s2.containsFieldRef()) {
				final FieldRef _fieldRef2 = s2.getFieldRef();
				final FieldRef _fieldRef1 = s1.getFieldRef();
				final SootField _field1 = _fieldRef1.getField();

				if (_field1.equals(_fieldRef2.getField())) {
					if (_fieldRef1 instanceof InstanceFieldRef && _fieldRef2 instanceof InstanceFieldRef) {
						_result =
							einfo.shared(((InstanceFieldRef) _fieldRef1).getBase(), m1,
								((InstanceFieldRef) _fieldRef2).getBase(), m2);
					} else if (_fieldRef1 instanceof StaticFieldRef && _fieldRef2 instanceof StaticFieldRef) {
						_result =
							!EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_field1.getType())
							  || einfo.shared(_fieldRef1, m1, _fieldRef2, m2);
					}
				}
			}
			return _result;
		}
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String arg0)
	  throws Exception {
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration()
	  throws Exception {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map arg)
	  throws Exception {
		final Scene _scene = (Scene) arg.get(SCENE);

		if (_scene == null) {
			LOGGER.error("A scene must be provided.");
			throw new IllegalArgumentException("A scene must be provided.");
		}
		env = new Environment(_scene);

		final Collection _rootMethods = (Collection) arg.get(ROOT_METHODS);

		if (_rootMethods == null || _rootMethods.isEmpty()) {
			final String _msg = "Atleast one method should be specified as the entry-point into the system.";
			LOGGER.fatal(_msg);
			throw new IllegalArgumentException(_msg);
		}
		rootMethods = new ArrayList();
		rootMethods.addAll(_rootMethods);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List getInputParameterList() {
		return Collections.unmodifiableList(IN_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputMap()
	 */
	public Map getOutputMap() {
		final Map _map = new HashMap();
		_map.put(DEPENDENCE, dependence);
		_map.put(KNOWN_TRANSITIONS, seenStmts);
		_map.put(MAY_FOLLOW_RELATION, mayFollow);
		return _map;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List getOutputParameterList() {
		return Collections.unmodifiableList(OUT_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolConfigurationView()
	 */
	public ToolConfigurationView getToolConfigurationView() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	public void quit()
	  throws Exception {
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run()
	  throws Exception {
		run(env, rootMethods);
	}

	/**
	 * This method constructs the BIR representation of the name of a method.
	 *
	 * @param isVirtual shows whether or not this method is invoked in a virtual invoke expression.
	 * @param sm The soot method whose name will be compiled here.
	 *
	 * @return the BIR representation of the name of sm.
	 */
	static String constructMethodName(final boolean isVirtual, final SootMethod sm) {
		final SootClass _sc = sm.getDeclaringClass();
		final String _className = _sc.getName();
		final String _methodName = sm.getName();
		final int _size = sm.getParameterCount();
		final String[] _paramTypeNames = new String[_size];

		//get the array of the soot types of the parameters of the method
		for (int _i = 0; _i < _size; _i++) {
			_paramTypeNames[_i] = sm.getParameterType(_i).toString();
		}

		//construct the method name.
		final StringBuffer _sb = new StringBuffer(isVirtual ? VIRTUAL_PREFIX
															: METHOD_PREFIX);
		_sb.append(_className);
		_sb.append('.');
		_sb.append(_methodName);
		_sb.append('(');

		final int _size1 = _paramTypeNames.length;

		if (_size1 > 0) {
			_sb.append(_paramTypeNames[0]);

			for (int _i = 1; _i < _size1; _i++) {
				_sb.append(',');
				_sb.append(_paramTypeNames[_i]);
			}
		}

		_sb.append(')');
		_sb.append(isVirtual ? VIRTUAL_SUFFIX
							 : METHOD_SUFFIX);

		return _sb.toString();
	}

	/**
	 * Executes the tool.
	 *
	 * @param env to be analyzed.
	 * @param rootMethods are the entry points to the environment.
	 */
	void run(final IEnvironment env, final Collection rootMethods) {
		final String _tagName = "DependenceInfoTool:FA";
		final IValueAnalyzer _aa =
			OFAnalyzer.getFSOSAnalyzer(_tagName, TokenUtil.getTokenManager(new SootValueTypeManager()));

		_aa.analyze(env, rootMethods);

		final IStmtGraphFactory _stmtGraphFactory = new CompleteStmtGraphFactory();
		final BasicBlockGraphMgr _bbm = new BasicBlockGraphMgr();
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final Collection _processors = new ArrayList();
		final PairManager _pairManager = new PairManager(false, true);
		final CallGraphInfo _cgi = new CallGraphInfo(new PairManager(false, true));
		final IThreadGraphInfo _tgi = new ThreadGraph(_cgi, new CFGAnalysis(_cgi, _bbm), _pairManager);
		final ValueAnalyzerBasedProcessingController _cgipc = new ValueAnalyzerBasedProcessingController();
		final MetricsProcessor _countingProcessor = new MetricsProcessor();
		final OFABasedCallInfoCollector _callGraphInfoCollector = new OFABasedCallInfoCollector();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_bbm.setStmtGraphFactory(_stmtGraphFactory);
		_ssr.setBbgFactory(_bbm);

		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(_aa);
		_pc.setProcessingFilter(new TagBasedProcessingFilter(_tagName));

		_cgipc.setAnalyzer(_aa);
		_cgipc.setProcessingFilter(new CGBasedProcessingFilter(_cgi));
		_cgipc.setStmtSequencesRetriever(_ssr);

		final Map _info = new HashMap();
		_info.put(ICallGraphInfo.ID, _cgi);
		_info.put(IThreadGraphInfo.ID, _tgi);
		_info.put(PairManager.ID, _pairManager);
		_info.put(IEnvironment.ID, _aa.getEnvironment());
		_info.put(IValueAnalyzer.ID, _aa);

		final EquivalenceClassBasedEscapeAnalysis _ecba = new EquivalenceClassBasedEscapeAnalysis(_cgi, null, _bbm);
		_info.put(IEscapeInfo.ID, _ecba);
		_callGraphInfoCollector.reset();
		_processors.clear();
		_processors.add(_callGraphInfoCollector);
		_pc.reset();
		_pc.driveProcessors(_processors);
		_cgi.createCallGraphInfo(_callGraphInfoCollector.getCallInfo());

		_processors.clear();
		((ThreadGraph) _tgi).reset();
		_processors.add(_tgi);
		_processors.add(_countingProcessor);
		_cgipc.reset();
		_cgipc.driveProcessors(_processors);

		final AnalysesController _ac = new AnalysesController(_info, _cgipc, _bbm);
		_ac.addAnalyses(IEscapeInfo.ID, Collections.singleton(_ecba));

		final InterferenceDAv3 _interferenceDA = new InterferenceDAv3();
		_interferenceDA.setUseOFA(true);
		_ac.addAnalyses(IDependencyAnalysis.INTERFERENCE_DA, Collections.singleton(_interferenceDA));

		_ac.initialize();
		_ac.execute();

		final ProcessingController _pc2 = new ProcessingController();
		_pc2.setEnvironment(_aa.getEnvironment());
		_pc2.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));
		_pc2.setStmtSequencesRetriever(_ssr);

		final LockAcquisitionBasedEquivalence _lbe = new LockAcquisitionBasedEquivalence(_ecba, _cgi);
		_lbe.hookup(_pc2);
		_pc2.process();
		_lbe.unhook(_pc2);

		final DependenceAndMayFollowInfoCalculator _proc =
			new DependenceAndMayFollowInfoCalculator(_interferenceDA, _lbe, _ecba, _tgi, new CFGAnalysis(_cgi, _bbm));
		_proc.hookup(_pc2);
		_pc2.process();
		_proc.unhook(_pc2);
	}
}

// End of File
