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
	 */
	private final Collection<String> retain;

	/**
	 * The environment on which this processor is operating.
	 */
	private final IEnvironment environment;

	/**
	 * Creates an instance of this class.
	 *
	 * @param env on which this processor should operate on.
	 * @param classesToRetain is the collection of FQN of classes that need to be retained in the slice.
	 * @pre env != null and classesToRetain != null and classesToRetain.oclIsKindOf(Collection(String))
	 */
	public ExecutableSlicePostProcessorAndModifier(final IEnvironment env, final Collection<String> classesToRetain) {
		environment = env;
		retain = classesToRetain;
	}

	/**
	 * @see ExecutableSlicePostProcessor#getClassHierarchyContainingClasses(Collection)
	 */
	@Override protected IClassHierarchy getClassHierarchyContainingClasses(final Collection<SootClass> classes) {
		final Collection<SootClass> _cl = new ArrayList<SootClass>(classes);
		final ClassHierarchy _ch = ClassHierarchy.createClassHierarchyFrom(_cl);
		final Iterator<String> _i = retain.iterator();
		final int _iEnd = retain.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final String _scName = _i.next();

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
