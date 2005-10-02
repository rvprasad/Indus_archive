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
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getClass(java.lang.String)
	 */
	public SootClass getClass(final String className) {
		return system.getSootClass(className);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getClasses()
	 */
	public Collection<SootClass> getClasses() {
		return system.getClasses();
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getRoots()
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
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#hasClass(java.lang.String)
	 */
	public boolean hasClass(final String scName) {
		return system.containsClass(scName);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#removeClass(soot.SootClass)
	 */
	public void removeClass(final SootClass clazz) {
		system.removeClass(clazz);
	}
}

// End of File
