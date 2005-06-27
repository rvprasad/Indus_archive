
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
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;

import org.apache.commons.collections.map.LRUMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;


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
  extends AbstractAnalysis {
	/** 
	 * The id of this analysis.
	 */
	public static final Object ID = "equivalence class based escape analysis";

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
	private static final Log LOGGER = LogFactory.getLog(EquivalenceClassBasedEscapeAnalysis.class);

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
	 * This provides thread-graph information.
	 */
	final IThreadGraphInfo tgi;

	/** 
	 * This maps classes to alias sets that serve as bases for static fields.
	 *
	 * @invariant class2aliasSets->forall(o | o.oclIsKindOf(AliasSet))
	 */
	final Map class2aliasSet;

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
	 * This is a cache variable that holds local alias set map between method calls.
	 *
	 * @invariant localASsCache.oclIsKindOf(Local, AliasSet)
	 */
	Map localASsCache;

	/** 
	 * This maintains a cache of query to alias set.
	 *
	 * @invariant query2handle.oclIsKindOf(Map(Pair(AliasSet, String[]), AliasSet))
	 */
	final Map query2handle = new LRUMap();

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
	 * This is the default verdict for escapes queries.
	 */
	boolean escapesDefaultValue;

	/** 
	 * This is the default verdict for access-path based read queries.
	 */
	boolean readDefaultValue;

	/** 
	 * This is the default verdict for access-path based write queries.
	 */
	boolean writeDefaultValue;

	/** 
	 * This is the object that exposes object escape info calculated by this instance.
	 */
	private final EscapeInfo escapeInfo;

	/** 
	 * This is the object that exposes object read-write info calculated by this instance.
	 */
	private final ReadWriteInfo objectReadWriteInfo;

	/**
	 * Creates a new EquivalenceClassBasedEscapeAnalysis object.  The default value for escapes, reads, and writes is set to
	 * <code>true</code>, <code>false</code>, and <code>false</code>, respectively.
	 *
	 * @param callgraph provides call-graph information.
	 * @param threadgraph provides thread graph information.  If this is <code>null</code> then read-write specific thread
	 * 		  information is not captured.
	 * @param basicBlockGraphMgr provides basic block graphs required by this analysis.
	 *
	 * @pre scene != null and callgraph != null and threadgraph != null
	 */
	public EquivalenceClassBasedEscapeAnalysis(final ICallGraphInfo callgraph, final IThreadGraphInfo threadgraph,
		final BasicBlockGraphMgr basicBlockGraphMgr) {
		cgi = callgraph;
		tgi = threadgraph;
		class2aliasSet = new HashMap();
		method2Triple = new HashMap();
		stmtProcessor = new StmtProcessor(this);
		valueProcessor = new ValueProcessor(this);
		bbm = basicBlockGraphMgr;
		context = new Context();
		cfgAnalysis = new CFGAnalysis(cgi, bbm);
		preprocessor = new PreProcessor();
		escapesDefaultValue = true;
		readDefaultValue = false;
		writeDefaultValue = false;
		escapeInfo = new EscapeInfo(this);
		objectReadWriteInfo = new ReadWriteInfo(this);
	}

	/**
	 * This class retrieves the alias set corresponding to a param/arg position from a method context.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class ArgParamAliasSetRetriever
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
	class SiteContextRetriever
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
	 * This class is used to create alias sets for global variables.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class PreProcessor
	  extends AbstractProcessor {
		/**
		 * {@inheritDoc}  Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's
		 * algorithm.
		 */
		public void callback(final SootMethod sm) {
			final MethodContext _methodContext = new MethodContext(sm, EquivalenceClassBasedEscapeAnalysis.this);
			method2Triple.put(sm, new Triple(_methodContext, new HashMap(), new HashMap()));

			if (sm.isSynchronized() && !sm.isStatic()) {
				final AliasSet _thisAS = _methodContext.getThisAS();
				_thisAS.setLocked();
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
	 * Retrieves escape info provider.
	 *
	 * @return an escape info provider.
	 *
	 * @post result != null
	 */
	public IEscapeInfo getEscapeInfo() {
		return escapeInfo;
	}

	/**
	 * Sets the default value to be returned on unanswerable escape equeries.
	 *
	 * @param value the new value of <code>escapesDefaultValue</code>.
	 */
	public void setEscapesDefaultValue(final boolean value) {
		this.escapesDefaultValue = value;
	}

	/**
	 * Sets the default value to be returned on unanswerable access-path based read queries.
	 *
	 * @param value the new value of <code>readDefaultValue</code>.
	 */
	public void setReadDefaultValue(final boolean value) {
		this.readDefaultValue = value;
	}

	/**
	 * Retrieves read-write info provider.
	 *
	 * @return an read-write info provider.
	 *
	 * @post result != null
	 */
	public IReadWriteInfo getReadWriteInfo() {
		return objectReadWriteInfo;
	}

	/**
	 * Sets the default value to be returned on unanswerable access-path based written queries.
	 *
	 * @param value the new value of <code>value</code>.
	 */
	public void setWriteDefaultValue(final boolean value) {
		this.writeDefaultValue = value;
	}

	/**
	 * Executes phase 2 and 3 as mentioned in the technical report.  It processed each methods in the call-graph bottom-up
	 * propogating the  alias set information in a collective fashion. It then propogates the information top-down in the
	 * call-graph.
	 */
	public void analyze() {
		unstable();
		escapeInfo.unstableAdapter();
		objectReadWriteInfo.unstableAdapter();

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
		escapeInfo.stableAdapter();
		objectReadWriteInfo.stableAdapter();
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
		class2aliasSet.clear();
		method2Triple.clear();
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
	 * Retrieves the alias set for the class. This acts like "this" variable for static fields defined in the  given class.
	 *
	 * @param declaringClass of interest.
	 *
	 * @return the alias set.
	 *
	 * @pre declaringClass != null
	 * @post result != null
	 */
	AliasSet getASForClass(final SootClass declaringClass) {
		AliasSet _result = (AliasSet) class2aliasSet.get(declaringClass);

		if (_result == null) {
			_result = AliasSet.getASForType(declaringClass.getType());
			class2aliasSet.put(declaringClass, _result);
		}
		return _result;
	}

	/**
	 * Retrieves the alias set corresponding to the given value.  This method cannot handled <code>CaughtExceptionRef</code>.
	 *
	 * @param v is the value for which the alias set is requested.
	 * @param sm is the method in which <code>v</code> occurs.
	 *
	 * @return the alias set corresponding to <code>v</code>.
	 *
	 * @throws IllegalArgumentException if <code>sm</code> was not analyzed.
	 *
	 * @pre v.isOclKindOf(Local) or v.isOclKindOf(ArrayRef) or v.isOclKindOf(FieldRef) or v.isOclKindOf(ArrayRef) or
	 * 		v.isOclKindOf(InstanceFieldRef) or v.isOclIsKindOf(ParameterRef)
	 */
	AliasSet getAliasSetFor(final Value v, final SootMethod sm) {
		final Triple _trp = (Triple) method2Triple.get(sm);

		if (_trp == null) {
			throw new IllegalArgumentException("Method " + sm + " was not analyzed.");
		}

		final Map _local2AS = (Map) _trp.getSecond();
		final AliasSet _result;

		if (v instanceof InstanceFieldRef) {
			final InstanceFieldRef _i = (InstanceFieldRef) v;
			final AliasSet _temp = (AliasSet) _local2AS.get(_i.getBase());
			_result = _temp.getASForField(_i.getField().getSignature());
		} else if (v instanceof StaticFieldRef) {
			final SootField _field = ((StaticFieldRef) v).getField();
			final AliasSet _base = getASForClass(_field.getDeclaringClass());
			_result = _base.getASForField(_field.getSignature());
		} else if (v instanceof ArrayRef) {
			final ArrayRef _a = (ArrayRef) v;
			final AliasSet _temp = (AliasSet) _local2AS.get(_a.getBase());
			_result = _temp.getASForField(IReadWriteInfo.ARRAY_FIELD);
		} else if (v instanceof Local) {
			_result = (AliasSet) _local2AS.get(v);
		} else if (v instanceof ThisRef) {
			_result = ((MethodContext) _trp.getFirst()).getThisAS();
		} else if (v instanceof ParameterRef) {
			_result = ((MethodContext) _trp.getFirst()).getParamAS(((ParameterRef) v).getIndex());
		} else if (v instanceof CaughtExceptionRef) {
			final String _msg = "CaughtExceptionRef cannot be handled.";
			LOGGER.error(_msg);
			throw new IllegalArgumentException(_msg);
		} else {
			_result = null;
		}

		return _result;
	}

	/**
	 * Retrieves the alias set for the given soot class.
	 *
	 * @param sc is the class of interest.
	 *
	 * @return an alias set.
	 */
	AliasSet getAliasSetFor(final SootClass sc) {
		return (AliasSet) class2aliasSet.get(sc);
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
	 * Validates the given parameter position in the given method.
	 *
	 * @param paramPos obviously.
	 * @param method in which the position is being validated.
	 *
	 * @throws IllegalArgumentException if the given position is invalid.
	 *
	 * @pre method != null
	 */
	void validate(final int paramPos, final SootMethod method)
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
	void validate(final SootMethod method)
	  throws IllegalArgumentException {
		if (method.isStatic()) {
			throw new IllegalArgumentException("The provided method should be non-static.");
		}
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
	 * Performs phase 2 processing as described in the paper mentioned in the documentation of this class.
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
				_wb.addWork(_bbg.getHead());

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
}

// End of File
