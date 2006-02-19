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
 * @param <V> DOCUMENT ME!
 * @param <R> DOCUMENT ME!
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
	 * Creates an instacne of this class.
	 * 
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
		private BitSetTokens(int initLength) {
			assert initLength >= 0;
			bitset = new BitSet(initLength);
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone()
		 */
		@Override public BitSetTokens getClone(@SuppressWarnings("unused") final Object... o) {
			final BitSetTokens _result = new BitSetTokens(bitset.size());
			_result.bitset.or(bitset);
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#isEmpty()
		 */
		public boolean isEmpty() {
			return bitset.cardinality() == 0;
		}

		/**
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
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#addTokens(ITokens)
		 */
		public void addTokens(final BitSetTokens newTokens) {
			bitset.or(newTokens.bitset);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#clear()
		 */
		public void clear() {
			bitset.clear();
		}

		/**
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
	 * @see ITokenManager#getNewTokenSet()
	 */
	public BitSetTokens getNewTokenSet() {
		return new BitSetTokens();
	}

	/**
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
	 * @see ITokenManager#reset()
	 */
	@Override public void reset() {
		super.reset();
		valueList.clear();
		type2tokens.clear();
	}

	/**
	 * @see AbstractTokenManager#getNewFilterForType(IType)
	 */
	@Override protected BitSetTokenFilter getNewFilterForType(final IType type) {
		final BitSet _mask = MapUtils.getFromMapUsingFactory(type2tokens, type, CollectionUtils.BITSET_FACTORY);

		return new BitSetTokenFilter(_mask);
	}

	/**
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
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.AbstractTokenManager#getValues()
	 */
	@Override protected Collection<V> getValues() {
		return valueList;
	}
}

// End of File
