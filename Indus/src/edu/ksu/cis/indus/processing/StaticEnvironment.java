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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;

/**
 * This implementation provides an environment that is static in terms of the classes it contains.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class StaticEnvironment
		implements IEnvironment {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StaticEnvironment.class);

	/**
	 * The scene/system being represented.
	 */
	private final Collection<SootClass> classes;

	/**
	 * Creates a new Environment object.
	 * 
	 * @param classesInThisEnv the set of classes to be exposed in this environment.
	 * @pre classesInThisEnv != null
	 */
	public StaticEnvironment(final Collection<SootClass> classesInThisEnv) {
		classes = new HashSet<SootClass>(classesInThisEnv);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getClass(java.lang.String)
	 */
	public SootClass getClass(@SuppressWarnings("unused") final String className) {
		final String _msg = "This operation is not supported by this implementation.";
		LOGGER.error("getClass() -  : _msg = " + _msg);
		throw new UnsupportedOperationException(_msg);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getClasses()
	 */
	public Collection<SootClass> getClasses() {
		return Collections.unmodifiableCollection(classes);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#getRoots()
	 */
	public Collection getRoots() {
		final Collection<SootMethod> _temp = new HashSet<SootMethod>();
		final List<ArrayType> _argList = new ArrayList<ArrayType>();
		_argList.add(ArrayType.v(RefType.v("java.lang.String"), 1));

		for (final Iterator<SootClass> _i = classes.iterator(); _i.hasNext();) {
			final SootClass _sc = _i.next();
			final SootMethod _sm = _sc.getMethod("main", _argList, VoidType.v());

			if (_sm != null && _sm.isStatic() && _sm.isPublic()) {
				_temp.add(_sm);
			}
		}
		return null;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#hasClass(java.lang.String)
	 */
	public boolean hasClass(final String scName) {
		final Iterator<SootClass> _i = classes.iterator();
		final int _iEnd = classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();

			if (_sc.getName().equals(scName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEnvironment#removeClass(soot.SootClass)
	 */
	public void removeClass(@SuppressWarnings("unused") final SootClass clazz) {
		final String _msg = "This operation is not supported by this implementation.";
		LOGGER.error("getClass() -  : _msg = " + _msg);
		throw new UnsupportedOperationException(_msg);
	}
}

// End of File
