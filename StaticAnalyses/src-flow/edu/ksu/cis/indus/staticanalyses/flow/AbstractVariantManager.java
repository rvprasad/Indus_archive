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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages variants. An variant manager classes should extend this class. This class embodies the logic to manage
 * the variants.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <V> DOCUMENT ME!
 * @param <E> DOCUMENT ME!
 * @param <SYM> DOCUMENT ME!
 * @param <N> DOCUMENT ME!
 */
public abstract class AbstractVariantManager<V extends IVariant, E, SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>>
		implements IVariantManager<V, E> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVariantManager.class);

	/**
	 * The instance of the framework in which this object is used.
	 *
	 * @invariant fa != null
	 */
	protected final FA<SYM, T, N> fa;

	/**
	 * A manager of indices that map entities to variants.
	 *
	 * @invariant indexManager != null
	 */
	private final IIndexManager<? extends IIndex<?>, E> idxManager;

	/**
	 * A map from indices to variants.
	 *
	 * @invariant index2variant != null
	 */
	private final Map<IIndex<?>, V> index2variant = new HashMap<IIndex<?>, V>();

	/**
	 * Creates a new <code>AbstractVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices that map the entities to variants.
	 * @pre theAnalysis != null and indexManager != null
	 */
	AbstractVariantManager(final FA<SYM, T, N> theAnalysis, final IIndexManager<? extends IIndex<?>, E> indexManager) {
		this.fa = theAnalysis;
		this.idxManager = indexManager;
	}

	/**
	 * Returns the variant corresponding to the given entity in the given context, if one exists.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant correponding to the entity in the given context, if one exists. <code>null</code> if none exist.
	 * @pre o != null and context != null
	 */
	public final V query(final E o, final Context context) {
		return index2variant.get(idxManager.getIndex(o, context));
	}

	/**
	 * Resets the manager. All internal data structures are reset to enable a new session of usage.
	 */
	public void reset() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("IVariant manager being reset.");
		}
		index2variant.clear();
		idxManager.reset();
	}

	/**
	 * Returns the variant corresponding to the given entity in the given context. If a variant does not exist, a new one is
	 * created. If one exists, it shall be returned.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant correponding to the entity in the given context.
	 * @pre o != null and context != null
	 * @post result != null
	 */
	public final V select(final E o, final Context context) {
		final IIndex<?> _index = idxManager.getIndex(o, context);
		V _temp = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering - IIndex: " + _index + "\n" + o + "\n" + context);
		}

		if (index2variant.containsKey(_index)) {
			_temp = index2variant.get(_index);
		} else if (!fa.getAnalyzer().isStable()) {
			_temp = getNewVariant(o);
			index2variant.put(_index, _temp);
			_temp.process();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Exiting - IIndex");
		}

		return _temp;
	}

	/**
	 * Returns the new variant correponding to the given object. This is a template method to be provided by concrete
	 * implementations.
	 *
	 * @param o the object whose corresponding variant is to be returned.
	 * @return the new variant corresponding to the given object.
	 * @pre o != null
	 * @post result != null
	 */
	protected abstract V getNewVariant(final E o);

	/**
	 * Returns the total variants managed by this manager.
	 *
	 * @return number of variants managed.
	 */
	protected int getVariantCount() {
		return index2variant.values().size();
	}

	/**
	 * Retrieves the variants managed by this object.
	 *
	 * @return the variants.
	 * @post result != null
	 */
	Collection<V> getVariants() {
		return index2variant.values();
	}
}

// End of File
