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

package edu.ksu.cis.indus.processing;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;

/**
 * This class wraps Soot's <code>Scene</code> class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Environment
		implements IEnvironment {

	/**
	 * The scene/system being represented.
	 */
	private final Scene system;

	/**
	 * Creates a new Environment object.
	 * 
	 * @param scene to be wrapped by this object.
	 */
	public Environment(final Scene scene) {
		system = scene;
	}

	/**
	 * {@inheritDoc}
	 */
	public SootClass getClass(final String className) {
		if (!system.containsClass(className)) {
			system.loadClassAndSupport(className);
		}
		return system.getSootClass(className);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<SootClass> getClasses() {
		return system.getClasses();
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<SootMethod> getRoots() {
		final Collection<SootMethod> _temp = new HashSet<SootMethod>();
		final List<ArrayType> _argList = Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1));

		for (final Iterator<SootClass> _i = getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = _i.next();
			final SootMethod _sm = _sc.getMethod("main", _argList, VoidType.v());

			if (_sm != null && _sm.isStatic() && _sm.isPublic()) {
				_temp.add(_sm);
			}
		}
		return _temp;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasClass(final String scName) {
		return system.containsClass(scName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeClass(final SootClass clazz) {
		system.removeClass(clazz);
	}
}

// End of File
