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
import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class realizes a token manager that represents tokens asis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <V> is the type of the value object (in the representation).
 * @param <R> is the type of the representation types.
 */
public final class CollectionTokenManager<V, R>
		extends AbstractTokenManager<CollectionTokenManager<V, R>.CollectionTokens, V, R> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(CollectionTokenManager.class);

	/**
	 * Creates an instacne of this class.
	 * 
	 * @param typeManager to be used.
	 * @pre typeManager != null
	 */
	public CollectionTokenManager(final ITypeManager<R, V> typeManager) {
		super(typeManager);
	}

	/**
	 * This class represents a token filter based on collection of tokens.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CollectionTokenFilter
			implements ITokenFilter<CollectionTokenManager<V, R>.CollectionTokens, V> {

		/**
		 * The type associated with the filter.
		 */
		private final IType filterType;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param type is the type used to filter.
		 * @pre type != null
		 */
		CollectionTokenFilter(final IType type) {
			filterType = type;
		}

		/**
		 * {@inheritDoc}
		 */
		public CollectionTokenManager<V, R>.CollectionTokens filter(final CollectionTokenManager<V, R>.CollectionTokens tokens) {
			final Collection<V> _filterate = new ArrayList<V>();

			for (final Iterator<V> _i = tokens.getValues().iterator(); _i.hasNext();) {
				final V _value = _i.next();

				if (typeMgr.getAllTypes(_value).contains(filterType)) {
					_filterate.add(_value);
				}
			}
			return getTokens(_filterate);
		}
	}

	/**
	 * This class represents a collection of tokens represented asis.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CollectionTokens
			extends AbstractPrototype<CollectionTokens>
			implements ITokens<CollectionTokens, V> {

		/**
		 * The collection of values.
		 * 
		 * @invariant values != null
		 */
		private Collection<V> values;

		/**
		 * Creates a new instance of this class.
		 * 
		 * @param initValues are the values to be put into this instnace.
		 * @pre initValues != null
		 */
		CollectionTokens(final Collection<V> initValues) {
			values = new HashSet<V>(initValues);
		}

		/**
		 * Creates an instance of this class.
		 */
		CollectionTokens() {
			values = new HashSet<V>();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override public CollectionTokens getClone(@SuppressWarnings("unused") final Object... o) {
			return new CollectionTokens(values);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEmpty() {
			return values.isEmpty();
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection<V> getValues() {
			return Collections.unmodifiableCollection(values);
		}

		/**
		 * {@inheritDoc}
		 */
		public void addTokens(final CollectionTokens newTokens) {
			values.addAll(newTokens.values);
		}

		/**
		 * {@inheritDoc}
		 */
		public void clear() {
			values.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		public CollectionTokens diffTokens(final CollectionTokens tokens) {
			return new CollectionTokens(SetUtils.difference(values, tokens.values));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CollectionTokens getNewTokenSet() {
		return new CollectionTokens();
	}

	/**
	 * {@inheritDoc}
	 */
	public CollectionTokens getTokens(final Collection<V> values) {
		return new CollectionTokens(values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected CollectionTokenFilter getNewFilterForType(final IType type) {
		return new CollectionTokenFilter(type);
	}
}

// End of File
