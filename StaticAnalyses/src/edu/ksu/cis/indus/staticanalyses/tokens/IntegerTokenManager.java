
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


import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.soot.Constants;
import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;



/**
 * This class realizes a token manager that represents tokens as bit positions in an integer via bit-encoding.  This implies
 * that this token manager can only handle system with 31 values.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <V> DOCUMENT ME!
 * @param <R> DOCUMENT ME!
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
		 *
		 * @pre tokenManager != null
		 */
		IntegerTokens(final IntegerTokenManager<V, R> tokenManager) {
			tokenMgr = tokenManager;
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone()
		 */
		@Override public IntegerTokens getClone(@SuppressWarnings("unused") final Object...o) {
			final IntegerTokens _result = new IntegerTokens(tokenMgr);
			_result.integer = integer;
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#isEmpty()
		 */
		public boolean isEmpty() {
			return integer == 0;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#getValues()
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
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#addTokens(ITokens)
		 */
		public void addTokens(final IntegerTokens newTokens) {
			integer |= newTokens.integer;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#clear()
		 */
		public void clear() {
			integer = 0;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#diffTokens(ITokens)
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
	 * @author venku To change this generated comment go to  Window>Preferences>Java>Code Generation>Code Template
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
		 *
		 * @pre type != null
		 */
		IntegerTokenFilter(final Object type) {
			filterType = type;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter#filter(ITokens)
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
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getNewTokenSet()
	 */
	public IntegerTokens getNewTokenSet() {
		return new IntegerTokens(this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTokens(java.util.Collection)
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
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	@Override public void reset() {
		super.reset();
		type2tokens.clear();
		valueList.clear();
	}

	/**
	 * @see AbstractTokenManager#getNewFilterForType(IType)
	 */
	@Override protected ITokenFilter<IntegerTokens, V> getNewFilterForType(final IType type) {
		return new IntegerTokenFilter(type);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.AbstractTokenManager#getValues()
	 */
	@Override protected Collection<V> getValues() {
		return valueList;
	}

	/**
	 * @see AbstractTokenManager#recordNewTokenTypeRelations(Collection, IType)
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
