
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.common.CollectionsModifier;

import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	final List valueList = new ArrayList();

	/**
	 * The mapping between types and the sequence of bits that represent the values that are of the key type.
	 *
	 * @invariant type2tokens.oclIsKindOf(Map(IType, BitSet))
	 * @invariant type2tokens.values()->forall( o | o.size() &lt;= valueList.size())
	 */
	private final Map type2tokens = new HashMap();

	/**
	 * Creates an instacne of this class.
	 *
	 * @param typeManager to be used.
	 *
	 * @pre typeManager != null
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
		 * @pre tokenMgr != null
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
			final Collection _result = new ArrayList();

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
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTokens(java.util.Collection)
	 */
	public ITokens getTokens(final Collection values) {
		final BitSetTokens _result = new BitSetTokens(this);

		for (final Iterator _i = values.iterator(); _i.hasNext();) {
			final Object _value = _i.next();
			final int _index = valueList.indexOf(_value);

			if (_index != -1) {
				_result.bitset.set(_index);
			} else {
				valueList.add(_value);

				final int _newIndex = valueList.indexOf(_value);
				_result.bitset.set(_newIndex);

				final Collection _types = typeMgr.getAllTypes(_value);

				for (final Iterator _j = _types.iterator(); _j.hasNext();) {
					final Object _type = _j.next();
					final BitSet _tokens = (BitSet) CollectionsModifier.getFromMap(type2tokens, _type, new BitSet());
					_tokens.set(_newIndex);
				}
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeBasedFilter(IType)
	 */
	public ITokenFilter getTypeBasedFilter(final IType type) {
		BitSet _mask = (BitSet) type2tokens.get(type);

		if (_mask == null) {
			_mask = new BitSet();
			type2tokens.put(type, _mask);
		}

		final BitSetTokenFilter _result = new BitSetTokenFilter(_mask);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	public void reset() {
		valueList.clear();
		type2tokens.clear();
	}
}

/*
   ChangeLog:
   $Log$
 */