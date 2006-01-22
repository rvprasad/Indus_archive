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
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
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
 * This class provides interference dependency information. This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal Study of Slicing for Multi-threaded Program with
 * JVM Concurrency Primitives"</a>.
 * <p>
 * The calculated information is very pessimistic. This analysis uses points-to analysis to check if the primaries of two
 * field references may be aliased.
 * </p>
 * <p>
 * This analysis assumes that there can be no interference between field references in class initializers. This is true for
 * good programs.
 * </p>
 * <p>
 * This analysis should be <code>setup</code> before preprocessing.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InterferenceDAv1
		extends
		AbstractDependencyAnalysis<AssignStmt, SootMethod, Pair<AssignStmt, SootMethod>, Object, Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>, AssignStmt, SootMethod, Pair<AssignStmt, SootMethod>, Object, Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>> {

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
		 * Called by the controller when it encounters an assignment statement. This records array access and field access
		 * expressions.
		 * 
		 * @param stmt in which the access expression occurs.
		 * @param context in which <code>stmt</code> occurs.
		 * @pre stmt.isOclKindOf(AssignStmt)
		 * @pre context.getCurrentMethod() != null
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt,Context)
		 */
		@Override public void callback(final Stmt stmt, final Context context) {
			final AssignStmt _as = (AssignStmt) stmt;
			Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> _temp = null;

			if (_as.containsFieldRef()) {
				if (_as.getLeftOp() instanceof FieldRef) {
					final SootField _sf = ((FieldRef) _as.getLeftOp()).getField();
					_temp = MapUtils.getMapFromMap(dependee2dependent, _sf);
				} else {
					final SootField _sf = ((FieldRef) _as.getRightOp()).getField();
					_temp = MapUtils.getMapFromMap(dependent2dependee, _sf);
				}
			} else if (_as.containsArrayRef()) {
				if (_as.getLeftOp() instanceof ArrayRef) {
					final ArrayType _at = (ArrayType) ((ArrayRef) _as.getLeftOp()).getBase().getType();
					_temp = MapUtils.getMapFromMap(dependee2dependent, _at);
				} else {
					final ArrayType _at = (ArrayType) ((ArrayRef) _as.getRightOp()).getBase().getType();
					_temp = MapUtils.getMapFromMap(dependent2dependee, _at);
				}
			}

			if (_temp != null) {
				_temp.put(pairMgr.getPair(_as, context.getCurrentMethod()), null);
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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(InterferenceDAv1.class);

	/**
	 * This manages pairs.
	 */
	protected PairManager pairMgr;

	/**
	 * This provides threading information pertaining to the system being analyzed.
	 */
	protected IThreadGraphInfo tgi;

	/**
	 * The object flow analysis to be used.
	 */
	private IValueAnalyzer<Value> ofa;

	/**
	 * This indicates if object flow analysis should be used.
	 */
	private boolean useOFA;

	/**
	 * Creates a new InterferenceDAv1 object.
	 */
	public InterferenceDAv1() {
		super(Direction.BI_DIRECTIONAL);
		preprocessor = new PreProcessor();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Interference Dependence [" + this.getClass() + "] processing");
		}

		// we return immediately if there are no start sites in the system.
		if (tgi.getCreationSites().isEmpty()) {
			stable();
			return;
		}

		for (final Iterator<Object> _i = dependent2dependee.keySet().iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (dependee2dependent.get(_o) == null) {
				continue;
			}

			final Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> _dtMap = dependent2dependee
					.get(_o);
			final Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> _deMap = dependee2dependent
					.get(_o);

			for (final Iterator<Pair<AssignStmt, SootMethod>> _j = _dtMap.keySet().iterator(); _j.hasNext();) {
				final Pair<AssignStmt, SootMethod> _dt = _j.next();

				for (final Iterator<Pair<AssignStmt, SootMethod>> _k = _deMap.keySet().iterator(); _k.hasNext();) {
					final Pair<AssignStmt, SootMethod> _de = _k.next();

					if (isDependentOn(_dt, _de)) {
						MapUtils.putIntoCollectionInMap(_dtMap, _dt, _de);
						MapUtils.putIntoCollectionInMap(_deMap, _de, _dt);
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
	 * Returns the statements on which the field/array reference at the given statement and method depends on.
	 * 
	 * @param stmt is the statement in which the array/field reference occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @return a colleciton of pairs comprising of a statement and a method.
	 * @see AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection<Pair<AssignStmt, SootMethod>> getDependees(final AssignStmt stmt, final SootMethod method) {
		Collection<Pair<AssignStmt, SootMethod>> _result = Collections.<Pair<AssignStmt, SootMethod>> emptySet();
		Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> _pair2set = null;
		Object _dependent = null;

		if (stmt.containsArrayRef()) {
			_dependent = stmt.getArrayRef().getBase().getType();
		} else if (stmt.containsFieldRef()) {
			_dependent = stmt.getFieldRef().getField();
		}

		if (_dependent != null) {
			_pair2set = MapUtils.queryMap(dependent2dependee, _dependent);
			final Collection<Pair<AssignStmt, SootMethod>> _set = MapUtils.queryCollection(_pair2set, pairMgr.getPair(stmt,
					method));
			_result = Collections.unmodifiableCollection(_set);
		}
		return _result;
	}

	/**
	 * Returns the statements which depend on the field/array reference at the given statement and method.
	 * 
	 * @param stmt is the statement in which the array/field reference occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @return a colleciton of pairs comprising of a statement and a method.
	 * @see AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection<Pair<AssignStmt, SootMethod>> getDependents(final AssignStmt stmt, final SootMethod method) {
		Collection<Pair<AssignStmt, SootMethod>> _result = Collections.emptyList();
		Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> _pair2set = null;
		Object _dependee = null;

		if (stmt.containsArrayRef()) {
			_dependee = stmt.getArrayRef().getBase().getType();
		} else if (stmt.containsFieldRef()) {
			_dependee = stmt.getFieldRef().getField();
		}

		if (_dependee != null) {
			_pair2set = MapUtils.queryMap(dependee2dependent, _dependee);
			final Collection<Pair<AssignStmt, SootMethod>> _set = MapUtils.queryCollection(_pair2set, pairMgr.getPair(stmt,
					method));
			_result = Collections.unmodifiableCollection(_set);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection<IDependencyAnalysis.DependenceSort> getIds() {
		return Collections.singleton(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	@Override public void reset() {
		super.reset();
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
	 * Returns a stringized representation of this analysis. The representation includes the results of the analysis.
	 * 
	 * @return a stringized representation of this object.
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for Interference dependence as calculated by "
				+ getClass().getName() + "\n");
		int _lEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		final List<Map.Entry<Object, Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>>> _entrySet = new ArrayList<Map.Entry<Object, Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>>>(
				dependent2dependee.entrySet());
		Collections.sort(_entrySet, ToStringBasedComparator.getComparator());

		for (final Iterator<Map.Entry<Object, Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>>> _i = _entrySet
				.iterator(); _i.hasNext();) {
			final Map.Entry<Object, Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>> _entry = _i
					.next();
			_lEdgeCount = 0;

			for (final Iterator<Map.Entry<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>> _j = _entry
					.getValue().entrySet().iterator(); _j.hasNext();) {
				final Map.Entry<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> _entry2 = _j.next();

				final Collection<Pair<AssignStmt, SootMethod>> _collection = _entry2.getValue();

				if (_collection != null) {
					for (final Iterator<Pair<AssignStmt, SootMethod>> _k = _collection.iterator(); _k.hasNext();) {
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

	// /CLOVER:OFF

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependenceRetriever()
	 */
	@Override protected IDependenceRetriever<AssignStmt, SootMethod, Pair<AssignStmt, SootMethod>, AssignStmt, SootMethod, Pair<AssignStmt, SootMethod>> getDependenceRetriever() {
		return new PairRetriever<AssignStmt, SootMethod, AssignStmt, AssignStmt, SootMethod, AssignStmt>();
	}

	// /CLOVER:ON

	/**
	 * Checks if the given array accesses are interference dependent on each other.
	 * 
	 * @param dependent is location of the dependent array access expression.
	 * @param dependee is location of the dependee array access expression.
	 * @param dependentArrayRef is the dependent array access expression.
	 * @param dependeeArrayRef is the dependee array access expression.
	 * @return <code>true</code> if an interference dependence exists; <code>false</code> otherwise.
	 * @pre dependent != null and dependee != null and dependentArrayRef != null and dependeeArrayRef != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	protected boolean isArrayDependentOn(final Pair<AssignStmt, SootMethod> dependent,
			final Pair<AssignStmt, SootMethod> dependee, final ArrayRef dependentArrayRef, final ArrayRef dependeeArrayRef) {
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
	 * Checks if the given instance field access expression are interference dependent on each other.
	 * 
	 * @param dependent is location of the dependent field access expression.
	 * @param dependee is location of the dependee field access expression.
	 * @param dependentFieldRef is the dependent field access expression.
	 * @param dependeeFieldRef is the dependee field access expression.
	 * @return <code>true</code> if an interference dependence exists; <code>false</code> otherwise.
	 * @pre dependent != null and dependee != null and dependentFieldRef != null and dependeeFieldRef != null
	 */
	protected boolean isInstanceFieldDependentOn(final Pair<AssignStmt, SootMethod> dependent,
			final Pair<AssignStmt, SootMethod> dependee, final InstanceFieldRef dependentFieldRef,
			final InstanceFieldRef dependeeFieldRef) {
		boolean _result;
		final SootField _ifr2 = dependentFieldRef.getField();
		_result = dependeeFieldRef.getField().equals(_ifr2) && !_ifr2.isFinal();

		if (_result && useOFA) {
			_result = isFieldDependentOnByOFA(dependent, dependee);
		}
		return _result;
	}

	/**
	 * Checks if the given instance field access expression are interference dependent on each other.
	 * 
	 * @param dependent is location of the dependent field access expression.
	 * @param dependee is location of the dependee field access expression.
	 * @param dependentFieldRef is the dependent field access expression.
	 * @param dependeeFieldRef is the dependee field access expression.
	 * @return <code>true</code> if an interference dependence exists; <code>false</code> otherwise.
	 * @pre dependent != null and dependee != null and dependentFieldRef != null and dependeeFieldRef != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	protected boolean isStaticFieldDependentOn(final Pair<AssignStmt, SootMethod> dependent,
			final Pair<AssignStmt, SootMethod> dependee, final StaticFieldRef dependentFieldRef,
			final StaticFieldRef dependeeFieldRef) {
		boolean _result;
		final SootField _field = dependeeFieldRef.getField();
		_result = dependentFieldRef.getField().equals(_field) && !_field.isFinal();

		if (_result) {
			final SootMethod _deeMethod = dependee.getSecond();
			final String _name = _deeMethod.getName();
			_result = !(_name.equals("<clinit>") && _field.getDeclaringClass().equals(_deeMethod.getDeclaringClass()));
		}
		return _result;
	}

	/**
	 * Extracts information as provided by environment at initialization time.
	 * 
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 *             <code>info</code> member.
	 * @pre info.get(PairManager.ID) != null and info.get(IThreadGraphInfo.ID) != null
	 * @pre useOFA implies info.get(IValueAnalyzer.ID) != null and info.get(IValueAnalyzer.ID).oclIsKindOf(OFAnalyzer)
	 */
	@Override protected void setup() throws InitializationException {
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
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 * @pre dependent != null and dependee != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependent.getFirst().containsArrayRef()
	 * @pre dependee.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.getFirst().containsArrayRef()
	 */
	private boolean isArrayDependentOnByOFA(final Pair<AssignStmt, SootMethod> dependent,
			final Pair<AssignStmt, SootMethod> dependee) {
		boolean _result;
		final ArrayRef _ifr1 = (ArrayRef) (dependee.getFirst()).getLeftOp();
		final ArrayRef _ifr2 = (ArrayRef) (dependent.getFirst()).getRightOp();

		final Context _context = new AllocationContext();
		_context.setProgramPoint(_ifr1.getBaseBox());
		_context.setStmt(dependee.getFirst());
		_context.setRootMethod(dependee.getSecond());

		final Collection<Value> _c1 = ofa.getValues(_ifr1.getBase(), _context);
		_context.setProgramPoint(_ifr2.getBaseBox());
		_context.setStmt(dependent.getFirst());
		_context.setRootMethod(dependent.getSecond());

		final Collection<Value> _c2 = ofa.getValues(_ifr2.getBase(), _context);
		final Collection<Value> _temp = SetUtils.intersection(_c1, _c2);

		while (_temp.remove(NullConstant.v())) {
			; // does nothing
		}
		_result = !_temp.isEmpty();
		return _result;
	}

	/**
	 * Checks if the given array/field access expression is dependent on the given array/field definition expression.
	 * 
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 * @pre dependent != null and dependee != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private boolean isDependentOn(final Pair<AssignStmt, SootMethod> dependent, final Pair<AssignStmt, SootMethod> dependee) {
		final SootMethod _deMethod = dependee.getSecond();
		final SootMethod _dtMethod = dependent.getSecond();
		boolean _result = !tgi.mustOccurInSameThread(_deMethod, _dtMethod);

		if (_result) {
			final Value _de = dependee.getFirst().getLeftOp();
			final Value _dt = dependent.getFirst().getRightOp();

			if (_de instanceof ArrayRef && _dt instanceof ArrayRef) {
				_result = isArrayDependentOn(dependent, dependee, (ArrayRef) _dt, (ArrayRef) _de);
			} else if (_dt instanceof InstanceFieldRef && _de instanceof InstanceFieldRef) {
				_result = isInstanceFieldDependentOn(dependent, dependee, (InstanceFieldRef) _dt, (InstanceFieldRef) _de);
			} else if (_dt instanceof StaticFieldRef && _de instanceof StaticFieldRef) {
				_result = isStaticFieldDependentOn(dependent, dependee, (StaticFieldRef) _dt, (StaticFieldRef) _de);
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
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 * @pre dependent != null and dependee != null
	 * @pre dependent.oclIsKindOf(Pair(Stmt, SootMethod)) and dependent.getFirst().containsFieldRef()
	 * @pre dependee.oclIsKindOf(Pair(Stmt, SootMethod)) and dependee.getFirst().containsFieldRef()
	 */
	private boolean isFieldDependentOnByOFA(final Pair<AssignStmt, SootMethod> dependent,
			final Pair<AssignStmt, SootMethod> dependee) {
		boolean _result;
		final InstanceFieldRef _ifr1 = (InstanceFieldRef) (dependee.getFirst()).getLeftOp();
		final InstanceFieldRef _ifr2 = (InstanceFieldRef) (dependent.getFirst()).getRightOp();

		final Context _context = new AllocationContext();
		_context.setProgramPoint(_ifr1.getBaseBox());
		_context.setStmt(dependee.getFirst());
		_context.setRootMethod(dependee.getSecond());

		final Collection<Value> _c1 = ofa.getValues(_ifr1.getBase(), _context);
		_context.setProgramPoint(_ifr2.getBaseBox());
		_context.setStmt(dependent.getFirst());
		_context.setRootMethod(dependent.getSecond());

		final Collection<Value> _c2 = ofa.getValues(_ifr2.getBase(), _context);
		final Collection<Value> _temp = SetUtils.intersection(_c1, _c2);

		while (_temp.remove(NullConstant.v())) {
			; // does nothing
		}
		_result = !_temp.isEmpty();
		return _result;
	}
}

// End of File
