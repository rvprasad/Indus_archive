
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

import edu.ksu.cis.indus.interfaces.IEnvironment;

import soot.SootClass;


/**
 * This class represents class-level scope specification.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class ClassSpecification
  extends AbstractSpecification {
	/** 
	 * This is the type specification.
	 */
	private TypeSpecification typeSpec;

	/**
	 * Checks if the given class is in the scope of this specification in the given environment.
	 *
	 * @param clazz to be checked for scope constraints.
	 * @param system in which the check the constraints.
	 *
	 * @return <code>true</code> if the given class lies within the scope defined by this specification; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre clazz != null and system != null
	 */
	public boolean isInScope(final SootClass clazz, final IEnvironment system) {
		return accessConformant(new AccessSpecifierWrapper(clazz)) && typeSpec.conformant(clazz.getType(), system);
	}

	/**
	 * Sets the value of <code>typeSpec</code>.
	 *
	 * @param theTypeSpec the new value of <code>typeSpec</code>.
	 */
	void setTypeSpec(final TypeSpecification theTypeSpec) {
		typeSpec = theTypeSpec;
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
