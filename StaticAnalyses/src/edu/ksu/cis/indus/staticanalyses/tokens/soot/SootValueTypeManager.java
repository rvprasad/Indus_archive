
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
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationDetector;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;
import edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import soot.ArrayType;
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
  extends Observable
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
	private final IDynamicTokenTypeRelationDetector evaluator;

	/** 
	 * This maps soot types to user's type.
	 */
	private final Map sootType2Type;

	/** 
	 * This predicate is used to detect types that can hold null constant.
	 */
	private final Predicate typesForNullConstPredicate;

	/** 
	 * The type corresponding to the soot type representing <code>java.lang.Object</code>.
	 */
	private IType tokenTypeForObjectType;

	/**
	 * Creates an instance of this class.
	 */
	public SootValueTypeManager() {
		super();
		typesForNullConstant = new HashSet();
		typesForNullConstPredicate = PredicateUtils.instanceofPredicate(RefLikeType.class);
		evaluator = new SootDynamicTokenTypeRelationDetector();
		sootType2Type = new HashMap(Constants.getNumOfClassesInApplication());
	}

	/**
	 * This is a dummy implementation of <code>IType</code>.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	static class DummyType
	  implements IType {
		/** 
		 * The soot type represented by this instance.
		 */
		final Type sootType;

		/**
		 * Creates an instance of this class.
		 *
		 * @param sType is the represented Soot type.
		 *
		 * @pre sType != null
		 */
		public DummyType(final Type sType) {
			sootType = sType;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @pre value.oclIsKindOf(soot.Value)
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
		} else if (_type instanceof ArrayType) {
			final IType _t = getTokenTypeForObjectType();

			if (_t != null) {
				_result.add(_t);
			}
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getDynamicTokenTypeRelationEvaluator()
	 */
	public IDynamicTokenTypeRelationDetector getDynamicTokenTypeRelationEvaluator() {
		return evaluator;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @pre value.oclIsKindOf(soot.Value)
	 */
	public IType getExactType(final Object value) {
		return getTokenTypeForRepType(((Value) value).getType());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getTokenTypeForRepType(Object)
	 */
	public IType getTokenTypeForRepType(final Object sootType) {
		IType _result = (IType) sootType2Type.get(sootType);

		if (_result == null) {
			_result = new DummyType((Type) sootType);
			sootType2Type.put(sootType, _result);
			setChanged();
			notifyObservers(new NewTypeCreated(_result));
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
		tokenTypeForObjectType = null;
	}

	/**
	 * Retrieves the type for the soot type representing <code>java.lang.Object</code>.
	 *
	 * @return the type
	 */
	private IType getTokenTypeForObjectType() {
		if (tokenTypeForObjectType == null) {
			final Set _entrySet = sootType2Type.entrySet();
			final Iterator _i = _entrySet.iterator();
			final int _iEnd = _entrySet.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Type _sootType = (Type) _entry.getKey();

				if (_sootType instanceof RefType) {
					final SootClass _sc = ((RefType) _sootType).getSootClass();

					if (_sc.getName().equals("java.lang.Object")) {
						tokenTypeForObjectType = (IType) _entry.getValue();
						break;
					}
				}
			}
		}
		return tokenTypeForObjectType;
	}
}

// End of File
