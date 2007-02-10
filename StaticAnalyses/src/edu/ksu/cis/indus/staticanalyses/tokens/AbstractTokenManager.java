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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager.NewTypeCreated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * This class provides the abstract implementation of <code>ITokenmanager</code>. It is advised that all token managers
 * extend this class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of token set objects.
 * @param <V> is the type of the value objects (in the representation).
 * @param <R> is the type of the representation types.
 */
public abstract class AbstractTokenManager<T extends ITokens<T, V>, V, R>
		implements ITokenManager<T, V, R>, Observer {

	/**
	 * This provides the logic to update token to type relation. A simple situation is that in Java <code>null</code> is a
	 * valid value/token of all reference types in the system. Hence, token to type relation will change as more types are
	 * loaded into the system after <code>null</code> has been considered.
	 */
	protected final IDynamicTokenTypeRelationDetector<V> onlineTokenTypeRelationEvalutator;

	/**
	 * The type manager that manages the types of the tokens managed by this object.
	 * 
	 * @invariant typeMgr != null
	 */
	protected final ITypeManager<R, V> typeMgr;

	/**
	 * The mapping between types to the type based filter.
	 */
	private final Map<IType, ITokenFilter<T, V>> type2filter = new HashMap<IType, ITokenFilter<T, V>>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param typeManager manages the types of the tokens managed by this object. The client should relinquish ownership of
	 *            the given argument. This argument is provided for configurability.
	 * @pre typeManager != null
	 */
	public AbstractTokenManager(final ITypeManager<R, V> typeManager) {
		typeMgr = typeManager;
		onlineTokenTypeRelationEvalutator = typeManager.getDynamicTokenTypeRelationEvaluator();
	}

	/**
	 * {@inheritDoc}
	 */
	public final ITokenFilter<T, V> getTypeBasedFilter(final IType type) {
		ITokenFilter<T, V> _result = type2filter.get(type);

		if (_result == null) {
			_result = getNewFilterForType(type);
			type2filter.put(type, _result);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITypeManager<R, V> getTypeManager() {
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
	public final void update(@SuppressWarnings("unused") final Observable observer, final Object arg) {
		if (arg instanceof NewTypeCreated && onlineTokenTypeRelationEvalutator != null) {
			final IType _type = ((NewTypeCreated) arg).getCreatedType();
			final Collection<V> _values = onlineTokenTypeRelationEvalutator.getValuesConformingTo(getValues(), _type);
			if (!_values.isEmpty()) {
				recordNewTokenTypeRelations(_values, _type);
			}
		}
	}

	/**
	 * Retrieves a new token filter for the given type.
	 * 
	 * @param type for which the filter is requested.
	 * @return a new token filter.
	 * @pre type != null
	 */
	protected abstract ITokenFilter<T, V> getNewFilterForType(final IType type);

	/**
	 * Retrieves the values being managed by this manager. This implementation returns an empty collection. This is used to
	 * update value-token-type relation on-the-fly.
	 * 
	 * @return the values being managed by this manager.
	 */
	protected Collection<V> getValues() {
		return Collections.emptySet();
	}

	/**
	 * Records the new token-type relations. This implementation does nothing. This method will be called only be called if
	 * new token-type relations are discovered on-the-fly.
	 * 
	 * @param values whose type has been incrementally changed. This is guaranteed to be the objects in the collection
	 *            <code>values</code> provided to <code>fixupTokenTypeRelation</code> method.
	 * @param type is the new additional type of <code>values</code>. This is guaranteed to be one of the objects in the
	 *            collection <code>types</code> provided to <code>fixupTokenTypeRelation</code> method.
	 * @pre values != null and type != null
	 * @pre not values.isEmpty()
	 */
	@Empty protected void recordNewTokenTypeRelations(@SuppressWarnings("unused") final Collection<V> values,
			@SuppressWarnings("unused") final IType type) {
		// does nothing
	}
}

// End of File
