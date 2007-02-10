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

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.ListOrderedSet;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.soot.Constants;
import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class realizes a token manager that represents tokens as bits via bit-encoding.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <V> is the type of the value object (in the representation).
 * @param <R> is the type of the representation types.
 */
public final class BitSetTokenManager<V, R>
		extends AbstractTokenManager<BitSetTokenManager<V, R>.BitSetTokens, V, R> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(BitSetTokenManager.class);

	/**
	 * The list used to canonicalize bit position for values.
	 * 
	 * @invariant valueList.oclIsKindOf(Sequence(Object))
	 */
	final ListOrderedSet<V> valueList = new ListOrderedSet<V>();

	/**
	 * The mapping between types and the sequence of bits that represent the values that are of the key type.
	 * 
	 * @invariant type2tokens.values()->forall( o | o.size() &lt;= valueList.size())
	 */
	private final Map<IType, BitSet> type2tokens = new HashMap<IType, BitSet>(Constants.getNumOfClassesInApplication());

	/**
	 * The mapping between type in the token universe to that types filter.
	 */
	private final Map<IType, BitSetTokenFilter> type2tokenfilters = new HashMap<IType, BitSetTokenFilter>(Constants
			.getNumOfClassesInApplication());

	/**
	 * Creates an instacne of this class.
	 * 
	 * @param typeManager to be used.
	 * @see AbstractTokenManager#AbstractTokenManager(ITypeManager)
	 */
	public BitSetTokenManager(final ITypeManager<R, V> typeManager) {
		super(typeManager);
		typeManager.addObserver(this);
	}

	/**
	 * This class represents a token filter based on bit representation of tokens.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class BitSetTokenFilter
			implements ITokenFilter<BitSetTokenManager<V, R>.BitSetTokens, V> {

		/**
		 * The filter mask.
		 */
		private final BitSet bitmask;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param mask is the filter mask.
		 * @pre mask != null
		 */
		BitSetTokenFilter(final BitSet mask) {
			bitmask = mask;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter#filter(ITokens)
		 */
		public BitSetTokens filter(final BitSetTokens tokens) {
			final BitSetTokens _result = new BitSetTokens(bitmask.size());
			final BitSet _temp = _result.bitset;
			_temp.or(bitmask);
			_temp.and(tokens.bitset);
			return _result;
		}
	}

	/**
	 * This class represents a collection of tokens represented as bits in a bitset.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class BitSetTokens
			extends AbstractPrototype<BitSetTokens>
			implements ITokens<BitSetTokens, V> {

		/**
		 * The bitset used to capture the representation of the tokens.
		 */
		final BitSet bitset;

		/**
		 * Creates a new BitSetTokens object.
		 */
		BitSetTokens() {
			this(8);
		}

		/**
		 * Creates an instance of this class.
		 * 
		 * @param initLength of the bitset.
		 * @pre initLength >= 0
		 */
		BitSetTokens(final int initLength) {
			assert initLength >= 0;
			bitset = new BitSet(initLength);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override public BitSetTokens getClone(@SuppressWarnings("unused") final Object... o) {
			final BitSetTokens _result = new BitSetTokens(bitset.size());
			_result.bitset.or(bitset);
			return _result;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#isEmpty()
		 */
		public boolean isEmpty() {
			return bitset.cardinality() == 0;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#getValues()
		 */
		public Collection<V> getValues() {
			final Collection<V> _result = new ArrayList<V>(bitset.cardinality());

			for (int _i = bitset.nextSetBit(0); _i >= 0; _i = bitset.nextSetBit(_i + 1)) {
				_result.add(BitSetTokenManager.this.valueList.get(_i));
			}
			return _result;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#addTokens(ITokens)
		 */
		public void addTokens(final BitSetTokens newTokens) {
			bitset.or(newTokens.bitset);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#clear()
		 */
		public void clear() {
			bitset.clear();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#diffTokens(ITokens)
		 */
		public BitSetTokens diffTokens(final BitSetTokens tokens) {
			final BitSetTokens _result = new BitSetTokens(bitset.size());
			_result.bitset.or(bitset);

			if (bitset.intersects(tokens.bitset)) {
				_result.bitset.andNot(tokens.bitset);
			}

			return _result;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see ITokenManager#getNewTokenSet()
	 */
	public BitSetTokens getNewTokenSet() {
		return new BitSetTokens();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see ITokenManager#getTokens(java.util.Collection)
	 */
	public BitSetTokens getTokens(final Collection<V> values) {
		final BitSetTokens _result = new BitSetTokens(values.size());

		if (!values.isEmpty()) {
			final Collection<V> _commons = SetUtils.intersection(valueList, values);

			for (final Iterator<V> _i = _commons.iterator(); _i.hasNext();) {
				final V _o = _i.next();
				_result.bitset.set(valueList.indexOf(_o));
			}

			final Collection<V> _diff = SetUtils.difference(values, _commons);
			int _index = valueList.size();

			for (final Iterator<V> _i = _diff.iterator(); _i.hasNext();) {
				final V _value = _i.next();
				valueList.add(_value);
				_result.bitset.set(_index);

				final Collection<IType> _types = typeMgr.getAllTypes(_value);

				for (final Iterator<IType> _j = _types.iterator(); _j.hasNext();) {
					final IType _type = _j.next();
					final BitSet _tokens = MapUtils
							.getFromMapUsingFactory(type2tokens, _type, CollectionUtils.BITSET_FACTORY);
					_tokens.set(_index);
				}
				_index++;
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see ITokenManager#reset()
	 */
	@Override public void reset() {
		super.reset();
		valueList.clear();
		type2tokens.clear();
		type2tokenfilters.clear();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractTokenManager#getNewFilterForType(IType)
	 */
	@Override protected BitSetTokenFilter getNewFilterForType(final IType type) {
		final BitSetTokenFilter _result;

		if (type2tokenfilters.containsKey(type)) {
			_result = type2tokenfilters.get(type);
		} else {
			final BitSet _mask = MapUtils.getFromMapUsingFactory(type2tokens, type, CollectionUtils.BITSET_FACTORY);
			_result = new BitSetTokenFilter(_mask);
			type2tokenfilters.put(type, _result);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractTokenManager#recordNewTokenTypeRelations(Collection, IType)
	 */
	@Override protected void recordNewTokenTypeRelations(final Collection<V> values, final IType type) {
		final BitSet _b = MapUtils.getFromMapUsingFactory(type2tokens, type, CollectionUtils.BITSET_FACTORY);
		final Iterator<V> _i = values.iterator();
		final int _iEnd = values.size();
		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			_b.set(valueList.indexOf(_i.next()));
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.AbstractTokenManager#getValues()
	 */
	@Override protected Collection<V> getValues() {
		return valueList;
	}
}

// End of File
