
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

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

		if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(sm.getReturnType())) {
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
		MethodContext _clone = null;

		if (find() != this) {
			_clone = (MethodContext) ((MethodContext) find()).clone();
		} else {
			_clone = (MethodContext) super.clone();

			final Map _clonee2clone = new HashMap();
			_clone.set = null;

			/*
			 * map from the representative to the clone. The clone is always the representative element, but this is not
			 * true for the clonee.
			 */
			
			if (thisAS != null) {
				_clone.thisAS = (AliasSet) thisAS.clone();
				_clonee2clone.put(thisAS.find(), _clone.thisAS);
			}
			_clone.argAliasSets = new ArrayList();

			for (final Iterator _i = argAliasSets.iterator(); _i.hasNext();) {
				final AliasSet _tmp = (AliasSet) _i.next();

				if (_tmp != null) {
					final AliasSet _o = (AliasSet) _tmp.clone();
					_clone.argAliasSets.add(_o);
					_clonee2clone.put(_tmp.find(), _o);
				} else {
					_clone.argAliasSets.add(null);
				}
			}

			if (ret != null) {
				_clone.ret = (AliasSet) ret.clone();
				_clonee2clone.put(ret.find(), _clone.ret);
			}
			_clone.thrown = (AliasSet) thrown.clone();
			_clonee2clone.put(thrown.find(), _clone.thrown);
			AliasSet.fixUpFieldMapsOfClone(_clonee2clone);
		}
		return _clone;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("thrown", this.thrown).append("argAliasSets", this.argAliasSets)
										  .append("ret", this.ret).append("thisAS", this.thisAS).append("method", this.method)
										  .toString();
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
	 * Rewires the context such that it contains only representative alias sets and no the nominal(indirectional) alias sets.
	 */
	void discardReferentialAliasSets() {
		if (thrown != null) {
			thrown = (AliasSet) thrown.find();
		}

		if (thisAS != null) {
			thisAS = (AliasSet) thisAS.find();
		}

		if (ret != null) {
			ret = (AliasSet) ret.find();
		}

		if (argAliasSets != null && !argAliasSets.isEmpty()) {
			final int _size = argAliasSets.size();

			for (int _i = 0; _i < _size; _i++) {
				final Object _temp = argAliasSets.get(_i);

				if (_temp != null) {
					argAliasSets.set(_i, ((AliasSet) _temp).find());
				}
			}
		}
	}

	/**
	 * Marks all reachable alias sets as being crossing thread boundary, i.e, visible in multiple threads..
	 */
	void markAsCrossingThreadBoundary() {
		if (find() != this) {
			((MethodContext) find()).markAsCrossingThreadBoundary();
		} else {
			if (ret != null) {
				ret.markAsCrossingThreadBoundary();
			}

			if (thrown != null) {
				thrown.markAsCrossingThreadBoundary();
			}

			if (thisAS != null) {
				thisAS.markAsCrossingThreadBoundary();
			}

			for (final Iterator _i = argAliasSets.iterator(); _i.hasNext();) {
				final AliasSet _argAS = (AliasSet) _i.next();

				if (_argAS != null) {
					_argAS.markAsCrossingThreadBoundary();
				}
			}
		}
	}

	/**
	 * Propogates the information from the srouce context to the destination context.  Please refer to the   {@link
	 * #unifyMethodContext(MethodContext) unify(MethodContext)} for important information.
	 *
	 * @param from is the source of the information transfer.
	 * @param to is the destination of the information transfer.
	 *
	 * @pre from != null and to != null
	 */
	static void propogateInfoFromTo(final MethodContext from, final MethodContext to) {
		final MethodContext _fromRep = (MethodContext) from.find();
		final MethodContext _toRep = (MethodContext) to.find();

		final int _paramCount = _fromRep.method.getParameterCount();

		for (int _i = 0; _i < _paramCount; _i++) {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_fromRep.method.getParameterType(_i))) {
				final AliasSet _temp1 = (AliasSet) _fromRep.argAliasSets.get(_i);
				final AliasSet _temp2 = (AliasSet) _toRep.argAliasSets.get(_i);

				if (_temp1 != null && _temp2 != null) {
					AliasSet.propogateInfoFromTo(_temp1, _temp2);
				}
			}
		}

		final AliasSet _retAS = _fromRep.ret;

		if (_retAS != null) {
			AliasSet.propogateInfoFromTo(_retAS, _toRep.ret);
		}
		AliasSet.propogateInfoFromTo(_fromRep.thrown, _toRep.thrown);

		final AliasSet _thisAS = _fromRep.thisAS;

		if (_thisAS != null) {
			AliasSet.propogateInfoFromTo(_thisAS, _toRep.thisAS);
		}
	}

	/**
	 * Unifies this object with itself.  This is required while dealing with call-sites which may be executed multiple times.
	 */
	void selfUnify() {
		final MethodContext _methodContext = (MethodContext) find();
		final int _paramCount = method.getParameterCount();

		for (int _i = 0; _i < _paramCount; _i++) {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(method.getParameterType(_i))) {
				((AliasSet) _methodContext.argAliasSets.get(_i)).selfUnify();
			}
		}

		final AliasSet _mRet = _methodContext.ret;

		if (_mRet != null) {
			_mRet.selfUnify();
		}
		_methodContext.thrown.selfUnify();

		final AliasSet _mThis = _methodContext.thisAS;

		if (_mThis != null) {
			_mThis.selfUnify();
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
	 */
	void unifyMethodContext(final MethodContext p) {
		if (p == null) {
			LOGGER.error("Unification with null requested.");
		}

		final MethodContext _m = (MethodContext) find();
		final MethodContext _n = (MethodContext) p.find();

		if (_m != _n) {
			unifyParameters(_m, _n);
			unifyAliasSets(_m.ret, _n.ret);
			unifyAliasSets(_m.thrown, _n.thrown);
			unifyAliasSets(_m.thisAS, _n.thisAS);
			_m.union(_n);
		}
	}

	/**
	 * Unifies the given alias sets.
	 *
	 * @param aliasSet1 is one of the alias set to be unified.
	 * @param aliasSet2 is the other alias set to be unified.
	 *
	 * @pre aliasSet1 != null and aliasSet2 != null
	 */
	private void unifyAliasSets(final AliasSet aliasSet1, final AliasSet aliasSet2) {
		if ((aliasSet1 == null && aliasSet2 != null) || (aliasSet1 != null && aliasSet2 == null)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Incompatible method contexts being unified - return value - " + aliasSet1 + " " + aliasSet2);
			}
		} else if (aliasSet1 != null) {
			aliasSet1.unifyAliasSet(aliasSet2);
		}
	}

	/**
	 * Unify the alias sets of the parameters in the given method contexts.
	 *
	 * @param methodContext1 is one of the method context that contains the parameter alias sets to be unified.
	 * @param methodContext2 is the other method context that contains the parameter alias sets to be unified.
	 *
	 * @pre methodContext1 != null and methodContext2 != null
	 */
	private void unifyParameters(final MethodContext methodContext1, final MethodContext methodContext2) {
		final int _paramCount = method.getParameterCount();

		for (int _i = 0; _i < _paramCount; _i++) {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(method.getParameterType(_i))) {
				final AliasSet _mAS = (AliasSet) methodContext1.argAliasSets.get(_i);
				final AliasSet _nAS = (AliasSet) methodContext2.argAliasSets.get(_i);

				if (_mAS == null && _nAS != null || _mAS != null && _nAS == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Incompatible method contexts being unified - argument[" + _i + "] - " + _mAS + " "
							+ _nAS + " " + method.getSignature());
					}
				} else if (_mAS != null) {
					_mAS.unifyAliasSet(_nAS);
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.18  2004/07/30 07:47:35  venku
   - there was a bug in escape analysis cloning and union algorithm.  FIXED.
   Revision 1.17  2004/07/17 19:37:18  venku
   - ECBA was incorrect for the following reasons.
     - it fails if the start sites are not in the same method.
     - it fails if the access in the threads occur in methods other than the
       one in which the new thread is started.
     - The above issues were addressed.
   Revision 1.16  2004/04/22 09:49:46  venku
   - added logic to discard fast-union-find elements which serve as levels of indirections.
   Revision 1.15  2004/01/21 09:58:16  venku
   - throw alias sets were being unified via AliasSet.unify().  Used
     unifyAliasSets() instead.
   Revision 1.14  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
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
