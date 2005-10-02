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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ParameterRef;


/**
 * This processor can be used to erase a collection of classes along with their references. References to the erased class are
 * replaced by a reference to the parent class of the erased class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class ClassEraser
		extends AbstractProcessor {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassEraser.class);

	/**
	 * The collection of classes to erase.
	 */
	private final Collection<SootClass> classesToErase;

	/**
	 * A work bag cache.
	 */
	private final IWorkBag<Pair<SootClass, SootClass>> wbCache = new LIFOWorkBag<Pair<SootClass, SootClass>>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param classes to be erased.
	 */
	ClassEraser(final Collection<SootClass> classes) {
		classesToErase = classes;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ClassEraser(classes = " + classes + ") - Classes to be erased");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootClass)
	 */
	@SuppressWarnings("unchecked") @Override public void callback(final SootClass clazz) {
		super.callback(clazz);

		wbCache.clear();

		if (clazz.hasSuperclass()) {
			final SootClass _superClass = clazz.getSuperclass();

			if (classesToErase.contains(_superClass)) {
				wbCache.addWork(new Pair<SootClass, SootClass>(clazz, _superClass));
			}
		}

		final Iterator<SootClass> _i = clazz.getInterfaces().iterator();
		final int _iEnd = clazz.getInterfaces().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _intr = _i.next();

			if (classesToErase.contains(_intr)) {
				wbCache.addWork(new Pair<SootClass, SootClass>(clazz, _intr));
			}
		}

		while (wbCache.hasWork()) {
			final Pair<SootClass, SootClass> _p = wbCache.getWork();
			final SootClass _clazz = _p.getFirst();
			final SootClass _superClass = _p.getSecond();

			if (_superClass.isInterface()) {
				final Collection<SootClass> _interfaces = _clazz.getInterfaces();
				_interfaces.remove(_superClass);

				final Collection<SootClass> _superSuperInterfaces = _superClass.getInterfaces();
				_interfaces.addAll(_superSuperInterfaces);

				final Iterator<SootClass> _j = _superSuperInterfaces.iterator();
				final int _jEnd = _superSuperInterfaces.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final SootClass _superSuperInterface = _j.next();

					if (classesToErase.contains(_superSuperInterface)) {
						wbCache.addWork(new Pair<SootClass, SootClass>(_clazz, _superSuperInterface));
					}
				}
			} else if (_superClass.hasSuperclass()) {
				final SootClass _superSuperClass = _superClass.getSuperclass();
				_clazz.setSuperclass(_superSuperClass);

				if (classesToErase.contains(_superSuperClass)) {
					wbCache.addWork(new Pair<SootClass, SootClass>(_clazz, _superSuperClass));
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootField)
	 */
	@Override public void callback(final SootField field) {
		super.callback(field);
		field.setType(getType(field.getType()));
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootMethod)
	 */
	@SuppressWarnings("unchecked") @Override public void callback(final SootMethod method) {
		super.callback(method);

		final List _c = new ArrayList();
		final Iterator<SootClass> _i = method.getExceptions().iterator();
		final int _iEnd = method.getExceptions().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();
			_i.remove();
			_c.add(getClass(_sc));
		}

		method.getExceptions().addAll(_c);

		_c.clear();

		final Iterator<Type> _j = method.getParameterTypes().iterator();
		final int _jEnd = method.getParameterTypes().size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Type _type = _j.next();
			_j.remove();
			_c.add(getType(_type));
		}
		method.setParameterTypes(_c);

		method.setReturnType(getType(method.getReturnType()));

		if (method.hasActiveBody()) {
			_c.clear();
			_c.addAll(method.getActiveBody().getTraps());

			@SuppressWarnings("unchecked") final Iterator<Trap> _k = _c.iterator();
			final int _kEnd = _c.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final Trap _t = _k.next();
				_t.setException(getClass(_t.getException()));
			}
		}
	}

	/**
	 * @see AbstractProcessor#callback(ValueBox, Context)
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
		super.callback(vBox, context);

		final Value _v = vBox.getValue();

		if (_v instanceof Local) {
			final Local _l = (Local) _v;
			_l.setType(getType(_l.getType()));
		} else if (_v instanceof ParameterRef) {
			final ParameterRef _l = (ParameterRef) _v;
			vBox.setValue(new ParameterRef(getType(_l.getType()), _l.getIndex()));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.registerForAllValues(this);
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllValues(this);
		ppc.unregister(this);
	}

	/**
	 * Retrieves the class that can replace the given class.
	 * 
	 * @param clazz that can be replaced.
	 * @return the replacement class. This can be the same as <code>clazz</code>.
	 * @pre clazz != null
	 */
	private SootClass getClass(final SootClass clazz) {
		SootClass _result = clazz;

		while (classesToErase.contains(_result)) {
			if (_result.hasSuperclass()) {
				_result = _result.getSuperclass();
			}
		}
		return _result;
	}

	/**
	 * Retrieves the type that can replace the given type.
	 * 
	 * @param type that can be replaced.
	 * @return the replacement class. This can be the same as <code>type</code>.
	 * @pre type != null
	 */
	private Type getType(final Type type) {
		Type _result = type;

		if (_result instanceof RefType) {
			SootClass _t = ((RefType) type).getSootClass();
			_t = getClass(_t);
			_result = _t.getType();
		} else if (type instanceof ArrayType) {
			final ArrayType _aType = (ArrayType) type;
			final Type _bType = _aType.baseType;

			if (_bType instanceof RefType) {
				SootClass _t = ((RefType) _bType).getSootClass();
				_t = getClass(_t);
				_result = ArrayType.v(_t.getType(), _aType.numDimensions);
			}
		}
		return _result;
	}
}

// End of File
