
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IObjectReadWriteInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;

import org.apache.commons.collections.map.LRUMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class represents Equivalence Class-based analysis to calculate escape information of objects.  Escape information is
 * provided in terms of share-ability of the object bound to a given value in a given method.  This analysis is overloaded
 * as a symbolic analysis to calculate information that can be used to prune ready-dependence edges.
 * 
 * <p>
 * This analysis requires <code>local splitting</code> option of Soot framework to be enabled while generating the Jimple for
 * the system being analyzed.
 * </p>
 * 
 * <p>
 * The implementation is based on the techreport <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports/SAnToS-TR2003-6.pdf">Honing the  Detection of Interference
 * and Ready Dependence for Slicing Concurrent Java Programs.</a>
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class EquivalenceClassBasedEscapeAnalysis
  extends AbstractAnalysis
  implements IObjectReadWriteInfo,
	  IEscapeInfo {
	/*
	 * xxxCache variables do not capture state of the object.  Rather they are used cache values across method calls.  Hence,
	 * any subclasses of this class should  not reply on these variables as they may be removed in the future.
	 */

	/** 
	 * The logger used by instances of <code>ValueProcessor</code> class to log messages.
	 */
	static final Log VALUE_PROCESSOR_LOGGER = LogFactory.getLog(ValueProcessor.class);

	/** 
	 * The logger used by instances of <code>StmtProcessor</code> class to log messages.
	 */
	static final Log STMT_PROCESSOR_LOGGER = LogFactory.getLog(StmtProcessor.class);

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(EquivalenceClassBasedEscapeAnalysis.class);

	/** 
	 * This is used to retrieve the alias set for "this" from a given method context.
	 */
	private static final Transformer thisAliasSetRetriever =
		new Transformer() {
			public Object transform(final Object input) {
				return ((MethodContext) input).thisAS;
			}
		};

	/** 
	 * This manages the basic block graphs corresponding to the methods in being analyzed.
	 */
	final BasicBlockGraphMgr bbm;

	/** 
	 * This provides inter-procedural control-flow information.
	 */
	final CFGAnalysis cfgAnalysis;

	/** 
	 * This provides context information pertaining to caller-callee relation across method calls.  The method stored in the
	 * context is the caller.  The statement is one in which invocation occurs.  The program point is at which place the
	 * invocation happens.
	 */
	final Context context;

	/** 
	 * This provides call-graph information.
	 */
	final ICallGraphInfo cgi;

	/** 
	 * This maps global/static fields to their alias sets.
	 *
	 * @invariant globalASs->forall(o | o.oclIsKindOf(AliasSet))
	 */
	final Map globalASs;

	/** 
	 * This maps a method to a triple containing the method context, the alias sets for the locals in the method (key), and
	 * the site contexts for all the call-sites (caller-side triple) in the method(key).
	 *
	 * @invariant method2Triple.oclIsKindOf(Map(SootMethod, Triple(MethodContext, Map(Local, AliasSet), Map(CallTriple,
	 * 			  MethodContext))))
	 */
	final Map method2Triple;

	/** 
	 * This is the statement processor used to analyze the methods.
	 */
	final StmtProcessor stmtProcessor;

	/** 
	 * This is the <code>Value</code> processor used to process Jimple pieces that make up the methods.
	 */
	final ValueProcessor valueProcessor;

	/** 
	 * This provides thread-graph information.
	 */
	IThreadGraphInfo tgi;

	/** 
	 * This is a cache variable that holds local alias set map between method calls.
	 *
	 * @invariant localASsCache.oclIsKindOf(Local, AliasSet)
	 */
	Map localASsCache;

	/** 
	 * This is a cache variable that holds site context map between method calls.
	 *
	 * @invariant scCache.oclIsKindOf(CallTriple, MethodContex)
	 */
	Map scCache;

	/** 
	 * This is a cache variable that holds method context map between method calls.
	 */
	MethodContext methodCtxtCache;

	/** 
	 * This maintains a cache of query to alias set.
	 *
	 * @invariant query2handle.oclIsKindOf(Map(Pair(AliasSet, String[]), AliasSet))
	 */
	private final Map query2handle = new LRUMap();

	/** 
	 * This retrieves the method context of a method.
	 */
	private final Transformer methodCtxtRetriever =
		new Transformer() {
			public Object transform(final Object input) {
				final Triple _t = (Triple) method2Triple.get(input);
				return _t != null ? _t.getFirst()
								  : null;
			}
		};

	/**
	 * Creates a new EquivalenceClassBasedEscapeAnalysis object.
	 *
	 * @param callgraph provides call-graph information.
	 * @param threadGraph provides thread graph information.  If this is <code>null</code> then read-write specific thread
	 * 		  information is not captured.
	 * @param basicBlockGraphMgr provides basic block graphs required by this analysis.
	 *
	 * @pre scene != null and callgraph != null and tgi != null
	 */
	public EquivalenceClassBasedEscapeAnalysis(final ICallGraphInfo callgraph, final IThreadGraphInfo threadGraph,
		final BasicBlockGraphMgr basicBlockGraphMgr) {
		cgi = callgraph;
		tgi = threadGraph;
		globalASs = new HashMap();
		method2Triple = new HashMap();
		stmtProcessor = new StmtProcessor(this);
		valueProcessor = new ValueProcessor(this);
		bbm = basicBlockGraphMgr;
		context = new Context();
		cfgAnalysis = new CFGAnalysis(cgi, bbm);
		preprocessor = new PreProcessor();
	}

	/**
	 * This class is used to create alias sets for global variables.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class PreProcessor
	  extends AbstractProcessor {
		/**
		 * {@inheritDoc}  Creates an alias set for the static fields.  This is the creation of global alias sets in Ruf's
		 * algorithm.
		 */
		public void callback(final SootField sf) {
			if (Modifier.isStatic(sf.getModifiers())) {
				final AliasSet _t = AliasSet.getASForType(sf.getType());

				if (_t != null) {
					_t.setGlobal();
					globalASs.put(sf.getSignature(), _t);
				}
			}
		}

		/**
		 * {@inheritDoc}  Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's
		 * algorithm.
		 */
		public void callback(final SootMethod sm) {
			final MethodContext _methodContext = new MethodContext(sm);
			method2Triple.put(sm, new Triple(_methodContext, new HashMap(), new HashMap()));

			if (sm.isSynchronized() && !sm.isStatic()) {
				_methodContext.getThisAS().addNewLockEntity();
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(this);
		}
	}


	/**
	 * This class retrieves the alias set corresponding to a param/arg position from a method context.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class ArgParamAliasSetRetriever
	  implements Transformer {
		/** 
		 * This is the position of the param/arg.
		 */
		private final int position;

		/**
		 * Creates an instance of this class.
		 *
		 * @param pos is the arg/param position of interest.
		 */
		ArgParamAliasSetRetriever(final int pos) {
			this.position = pos;
		}

		/**
		 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
		 */
		public Object transform(final Object input) {
			return ((MethodContext) input).getParamAS(position);
		}
	}


	/**
	 * This retrives the site context in a method based on the initialized call-site.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class SiteContextRetriever
	  implements Transformer {
		/** 
		 * This is the call-site.
		 */
		final Triple callerTriple;

		/**
		 * Creates an instance of this class.
		 *
		 * @param triple of interest.
		 *
		 * @pre triple != null
		 */
		SiteContextRetriever(final Triple triple) {
			callerTriple = triple;
		}

		/**
		 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
		 */
		public Object transform(final Object input) {
			final Triple _t = (Triple) method2Triple.get(input);
			return _t != null ? ((Map) _t.getThird()).get(callerTriple)
							  : null;
		}
	}

	/**
	 * @see IObjectReadWriteInfo#isArgumentBasedAccessPathRead(edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple,
	 * 		int, java.lang.String[], boolean)
	 */
	public boolean isArgumentBasedAccessPathRead(final CallTriple callerTriple, final int argPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException {
		final SootMethod _callee = callerTriple.getExpr().getMethod();

		validate(argPos, _callee);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(new SiteContextRetriever(callerTriple), new ArgParamAliasSetRetriever(argPos));
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IObjectReadWriteInfo#isArgumentBasedAccessPathWritten(ICallGraphInfo.CallTriple, int, String[],     boolean)
	 */
	public boolean isArgumentBasedAccessPathWritten(final CallTriple callerTriple, final int argPos,
		final String[] accesspath, final boolean recurse) {
		final SootMethod _callee = callerTriple.getExpr().getMethod();

		validate(argPos, _callee);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(new SiteContextRetriever(callerTriple), new ArgParamAliasSetRetriever(argPos));
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		final Collection _temp = new ArrayList();
		_temp.add(IEscapeInfo.ID);
		_temp.add(IObjectReadWriteInfo.ID);
		return _temp;
	}

	/**
	 * @see IObjectReadWriteInfo#isParameterBasedAccessPathRead(soot.SootMethod, int, java.lang.String[], boolean)
	 */
	public boolean isParameterBasedAccessPathRead(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException {
		validate(paramPos, method);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(methodCtxtRetriever, new ArgParamAliasSetRetriever(paramPos));
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IObjectReadWriteInfo#isParameterBasedAccessPathWritten(SootMethod, int, String[], boolean)
	 */
	public boolean isParameterBasedAccessPathWritten(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse) {
		validate(paramPos, method);

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(methodCtxtRetriever, new ArgParamAliasSetRetriever(paramPos));
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOf(soot.Local, soot.SootMethod)
	 */
	public Collection getReadingThreadsOf(final Local local, final SootMethod method) {
		Collection _result = Collections.EMPTY_SET;
		final Triple _triple = (Triple) method2Triple.get(method);

		if (_triple != null) {
			final Map _local2as = (Map) _triple.getSecond();
			final AliasSet _as = (AliasSet) _local2as.get(local);

			if (_as != null) {
				_result = _as.getReadThreads();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOf(int, soot.SootMethod)
	 */
	public Collection getReadingThreadsOf(final int paramIndex, final SootMethod method) {
		validate(paramIndex, method);

		final Collection _result;

		if (method.getParameterType(paramIndex) instanceof RefType) {
			final Triple _triple = (Triple) method2Triple.get(method);

			if (_triple != null) {
				final MethodContext _ctxt = (MethodContext) _triple.getFirst();
				_result = _ctxt.getParamAS(paramIndex).getReadThreads();
			} else {
				_result = Collections.EMPTY_SET;

				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No recorded information for " + method
						+ " is available.  Returning pessimistic (true) info.");
				}
			}
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOfThis(soot.SootMethod)
	 */
	public Collection getReadingThreadsOfThis(final SootMethod method) {
		validate(method);

		final Triple _triple = (Triple) method2Triple.get(method);
		final Collection _result;

		if (_triple != null) {
			final MethodContext _ctxt = (MethodContext) _triple.getFirst();
			_result = Collections.unmodifiableCollection(_ctxt.thisAS.getReadThreads());
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see IObjectReadWriteInfo#isReceiverBasedAccessPathRead(ICallGraphInfo.CallTriple,     java.lang.String[], boolean)
	 */
	public boolean isReceiverBasedAccessPathRead(final CallTriple callerTriple, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException {
		if (callerTriple.getExpr().getMethod().isStatic()) {
			throw new IllegalArgumentException("The invoked method should be non-static.");
		}

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(new SiteContextRetriever(callerTriple), thisAliasSetRetriever);
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IObjectReadWriteInfo#isReceiverBasedAccessPathWritten(CallTriple, String[], boolean)
	 */
	public boolean isReceiverBasedAccessPathWritten(final CallTriple callerTriple, final String[] accesspath,
		final boolean recurse) {
		if (callerTriple.getExpr().getMethod().isStatic()) {
			throw new IllegalArgumentException("The invoked method should be non-static.");
		}

		final Transformer _transformer =
			TransformerUtils.chainedTransformer(new SiteContextRetriever(callerTriple), thisAliasSetRetriever);
		return instanceDataReadWriteHelper(callerTriple.getMethod(), accesspath, recurse, _transformer, false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IObjectReadWriteInfo#isThisBasedAccessPathRead(soot.SootMethod, java.lang.String[],
	 * 		boolean)
	 */
	public boolean isThisBasedAccessPathRead(final SootMethod method, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException {
		validate(method);

		final Transformer _transformer = TransformerUtils.chainedTransformer(methodCtxtRetriever, thisAliasSetRetriever);
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, true);
	}

	/**
	 * @see IObjectReadWriteInfo#isThisBasedAccessPathWritten(SootMethod, String[], boolean)
	 */
	public boolean isThisBasedAccessPathWritten(final SootMethod method, final String[] accesspath, final boolean recurse) {
		validate(method);

		final Transformer _transformer = TransformerUtils.chainedTransformer(methodCtxtRetriever, thisAliasSetRetriever);
		return instanceDataReadWriteHelper(method, accesspath, recurse, _transformer, false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOf(soot.Local, soot.SootMethod)
	 */
	public Collection getWritingThreadsOf(final Local local, final SootMethod method) {
		Collection _result = Collections.EMPTY_SET;
		final Triple _triple = (Triple) method2Triple.get(method);

		if (_triple != null) {
			final Map _local2as = (Map) _triple.getSecond();
			final AliasSet _as = (AliasSet) _local2as.get(local);

			if (_as != null) {
				_result = _as.getWriteThreads();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOf(int, soot.SootMethod)
	 */
	public Collection getWritingThreadsOf(final int paramIndex, final SootMethod method) {
		validate(paramIndex, method);

		final Collection _result;

		if (method.getParameterType(paramIndex) instanceof RefType) {
			final Triple _triple = (Triple) method2Triple.get(method);

			if (_triple != null) {
				final MethodContext _ctxt = (MethodContext) _triple.getFirst();
				_result = _ctxt.getParamAS(paramIndex).getWriteThreads();
			} else {
				_result = Collections.EMPTY_SET;

				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No recorded information for " + method
						+ " is available.  Returning pessimistic (true) info.");
				}
			}
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOfThis(soot.SootMethod)
	 */
	public Collection getWritingThreadsOfThis(final SootMethod method) {
		validate(method);

		final Triple _triple = (Triple) method2Triple.get(method);
		final Collection _result;

		if (_triple != null) {
			final MethodContext _ctxt = (MethodContext) _triple.getFirst();
			_result = Collections.unmodifiableCollection(_ctxt.thisAS.getWriteThreads());
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * Executes phase 2 and 3 as mentioned in the technical report.  It processed each methods in the call-graph bottom-up
	 * propogating the  alias set information in a collective fashion. It then propogates the information top-down in the
	 * call-graph.
	 */
	public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Equivalence Class-based and Symbol-based Escape Analysis");
		}

		performPhase2();

		performPhase3();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Equivalence Class-based and Symbol-based Escape Analysis");
		}
		stable();
	}

	/**
	 * @see IEscapeInfo#areWaitAndNotifyCoupled(InvokeStmt, SootMethod, InvokeStmt, SootMethod)
	 */
	public boolean areWaitAndNotifyCoupled(final InvokeStmt wait, final SootMethod waitMethod, final InvokeStmt notify,
		final SootMethod notifyMethod) {
		final Triple _trp1 = (Triple) method2Triple.get(waitMethod);

		if (_trp1 == null) {
			throw new IllegalArgumentException(waitMethod + " was not processed.");
		}

		final Triple _trp2 = (Triple) method2Triple.get(notifyMethod);

		if (_trp2 == null) {
			throw new IllegalArgumentException(notifyMethod + " was not processed.");
		}

		final InvokeExpr _wi = wait.getInvokeExpr();
		final InvokeExpr _ni = notify.getInvokeExpr();
		boolean _result = false;

		if (_wi instanceof VirtualInvokeExpr && _ni instanceof VirtualInvokeExpr) {
			final VirtualInvokeExpr _wTemp = (VirtualInvokeExpr) _wi;
			final VirtualInvokeExpr _nTemp = (VirtualInvokeExpr) _ni;
			final SootMethod _wSM = _wTemp.getMethod();
			final SootMethod _nSM = _nTemp.getMethod();

			if (Util.isWaitMethod(_wSM) && Util.isNotifyMethod(_nSM)) {
				final AliasSet _as1 = (AliasSet) ((Map) _trp1.getSecond()).get(_wTemp.getBase());
				final AliasSet _as2 = (AliasSet) ((Map) _trp2.getSecond()).get(_nTemp.getBase());

				if ((_as1.getReadyEntities() != null) && (_as2.getReadyEntities() != null)) {
					_result = CollectionUtils.containsAny(_as1.getReadyEntities(), _as2.getReadyEntities());
				} else {
					/*
					 * This is the case where a start site has wait and notify called on a reference.
					 * In such cases, wait and notify fields are set on the alias set but there is not alias set
					 * with set values to trigger the change of Entity field.
					 * Only if the start site is loop enclosed should these cases flag dependency by setting Entity.
					 */
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
							"There are wait()s and/or notify()s in this program without corresponding notify()s and/or "
							+ "wait()s that occur in different threads - " + wait + "@" + waitMethod + " " + notify + "@"
							+ notifyMethod);
					}
				}
			}
		}

		return _result;
	}

	/**
	 * Checks if the given type can contribute to aliasing.  Only reference and array types can lead to aliasing.
	 *
	 * @param type to be checked for aliasing support.
	 *
	 * @return <code>true</code> if <code>type</code> can contribute aliasing; <code>false</code>, otherwise.
	 *
	 * @pre type != null
	 */
	public static boolean canHaveAliasSet(final Type type) {
		return type instanceof RefType || type instanceof ArrayType;
	}

	/**
	 * @see IEscapeInfo#areCoupledViaLocking(soot.Local, soot.SootMethod, soot.Local, soot.SootMethod)
	 */
	public boolean areCoupledViaLocking(final Local local1, final SootMethod method1, final Local local2,
		final SootMethod method2) {
		final boolean _result;

		if (local1 == null && local2 == null && method1.isStatic() && method2.isStatic()) {
			_result = method1.getDeclaringClass().equals(method2.getDeclaringClass());
		} else if ((local1 == null && method1.isStatic()) ^ (local2 == null && method2.isStatic())) {
			_result = true;
		} else {
			final Triple _trp1 = (Triple) method2Triple.get(method1);

			if (_trp1 == null) {
				throw new IllegalArgumentException(method1 + " was not processed.");
			}

			final AliasSet _a1;

			if (local1 != null) {
				_a1 = (AliasSet) ((Map) _trp1.getSecond()).get(local1);
			} else {
				_a1 = ((MethodContext) _trp1.getFirst()).getThisAS();
			}

			if (_a1 == null) {
				throw new IllegalArgumentException(local1 + " in " + method1 + " was not processed.");
			}

			final Triple _trp2 = (Triple) method2Triple.get(method2);

			if (_trp2 == null) {
				throw new IllegalArgumentException(method2 + " was not processed.");
			}

			final AliasSet _a2;

			if (local2 != null) {
				_a2 = (AliasSet) ((Map) _trp2.getSecond()).get(local2);
			} else {
				_a2 = ((MethodContext) _trp2.getFirst()).getThisAS();
			}

            if (_a2 == null) {
                throw new IllegalArgumentException(local2 + " in " + method2 + " was not processed.");
            }

            final Collection _a1LockEntities = _a1.getLockEntities();
			final Collection _a2LockEntities = _a2.getLockEntities();
			_result =
				_a1LockEntities != null && _a2LockEntities != null
				  && CollectionUtils.containsAny(_a1LockEntities, _a2LockEntities);
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#areMonitorsCoupled(MonitorStmt, SootMethod, MonitorStmt, SootMethod)
	 */
	public boolean areMonitorsCoupled(final MonitorStmt enter, final SootMethod enterMethod, final MonitorStmt exit,
		final SootMethod exitMethod) {
		final boolean _result;

		if (enterMethod.isStatic() || exitMethod.isStatic()) {
			_result = true;
		} else {
			final Triple _trp1 = (Triple) method2Triple.get(enterMethod);

			if (_trp1 == null) {
				throw new IllegalArgumentException(enterMethod + " was not processed.");
			}

			final Triple _trp2 = (Triple) method2Triple.get(exitMethod);

			if (_trp2 == null) {
				throw new IllegalArgumentException(exitMethod + " was not processed.");
			}

			final AliasSet _n;

			if (enter == null) {
				_n = ((MethodContext) _trp1.getFirst()).getThisAS();
			} else {
				_n = (AliasSet) ((Map) _trp1.getSecond()).get(enter.getOp());
			}

			final AliasSet _x;

			if (exit == null) {
				_x = ((MethodContext) _trp2.getFirst()).getThisAS();
			} else {
				_x = (AliasSet) ((Map) _trp2.getSecond()).get(exit.getOp());
			}

			final Collection _xLockEntities = _x.getLockEntities();
			final Collection _nLockEntities = _n.getLockEntities();
			_result =
				_xLockEntities != null && _nLockEntities != null
				  && CollectionUtils.containsAny(_nLockEntities, _xLockEntities);
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IObjectReadWriteInfo#doesInvocationReadGlobalData(edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple)
	 */
	public boolean doesInvocationReadGlobalData(final CallTriple callerTriple) {
		final SootMethod _caller = callerTriple.getMethod();
		return globalDataReadWriteInfoHelper(_caller, new SiteContextRetriever(callerTriple), true);
	}

	/**
	 * @see IObjectReadWriteInfo#doesInvocationWriteGlobalData(ICallGraphInfo.CallTriple)
	 */
	public boolean doesInvocationWriteGlobalData(final CallTriple callerTriple) {
		final SootMethod _caller = callerTriple.getMethod();
		return globalDataReadWriteInfoHelper(_caller, new SiteContextRetriever(callerTriple), false);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IObjectReadWriteInfo#doesMethodReadGlobalData(soot.SootMethod)
	 */
	public boolean doesMethodReadGlobalData(final SootMethod method) {
		return globalDataReadWriteInfoHelper(method, thisAliasSetRetriever, true);
	}

	/**
	 * @see IObjectReadWriteInfo#doesMethodWriteGlobalData(SootMethod)
	 */
	public boolean doesMethodWriteGlobalData(final SootMethod method) {
		return globalDataReadWriteInfoHelper(method, thisAliasSetRetriever, false);
	}

	/**
	 * @see IEscapeInfo#escapes(Value, SootMethod)
	 */
	public boolean escapes(final Value v, final SootMethod sm) {
		boolean _result = true;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = getAliasSetFor(v, sm).escapes();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", _e);
			}
		}

		return _result;
	}

	/**
	 * Flushes the site contexts.
	 */
	public void flushSiteContexts() {
		// delete references to site caches as they will not be used hereon.
		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final Triple _triple = (Triple) method2Triple.get(_sm);
			method2Triple.put(_sm, new Triple(_triple.getFirst(), _triple.getSecond(), null));
		}
	}

	/**
	 * Reset internal data structures.
	 */
	public void reset() {
		super.reset();
		globalASs.clear();
		method2Triple.clear();
	}

	/**
	 * @see IEscapeInfo#shared(Value, SootMethod, Value, SootMethod)
	 */
	public boolean shared(final Value v1, final SootMethod sm1, final Value v2, final SootMethod sm2) {
		boolean _result = escapes(v1, sm1) && escapes(v2, sm2);

		if (_result && !(v1 instanceof StaticFieldRef) && !(v2 instanceof StaticFieldRef)) {
			try {
				final Collection _o1 = getAliasSetFor(v1, sm1).getShareEntities();
				final Collection _o2 = getAliasSetFor(v2, sm2).getShareEntities();
				_result = (_o1 != null) && (_o2 != null) && CollectionUtils.containsAny(_o1, _o2);
			} catch (final NullPointerException _e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("There is no information about " + v1 + "/" + v2 + " occurring in " + sm1 + "/" + sm2
						+ ".  So, providing pessimistic info (true).", _e);
				}
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#thisEscapes(SootMethod)
	 */
	public boolean thisEscapes(final SootMethod method) {
		boolean _result = true;
		final Triple _triple = (Triple) method2Triple.get(method);

		if (_triple == null && LOGGER.isDebugEnabled()) {
			LOGGER.debug("There is no information about " + method + ".  So, providing pessimistic info (true).");
		} else {
			final AliasSet _as1 = ((MethodContext) _triple.getFirst()).getThisAS();

			// if non-static query the alias set of "this" variable.  If static, just return true assuming that the 
			// application to decide wisely :-)
			if (_as1 != null) {
				_result = _as1.escapes();
			}
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer _result = new StringBuffer("\n");
		final Set _entrySet1 = method2Triple.entrySet();
		final Iterator _i = _entrySet1.iterator();
		final int _iEnd = _entrySet1.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _entry1 = (Map.Entry) _i.next();
			_result.append(_entry1.getKey());
			_result.append(":\n");

			final Map _local2AS = (Map) ((Triple) _entry1.getValue()).getSecond();
			_result.append(_local2AS.toString());
			_result.append("\n");
		}
		return _result.toString();
	}

	/**
	 * Retrieves the alias set corresponding to the given value.
	 *
	 * @param v is the value for which the alias set is requested.
	 * @param sm is the method in which <code>v</code> occurs.
	 *
	 * @return the alias set corresponding to <code>v</code>.
	 *
	 * @throws IllegalArgumentException if <code>sm</code> was not analyzed.
	 *
	 * @pre v.isOclKindOf(Local) or v.isOclKindOf(ArrayRef) or v.isOclKindOf(FieldRef) or v.isOclKindOf(ArrayRef) or
	 * 		v.isOclKindOf(InstanceFieldRef) implies v.getBase().isOclKindOf(Local)
	 */
	AliasSet getAliasSetFor(final Value v, final SootMethod sm) {
		final Triple _trp = (Triple) method2Triple.get(sm);

		if (_trp == null) {
			throw new IllegalArgumentException("Method " + sm + " was not analyzed.");
		}

		final Map _local2AS = (Map) _trp.getSecond();
		AliasSet _result = null;

		if (canHaveAliasSet(v.getType())) {
			if (v instanceof InstanceFieldRef) {
				final InstanceFieldRef _i = (InstanceFieldRef) v;
				final AliasSet _temp = (AliasSet) _local2AS.get(_i.getBase());
				_result = _temp.getASForField(_i.getField().getSignature());
			} else if (v instanceof StaticFieldRef) {
				_result = (AliasSet) globalASs.get(((FieldRef) v).getField().getSignature());
			} else if (v instanceof ArrayRef) {
				final ArrayRef _a = (ArrayRef) v;
				final AliasSet _temp = (AliasSet) _local2AS.get(_a.getBase());
				_result = _temp.getASForField(ARRAY_FIELD);
			} else if (v instanceof Local) {
				_result = (AliasSet) _local2AS.get(v);
			} else if (v instanceof ThisRef) {
				_result = ((MethodContext) _trp.getFirst()).getThisAS();
			}
		}

		return _result;
	}

	/**
	 * Retrieves the alias set for "this" variable of the given method.
	 *
	 * @param method of interest.
	 *
	 * @return the alias set corresponding to the "this" variable of the given method.
	 *
	 * @pre method != null and method.isStatic()
	 */
	AliasSet getAliasSetForThis(final SootMethod method) {
		return ((MethodContext) ((Triple) method2Triple.get(method)).getFirst()).thisAS;
	}

	/**
	 * Retrieves the alias set on the callee side that corresponds to the given alias set on the caller side at the given
	 * call site in the caller.
	 *
	 * @param ref the reference alias set.
	 * @param callee provides the context in which the requested reference occurs.
	 * @param site the call site at which <code>callee</code> is called.
	 *
	 * @return the callee side alias set that corresponds to <code>ref</code>.  This will be <code>null</code> if there is no
	 * 		   such alias set.
	 *
	 * @pre ref != null and callee != null and site != null
	 */
	AliasSet getCalleeSideAliasSet(final AliasSet ref, final SootMethod callee, final CallTriple site) {
		final Triple _triple = (Triple) method2Triple.get(site.getMethod());
		final Map _callsite2mc = (Map) _triple.getThird();
		final MethodContext _callingContext = (MethodContext) _callsite2mc.get(site);
		final MethodContext _calleeContext = (MethodContext) ((Triple) method2Triple.get(callee)).getFirst();
		return _callingContext.getImageOfRefInGivenContext(ref, _calleeContext);
	}

	/**
	 * Retrieves the alias set on the caller side that corresponds to the given alias set on the callee side at the given
	 * call site in the caller.
	 *
	 * @param ref the reference alias set.
	 * @param callee the method in which <code>ref</code> occurs.
	 * @param site the call site at which <code>callee</code> is called.
	 *
	 * @return the caller side alias set that corresponds to <code>ref</code>.  This will be <code>null</code> if there is no
	 * 		   such alias set.
	 *
	 * @pre ref != null and callee != null and site != null
	 */
	AliasSet getCallerSideAliasSet(final AliasSet ref, final SootMethod callee, final CallTriple site) {
		final Triple _triple = (Triple) method2Triple.get(site.getMethod());
		final Map _callsite2mc = (Map) _triple.getThird();
		final MethodContext _callingContext = (MethodContext) _callsite2mc.get(site);
		final MethodContext _calleeContext = (MethodContext) ((Triple) method2Triple.get(callee)).getFirst();
		return _calleeContext.getImageOfRefInGivenContext(ref, _callingContext);
	}

	/**
	 * Rewires the method context, local variable alias sets, and site contexts such that they contain only representative
	 * alias sets and no the nominal(indirectional) alias sets.
	 *
	 * @param method for which this processing should occur.
	 *
	 * @pre method != null
	 */
	private void discardReferentialAliasSets(final SootMethod method) {
		if (localASsCache.isEmpty()) {
			localASsCache = Collections.EMPTY_MAP;
		} else {
			for (final Iterator _i = localASsCache.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final AliasSet _as = (AliasSet) _entry.getValue();
				final AliasSet _equiv = (AliasSet) _as.find();

				if (_equiv != _as) {
					_entry.setValue(_equiv);
				}
			}
		}

		if (scCache.isEmpty()) {
			scCache = Collections.EMPTY_MAP;
		} else {
			for (final Iterator _i = scCache.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final MethodContext _mc = (MethodContext) _entry.getValue();
				final MethodContext _mcRep = (MethodContext) _mc.find();

				if (_mcRep != _mc) {
					_entry.setValue(_mcRep);
				}
				_mcRep.discardReferentialAliasSets();
			}
		}
		methodCtxtCache.discardReferentialAliasSets();
		method2Triple.put(method, new Triple(methodCtxtCache, localASsCache, scCache));
	}

	/**
	 * Checks if the given method either reads or writes global data.
	 *
	 * @param method of interest.
	 * @param retriever to be used.
	 * @param read <code>true</code> indicates read information is requested; <code>false</code> indidates write  info is
	 * 		  requested.
	 *
	 * @return <code>true</code> if any global data was read when <code>read</code> is <code>true</code> and
	 * 		   <code>false</code> if it was not read when <code>read</code> was <code>true</code>. <code>true</code> if any
	 * 		   global data was written when <code>read</code> is <code>false</code> and <code>false</code> if it was not
	 * 		   written when <code>read</code> was <code> false</code>.
	 *
	 * @pre method != null and retriever != null
	 */
	private boolean globalDataReadWriteInfoHelper(final SootMethod method, final Transformer retriever, final boolean read) {
		final Triple _triple = (Triple) method2Triple.get(method);
		final boolean _result;

		if (_triple != null) {
			final MethodContext _ctxt = (MethodContext) retriever.transform(_triple);

			if (read) {
				_result = _ctxt.isGlobalDataRead();
			} else {
				_result = _ctxt.isGlobalDataWritten();
			}
		} else {
			_result = true;

			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
		}
		return _result;
	}

	/**
	 * Calculates the read-write information based on an access path rooted at the given method using the given transformer.
	 *
	 * @param method in which the accesspath is rooted.
	 * @param accesspath of interest.
	 * @param recurse <code>true</code> indicates that read/write beyond the end point should be considered.
	 * 		  <code>false</code>, otherwise.
	 * @param retriever to be used to get the method context and the alias set.
	 * @param read <code>true</code> indicates read information is requested; <code>false</code> indidates write  info is
	 * 		  requested.
	 *
	 * @return <code>true</code> if the given access path was read when <code>read</code> is <code>true</code> and
	 * 		   <code>false</code> if it was not read when <code>read</code> was <code>true</code>. <code>true</code> if the
	 * 		   given access path was written when <code>read</code> is <code>false</code> and <code>false</code> if it was
	 * 		   not written when <code>read</code> was <code> false</code>.
	 *
	 * @pre method != null and accesspath != null and retriever != null
	 */
	private boolean instanceDataReadWriteHelper(final SootMethod method, final String[] accesspath, final boolean recurse,
		final Transformer retriever, final boolean read) {
		final AliasSet _aliasSet = ((AliasSet) retriever.transform(method));
		final AliasSet _endPoint;

		if (_aliasSet == null) {
			_endPoint = null;
		} else {
			final Pair _pair = new Pair(_aliasSet.find(), accesspath);

			if (query2handle.containsKey(_pair)) {
				_endPoint = (AliasSet) query2handle.get(_pair);
			} else {
				_endPoint = _aliasSet.getAccessPathEndPoint(accesspath);
				query2handle.put(_pair, _endPoint);
			}
		}

		final boolean _result;

		if (_endPoint == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("isAccessPathOperatedHelper(method = " + method + ", accesspath = " + accesspath + ", recurse = "
					+ recurse + ", retriver = " + retriever + ") - No recorded information for " + method
					+ " is available.  Returning pessimistic (true) info.");
			}
			_result = true;
		} else {
			if (read) {
				_result = AliasSet.isAccessed(_endPoint, recurse);
			} else {
				_result = AliasSet.isFieldWritten(_endPoint, recurse);
			}
		}
		return _result;
	}

	/**
	 * Performs phase 2 processing as described in the paper described in the documentation of this class.
	 */
	private void performPhase2() {
		final Collection _processed = new HashSet();
		final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_processed);
		final Collection _sccs = cgi.getSCCs(false);

		// Phase 2: The SCCs are ordered bottom up. 
		for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
			final List _nodes = (List) _i.next();

			for (final Iterator _j = _nodes.iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Bottom-up processing method " + _sm);
				}

				if (!_sm.isConcrete()) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("NO BODY: " + _sm.getSignature());
					}

					continue;
				}

				final Triple _triple = (Triple) method2Triple.get(_sm);

				methodCtxtCache = (MethodContext) _triple.getFirst();
				localASsCache = (Map) _triple.getSecond();
				scCache = (Map) _triple.getThird();
				context.setRootMethod(_sm);

				final BasicBlockGraph _bbg = bbm.getBasicBlockGraph(_sm);
				_wb.clear();
				_processed.clear();
				_wb.addAllWork(_bbg.getHeads());

				while (_wb.hasWork()) {
					final BasicBlock _bb = (BasicBlock) _wb.getWork();

					for (final Iterator _k = _bb.getStmtsOf().iterator(); _k.hasNext();) {
						final Stmt _stmt = (Stmt) _k.next();
						context.setStmt(_stmt);
						stmtProcessor.process(_stmt);
					}
					_wb.addAllWorkNoDuplicates(_bb.getSuccsOf());
				}

				// discard alias sets that serve as a mere indirection level. 
				discardReferentialAliasSets(_sm);
			}
		}
	}

	/**
	 * Performs phase 3 processing as described in the paper described in the documentation of this class.
	 */
	private void performPhase3() {
		// Phase 3
		final List _methodsInTopologicalOrder = cgi.getMethodsInTopologicalOrder(true);

		for (final Iterator _cgiIterator = _methodsInTopologicalOrder.iterator(); _cgiIterator.hasNext();) {
			final SootMethod _caller = (SootMethod) _cgiIterator.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Top-down processing method : CALLER : " + _caller);
			}

			final Collection _callees = cgi.getCallees(_caller);
			final Triple _callerTriple = (Triple) method2Triple.get(_caller);
			final Map _ctrp2sc = (Map) _callerTriple.getThird();

			for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _callee = _ctrp.getMethod();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Top-down processing : CALLEE : " + _callee);
				}

				final Triple _calleeTriple = (Triple) method2Triple.get(_callee);

				/*
				 * NOTE: This is an anomaly which results from how an open system is closed.  Refer to MethodVariant.java for
				 * more info.
				 */
				if (_calleeTriple == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("NO CALLEE TRIPLE: " + _callee.getSignature());
					}

					continue;
				}

				final MethodContext _calleeMethodContext = (MethodContext) (_calleeTriple.getFirst());
				final CallTriple _callerTrp = new CallTriple(_caller, _ctrp.getStmt(), _ctrp.getExpr());
				final MethodContext _calleeSiteContext = (MethodContext) _ctrp2sc.get(_callerTrp);
				_calleeSiteContext.propogateInfoFromTo(_calleeMethodContext);
			}
		}
	}

	/**
	 * Validates the given parameter position in the given method.
	 *
	 * @param paramPos obviously.
	 * @param method in which the position is being validated.
	 *
	 * @throws IllegalArgumentException if the given position is invalid.
	 *
	 * @pre method != null
	 */
	private void validate(final int paramPos, final SootMethod method)
	  throws IllegalArgumentException {
		if (paramPos >= method.getParameterCount()) {
			throw new IllegalArgumentException(method + " has " + method.getParameterCount() + " arguments, but " + paramPos
				+ " was provided.");
		}
	}

	/**
	 * Validates if the given method is non-static.
	 *
	 * @param method of interest.
	 *
	 * @throws IllegalArgumentException if the given method is static.
	 */
	private void validate(final SootMethod method)
	  throws IllegalArgumentException {
		if (method.isStatic()) {
			throw new IllegalArgumentException("The provided method should be non-static.");
		}
	}
}

// End of File
