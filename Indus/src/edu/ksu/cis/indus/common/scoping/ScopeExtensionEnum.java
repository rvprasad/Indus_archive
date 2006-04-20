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
 * The enumeration to identify various scope extensions.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public enum ScopeExtensionEnum {
	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	EXCLUSIVE_ANCESTORS,

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	EXCLUSIVE_DESCENDANTS,

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	INCLUSIVE_ANCESTORS,

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	INCLUSIVE_DESCENDANTS,

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	IDENTITY,

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	PRIMITIVE;
}
