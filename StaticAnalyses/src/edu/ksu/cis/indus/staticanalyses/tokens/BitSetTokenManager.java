
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

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.Constants;

import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.collections.set.ListOrderedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class realizes a token manager that represents tokens as bits via bit-encoding.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class BitSetTokenManager
  extends AbstractTokenManager {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(BitSetTokenManager.class);

	/** 
	 * The list used to canonicalize bit position for values.
	 *
	 * @invariant valueList.oclIsKindOf(Sequence(Object))
	 * @invariant valueList->forall( o | valueList->remove(o)->forall(p | p != o))
	 */
	final ListOrderedSet valueList = new ListOrderedSet();

	/** 
	 * The mapping between types and the sequence of bits that represent the values that are of the key type.
	 *
	 * @invariant type2tokens.oclIsKindOf(Map(IType, BitSet))
	 * @invariant type2tokens.values()->forall( o | o.size() &lt;= valueList.size())
	 */
	private final Map type2tokens = new HashMap(Constants.getNumOfClassesInApplication());

	/**
	 * Creates an instacne of this class.
	 *
	 * @see AbstractTokenManager#AbstractTokenManager(ITypeManager)
	 */
	public BitSetTokenManager(final ITypeManager typeManager) {
		super(typeManager);
	}

	/**
	 * This class represents a token filter based on bit representation of tokens.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class BitSetTokenFilter
	  implements ITokenFilter {
		/** 
		 * The filter mask.
		 */
		private final BitSet bitmask;

		/**
		 * Creates an instance of this class.
		 *
		 * @param mask is the filter mask.
		 *
		 * @pre mask != null
		 */
		BitSetTokenFilter(final BitSet mask) {
			bitmask = mask;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter#filter(ITokens)
		 */
		public ITokens filter(final ITokens tokens) {
			final BitSetTokens _result = new BitSetTokens(BitSetTokenManager.this);
			final BitSet _temp = _result.bitset;
			_temp.or(((BitSetTokens) tokens).bitset);
			_temp.and(bitmask);
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
	  extends AbstractPrototype
	  implements ITokens {
		/** 
		 * The bitset used to capture the representation of the tokens.
		 */
		final BitSet bitset;

		/** 
		 * The token manager associated with this instance of collection of tokens.
		 *
		 * @invariant tokenMgr != null
		 */
		private final BitSetTokenManager tokenMgr;

		/**
		 * Creates a new BitSetTokens object.
		 *
		 * @param tokenManager associated with this instance.
		 *
		 * @pre tokenManager != null
		 */
		BitSetTokens(final BitSetTokenManager tokenManager) {
			tokenMgr = tokenManager;
			bitset = new BitSet();
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone()
		 */
		public Object getClone() {
			final BitSetTokens _result = new BitSetTokens(tokenMgr);
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
		public Collection getValues() {
			final Collection _result = new ArrayList(bitset.cardinality());

			for (int _i = bitset.nextSetBit(0); _i >= 0; _i = bitset.nextSetBit(_i + 1)) {
				_result.add(tokenMgr.valueList.get(_i));
			}
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#addTokens(ITokens)
		 */
		public void addTokens(final ITokens newTokens) {
			bitset.or(((BitSetTokens) newTokens).bitset);
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
		public ITokens diffTokens(final ITokens tokens) {
			final BitSetTokens _result = new BitSetTokens(tokenMgr);
			_result.bitset.or(bitset);
			_result.bitset.andNot(((BitSetTokens) tokens).bitset);
			return _result;
		}
	}

	/**
	 * @see ITokenManager#getNewTokenSet()
	 */
	public ITokens getNewTokenSet() {
		return new BitSetTokens(this);
	}

	/**
	 * @see ITokenManager#getTokens(java.util.Collection)
	 */
	public ITokens getTokens(final Collection values) {
		final BitSetTokens _result = new BitSetTokens(this);

		if (!values.isEmpty()) {
			final Collection _commons = CollectionUtils.intersection(valueList, values);

			for (final Iterator _i = _commons.iterator(); _i.hasNext();) {
				final Object _o = _i.next();
				_result.bitset.set(valueList.indexOf(_o));
			}

			final Collection _typeCol = new HashSet();
			final Collection _diff = CollectionUtils.subtract(values, _commons);
			int _index = valueList.size();

			for (final Iterator _i = _diff.iterator(); _i.hasNext();) {
				final Object _value = _i.next();
				valueList.add(_value);
				_result.bitset.set(_index);

				final Collection _types = typeMgr.getAllTypes(_value);

				for (final Iterator _j = _types.iterator(); _j.hasNext();) {
					final Object _type = _j.next();
					final BitSet _tokens =
						(BitSet) CollectionsUtilities.getFromMap(type2tokens, _type, CollectionsUtilities.BIT_SET_FACTORY);
					_tokens.set(_index);
				}
				_index++;
				_typeCol.addAll(_types);
			}

			fixupTokenTypeRelation(valueList, _typeCol);
		}
		return _result;
	}

	/**
	 * @see ITokenManager#reset()
	 */
	public void reset() {
		super.reset();
		valueList.clear();
		type2tokens.clear();
	}

	/**
	 * @see AbstractTokenManager#getNewFilterForType(IType)
	 */
	protected ITokenFilter getNewFilterForType(final IType type) {
		final BitSet _mask =
			(BitSet) CollectionsUtilities.getFromMap(type2tokens, type, CollectionsUtilities.BIT_SET_FACTORY);

		return new BitSetTokenFilter(_mask);
	}

	/**
	 * @see AbstractTokenManager#recordNewTokenTypeRelation(Object, Object)
	 */
	protected void recordNewTokenTypeRelation(final Object value, final Object type) {
		((BitSet) type2tokens.get(type)).set(valueList.indexOf(value));
	}
}

// End of File
