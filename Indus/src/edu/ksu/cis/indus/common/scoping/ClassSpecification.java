
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

package edu.ksu.cis.indus.common.scoping;

/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class ClassSpecification {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private AccessSpecification accessSpec;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private TypeSpecification typeSpec;

	/**
	 * Sets the value of <code>typeSpec</code>.
	 *
	 * @param typeSpec the new value of <code>typeSpec</code>.
	 */
	void setTypeSpec(TypeSpecification typeSpec) {
		this.typeSpec = typeSpec;
	}

	/**
	 * Retrieves the value in <code>typeSpec</code>.
	 *
	 * @return the value in <code>typeSpec</code>.
	 */
	TypeSpecification getTypeSpec() {
		return typeSpec;
	}
}

// End of File
