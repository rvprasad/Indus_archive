
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

import soot.SootMethod;
import soot.Type;

import edu.ksu.cis.indus.staticanalyses.support.FastUnionFindElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
class MethodContext
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

		int paramCount = sm.getParameterCount();

		if (paramCount > 0) {
			argAliasSets = new ArrayList(paramCount);

			for (int i = 0; i < paramCount; i++) {
				argAliasSets.add(AliasSet.getASForType(sm.getParameterType(i)));
			}
		} else {
			argAliasSets = Collections.EMPTY_LIST;
		}

		Type retType = sm.getReturnType();
		ret = AliasSet.getASForType(retType);
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
		MethodContext result = null;

		if (set != null) {
			result = (MethodContext) ((MethodContext) find()).clone();
		} else {
			result = (MethodContext) super.clone();

			Map clonee2clone = new HashMap();
			result.set = null;

			if (thisAS != null) {
				result.thisAS = (AliasSet) thisAS.clone();
				buildClonee2CloneMap(thisAS, result.thisAS, clonee2clone);
			}
			result.argAliasSets = new ArrayList();

			for (Iterator i = argAliasSets.iterator(); i.hasNext();) {
				AliasSet tmp = (AliasSet) i.next();

				if (tmp != null) {
					Object o = tmp.clone();
					result.argAliasSets.add(o);
					buildClonee2CloneMap(tmp, (AliasSet) o, clonee2clone);
				} else {
					result.argAliasSets.add(null);
				}
			}

			if (ret != null) {
				result.ret = (AliasSet) ret.clone();
				buildClonee2CloneMap(ret, result.ret, clonee2clone);
			}
			result.thrown = (AliasSet) thrown.clone();
			buildClonee2CloneMap(thrown, result.thrown, clonee2clone);
			unionclones(clonee2clone);
		}
		return result;
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
	 * Adds all alias sets reachable from this context to col.
	 *
	 * @param col is an out parameter to which the alias sets will be added.
	 *
	 * @pre col != null
	 * @post col.contains(col$pre)
	 */
	void addReachableAliasSetsTo(final Collection col) {
		AliasSet temp;

		if (thisAS != null) {
			temp = (AliasSet) thisAS.find();

			if (!col.contains(temp)) {
				temp.addReachableAliasSetsTo(col);
			}
		}

		if (ret != null) {
			temp = (AliasSet) ret.find();

			if (!col.contains(temp)) {
				temp.addReachableAliasSetsTo(col);
			}
		}
		temp = (AliasSet) thrown.find();

		if (!col.contains(temp)) {
			temp.addReachableAliasSetsTo(col);
		}

		int paramCount = method.getParameterCount();

		if (paramCount > 0) {
			for (int i = paramCount - 1; i >= 0; i--) {
				temp = (AliasSet) argAliasSets.get(i);

				if (temp != null) {
					temp = (AliasSet) temp.find();

					if (!col.contains(temp)) {
						temp.addReachableAliasSetsTo(col);
					}
				}
			}
		}
	}

	/**
	 * Propogates the information from this context to the given context.  Please refer to the {@link
	 * unify(MethodContext,boolean) unify} for important information.
	 *
	 * @param mc is the destination of the information transfer.
	 */
	void propogateInfoFromTo(final MethodContext mc) {
		MethodContext rep1 = (MethodContext) find();
		MethodContext rep2 = (MethodContext) mc.find();

		AliasSet temp1;
		AliasSet temp2;

		for (int i = method.getParameterCount() - 1; i >= 0; i--) {
			if (AliasSet.canHaveAliasSet(method.getParameterType(i))) {
				temp1 = (AliasSet) rep1.argAliasSets.get(i);
				temp2 = (AliasSet) rep2.argAliasSets.get(i);

				if (temp1 != null && temp2 != null) {
					temp1.propogateInfoFromTo(temp2);
				}
			}
		}

		temp1 = rep1.ret;

		if (temp1 != null) {
			temp1.propogateInfoFromTo(rep2.ret);
		}
		rep1.thrown.propogateInfoFromTo(rep2.thrown);

		temp1 = rep1.thisAS;

		if (temp1 != null) {
			temp1.propogateInfoFromTo(rep2.thisAS);
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

		MethodContext m = (MethodContext) find();
		MethodContext n = (MethodContext) p.find();

		if (m == n) {
			return;
		}

		for (int i = method.getParameterCount() - 1; i >= 0; i--) {
			if (AliasSet.canHaveAliasSet(method.getParameterType(i))) {
				AliasSet mAS = m.getParamAS(i);
				AliasSet nAS = n.getParamAS(i);

				if (mAS == null && nAS != null || mAS != null && nAS == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Incompatible method contexts being unified - argument[" + i + "] - " + mAS + " " + nAS
							+ " " + method.getSignature());
					}
				} else {
					mAS.unify(nAS, unifyAll);
				}
			}
		}

		AliasSet mRet = m.getReturnAS();
		AliasSet nRet = n.getReturnAS();

		if ((mRet == null && nRet != null) || (mRet != null && nRet == null)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Incompatible method contexts being unified - return value - " + mRet + " " + nRet);
			}
		} else if (mRet != null) {
			mRet.unify(nRet, unifyAll);
		}
		m.getThrownAS().unify(n.getThrownAS(), unifyAll);

		AliasSet mThis = m.getThisAS();
		AliasSet nThis = n.getThisAS();

		if ((mThis == null && nThis != null) || (mThis != null && nThis == null)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Incompatible method contexts being unified - staticness - " + mThis + " " + nThis);
			}
		} else if (mThis != null) {
			mThis.unify(nThis, unifyAll);
		}
		m.union(n);
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

		AliasSet rep = (AliasSet) s.find();

		Map sMap = rep.getFieldMap();

		for (Iterator i = sMap.keySet().iterator(); i.hasNext();) {
			String fieldName = (String) i.next();

			AliasSet a = (AliasSet) sMap.get(fieldName);

			if (clonee2clone.containsKey(a)) {
				d.putASForField(fieldName, (AliasSet) clonee2clone.get(a));
			} else {
				AliasSet b = d.getASForField(fieldName);

				if (b == null) {
					b = AliasSet.createAliasSet();
					d.putASForField(fieldName, b);
				}
				buildClonee2CloneMap(a, b, clonee2clone);
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
		Collection processed = new HashSet();

		for (Iterator i = clonee2clone.keySet().iterator(); i.hasNext();) {
			FastUnionFindElement k1 = (FastUnionFindElement) i.next();

			if (processed.contains(k1)) {
				continue;
			}

			for (Iterator j = clonee2clone.keySet().iterator(); j.hasNext();) {
				FastUnionFindElement k2 = (FastUnionFindElement) j.next();

				if (k1 == k2 || processed.contains(k2)) {
					continue;
				}

				if (k1.find() == k2.find()) {
					FastUnionFindElement v1 = (FastUnionFindElement) clonee2clone.get(k1);
					FastUnionFindElement v2 = (FastUnionFindElement) clonee2clone.get(k2);
					v1.find().union(v2.find());
				}
			}
			processed.add(k1);
		}
	}
}

/*
   ChangeLog:
   $Log$
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
