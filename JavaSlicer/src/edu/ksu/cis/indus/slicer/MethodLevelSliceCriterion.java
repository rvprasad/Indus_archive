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

package edu.ksu.cis.indus.slicer;

/**
 * This class represents method-level slice criterion. This class has support builtin for object pooling.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class MethodLevelSliceCriterion
		extends AbstractSliceCriterion {
	/**
	 * @see edu.ksu.cis.indus.slicer.AbstractSliceCriterion#equals(java.lang.Object)
	 */
	@Override public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof MethodLevelSliceCriterion)) {
			return false;
		}

		return super.equals(object);
	}
}

// End of File
