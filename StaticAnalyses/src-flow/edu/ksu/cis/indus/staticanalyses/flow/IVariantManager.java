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

/**
 * This interface is used to manage variants corresponding to entities.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <V> DOCUMENT ME!
 * @param <O> DOCUMENT ME!
 */
public interface IVariantManager<V extends IVariant, O> {

	/**
	 * Returns the variant corresponding to the given entity in the given context, if one exists.
	 * 
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant correponding to the entity in the given context, if one exists. <code>null</code> if none exist.
	 * @pre o != null and context != null
	 */
	V query(final O o, final Context context);

	/**
	 * Resets the manager. All internal data structures are reset to enable a new session of usage.
	 */
	void reset();

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
	V select(final O o, final Context context);
}

// End of File
