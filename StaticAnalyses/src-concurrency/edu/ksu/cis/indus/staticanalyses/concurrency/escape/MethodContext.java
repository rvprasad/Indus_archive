
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.datastructures.FastUnionFindElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Type;


/**
 * This class represents the method/site context as described in the techreport <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports/SAnToS-TR2003-6.pdf">Honing the  Detection of Interference
 * and Ready Dependence for Slicing Concurrent Java Programs.</a> This serves more as a container for the various alias
 * sets/equivalence classes that occur at the method interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
final class MethodContext
  extends FastUnionFindElement
  implements Cloneable {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(MethodContext.class);

	/**
	 * The alias set associated with the return value of the associated method.
	 *
	 * @invariant AliasSet.canHaveAliasSet(method.getReturnType()) implies ret != null
	 * @invariant not AliasSet.canHaveAliasSet(method.getReturnType()) implies ret == null
	 */
	AliasSet ret;

	/**
	 * The alias set associated with the <code>this</code> variable of the associated method.
	 *
	 * @invariant method.isStatic() implies thisAS == null
	 * @invariant not method.isStatic() implies thisAS != null
	 */
	AliasSet thisAS;

	/**
	 * The alias set associated with the exceptions thrown by  the associated method.
	 *
	 * @invariant thrown != null
	 */
	AliasSet thrown;

	/**
	 * The associated method.
	 */
	SootMethod method;

	/**
	 * The alias sets associated with the arguments to the associated method.
	 *
	 * @invariant method.getParameterTypes()->forall(o | AliasSet.canHaveAliasSet(o) implies
	 * 			  argAliasSets.get(method.getParameterTypes().indexOf(o)) != null)
	 * @invariant method.getParameterTypes()->forall(o | not AliasSet.canHaveAliasSet(o) implies
	 * 			  argAliasSets.get(method.getParameterTypes().indexOf(o)) == null)
	 */
	private List argAliasSets;

	/**
	 * Creates a new MethodContext object.
	 *
	 * @param sm is the method being represented by this object.
	 * @param thisASParam is the alias set corresponding to "this" variable.
	 * @param argASs is the alias sets corresponding to the arguments/parameters of the method.
	 * @param retAS is the alias set corresponding to the return value of the method.
	 * @param thrownAS is the alias set corresponding to the exceptions thrown by the method.
	 *
	 * @pre sm != null and argASs != null and thrownAS != null
	 */
	MethodContext(final SootMethod sm, final AliasSet thisASParam, final List argASs, final AliasSet retAS,
		final AliasSet thrownAS) {
		method = sm;
		argAliasSets = new ArrayList(argASs);

		if (AliasSet.canHaveAliasSet(sm.getReturnType())) {
			ret = retAS;
		}

		if (!sm.isStatic()) {
			this.thisAS = thisASParam;
		}
		thrown = thrownAS;
	}

	/**
	 * Creates a new MethodContext object.  The alias sets for the various parts of the method interface is created as
	 * required.
	 *
	 * @param sm is the method being represented by this object.
	 */
	MethodContext(final SootMethod sm) {
		method = sm;

		final int _paramCount = sm.getParameterCount();

		if (_paramCount > 0) {
			argAliasSets = new ArrayList(_paramCount);

			for (int _i = 0; _i < _paramCount; _i++) {
				argAliasSets.add(AliasSet.getASForType(sm.getParameterType(_i)));
			}
		} else {
			argAliasSets = Collections.EMPTY_LIST;
		}

		final Type _retType = sm.getReturnType();
		ret = AliasSet.getASForType(_retType);
		thrown = AliasSet.createAliasSet();

		if (!sm.isStatic()) {
			thisAS = AliasSet.createAliasSet();
		}
	}

	/**
	 * Clones this object.
	 *
	 * @return clone of this object.
	 *
	 * @throws CloneNotSupportedException if <code>java.lang.Object.clone()</code> fails.
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		MethodContext _result = null;

		if (set != null) {
			_result = (MethodContext) ((MethodContext) find()).clone();
		} else {
			_result = (MethodContext) super.clone();

			final Map _clonee2clone = new HashMap();
			_result.set = null;

			if (thisAS != null) {
				_result.thisAS = (AliasSet) thisAS.clone();
				buildClonee2CloneMap(thisAS, _result.thisAS, _clonee2clone);
			}
			_result.argAliasSets = new ArrayList();

			for (final Iterator _i = argAliasSets.iterator(); _i.hasNext();) {
				final AliasSet _tmp = (AliasSet) _i.next();

				if (_tmp != null) {
					final Object _o = _tmp.clone();
					_result.argAliasSets.add(_o);
					buildClonee2CloneMap(_tmp, (AliasSet) _o, _clonee2clone);
				} else {
					_result.argAliasSets.add(null);
				}
			}

			if (ret != null) {
				_result.ret = (AliasSet) ret.clone();
				buildClonee2CloneMap(ret, _result.ret, _clonee2clone);
			}
			_result.thrown = (AliasSet) thrown.clone();
			buildClonee2CloneMap(thrown, _result.thrown, _clonee2clone);
			unionclones(_clonee2clone);
		}
		return _result;
	}

	/**
	 * Retrieves the alias set corresponding to the parameter occuring at position <code>index</code> in the method
	 * interface.
	 *
	 * @param index is the position of the parameter.
	 *
	 * @return the corresponding alias set.
	 */
	AliasSet getParamAS(final int index) {
		return (AliasSet) ((MethodContext) find()).argAliasSets.get(index);
	}

	/**
	 * Retrieves the alias set corresponding to the return value of the method.
	 *
	 * @return the corresponding alias set.
	 */
	AliasSet getReturnAS() {
		return ((MethodContext) find()).ret;
	}

	/**
	 * Retrieves the alias set corresponding to "this" variable of the method.
	 *
	 * @return the corresponding alias set.
	 */
	AliasSet getThisAS() {
		return ((MethodContext) find()).thisAS;
	}

	/**
	 * Retrieves the alias set corresponding to the exceptions thrown by the method.
	 *
	 * @return the corresponding alias set.
	 *
	 * @post result != null
	 */
	AliasSet getThrownAS() {
		return ((MethodContext) find()).thrown;
	}

	/**
	 * Propogates the information from this context to the given context.  Please refer to the {@link
	 * unify(MethodContext,boolean) #unify} for important information.
	 *
	 * @param mc is the destination of the information transfer.
	 */
	void propogateInfoFromTo(final MethodContext mc) {
		final MethodContext _methodContext1 = (MethodContext) find();
		final MethodContext _methodContext2 = (MethodContext) mc.find();

		AliasSet _temp1;
		AliasSet _temp2;

		final int _paramCount = method.getParameterCount();

		for (int _i = 0; _i < _paramCount; _i++) {
			if (AliasSet.canHaveAliasSet(method.getParameterType(_i))) {
				_temp1 = (AliasSet) _methodContext1.argAliasSets.get(_i);
				_temp2 = (AliasSet) _methodContext2.argAliasSets.get(_i);

				if (_temp1 != null && _temp2 != null) {
					_temp1.propogateInfoFromTo(_temp2);
				}
			}
		}

		_temp1 = _methodContext1.ret;

		if (_temp1 != null) {
			_temp1.propogateInfoFromTo(_methodContext2.ret);
		}
		_methodContext1.thrown.propogateInfoFromTo(_methodContext2.thrown);

		_temp1 = _methodContext1.thisAS;

		if (_temp1 != null) {
			_temp1.propogateInfoFromTo(_methodContext2.thisAS);
		}
	}

	/**
	 * Unifies this object with itself.  This is required while dealing with call-sites which may be executed multiple times.
	 *
	 * @param unifyAll is the <code>unifyAll</code> argument for the unification of the contained alias sets.
	 */
	void selfUnify(final boolean unifyAll) {
		final MethodContext _methodContext = (MethodContext) find();
		final int _paramCount = method.getParameterCount();

		for (int _i = 0; _i < _paramCount; _i++) {
			if (AliasSet.canHaveAliasSet(method.getParameterType(_i))) {
				((AliasSet) _methodContext.argAliasSets.get(_i)).selfUnify(unifyAll);
			}
		}

		final AliasSet _mRet = _methodContext.ret;

		if (_mRet != null) {
			_mRet.selfUnify(unifyAll);
		}
		_methodContext.thrown.selfUnify(unifyAll);

		final AliasSet _mThis = _methodContext.thisAS;

		if (_mThis != null) {
			_mThis.selfUnify(unifyAll);
		}
	}

	/**
	 * Unifies this context with the given context.
	 * 
	 * <p>
	 * There is asymmetry in alias sets in case of contexts.  It is possible that this context may not have alias set for an
	 * argument location where as <code>p</code> may have one.  This happens in cases where this context corresponds to a
	 * site-context where <code>null</code> is being passed, hence, that arg position does not require an alias set whereas
	 * <code>p</code> being the context of the method being called requires alias set as there may be sites where the
	 * non-null argument may be provided. In such cases, we safely warn and continue. However, this cannot happen for thrown
	 * exception, this, or return references.  associated with call sites and methods.
	 * </p>
	 *
	 * @param p is the context with which the unification should occur.
	 * @param unifyAll is the <code>unifyAll</code> argument for the unification of the contained alias sets.
	 */
	void unify(final MethodContext p, final boolean unifyAll) {
		if (p == null) {
			LOGGER.error("Unification with null requested.");
		}

		MethodContext _m = (MethodContext) find();
		MethodContext _n = (MethodContext) p.find();

		if (_m == _n) {
			return;
		}

		_m.union(_n);

		final MethodContext _temp = (MethodContext) _m.find();

		if (_temp != _m) {
			_n = _m;
			_m = _temp;
		}

		unifyParameters(unifyAll, _m, _n);

		unifyAliasSets(unifyAll, _m.ret, _n.ret);
		_m.thrown.unify(_n.thrown, unifyAll);
		unifyAliasSets(unifyAll, _m.thisAS, _n.thisAS);
	}

	/**
	 * Builds the field map in the clone in such a way that it captures any circular references in existing in the clonee.
	 *
	 * @param s is the clonee.
	 * @param d is the cone
	 * @param clonee2clone maps the clonee alias sets to the clone alias sets.
	 *
	 * @pre s != null and d != null and clonee2clone != null
	 * @invariant clonee2clone.oclIsKindOf(Map(AliasSet, AliasSet))
	 */
	private void buildClonee2CloneMap(final AliasSet s, final AliasSet d, final Map clonee2clone) {
		clonee2clone.put(s, d);

		final AliasSet _rep = (AliasSet) s.find();

		final Map _sMap = _rep.getFieldMap();

		for (final Iterator _i = _sMap.keySet().iterator(); _i.hasNext();) {
			final String _fieldName = (String) _i.next();

			final AliasSet _a = (AliasSet) _sMap.get(_fieldName);

			if (clonee2clone.containsKey(_a)) {
				d.putASForField(_fieldName, (AliasSet) clonee2clone.get(_a));
			} else {
				AliasSet _b = d.getASForField(_fieldName);

				if (_b == null) {
					_b = AliasSet.createAliasSet();
					d.putASForField(_fieldName, _b);
				}
				buildClonee2CloneMap(_a, _b, clonee2clone);
			}
		}
	}

	/**
	 * Unifies the given alias sets.
	 *
	 * @param unifyAll is the <code>unifyAll</code> argument for the unification of the contained alias sets.
	 * @param aliasSet1 is one of the alias set to be unified.
	 * @param aliasSet2 is the other alias set to be unified.
	 *
	 * @pre aliasSet1 != null and aliasSet2 != null
	 */
	private void unifyAliasSets(final boolean unifyAll, final AliasSet aliasSet1, final AliasSet aliasSet2) {
		if ((aliasSet1 == null && aliasSet2 != null) || (aliasSet1 != null && aliasSet2 == null)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Incompatible method contexts being unified - return value - " + aliasSet1 + " " + aliasSet2);
			}
		} else if (aliasSet1 != null) {
			aliasSet1.unify(aliasSet2, unifyAll);
		}
	}

	/**
	 * Unify the alias sets of the parameters in the given method contexts.
	 *
	 * @param unifyAll is the <code>unifyAll</code> argument for the unification of the contained alias sets.
	 * @param methodContext1 is one of the method context that contains the parameter alias sets to be unified.
	 * @param methodContext2 is the other method context that contains the parameter alias sets to be unified.
	 *
	 * @pre methodContext1 != null and methodContext2 != null
	 */
	private void unifyParameters(final boolean unifyAll, final MethodContext methodContext1,
		final MethodContext methodContext2) {
		final int _paramCount = method.getParameterCount();

		for (int _i = 0; _i < _paramCount; _i++) {
			if (AliasSet.canHaveAliasSet(method.getParameterType(_i))) {
				final AliasSet _mAS = (AliasSet) methodContext1.argAliasSets.get(_i);
				final AliasSet _nAS = (AliasSet) methodContext2.argAliasSets.get(_i);

				if (_mAS == null && _nAS != null || _mAS != null && _nAS == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Incompatible method contexts being unified - argument[" + _i + "] - " + _mAS + " "
							+ _nAS + " " + method.getSignature());
					}
				} else if (_mAS != null) {
					_mAS.unify(_nAS, unifyAll);
				}
			}
		}
	}

	/**
	 * Unions (fast-find union operation) the clone alias sets if their corresponding clonee alias sets are unioned.  This is
	 * required to maintain the relation between alias sets upon cloning the method context.
	 *
	 * @param clonee2clone maps the clonee alias sets to the clone alias sets.
	 *
	 * @invariant clonee2clone.oclIsKindOf(Map(AliasSet, AliasSet))
	 */
	private void unionclones(final Map clonee2clone) {
		final Collection _processed = new HashSet();

		for (final Iterator _i = clonee2clone.keySet().iterator(); _i.hasNext();) {
			final FastUnionFindElement _k1 = (FastUnionFindElement) _i.next();

			if (_processed.contains(_k1)) {
				continue;
			}

			for (final Iterator _j = clonee2clone.keySet().iterator(); _j.hasNext();) {
				final FastUnionFindElement _k2 = (FastUnionFindElement) _j.next();

				if (_k1 == _k2 || _processed.contains(_k2)) {
					continue;
				}

				if (_k1.find() == _k2.find()) {
					final FastUnionFindElement _v1 = (FastUnionFindElement) clonee2clone.get(_k1);
					final FastUnionFindElement _v2 = (FastUnionFindElement) clonee2clone.get(_k2);
					_v1.find().union(_v2.find());
				}
			}
			_processed.add(_k1);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2004/01/03 21:20:06  venku
   - deleted unused methods.

   Revision 1.12  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.

   Revision 1.11  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.10  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.9  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.7  2003/10/05 16:22:25  venku
   - Interference dependence is now symbol based.
   - Both interference and ready dependence consider
     loop information in a more sound manner.
   - ripple effect of the above.
   Revision 1.6  2003/10/05 06:31:35  venku
   - Things work.  The bug was the order in which the
     parameter alias sets were being accessed.  FIXED.
   Revision 1.5  2003/09/29 13:32:27  venku
   - @#@%
   Revision 1.4  2003/09/29 11:29:08  venku
   - added more log information.
   Revision 1.3  2003/09/01 12:01:30  venku
   Major:
   - Ready dependence info in ECBA was flaky as it did not consider
     impact of multiple call sites with contradicting wait/notify use of
     the primary.  FIXED.
   Revision 1.2  2003/09/01 08:01:59  venku
   Major:
   - It is possible for call sites to pass null arguments. In such cases,
     there need not be alias sets corresponding to these arguments.
     Hence, while unification and propogation it is possible to have null
     and non-null alias set references.  This has been made safe. FIXED.
    - Ripple effect in AliasSet.
   Revision 1.1  2003/08/21 01:24:25  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Revision 1.3  2003/08/11 06:29:07  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:39:07  venku
   Major:
    - Moved the package under indus umbrella.
   Minor:
    - changes to accomodate ripple effect from support package.
   Revision 1.2  2003/07/27 21:22:14  venku
   Minor:
    - removed unnecessary casts.
   Revision 1.1  2003/07/27 20:52:39  venku
   First of the many refactoring while building towards slicer release.
   This is the escape analysis refactored and implemented as per to tech report.
 */
