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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.soot.Constants;
import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class realizes a token manager that represents tokens as bit positions in an integer via bit-encoding. This implies
 * that this token manager can only handle system with 31 values.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <V> is the type of the value object (in the representation).
 * @param <R> is the type of the representation types.
 */
public class IntegerTokenManager<V, R>
		extends AbstractTokenManager<IntegerTokenManager<V, R>.IntegerTokens, V, R> {

	/**
	 * The number of values that can be managed by using int-based bit-encoding.
	 */
	static final int NO_OF_BITS_IN_AN_INTEGER = 31;

	/**
	 * The list used to canonicalize bit position for values.
	 * 
	 * @invariant valueList->forall( o | valueList->remove(o)->forall(p | p != o))
	 */
	final List<V> valueList = new ArrayList<V>();

	/**
	 * The mapping between types and the sequence of bits that represent the values that are of the key type.
	 * 
	 * @invariant type2tokens.oclIsKindOf(TObjectIntHashMap(IType, int))
	 */
	final TObjectIntHashMap type2tokens = new TObjectIntHashMap(Constants.getNumOfClassesInApplication());

	/**
	 * Creates an instacne of this class.
	 * 
	 * @param typeManager to be used.
	 * @see AbstractTokenManager#AbstractTokenManager(ITypeManager)
	 */
	public IntegerTokenManager(final ITypeManager<R, V> typeManager) {
		super(typeManager);
		typeManager.addObserver(this);
	}

	/**
	 * This class represents a collection of tokens represented as bits in an integer.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class IntegerTokens
			extends AbstractPrototype<IntegerTokens>
			implements ITokens<IntegerTokens, V> {

		/**
		 * The integer used to capture the representation of the tokens.
		 */
		int integer;

		/**
		 * The token manager associated with this instance of collection of tokens.
		 * 
		 * @invariant tokenMgr != null
		 */
		private IntegerTokenManager<V, R> tokenMgr;

		/**
		 * Creates a new IntegerTokens object.
		 * 
		 * @param tokenManager associated with this instance.
		 * @pre tokenManager != null
		 */
		IntegerTokens(final IntegerTokenManager<V, R> tokenManager) {
			tokenMgr = tokenManager;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override public IntegerTokens getClone(@SuppressWarnings("unused") final Object... o) {
			final IntegerTokens _result = new IntegerTokens(tokenMgr);
			_result.integer = integer;
			return _result;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEmpty() {
			return integer == 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection<V> getValues() {
			final Collection<V> _result = new ArrayList<V>();

			for (int _i = 0; _i < IntegerTokenManager.NO_OF_BITS_IN_AN_INTEGER; _i++) {
				if ((integer & (1 << _i)) > 0) {
					_result.add(tokenMgr.valueList.get(_i));
				}
			}
			return _result;
		}

		/**
		 * {@inheritDoc}
		 */
		public void addTokens(final IntegerTokens newTokens) {
			integer |= newTokens.integer;
		}

		/**
		 * {@inheritDoc}
		 */
		public void clear() {
			integer = 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public IntegerTokens diffTokens(final IntegerTokens tokens) {
			final IntegerTokens _result = new IntegerTokens(tokenMgr);
			_result.integer = integer & ~tokens.integer;
			return _result;
		}
	}

	/**
	 * This class represents a token filter based on bit representation of tokens via integers.
	 * 
	 * @author venku To change this generated comment go to Window>Preferences>Java>Code Generation>Code Template
	 */
	private class IntegerTokenFilter
			implements ITokenFilter<IntegerTokenManager<V, R>.IntegerTokens, V> {

		/**
		 * The type of values to let through the filter.
		 * 
		 * @invariant filterType != null
		 */
		final Object filterType;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param type is the filter type.
		 * @pre type != null
		 */
		IntegerTokenFilter(final Object type) {
			filterType = type;
		}

		/**
		 * {@inheritDoc}
		 */
		public IntegerTokenManager<V, R>.IntegerTokens filter(final IntegerTokenManager<V, R>.IntegerTokens tokens) {
			final IntegerTokenManager<V, R> _l = IntegerTokenManager.this;
			final IntegerTokenManager<V, R>.IntegerTokens _result = new IntegerTokens(_l);
			_result.integer |= tokens.integer;
			_result.integer &= type2tokens.get(filterType);
			return _result;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IntegerTokens getNewTokenSet() {
		return new IntegerTokens(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IntegerTokens getTokens(final Collection<V> values) {
		final IntegerTokens _result = new IntegerTokens(this);

		if (!values.isEmpty()) {
			final Collection<V> _commons = SetUtils.intersection(valueList, values);

			for (final Iterator<V> _i = _commons.iterator(); _i.hasNext();) {
				_result.integer |= 1 << valueList.indexOf(_i.next());
			}

			final Collection<V> _diff = SetUtils.difference(values, _commons);
			int _index = 1 << valueList.size();

			for (final Iterator<V> _i = _diff.iterator(); _i.hasNext();) {
				if (valueList.size() == NO_OF_BITS_IN_AN_INTEGER) {
					throw new IllegalStateException("This token manager cannot handle a type system instance with more than "
							+ NO_OF_BITS_IN_AN_INTEGER + " values.");
				}

				final V _value = _i.next();
				valueList.add(_value);
				_result.integer |= _index;

				final Collection<IType> _types = typeMgr.getAllTypes(_value);

				for (final Iterator<IType> _j = _types.iterator(); _j.hasNext();) {
					final Object _type = _j.next();
					final int _t = type2tokens.get(_type);
					type2tokens.put(_type, _t | _index);
				}
				_index <<= 1;
			}
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void reset() {
		super.reset();
		type2tokens.clear();
		valueList.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected ITokenFilter<IntegerTokens, V> getNewFilterForType(final IType type) {
		return new IntegerTokenFilter(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected Collection<V> getValues() {
		return valueList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void recordNewTokenTypeRelations(final Collection<V> values, final IType type) {
		int _t = type2tokens.get(type);
		final Iterator<V> _i = values.iterator();
		final int _iEnd = values.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			_t |= valueList.indexOf(_i.next());
		}
		type2tokens.put(type, _t);
	}
}

// End of File
