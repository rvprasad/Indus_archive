
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This class provides the abstract implementation of <code>ITokenmanager</code>.  It is advised that all token managers
 * extend this class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractTokenManager
  implements ITokenManager {
	/** 
	 * This provides the logic to update token to type relation.  A simple situation is that in Java <code>null</code> is a
	 * valid value/token of all reference types in the system. Hence, token to type relation will change as more types are
	 * loaded into the system after <code>null</code> has been considered.
	 */
	protected final IDynamicTokenTypeRelationEvaluator onlineTokenTypeEvalutator;

	/** 
	 * The type manager that manages the types of the tokens managed by this object.
	 *
	 * @invariant typeMgr != null
	 */
	protected final ITypeManager typeMgr;

	/** 
	 * The mapping between types to the type based filter.
	 *
	 * @invariant type2filter.oclIsKindOf(Map(IType, ITokenFilter))
	 */
	private final Map type2filter = new HashMap();

	/**
	 * Creates an instance of this class.
	 *
	 * @param typeManager manages the types of the tokens managed by this object.  The client should relinquish ownership of
	 * 		  the given argument.  This argument is provided for configurability.
	 *
	 * @pre typeManager != null
	 */
	public AbstractTokenManager(final ITypeManager typeManager) {
		typeMgr = typeManager;
		onlineTokenTypeEvalutator = typeManager.getDynamicTokenTypeRelationEvaluator();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeBasedFilter(IType)
	 */
	public final ITokenFilter getTypeBasedFilter(final IType type) {
		ITokenFilter _result = (ITokenFilter) type2filter.get(type);

		if (_result == null) {
			_result = getNewFilterForType(type);
			type2filter.put(type, _result);
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeManager()
	 */
	public ITypeManager getTypeManager() {
		return typeMgr;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	public void reset() {
		typeMgr.reset();
		type2filter.clear();
	}

	/**
	 * Evaluates if the types of any of values seen till now have changed (incrementally) and records these new relation.
	 *
	 * @param seenValues are the values seen up until now.
	 * @param newTypes that need to be considered for new token-type relation ships.
	 *
	 * @pre seenValues != null and newTypes != null
	 */
	protected final void fixupTokenTypeRelation(final Collection seenValues, final Collection newTypes) {
		if (onlineTokenTypeEvalutator != null) {
			final Collection _value2TypesToUpdate =
				onlineTokenTypeEvalutator.getValue2TypesToUpdate(seenValues, newTypes).entrySet();
			final Iterator _i = _value2TypesToUpdate.iterator();
			final int _iEnd = _value2TypesToUpdate.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Object _value = _entry.getKey();
				final Collection _typesToUpdate = (Collection) _entry.getValue();
				final Iterator _j = _typesToUpdate.iterator();
				final int _jEnd = _typesToUpdate.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final Object _type = _j.next();
					recordNewTokenTypeRelation(_value, _type);
				}
			}
		}
	}

	/**
	 * Retrieves a new token filter for the given type.
	 *
	 * @param type for which the filter is requested.
	 *
	 * @return a new token filter.
	 *
	 * @pre type != null
	 */
	protected abstract ITokenFilter getNewFilterForType(final IType type);

	/**
	 * Records the new token-type relation.  This implementation does nothing.
	 *
	 * @param value whose type has been incrementally changed. This is guaranteed to be one of the objects in the collection
	 * 		  <code>values</code> provided to <code>fixupTokenTypeRelation</code> method.
	 * @param type is the new additional type of <code>value</code>. This is guaranteed to be one of the objects in the
	 * 		  collection  <code>types</code> provided to <code>fixupTokenTypeRelation</code> method.
	 *
	 * @pre value != null and type != null
	 */
	protected void recordNewTokenTypeRelation(final Object value, final Object type) {
	}
}

// End of File
