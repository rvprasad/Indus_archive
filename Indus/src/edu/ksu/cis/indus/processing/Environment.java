
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * This class wraps Soot's <code>Scene</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Environment
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
	public Collection getClasses() {
		return system.getClasses();
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getRoots()
	 */
	public Collection getRoots() {
		Collection result = new HashSet();
		List argList = new ArrayList();
		argList.add(ArrayType.v(RefType.v("java.lang.String"), 1));

		for (Iterator i = system.getClasses().iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();
			SootMethod sm = sc.getMethod("main", argList, VoidType.v());

			if (sm != null && sm.isStatic() && sm.isPublic()) {
				result.add(sm);
			}
		}
		return null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/06 07:06:57  venku
   - a wrapper to expose Scene as an IEnvironment.

 */
