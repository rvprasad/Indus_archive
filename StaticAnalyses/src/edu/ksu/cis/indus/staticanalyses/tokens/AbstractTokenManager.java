
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

import edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager.NewTypeCreated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


/**
 * This class provides the abstract implementation of <code>ITokenmanager</code>.  It is advised that all token managers
 * extend this class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractTokenManager
  implements ITokenManager, Observer {
	/** 
	 * This provides the logic to update token to type relation.  A simple situation is that in Java <code>null</code> is a
	 * valid value/token of all reference types in the system. Hence, token to type relation will change as more types are
	 * loaded into the system after <code>null</code> has been considered.
	 */
	protected final IDynamicTokenTypeRelationEvaluator onlineTokenTypeRelationEvalutator;

    
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
		onlineTokenTypeRelationEvalutator = typeManager.getDynamicTokenTypeRelationEvaluator();
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
     * {@inheritDoc}
     */
    public final void update(final Observable observer, final Object arg) {
		if (arg instanceof NewTypeCreated && onlineTokenTypeRelationEvalutator != null) {
            final IType _type = ((NewTypeCreated) arg).getCreatedType();
			final Collection _values = onlineTokenTypeRelationEvalutator.getValuesConformingTo(getValues(), _type);
            if (!_values.isEmpty()) {
                recordNewTokenTypeRelations(_values, _type);
            }
		}
	}

	/**
     * Retrieves the values being managed by this manager.  This implementation returns an empty collection.
     * 
     * This is used to update value-token-type relation on-the-fly. 
     * 
     * @return the values being managed by this manager.
     */
    protected Collection getValues() {
        return Collections.EMPTY_SET;
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
	 * Records the new token-type relations.  This implementation does nothing. This method will be called only be called if 
     * new token-type relations are discovered on-the-fly.
	 *
	 * @param values whose type has been incrementally changed. This is guaranteed to be the objects in the collection
	 * 		  <code>values</code> provided to <code>fixupTokenTypeRelation</code> method.
	 * @param type is the new additional type of <code>values</code>. This is guaranteed to be one of the objects in the
	 * 		  collection  <code>types</code> provided to <code>fixupTokenTypeRelation</code> method.
	 *
	 * @pre values != null and type != null
     * @pre not values.isEmpty()
	 */
	protected void recordNewTokenTypeRelations(final Collection values, final IType type) {
	}
}

// End of File
