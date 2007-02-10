/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.tokens.soot;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.staticanalyses.tokens.IDynamicTokenTypeRelationDetector;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;
import edu.ksu.cis.indus.staticanalyses.tokens.soot.SootValueTypeManager.DummyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.ArrayType;
import soot.RefType;
import soot.Value;

/**
 * This implementation caters to Java language type system.
 * <p>
 * This is required in situations when a value can be assigned to types of sort X but not all types of sort X are explored
 * before seeing the value during analysis. An example is <code>null</code> can be assigned to any reference types but not
 * all reference types will be explored before <code>null</code> is explored.
 * </p>
 * <p>
 * It also detects array value to Object class relation.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class SootDynamicTokenTypeRelationDetector
		implements IDynamicTokenTypeRelationDetector<Value> {

	/**
	 * This indicates that <code>null</code> was seen.
	 */
	private boolean nullConstSeen;

	/**
	 * This indicates that soot type representing <code>java.lang.Object</code> was seen.
	 */
	private boolean objectTypeSeen;

	/**
	 * This is the collection of types that have already been marked as compatible with <code>null</code>.
	 */
	private final Collection<IType> typesForNullConst = new ArrayList<IType>();

	/**
	 * @see IDynamicTokenTypeRelationDetector#getValuesConformingTo(Collection, IType)
	 */
	public Collection<Value> getValuesConformingTo(final Collection<Value> values, final IType type) {
		final Collection<Value> _result = new HashSet<Value>();
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
	 * Returns array values as conforming instances of <code>java.lang.Object</code> type.
	 * 
	 * @param values from which to get conforming values.
	 * @param type of the conforming values.
	 * @param result is the out parameter into which the conforming values will be added.
	 * @pre values != null and type != null and result != null
	 * @post result.containsAll(result$pre)
	 */
	private void processForArrayAndObjectTypeRelation(final Collection<Value> values, final IType type,
			final Collection<Value> result) {
		final DummyType _dType = (DummyType) type;
		final Object _sootType = _dType.sootType;

		if (!objectTypeSeen && _sootType instanceof RefType
				&& ((RefType) _sootType).getSootClass().getName().equals("java.lang.Object")) {
			final Iterator<Value> _i = values.iterator();
			final int _iEnd = values.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Value _value = _i.next();

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
	 * @pre values != null and type != null and result != null
	 * @post result.containsAll(result$pre)
	 */
	private void processForNullConstAndRefTypeRelation(final Collection<Value> values, final IType type,
			final Collection<Value> result) {
		nullConstSeen = nullConstSeen || CollectionUtils.exists(values, SootPredicatesAndTransformers.NULL_PREDICATE);

		if (nullConstSeen) {
			if (Util.isReferenceType(((DummyType) type).sootType) && !typesForNullConst.contains(type)) {
				typesForNullConst.add(type);
				result.add(CollectionUtils.find(values, SootPredicatesAndTransformers.NULL_PREDICATE));
			}
		}
	}
}

// End of File
