
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

package edu.ksu.cis.indus.staticanalyses.tokens.soot;

import edu.ksu.cis.indus.common.Constants;
import edu.ksu.cis.indus.common.MembershipPredicate;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationEvaluator;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;
import edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import soot.NullType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.Type;
import soot.Value;


/**
 * This class manages Soot value types and the corresponding types in user's type system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SootValueTypeManager
  implements ITypeManager {
	/** 
	 * This caches the soot/java types that have been "captured" as valid types fro <code>null</code>.
	 *
	 * @invariant typesForNullConstant.oclIsKindOf(Set(IType))
	 */
	final Collection typesForNullConstant;

	/** 
	 * This is the dynamic token-type relation evaluator.
	 */
	private final IDynamicTokenTypeRelationEvaluator evaluator;

	/** 
	 * This maps soot types to user's type.
	 */
	private final Map sootType2Type;

	/** 
	 * This predicate is used to detect types that can hold null constant and that have not been recorded so.
	 */
	private final Predicate typesForNullConstPredicate;

	/**
	 * Creates an instance of this class.
	 */
	public SootValueTypeManager() {
		super();
		typesForNullConstant = new ArrayList();
		typesForNullConstPredicate =
			PredicateUtils.andPredicate(new MembershipPredicate(false, typesForNullConstant),
				PredicateUtils.instanceofPredicate(RefLikeType.class));
		evaluator = new SootDynamicTokenTypeEvaluator();
		sootType2Type = new HashMap(Constants.getNumOfClassesInApplication());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getAllTypes(java.lang.Object)
	 */
	public Collection getAllTypes(final Object value) {
		final Value _theValue = (Value) value;
		final Type _type = _theValue.getType();
		final Collection _result = new ArrayList();

		_result.add(getTokenTypeForRepType(_type));

		if (_type instanceof RefType) {
			for (final Iterator _i = Util.getAncestors(((RefType) _type).getSootClass()).iterator(); _i.hasNext();) {
				_result.add(getTokenTypeForRepType(((SootClass) _i.next()).getType()));
			}
		}

		if (_type instanceof NullType) {
			final Collection _s = CollectionUtils.select(sootType2Type.keySet(), typesForNullConstPredicate);

			for (final Iterator _i = _s.iterator(); _i.hasNext();) {
				final Object _t = _i.next();
				_result.add(sootType2Type.get(_t));
			}
			typesForNullConstant.addAll(_s);
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getExactType(java.lang.Object)
	 */
	public IType getExactType(final Object value) {
		return getTokenTypeForRepType(((Value) value).getType());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getDynamicTokenTypeRelationEvaluator()
	 */
	public IDynamicTokenTypeRelationEvaluator getDynamicTokenTypeRelationEvaluator() {
		return evaluator;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getTokenTypeForRepType(Object)
	 */
	public IType getTokenTypeForRepType(final Object sootType) {
		IType _result = (IType) sootType2Type.get(sootType);

		if (_result == null) {
			_result = new IType() {
						;
					};
			sootType2Type.put(sootType, _result);
		}
		return _result;
	}

	/**
	 * Resets the manager.
	 */
	public void reset() {
		sootType2Type.clear();
		typesForNullConstant.clear();
		evaluator.reset();
	}
}

// End of File
