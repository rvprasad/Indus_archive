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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import soot.tagkit.Tag;

/**
 * This is a tag implementation which can be differentiated by the name given at instantiation time.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class NamedTag
		implements Tag {

	/**
	 * The name of this tag.
	 */
	@NonNull protected final String name;

	/**
	 * Creates a new NamedTag object.
	 * 
	 * @param theName is the name associated with this tag.
	 */
	public NamedTag(@NonNull @Immutable final String theName) {
		name = theName.intern();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof NamedTag) {
			final NamedTag _t = (NamedTag) o;
			_result = (this == o) || (name == _t.name) || ((name != null) && name.equals(_t.name));
		}
		return _result;
	}

	/**
	 * Returns the name of this tag as provided at it's creation.
	 * 
	 * @return the name of this tag.
	 */
	@Functional @NonNull public final String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public final byte[] getValue() {
		return name.getBytes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public int hashCode() {
		int _result = 17;
		_result = _result * 37 + name.hashCode();
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @Override public String toString() {
		return super.toString() + "[name = " + name + "]";
	}
}

// End of File
