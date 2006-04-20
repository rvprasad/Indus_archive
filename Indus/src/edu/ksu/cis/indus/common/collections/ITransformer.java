/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.ksu.cis.indus.common.collections;

/**
 * A transformer.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <I> is the type of input object to the transformer.
 * @param <O> is the type of output object to the transformer.
 */
public interface ITransformer<I, O> {

	/**
	 * Transforms the given object.
	 * 
	 * @param input is the object to be transformed
	 * @return the transformed object.
	 */
	O transform(I input);

}

// End of File
