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

package edu.ksu.cis.indus.common;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.NonNull;

import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class customizes the output of <code>toString()</code> by outputting the hashcode at the end instead of in the
 * beginning.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CustomToStringStyle
		extends ToStringStyle {

	/**
	 * This is the singleton object of this class.
	 */
	public static final ToStringStyle HASHCODE_AT_END_STYLE = new CustomToStringStyle();

	/**
	 * The serial version UID.
	 */
	static final long serialVersionUID = -2838615041966055043L;

	// / CLOVER:OFF

	/**
	 * Creates an instance of this class.
	 */
	@Empty private CustomToStringStyle() {
		// does nothing
	}

	// / CLOVER:ON

	/**
	 * {@inheritDoc}
	 */
	@Override public void appendEnd(@NonNull final StringBuffer buffer, final Object object) {
		super.appendEnd(buffer, object);
		appendIdentityHashCode(buffer, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void appendStart(@NonNull final StringBuffer buffer, final Object object) {
		appendClassName(buffer, object);
		appendContentStart(buffer);

		if (isFieldSeparatorAtStart()) {
			appendFieldSeparator(buffer);
		}
	}
}

// End of File
