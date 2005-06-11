
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;


/**
 * This class provides interference dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.  The calculated information is very pessimistic.  For fields, it assumes any
 * assignment to a field can affect any reference to the same field.  This is imprecise in the light of thread local objects
 * and unrelated primaries.
 * 
 * <p>
 * This analysis should be <code>setup</code> before preprocessing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependent2dependee.oclIsKindOf(Map(Object, Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt,
 * 			  SootMethodMethod)))))
 * @invariant dependee2dependent.oclIsKindOf(Map(Object, Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt,
 * 			  SootMethodMethod)))))
 */
public class InterferenceDAv1
  extends AbstractDependencyAnalysis {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(InterferenceDAv1.class);

	/** 
	 * This provides threading information pertaining to the system being analyzed.
	 */
	protected IThreadGraphInfo tgi;

	/** 
	 * This manages pairs.
	 */
	protected PairManager pairMgr;

	/** 
	 * The object flow analysis to be used.
	 */
	private IValueAnalyzer ofa;

	/** 
	 * This indicates if object flow analysis should be used.
	 */
	private boolean useOFA;

	/**
	 * Creates a new InterferenceDAv1 object.
	 */
	public InterferenceDAv1() {
		preprocessor = new PreProcessor();
	}

	/**
	 * A preprocessor which captures all the array and field access locations in the analyzed system.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Called by the controller when it encounters an assignment statement.  This records array access and field access
		 * expressions.
		 *
		 * @param stmt in which the access expression occurs.
		 * @param context in which <code>stmt</code> occurs.
		 *
		 * @pre stmt.isOclKindOf(AssignStmt)
		 * @pre context.getCurrentMethod() != null
		 *
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt,Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			final AssignStmt _as = (AssignStmt) stmt;
			Map _temp = null;

			if (_as.containsFieldRef()) {
				if (_as.getLeftOp() instanceof FieldRef) {
					final SootField _sf = ((FieldRef) _as.getLeftOp()).getField();
					_temp = CollectionsUtilities.getMapFromMap(dependee2dependent, _sf);
				} else {
					final SootField _sf = ((FieldRef) _as.getRightOp()).getField();
					_temp = CollectionsUtilities.getMapFromMap(dependent2dependee, _sf);
				}
			} else if (_as.containsArrayRef()) {
				if (_as.getLeftOp() instanceof ArrayRef) {
					final ArrayType _at = (ArrayType) ((ArrayRef) _as.getLeftOp()).getBase().getType();
					_temp = CollectionsUtilities.getMapFromMap(dependee2dependent, _at);
				} else {
					final ArrayType _at = (ArrayType) ((ArrayRef) _as.getRightOp()).getBase().getType();
					_temp = CollectionsUtilities.getMapFromMap(dependent2dependee, _at);
				}
			}

			if (_temp != null) {
				_temp.put(pairMgr.getPair(stmt, context.getCurrentMethod()), null);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			if (tgi == null) {
				throw new IllegalStateException("Please setup the enclosing analysis before starting preprocessing.");
			}

			// we do not hookup if there are no threads in the system.
			if (!tgi.getCreationSites().isEmpty()) {
				ppc.register(AssignStmt.class, this);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			if (tgi == null) {
				throw new IllegalStateException("Please setup the enclosing analysis before starting preprocessing.");
			}

			// we do not unhook if there are no threads in the system.
			if (!tgi.getCreationSites().isEmpty()) {
				ppc.unregister(AssignStmt.class, this);
			}
		}
	}

	/**
	 * Returns the statements on which the field/array reference at the given statement and method depends on.
	 *
	 * @param stmt is the statement in which the array/field reference occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre stmt.oclIsTypeOf(Stmt) or method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object stmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final Stmt _temp = (Stmt) stmt;
		Map _pair2set = null;
		Object _dependent = null;

		if (_temp.containsArrayRef()) {
			_dependent = _temp.getArrayRef().getBase().getType();
		} else if (_temp.containsFieldRef()) {
			_dependent = _temp.getFieldRef().getField();
		}

		if (_dependent != null) {
			_pair2set = (Map) MapUtils.getObject(dependent2dependee, _dependent, Collections.EMPTY_MAP);

			if (_pair2set != null) {
				final Collection _set = (Collection) _pair2set.get(pairMgr.getPair(stmt, method));

				if (_set != null) {
					_result = Collections.unmodifiableCollection(_set);
				}
			}
		}
		return _result;
	}

	/**
	 * Returns the statements which depend on the field/array reference at the given statement and method.
	 *
	 * @param stmt is the statement in which the array/field reference occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre stmt.oclIsTypeOf(Stmt) or method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final Stmt _temp = (Stmt) stmt;
		Map _pair2set = null;
		Object _dependee = null;

		if (_temp.containsArrayRef()) {
			_dependee = _temp.getArrayRef().getBase().getType();
		} else if (_temp.containsFieldRef()) {
			_dependee = _temp.getFieldRef().getField();
		}

		if (_dependee != null) {
			_pair2set = (Map) MapUtils.getObject(dependee2dependent, _dependee, Collections.EMPTY_MAP);

			if (_pair2set != null) {
				final Collection _set = (Collection) _pair2set.get(pairMgr.getPair(stmt, method));

				if (_set != null) {
					_result = Collections.unmodifiableCollection(_set);
				}
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  This implementation is bi-directional.
	 */
	public Object getDirection() {
		return BI_DIRECTIONAL;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.INTERFERENCE_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.PAIR_DEP_RETRIEVER);
	}

	/**
	 * Sets if object flow analysis should be used or not.
	 *
	 * @param flag <code>true</code> indicates that object flow analysis should be used; <code>false</code>, otherwise.
	 */
	public final void setUseOFA(final boolean flag) {
		useOFA = flag;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Interference Dependence [" + this.getClass() + "] processing");
		}

		// we return immediately if there are no start sites in the system.
		if (tgi.getCreationSites().isEmpty()) {
			stable();
			return;
		}

		for (final Iterator _i = dependent2dependee.keySet().iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (dependee2dependent.get(_o) == null) {
				continue;
			}

			final Map _dtMap = (Map) dependent2dependee.get(_o);
			final Map _deMap = (Map) dependee2dependent.get(_o);

			for (final Iterator _j = _dtMap.keySet().iterator(); _j.hasNext();) {
				final Pair _dt = (Pair) _j.next();

				for (final Iterator _k = _deMap.keySet().iterator(); _k.hasNext();) {
					final Pair _de = (Pair) _k.next();

					if (considerEffectOfClassInitializers(_dt, _de) && isDependentOn(_dt, _de)) {
						CollectionsUtilities.putIntoSetInMap(_dtMap, _dt, _de);
						CollectionsUtilities.putIntoSetInMap(_deMap, _de, _dt);
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Interference Dependence processing");
		}

		stable();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Interference dependence as calculated by " + getClass().getName() + "\n");
		int _lEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		final List _entrySet = new ArrayList(dependent2dependee.entrySet());
        Collections.sort(_entrySet, ToStringBasedComparator.SINGLETON);
        for (final Iterator _i = _entrySet.iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_lEdgeCount = 0;

			for (final Iterator _j = ((Map) _entry.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _entry2 = (Map.Entry) _j.next();

				final Collection _collection = (Collection) _entry2.getValue();

				if (_collection != null) {
					for (final Iterator _k = _collection.iterator(); _k.hasNext();) {
						_temp.append("\t\t" + _entry2.getKey() + " --> " + _k.next() + "\n");
					}
					_lEdgeCount += _collection.size();
				}
			}
			_result.append("\tFor " + _entry.getKey() + " there are " + _lEdgeCount + " Interference dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _lEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " Interference dependence edges exist.");
		return _result.toString();
	}

	///CLOVER:ON

	/**
	 * Checks if the given array accesses are interference dependent on each other.
	 *
	 * @param dependent is location of the dependent array access expression.
	 * @param dependee is location of the dependee array access expression.
	 * @param dependentArrayRef is the dependent array access expression.
	 * @param dependeeArrayRef is the dependee array access expression.
	 *
	 * @return <code>true</code> if an interference dependence exists; <code>false</code> otherwise.
	 *
	 * @pre dependent != null and dependee != null and dependentArrayRef != null and dependeeArrayRef != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	protected boolean isArrayDependentOn(final Pair dependent, final Pair dependee, final ArrayRef dependentArrayRef,
		final ArrayRef dependeeArrayRef) {
		boolean _result;
		final Type _t1 = dependeeArrayRef.getBase().getType();
		final Type _t2 = dependentArrayRef.getBase().getType();
		_result = _t1.equals(_t2);

		if (_result && useOFA) {
			_result = isArrayDependentOnByOFA(dependent, dependee);
		}
		return _result;
	}

	/**
	 * Checks if the given field access expression are interference dependent on each other.
	 *
	 * @param dependent is location of the dependent field access expression.
	 * @param dependee is location of the dependee field access expression.
	 * @param dependentFieldRef is the dependent field access expression.
	 * @param dependeeFieldRef is the dependee field access expression.
	 *
	 * @return <code>true</code> if an interference dependence exists; <code>false</code> otherwise.
	 *
	 * @pre dependent != null and dependee != null and dependentFieldRef != null and dependeeFieldRef != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	protected boolean isFieldDependentOn(final Pair dependent, final Pair dependee, final InstanceFieldRef dependentFieldRef,
		final InstanceFieldRef dependeeFieldRef) {
		boolean _result;
		final SootField _ifr1 = dependeeFieldRef.getField();
		final SootField _ifr2 = dependentFieldRef.getField();
		_result = _ifr1.equals(_ifr2);

		if (_result && useOFA) {
			_result = isFieldDependentOnByOFA(dependent, dependee);
		}
		return _result;
	}

	/**
	 * Extracts information as provided by environment at initialization time.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 *
	 * @pre info.get(PairManager.ID) != null and info.get(IThreadGraphInfo.ID) != null
	 * @pre useOFA implies info.get(IValueAnalyzer.ID) != null  and info.get(IValueAnalyzer.ID).oclIsKindOf(OFAnalyzer)
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ofa = (OFAnalyzer) info.get(IValueAnalyzer.ID);

		if (useOFA && ofa == null) {
			throw new InitializationException(IValueAnalyzer.ID + " was not provided in the info.");
		}

		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in info.");
		}

		tgi = (IThreadGraphInfo) info.get(IThreadGraphInfo.ID);

		if (tgi == null) {
			throw new InitializationException(IThreadGraphInfo.ID + " was not provided in info.");
		}
	}

	/**
	 * Checks if a dependence relation exists between the given entities based on object flow information assocaited with the
	 * base of the expression array access expression.
	 *
	 * @param dependent is the array read access site.
	 * @param dependee is the array write access site.
	 *
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependent.getFirst().containsArrayRef()
	 * @pre dependee.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.getFirst().containsArrayRef()
	 */
	private boolean isArrayDependentOnByOFA(final Pair dependent, final Pair dependee) {
		boolean _result;
		final ArrayRef _ifr1 = (ArrayRef) ((AssignStmt) dependee.getFirst()).getLeftOp();
		final ArrayRef _ifr2 = (ArrayRef) ((AssignStmt) dependent.getFirst()).getRightOp();

		final Context _context = new AllocationContext();
		_context.setProgramPoint(_ifr1.getBaseBox());
		_context.setStmt((Stmt) dependee.getFirst());
		_context.setRootMethod((SootMethod) dependee.getSecond());

		final Collection _c1 = ofa.getValues(_ifr1.getBase(), _context);
		_context.setProgramPoint(_ifr2.getBaseBox());
		_context.setStmt((Stmt) dependent.getFirst());
		_context.setRootMethod((SootMethod) dependent.getSecond());

		final Collection _c2 = ofa.getValues(_ifr2.getBase(), _context);
		final Collection _temp = CollectionUtils.intersection(_c1, _c2);

		while (_temp.remove(NullConstant.v())) {
			;
		}
		_result = !_temp.isEmpty();
		return _result;
	}

	/**
	 * Checks if the given array/field access expression is dependent on the given array/field definition expression.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private boolean isDependentOn(final Pair dependent, final Pair dependee) {
		final SootMethod _deMethod = (SootMethod) dependee.getSecond();
		final SootMethod _dtMethod = (SootMethod) dependent.getSecond();
		boolean _result = !tgi.mustOccurInSameThread(_deMethod, _dtMethod);

		if (_result) {
			final Value _de = ((AssignStmt) dependee.getFirst()).getLeftOp();
			final Value _dt = ((AssignStmt) dependent.getFirst()).getRightOp();

			if (_de instanceof ArrayRef && _dt instanceof ArrayRef) {
				_result = isArrayDependentOn(dependent, dependee, (ArrayRef) _dt, (ArrayRef) _de);
			} else if (_dt instanceof InstanceFieldRef && _de instanceof InstanceFieldRef) {
				_result = isFieldDependentOn(dependent, dependee, (InstanceFieldRef) _dt, (InstanceFieldRef) _de);
			}
		}

		return _result;
	}

	/**
	 * Checks if a dependence relation exists between the given entities based on object flow information assocaited with the
	 * base of the expression field access expression.
	 *
	 * @param dependent is the field read access site.
	 * @param dependee is the field write access site.
	 *
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependent.getFirst().containsFieldRef()
	 * @pre dependee.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.getFirst().containsFieldRef()
	 */
	private boolean isFieldDependentOnByOFA(final Pair dependent, final Pair dependee) {
		boolean _result;
		final InstanceFieldRef _ifr1 = (InstanceFieldRef) ((AssignStmt) dependee.getFirst()).getLeftOp();
		final InstanceFieldRef _ifr2 = (InstanceFieldRef) ((AssignStmt) dependent.getFirst()).getRightOp();

		final Context _context = new AllocationContext();
		_context.setProgramPoint(_ifr1.getBaseBox());
		_context.setStmt((Stmt) dependee.getFirst());
		_context.setRootMethod((SootMethod) dependee.getSecond());

		final Collection _c1 = ofa.getValues(_ifr1.getBase(), _context);
		_context.setProgramPoint(_ifr2.getBaseBox());
		_context.setStmt((Stmt) dependent.getFirst());
		_context.setRootMethod((SootMethod) dependent.getSecond());

		final Collection _c2 = ofa.getValues(_ifr2.getBase(), _context);
		final Collection _temp = CollectionUtils.intersection(_c1, _c2);

		while (_temp.remove(NullConstant.v())) {
			;
		}
		_result = !_temp.isEmpty();
		return _result;
	}

	/**
	 * Checks if the given dependence has any of the ends rooted in a class initializer and prunes the dependence based on
	 * this information.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code> if the dependence should be considered; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 */
	private boolean considerEffectOfClassInitializers(final Pair dependent, final Pair dependee) {
		final SootMethod _deMethod = (SootMethod) dependee.getSecond();
		final SootMethod _dtMethod = (SootMethod) dependent.getSecond();
		boolean _result = true;

		// If either one of the methods is a class initialization method then we can optimize.
		final boolean _deci = _deMethod.getName().equals("<clinit>");
		final boolean _dtci = _dtMethod.getName().equals("<clinit>");

		if (_deci || _dtci) {
			final SootClass _deClass = _deMethod.getDeclaringClass();
			final SootClass _dtClass = _dtMethod.getDeclaringClass();

			// if the classes of both the methods are not related
			_result = !Util.isHierarchicallyRelated(_deClass, _dtClass);

			if (_result) {
				final Value _de = ((AssignStmt) dependee.getFirst()).getLeftOp();
				final Value _dt = ((AssignStmt) dependent.getFirst()).getRightOp();

				if (_de instanceof StaticFieldRef) {
					final SootField _f1 = ((StaticFieldRef) _de).getField();
					final SootField _f2 = ((FieldRef) _dt).getField();

					/*
					 * if f1 and f2 are the same fields and
					 *   if deMethod is clinit and f1 was declared in deClass or
					 *      dtMethod is clinit and f2 was declared in dtClass then
					 *      the dependence is invalid and it should not be considered.
					 */
					_result =
						!((_f1.equals(_f2)
						  && ((_deci && _f1.getDeclaringClass().equals(_deClass))
						  || (_dtci && _f1.getDeclaringClass().equals(_dtClass)))));
				}
			}
		}
		return _result;
	}
}

// End of File
