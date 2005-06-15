
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import soot.SootClass;


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
	 * The names of the classes to be retained.
	 *
	 * @invariant retain.oclIsKindOf(Collection(String))
	 */
	private final Collection retain;

	/** 
	 * The environment on which this processor is operating.
	 */
	private final IEnvironment environment;

	/**
	 * Creates an instance of this class.
	 *
	 * @param env on which this processor should operate on.
	 * @param classesToRetain is the collection of FQN of classes that need to be retained in the slice.
	 *
	 * @pre env != null and classesToRetain != null and classesToRetain.oclIsKindOf(Collection(String))
	 */
	public ExecutableSlicePostProcessorAndModifier(final IEnvironment env, final Collection classesToRetain) {
		environment = env;
		retain = classesToRetain;
	}

	/**
	 * @see ExecutableSlicePostProcessor#getClassHierarchyContainingClasses(Collection)
	 */
	protected IClassHierarchy getClassHierarchyContainingClasses(final Collection classes) {
		final Collection _cl = new ArrayList(classes);
		final ClassHierarchy _ch = ClassHierarchy.createClassHierarchyFrom(_cl);
		final Iterator _i = retain.iterator();
		final int _iEnd = retain.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final String _scName = (String) _i.next();

			if (environment.hasClass(_scName)) {
				final SootClass _sc = environment.getClass(_scName);
				collector.includeInSlice(_sc);
				_cl.add(_sc);
			}
		}
		_ch.confine(_cl, true);
		_ch.updateEnvironment();
		return _ch;
	}
}

// End of File
