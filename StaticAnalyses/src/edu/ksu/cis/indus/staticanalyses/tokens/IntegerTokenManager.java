
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Factory;


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
	final Map type2tokens = new HashMap(Constants.getNumOfClassesInApplication());

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
				(MutableInteger) CollectionsUtilities.getFromMap(type2tokens, filterType, MutableInteger.FACTORY);
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
		 * A factory to create <code>MutableInteger</code>.
		 */
		public static final Factory FACTORY =
			new Factory() {
				public Object create() {
					return new MutableInteger();
				}
			};

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
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getNewTokenSet()
	 */
	public ITokens getNewTokenSet() {
		return new IntegerTokens(this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTokens(java.util.Collection)
	 */
	public ITokens getTokens(final Collection values) {
		final IntegerTokens _result = new IntegerTokens(this);

		if (!values.isEmpty()) {
			final Collection _commons = CollectionUtils.intersection(valueList, values);

			for (final Iterator _i = _commons.iterator(); _i.hasNext();) {
				_result.integer |= 1 << valueList.indexOf(_i.next());
			}

			final Collection _diff = CollectionUtils.subtract(values, _commons);
			int _index = 1 << valueList.size();

			for (final Iterator _i = _diff.iterator(); _i.hasNext();) {
				if (valueList.size() == NO_OF_BITS_IN_AN_INTEGER) {
					throw new IllegalStateException("This token manager cannot handle a type system instance with more than "
						+ NO_OF_BITS_IN_AN_INTEGER + " values.");
				}

				final Object _value = _i.next();
				valueList.add(_value);
				_result.integer |= _index;

				final Collection _types = typeMgr.getAllTypes(_value);

				for (final Iterator _j = _types.iterator(); _j.hasNext();) {
					final Object _type = _j.next();
					final MutableInteger _integer =
						(MutableInteger) CollectionsUtilities.getFromMap(type2tokens, _type, MutableInteger.FACTORY);
					_integer.setValue(_integer.intValue() | _index);
				}
				_index <<= 1;
			}
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	public void reset() {
		super.reset();
		type2tokens.clear();
		valueList.clear();
	}

	/**
	 * @see AbstractTokenManager#getNewFilterForType(edu.ksu.cis.indus.staticanalyses.tokens.IType)
	 */
	protected ITokenFilter getNewFilterForType(final IType type) {
		return new IntegerTokenFilter(type);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2004/08/09 03:11:49  venku
   - each type-based filter is dependent on the values of that type.  As these do
     not change in an analysis, it is wise to cache these filters for reuse.
   Revision 1.9  2004/08/08 10:11:35  venku
   - added a new class to configure constants used when creating data structures.
   - ripple effect.
   Revision 1.8  2004/07/17 23:32:18  venku
   - used Factory() pattern to populate values in maps and lists in CollectionsUtilities methods.
   - ripple effect.
   Revision 1.7  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.6  2004/05/20 07:29:41  venku
   - optimized the token set to be optimal when created.
   - added new method to retrieve empty token sets (getNewTokenSet()).
   Revision 1.5  2004/05/19 00:20:49  venku
   - optimized getTokens() method.
   Revision 1.4  2004/05/06 22:27:29  venku
   - optimized getTokens() by avoiding redundant calls when adding the values
     to the end of the list.
   Revision 1.3  2004/04/17 20:28:38  venku
   - coding conventions.
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
