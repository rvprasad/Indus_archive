
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
import edu.ksu.cis.indus.staticanalyses.concurrency.LockingBasedEquivalence;
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
import soot.jimple.EnterMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependenceInfoTool
  extends BaseObservable
  implements Tool {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(CustomProcessor.class);

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String METHOD_PREFIX = "{|";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String METHOD_SUFFIX = "|}";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String VIRTUAL_PREFIX = "+|";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String VIRTUAL_SUFFIX = "|+";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static final String SYNC_METHOD_LOCATIONS = " sync";

	/** 
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "scene";

	/** 
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "entryPoints";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final Object DEPENDENCE = "dependence information";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final Object KNOWN_TRANSITIONS = "known transitions information";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final Object MAY_FOLLOW_RELATION = "may happen relation";

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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Collection seenStmts = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Map dependence = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Map mayFollow = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection rootMethods;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IEnvironment env;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CustomProcessor
	  extends AbstractProcessor {
		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Collection virtualMethods = new HashSet();

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final InterferenceDAv1 interferenceDA;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final LockingBasedEquivalence locking;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		final Map map = new HashMap();

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final CFGAnalysis cfg;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final Collection defStmts = new HashSet();

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final IEscapeInfo einfo;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final IThreadGraphInfo tgi;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private final Map method2birsig = new HashMap();

		/**
		 * Creates an instance of this class.
		 *
		 * @param ida
		 * @param lbe
		 * @param escapeInfo DOCUMENT ME!
		 * @param threadGraph
		 * @param cfgAnalysis
		 */
		public CustomProcessor(final InterferenceDAv1 ida, final LockingBasedEquivalence lbe, final IEscapeInfo escapeInfo,
			final IThreadGraphInfo threadGraph, final CFGAnalysis cfgAnalysis) {
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
				CollectionsUtilities.putAllIntoSetInMap(map, _p, locking.getLockingBasedEquivalentsFor(_p));
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
				CollectionsUtilities.putAllIntoSetInMap(map, _pair, interferenceDA.getDependees(_stmt, _currentMethod));
				CollectionsUtilities.putAllIntoSetInMap(map, _pair, interferenceDA.getDependents(_stmt, _currentMethod));

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
			calculateLockingDependence();
			calculatedMayHappenInFutureRelation();

			final Iterator _i = map.entrySet().iterator();
			final int _iEnd = map.entrySet().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Pair _pair = (Pair) _entry.getKey();
				final Collection _depends = (Collection) _entry.getValue();
				translateIntoBIR(dependence, _pair, _depends);
			}

			writeDataToFiles();
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
			ppc.register(InvokeStmt.class, this);
			ppc.register(EnterMonitorStmt.class, this);
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
			ppc.unregister(InvokeStmt.class, this);
			ppc.unregister(EnterMonitorStmt.class, this);
			ppc.unregister(ArrayRef.class, this);
			ppc.unregister(InstanceFieldRef.class, this);
			ppc.unregister(StaticFieldRef.class, this);
			ppc.unregister(VirtualInvokeExpr.class, this);
		}

		/**
		 * DOCUMENT ME!
		 */
		private void calculateLockingDependence() {
			final Iterator _i = locking.getNonSingularLockingBasedEquivalents().iterator();
			final int _iEnd = locking.getNonSingularLockingBasedEquivalents().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Pair _p = (Pair) _i.next();
				CollectionsUtilities.putAllIntoSetInMap(map, _p, locking.getLockingBasedEquivalentsFor(_p));
			}
		}

		/**
		 * DOCUMENT ME!
		 */
		private void calculateWriteWriteDependence() {
			final Iterator _i = defStmts.iterator();
			final int _iEnd = defStmts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Pair _p1 = (Pair) _i.next();
				final Stmt _s1 = (Stmt) _p1.getFirst();
				final SootMethod _m1 = (SootMethod) _p1.getSecond();
				final Iterator _j = defStmts.iterator();

				for (int _jIndex = 0; _jIndex < _iEnd; _jIndex++) {
					final Pair _p2 = (Pair) _j.next();
					final Stmt _s2 = (Stmt) _p2.getFirst();
					final SootMethod _m2 = (SootMethod) _p2.getSecond();

					if (shared(_s1, _m1, _s2, _m2)) {
						CollectionsUtilities.putIntoSetInMap(map, _p1, _p2);
						CollectionsUtilities.putIntoSetInMap(map, _p2, _p1);
					}
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 */
		private void calculatedMayHappenInFutureRelation() {
			final Set _keys = map.keySet();
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

					if (cfg.isReachableViaInterProceduralControlFlow(_mSrc, _sSrc, _mDest, _sDest, tgi)) {
						CollectionsUtilities.putIntoSetInMap(mayFollow, _pSrcInBIR, generateBIRRep(_pDest));
					}
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param p DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 *
		 * @throws IllegalStateException DOCUMENT ME!
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
				_result = _sig + SYNC_METHOD_LOCATIONS;
			} else {
				throw new IllegalStateException("Hmm");
			}
			return _result;
		}

		/**
		 * DOCUMENT ME!
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
		private boolean shared(final Stmt s1, final SootMethod m1, final Stmt s2, final SootMethod m2) {
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

		/**
		 * DOCUMENT ME!
		 *
		 * @param result
		 * @param pair
		 * @param depends
		 */
		private void translateIntoBIR(final Map result, final Pair pair, final Collection depends) {
			final String _t = generateBIRRep(pair);
			final Iterator _j = depends.iterator();
			final int _jEnd = depends.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Pair _p = (Pair) _j.next();
				final String _t2 = generateBIRRep(_p);
				CollectionsUtilities.putIntoSetInMap(result, _t, _t2);
			}
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

				if (LOGGER.isDebugEnabled()) {
					final ByteArrayOutputStream _b1 = new ByteArrayOutputStream();
					MapUtils.verbosePrint(new PrintStream(_b1), "Dependence Relation", _temp1);

					final ByteArrayOutputStream _b3 = new ByteArrayOutputStream();
					MapUtils.verbosePrint(new PrintStream(_b3), "May Follow Relation", _temp3);
					LOGGER.debug("consolidate()" + _b1.toString() + "\n" + _temp2 + "\n" + _b3.toString());
				}

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
	 * DOCUMENT ME!
	 *
	 * @param env
	 * @param rootMethods
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

		System.out.println(_interferenceDA);

		final ProcessingController _pc2 = new ProcessingController();
		_pc2.setEnvironment(_aa.getEnvironment());
		_pc2.setProcessingFilter(new CGBasedXMLizingProcessingFilter(_cgi));
		_pc2.setStmtSequencesRetriever(_ssr);

		final LockingBasedEquivalence _lbe = new LockingBasedEquivalence(_ecba, _cgi);
		_lbe.hookup(_pc2);
		_pc2.process();
		_lbe.unhook(_pc2);

		System.out.println(_lbe);

		final CustomProcessor _proc = new CustomProcessor(_interferenceDA, _lbe, _ecba, _tgi, new CFGAnalysis(_cgi, _bbm));
		_proc.hookup(_pc2);
		_pc2.process();
		_proc.unhook(_pc2);
	}
}

// End of File
