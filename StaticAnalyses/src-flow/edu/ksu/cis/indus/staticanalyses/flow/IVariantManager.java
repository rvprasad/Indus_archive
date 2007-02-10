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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.processing.Context;

/**
 * This interface is used to manage variants corresponding to entities.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <V> is the type of the variant.
 * @param <E> is the type of the entity whose variance is being managed.
 */
public interface IVariantManager<V extends IVariant, E> {

	/**
	 * Returns the variant corresponding to the given entity in the given context, if one exists.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 * @return the variant correponding to the entity in the given context, if one exists. <code>null</code> if none exist.
	 * @pre o != null and context != null
	 */
	V query(final E o, final Context context);

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
	V select(final E o, final Context context);
}

// End of File
