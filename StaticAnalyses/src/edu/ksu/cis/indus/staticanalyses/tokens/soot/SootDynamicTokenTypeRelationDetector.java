
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

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationDetector;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager.DummyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import soot.ArrayType;
import soot.RefType;
import soot.Value;

import soot.jimple.NullConstant;


/**
 * This implementation caters to Java language type system.
 * 
 * <p>
 * This is required in situations when a value can be assigned to types of sort X but not all types of sort X are  explored
 * before seeing the value during analysis.  An example is <code>null</code> can be assigned to any reference types  but not
 * all reference types will be explored before <code>null</code> is explored.
 * </p>
 * 
 * <p>
 * It also detects array value to Object class relation.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class SootDynamicTokenTypeRelationDetector
  implements IDynamicTokenTypeRelationDetector {
	/** 
	 * This predicate filters out <code>NullConstant</code> values.
	 */
	public static final Predicate NULL_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object object) {
				return object instanceof NullConstant;
			}
		};

	/** 
	 * This is the collection of types that have already been marked as compatible with <code>null</code>.
	 */
	private final Collection typesForNullConst = new ArrayList();

	/** 
	 * This indicates that <code>null</code> was seen.
	 */
	private boolean nullConstSeen;

	/** 
	 * This indicates that soot type representing <code>java.lang.Object</code> was seen.
	 */
	private boolean objectTypeSeen;

	/**
	 * @see IDynamicTokenTypeRelationDetector#getValuesConformingTo(Collection, IType)
	 */
	public Collection getValuesConformingTo(final Collection values, final IType type) {
		final Collection _result = new HashSet();
		processForNullConstAndRefTypeRelation(values, type, _result);
		processForArrayAndObjectTypeRelation(values, type, _result);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationDetector#reset()
	 */
	public void reset() {
		nullConstSeen = false;
		objectTypeSeen = false;
		typesForNullConst.clear();
	}

	/**
	 * Returns array values as conforming instances of <code>java.lang.Object</code>  type.
	 *
	 * @param values from which to get conforming values.
	 * @param type of the conforming values.
	 * @param result is the out parameter into which the conforming values will be added.
	 *
	 * @pre values != null and type != null and result != null
	 * @pre values.oclIsKindOf(Collection(Value))
	 * @pre result.oclIsKindOf(Collection(Value))
	 * @post result.oclIsKindOf(Collection(Value))
	 * @post result.containsAll(result$pre)
	 */
	private void processForArrayAndObjectTypeRelation(final Collection values, final IType type, final Collection result) {
		final DummyType _dType = (DummyType) type;
		final Object _sootType = _dType.sootType;

		if (!objectTypeSeen
			  && _sootType instanceof RefType
			  && ((RefType) _sootType).getSootClass().getName().equals("java.lang.Object")) {
			final Iterator _i = values.iterator();
			final int _iEnd = values.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Value _value = (Value) _i.next();

				if (_value.getType() instanceof ArrayType) {
					result.add(_value);
				}
			}
			objectTypeSeen = true;
		}
	}

	/**
	 * Returns null constant value as conforming instances of reference types.
	 *
	 * @param values from which to get conforming values.
	 * @param type of the conforming values.
	 * @param result is the out parameter into which the conforming values will be added.
	 *
	 * @pre values != null and type != null and result != null
	 * @pre values.oclIsKindOf(Collection(Value))
	 * @pre result.oclIsKindOf(Collection(Value))
	 * @post result.oclIsKindOf(Collection(Value))
	 * @post result.containsAll(result$pre)
	 */
	private void processForNullConstAndRefTypeRelation(final Collection values, final IType type, final Collection result) {
		nullConstSeen = nullConstSeen || CollectionUtils.exists(values, NULL_PREDICATE);

		if (nullConstSeen) {
			if (Util.isReferenceType(((DummyType) type).sootType) && !typesForNullConst.contains(type)) {
				typesForNullConst.add(type);
				result.add(CollectionUtils.find(values, NULL_PREDICATE));
			}
		}
	}
}

// End of File
