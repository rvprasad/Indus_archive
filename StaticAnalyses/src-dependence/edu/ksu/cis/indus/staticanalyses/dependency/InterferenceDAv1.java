
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

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	  extends AbstractValueAnalyzerBasedProcessor {
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
			final SootMethod _method = context.getCurrentMethod();
			final AssignStmt _as = (AssignStmt) stmt;
			Map _temp = null;

			if (_as.containsFieldRef()) {
				if (_as.getLeftOp() instanceof FieldRef) {
					final SootField _sf = ((FieldRef) _as.getLeftOp()).getField();
					_temp = getDependeXXMapHelper(dependee2dependent, _sf);
				} else {
					final SootField _sf = ((FieldRef) _as.getRightOp()).getField();
					_temp = getDependeXXMapHelper(dependent2dependee, _sf);
				}
			} else if (_as.containsArrayRef()) {
				if (_as.getLeftOp() instanceof ArrayRef) {
					final ArrayType _at = (ArrayType) ((ArrayRef) _as.getLeftOp()).getBase().getType();
					_temp = getDependeXXMapHelper(dependee2dependent, _at);
				} else {
					final ArrayType _at = (ArrayType) ((ArrayRef) _as.getRightOp()).getBase().getType();
					_temp = getDependeXXMapHelper(dependent2dependee, _at);
				}
			}

			if (_temp != null) {
				final Pair _p = pairMgr.getOptimizedPair(_as, _method);

				if (_temp.get(_p) == null) {
					_temp.put(_p, Collections.EMPTY_LIST);
				}
			}
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			if (tgi == null) {
				throw new IllegalStateException("Please setup the enclosing analysis before starting preprocessing.");
			}

			// we do not hookup if there are no threads in the system.
			if (tgi.getStartSites().size() != 0) {
				ppc.register(AssignStmt.class, this);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			if (tgi == null) {
				throw new IllegalStateException("Please setup the enclosing analysis before starting preprocessing.");
			}

			// we do not unhook if there are no threads in the system.
			if (tgi.getStartSites().size() != 0) {
				ppc.unregister(AssignStmt.class, this);
			}
		}

		/**
		 * Helper method for getDependeXXMap() methods.
		 *
		 * @param map from which to extract the result map.
		 * @param o is the field/array reference of interest.
		 *
		 * @return the map corresponding to the <code>o</code>.
		 */
		private Map getDependeXXMapHelper(final Map map, final Object o) {
			Map _result = (Map) map.get(o);

			if (_result == null) {
				_result = new HashMap();
				map.put(o, _result);
			}
			return _result;
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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
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
				final Collection _set = (Collection) _pair2set.get(pairMgr.getUnOptimizedPair(stmt, method));

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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
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
				final Collection _set = (Collection) _pair2set.get(pairMgr.getUnOptimizedPair(stmt, method));

				if (_set != null) {
					_result = Collections.unmodifiableCollection(_set);
				}
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return AbstractDependencyAnalysis.INTERFERENCE_DA;
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
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Interference Dependence [" + this.getClass() + "] processing");
		}

		// we return immediately if there are no start sites in the system.
		if (tgi.getStartSites().size() == 0) {
			stable = true;
			return;
		}

		for (final Iterator _i = dependent2dependee.keySet().iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (dependee2dependent.get(_o) == null) {
				continue;
			}

			final Map _deMap = (Map) dependent2dependee.get(_o);
			final Map _dtMap = (Map) dependee2dependent.get(_o);

			for (final Iterator _j = _deMap.keySet().iterator(); _j.hasNext();) {
				final Pair _dt = (Pair) _j.next();

				for (final Iterator _k = _dtMap.keySet().iterator(); _k.hasNext();) {
					final Pair _de = (Pair) _k.next();

					if (considerClassInitializers(_dt, _de) && isDependentOn(_dt, _de)) {
						Collection _t = (Collection) _deMap.get(_dt);

						if (_t.equals(Collections.EMPTY_LIST)) {
							_t = new HashSet();
							_deMap.put(_dt, _t);
						}
						_t.add(_de);
						_t = (Collection) _dtMap.get(_de);

						if (_t.equals(Collections.EMPTY_LIST)) {
							_t = new HashSet();
							_dtMap.put(_de, _t);
						}
						_t.add(_dt);
					}
				}
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Interference Dependence processing");
		}

		stable = true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
	}

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

		for (final Iterator _i = dependent2dependee.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_lEdgeCount = 0;

			for (final Iterator _j = ((Map) _entry.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _entry2 = (Map.Entry) _j.next();

				for (final Iterator _k = ((Collection) _entry2.getValue()).iterator(); _k.hasNext();) {
					_temp.append("\t\t" + _entry2.getKey() + " --> " + _k.next() + "\n");
				}
				_lEdgeCount += ((Collection) _entry2.getValue()).size();
			}
			_result.append("\tFor " + _entry.getKey() + " there are " + _lEdgeCount + " Interference dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _lEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " Interference dependence edges exist.");
		return _result.toString();
	}

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

		if (ofa == null) {
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
		final ArrayRef _ifr1 = ((ArrayRef) ((AssignStmt) dependee.getFirst()).getLeftOp());
		final ArrayRef _ifr2 = ((ArrayRef) ((AssignStmt) dependent.getFirst()).getRightOp());

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
		boolean _result = true;
		final Value _de = ((AssignStmt) dependee.getFirst()).getLeftOp();
		final Value _dt = ((AssignStmt) dependent.getFirst()).getRightOp();

		if (_de instanceof ArrayRef && _dt instanceof ArrayRef) {
			_result = isArrayDependentOn(dependent, dependee, (ArrayRef) _dt, (ArrayRef) _de);
		} else if (_dt instanceof InstanceFieldRef && _de instanceof InstanceFieldRef) {
			_result = isFieldDependentOn(dependent, dependee, (InstanceFieldRef) _dt, (InstanceFieldRef) _de);
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
		final InstanceFieldRef _ifr1 = ((InstanceFieldRef) ((AssignStmt) dependee.getFirst()).getLeftOp());
		final InstanceFieldRef _ifr2 = ((InstanceFieldRef) ((AssignStmt) dependent.getFirst()).getRightOp());

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
	 * Checks if the given dependence has any of the ends rooted in a class initializer and prunes the  dependence based on
	 * this information.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code> if the dependence should be considered; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 */
	private boolean considerClassInitializers(final Pair dependent, final Pair dependee) {
		final SootMethod _deMethod = (SootMethod) dependee.getSecond();
		final SootMethod _dtMethod = (SootMethod) dependent.getSecond();
		boolean _result = true;

		// If any one of the method is a class initialization method then we can optimize.
		final boolean _deci = _deMethod.getName().equals("<clinit>");
		final boolean _dtci = _dtMethod.getName().equals("<clinit>");

		if (_deci || _dtci) {
			final SootClass _deClass = _deMethod.getDeclaringClass();
			final SootClass _dtClass = _dtMethod.getDeclaringClass();

			// if the classes of both the methods are related
			if (Util.isHierarchicallyRelated(_deClass, _dtClass)) {
				_result = false;
			} else {
				final Value _de = ((AssignStmt) dependee.getFirst()).getLeftOp();
				final Value _dt = ((AssignStmt) dependent.getFirst()).getRightOp();

				if (_dt instanceof StaticFieldRef && _de instanceof StaticFieldRef) {
					final SootField _f1 = ((StaticFieldRef) _de).getField();
					final SootField _f2 = ((StaticFieldRef) _dt).getField();

					if (_f1.equals(_f2)
						  && ((_deci && _f1.getDeclaringClass().equals(_deClass))
						  || (_dtci && _f1.getDeclaringClass().equals(_dtClass)))) {
						_result = false;
					}
				}
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.35  2004/03/03 10:11:40  venku
   - formatting.

   Revision 1.34  2004/03/03 10:07:24  venku
   - renamed dependeeMap as dependent2dependee
   - renamed dependentmap as dependee2dependent
   Revision 1.33  2004/02/12 21:32:21  venku
   - refactored the code to test the escaping/sharing status of the
     base rather than the field/array location (bummer).
   Revision 1.32  2004/01/25 15:32:41  venku
   - enabled ready and interference dependences to be OFA aware.
   Revision 1.31  2004/01/18 00:02:01  venku
   - more logging info.
   Revision 1.30  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.29  2003/12/16 06:52:47  venku
   - optimization when there are no threads.
   Revision 1.28  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.27  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.26  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.25  2003/12/08 09:37:23  venku
   - use class initialization optimization by default.
   Revision 1.24  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.23  2003/11/26 09:16:26  venku
   - structure via which information is stored is different
     from the structure assumed while querying.  FIXED.
   Revision 1.22  2003/11/26 06:14:02  venku
   - added logic to consider class initializers.
   Revision 1.21  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.20  2003/11/10 20:03:55  venku
   - In Jimple, only one ArrayRef or FieldRef can occur in
     a statement.  We now use this information to make
     getDependeXXX() methods of signature type Stmt
     and Method.
   Revision 1.19  2003/11/10 08:06:01  venku
   - documentation.
   Revision 1.18  2003/11/10 03:17:18  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.17  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.16  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.15  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.14  2003/10/05 16:23:34  venku
   - formatting.
   Revision 1.13  2003/10/05 16:20:58  venku
   - made dependence type-based.
   Revision 1.12  2003/09/29 06:40:35  venku
   - reset() was being called on an argument.  FIXED.
   Revision 1.11  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.10  2003/09/12 22:33:08  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.9  2003/09/11 12:35:48  venku
   - formatting.
   Revision 1.8  2003/09/10 11:49:31  venku
   - documentation change.
   Revision 1.7  2003/09/08 02:19:38  venku
   - it now only requires call graph info and basic block graph manager
   - checkForLoopEnclosedNewExpr() is now applicable to any allocation sites
   - added a new method to extract basic block graph
   Revision 1.6  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.5  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:32:40  venku
   - Utilized containsXXX() method in Stmt
   - Even with escape information there is an issue with sequential paths.
     This issue has been injected as TODO item, but not addressed.
 */
