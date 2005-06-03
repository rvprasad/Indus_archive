
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

package edu.ksu.cis.indus.slicer.transformations;

import edu.ksu.cis.indus.interfaces.IClassHierarchy;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.staticanalyses.impl.ClassHierarchy;

import edu.ksu.cis.indus.tools.slicer.processing.ExecutableSlicePostProcessor;

import java.util.Collection;


/**
 * This implementation modifies the class hierarchy to minimize the number of required classes.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExecutableSlicePostProcessorAndModifier
  extends ExecutableSlicePostProcessor {
	/** 
	 * The environment on which this processor is operating.
	 */
	private final IEnvironment environment;

	/**
	 * Creates an instance of this class.
	 *
	 * @param env on which this processor should operate on.
	 *
	 * @pre env != null
	 */
	public ExecutableSlicePostProcessorAndModifier(final IEnvironment env) {
		environment = env;
	}

	/**
	 * @see ExecutableSlicePostProcessor#getClassHierarchyContainingClasses(Collection)
	 */
	protected IClassHierarchy getClassHierarchyContainingClasses(final Collection classes) {
		final ClassHierarchy _ch = ClassHierarchy.createClassHierarchyFrom(classes);
		_ch.confine(classes, true);
		_ch.updateEnvironment();
		return _ch;
	}
}

// End of File
