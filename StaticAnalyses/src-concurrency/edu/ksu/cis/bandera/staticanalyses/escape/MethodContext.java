
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.bandera.staticanalyses.escape;

import soot.SootMethod;
import soot.Type;

import edu.ksu.cis.bandera.staticanalyses.support.FastUnionFindElement;

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

			for (int i = 0; i < sm.getParameterCount(); i++) {
				argAliasSets.add(AliasSet.getASForType(sm.getParameterType(i)));
			}
		} else {
			argAliasSets = Collections.EMPTY_LIST;
		}

		Type type = sm.getReturnType();
		ret = AliasSet.getASForType(type);
		thrown = AliasSet.createAliasSet();

		if (!sm.isStatic()) {
			thisAS = AliasSet.createAliasSet();  //getASForClass(sm.getDeclaringClass());
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
		return (AliasSet) ((MethodContext) find()).ret;
	}

	/**
	 * Retrieves the alias set corresponding to "this" variable of the method.
	 *
	 * @return the corresponding alias set.
	 */
	AliasSet getThisAS() {
		return (AliasSet) ((MethodContext) find()).thisAS;
	}

	/**
	 * Retrieves the alias set corresponding to the exceptions thrown by the method.
	 *
	 * @return the corresponding alias set.
	 *
	 * @post result != null
	 */
	AliasSet getThrownAS() {
		return (AliasSet) ((MethodContext) find()).thrown;
	}

	/**
	 * Propogates the information from this context to the given context.
	 *
	 * @param mc is the destination of the information transfer.
	 */
	void propogateInfoFromTo(final MethodContext mc) {
		MethodContext rep1 = (MethodContext) find();
		MethodContext rep2 = (MethodContext) mc.find();

		for (int i = method.getParameterCount() - 1; i >= 0; i--) {
			if (AliasSet.canHaveAliasSet(method.getParameterType(i))) {
				((AliasSet) rep1.argAliasSets.get(i)).propogateInfoFromTo((AliasSet) rep2.argAliasSets.get(i));
			}
		}

		AliasSet temp = rep1.ret;

		if (temp != null) {
			temp.propogateInfoFromTo(rep2.ret);
		}
		rep1.thrown.propogateInfoFromTo(rep2.thrown);

		temp = rep1.thisAS;

		if (temp != null) {
			temp.propogateInfoFromTo(rep2.thisAS);
		}
	}

	/**
	 * Unifies this context with the given context.
	 *
	 * @param p is the context with which the unification should occur.
	 * @param unifyAll is the <code>unifyAll</code> argument for the unification of the contained alias sets.
	 *
	 * @throws IllegalStateException in case the corresponding alias sets in the contexts do not match.
	 */
	void unify(final MethodContext p, final boolean unifyAll)
	  throws IllegalStateException {
		if (p == null) {
			LOGGER.warn("Unification with null requested.");
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
					throw new IllegalStateException("Incompatible method contexts being unified - argument " + i + ".");
				} else {
					mAS.unify(nAS, unifyAll);
				}
			}
		}

		AliasSet mRet = m.getReturnAS();
		AliasSet nRet = n.getReturnAS();

		if ((mRet == null && nRet != null) || (mRet != null && nRet == null)) {
			throw new IllegalStateException("Incompatible method contexts being unified - return value.");
		}

		if (mRet != null) {
			mRet.unify(nRet, unifyAll);
		}
		m.getThrownAS().unify(n.getThrownAS(), unifyAll);

		AliasSet mThis = m.getThisAS();
		AliasSet nThis = n.getThisAS();

		if ((mThis == null && nThis != null) || (mThis != null && nThis == null)) {
			throw new IllegalStateException("Incompatible method contexts being unified - staticness");
		}

		if (mThis != null) {
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

/*****
 ChangeLog:

$Log$

*****/
