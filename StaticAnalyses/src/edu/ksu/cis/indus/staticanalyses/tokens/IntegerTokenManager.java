
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class realizes a token manager that represents tokens as bit positions in an integer via bit-encoding.  This implies
 * that this token manager can only handle system with 31 values.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class IntegerTokenManager
  extends AbstractTokenManager {
	/**
	 * The number of values that can be managed by using int-based bit-encoding.
	 */
	static final int NO_OF_BITS_IN_AN_INTEGER = 31;

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
	 * @invariant type2tokens.oclIsKindOf(Map(IType, MutableInteger))
	 */
	final Map type2tokens = new HashMap();

	/**
	 * Creates an instacne of this class.
	 *
	 * @param typeManager to be used.
	 *
	 * @pre typeManager != null
	 */
	public IntegerTokenManager(final ITypeManager typeManager) {
		super(typeManager);
	}

	/**
	 * This class represents a token filter based on bit representation of tokens via integers.
	 *
	 * @author venku To change this generated comment go to  Window>Preferences>Java>Code Generation>Code Template
	 */
	public class IntegerTokenFilter
	  implements ITokenFilter {
		/**
		 * The type of values to let through the filter.
		 *
		 * @pre filterType != null
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
		public ITokens filter(final ITokens tokens) {
			final IntegerTokens _result = new IntegerTokens(IntegerTokenManager.this);
			_result.integer |= ((IntegerTokens) tokens).integer;

			final MutableInteger _tokens =
				(MutableInteger) CollectionsModifier.getFromMap(type2tokens, filterType, new MutableInteger());
			_result.integer &= _tokens.intValue();
			return _result;
		}
	}


	/**
	 * This class is the mutable counterpart of <code>java.lang.Integer</code>.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class MutableInteger
	  extends Number {
		/**
		 * The value of this integer.
		 */
		private int value;

		/**
		 * Set the value of this integer to the given value.
		 *
		 * @param newValue is the new value.
		 */
		public void setValue(final int newValue) {
			value = newValue;
		}

		/**
		 * @see java.lang.Number#doubleValue()
		 */
		public double doubleValue() {
			return value;
		}

		/**
		 * @see java.lang.Number#floatValue()
		 */
		public float floatValue() {
			return value;
		}

		/**
		 * @see java.lang.Number#intValue()
		 */
		public int intValue() {
			return value;
		}

		/**
		 * @see java.lang.Number#longValue()
		 */
		public long longValue() {
			return value;
		}
	}


	/**
	 * This class represents a collection of tokens represented as bits in an integer.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	class IntegerTokens
	  extends AbstractPrototype
	  implements ITokens {
		/**
		 * The integer used to capture the representation of the tokens.
		 */
		int integer;

		/**
		 * The token manager associated with this instance of collection of tokens.
		 *
		 * @pre tokenMgr != null
		 */
		private IntegerTokenManager tokenMgr;

		/**
		 * Creates a new BitSetTokens object.
		 *
		 * @param tokenManager associated with this instance.
		 *
		 * @pre tokenManager != null
		 */
		IntegerTokens(final IntegerTokenManager tokenManager) {
			tokenMgr = tokenManager;
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone()
		 */
		public Object getClone() {
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
		public Collection getValues() {
			final Collection _result = new ArrayList();

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
		public void addTokens(final ITokens newTokens) {
			integer |= ((IntegerTokens) newTokens).integer;
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
		public ITokens diffTokens(final ITokens tokens) {
			final IntegerTokens _result = new IntegerTokens(tokenMgr);
			_result.integer = integer & ~((IntegerTokens) tokens).integer;
			return _result;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTokens(java.util.Collection)
	 */
	public ITokens getTokens(final Collection values) {
		final IntegerTokens _result = new IntegerTokens(this);

		for (final Iterator _i = values.iterator(); _i.hasNext();) {
			final Object _value = _i.next();
			final int _index = valueList.indexOf(_value);

			if (_index != -1) {
				_result.integer |= 1 << _index;
			} else {
				if (valueList.size() == NO_OF_BITS_IN_AN_INTEGER) {
					throw new IllegalStateException("This token manager cannot handle a type system instance with more than "
						+ NO_OF_BITS_IN_AN_INTEGER + " values.");
				}

				valueList.add(_value);

				final int _newIndex = valueList.indexOf(_value);
				final int _newValueRep = 1 << _newIndex;
				_result.integer |= _newValueRep;

				final Collection _types = typeMgr.getAllTypes(_value);

				for (final Iterator _j = _types.iterator(); _j.hasNext();) {
					final Object _type = _j.next();
					final MutableInteger _integer =
						(MutableInteger) CollectionsModifier.getFromMap(type2tokens, _type, new MutableInteger());
					_integer.setValue(_integer.intValue() | _newValueRep);
				}
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeBasedFilter(IType)
	 */
	public ITokenFilter getTypeBasedFilter(final IType type) {
		final IntegerTokenFilter _result = new IntegerTokenFilter(type);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	public void reset() {
		type2tokens.clear();
		valueList.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/04/17 09:17:44  venku
   - introduced the mutable counterpart of Integer class.
   - used that in this manager.
   - fixed some "counting" errors.

   Revision 1.1  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
 */
