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
	public boolean equals(Object object) {
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
