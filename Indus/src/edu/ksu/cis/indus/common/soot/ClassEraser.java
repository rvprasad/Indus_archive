
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.util.Chain;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
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
	private static final Log LOGGER = LogFactory.getLog(ClassEraser.class);

	/** 
	 * The collection of classes to erase.
	 *
	 * @invariant classesToErase.oclIsKindOf(Collection(SootClass))
	 */
	private final Collection classesToErase;

	/** 
	 * A work bag cache.
	 */
	private final IWorkBag wbCache = new LIFOWorkBag();

	/**
	 * Creates an instance of this class.
	 *
	 * @param classes to be erased.
	 *
	 * @pre classes.oclIsKindOf(Collection(SootClass))
	 */
	ClassEraser(final Collection classes) {
		classesToErase = classes;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ClassEraser(classes = " + classes + ") - Classes to be erased");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		super.callback(clazz);

		wbCache.clear();

		if (clazz.hasSuperclass()) {
			final SootClass _superClass = clazz.getSuperclass();

			if (classesToErase.contains(_superClass)) {
				wbCache.addWork(new Pair(clazz, _superClass));
			}
		}

		final Iterator _i = clazz.getInterfaces().iterator();
		final int _iEnd = clazz.getInterfaces().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _intr = (SootClass) _i.next();

			if (classesToErase.contains(_intr)) {
				wbCache.addWork(new Pair(clazz, _intr));
			}
		}

		while (wbCache.hasWork()) {
			final Pair _p = (Pair) wbCache.getWork();
			final SootClass _clazz = (SootClass) _p.getFirst();
			final SootClass _superClass = (SootClass) _p.getSecond();

			if (_superClass.isInterface()) {
				final Collection _interfaces = _clazz.getInterfaces();
				_interfaces.remove(_superClass);

				final Chain _superSuperInterfaces = _superClass.getInterfaces();
				_interfaces.addAll(_superSuperInterfaces);

				final Iterator _j = _superSuperInterfaces.iterator();
				final int _jEnd = _superSuperInterfaces.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final SootClass _superSuperInterface = (SootClass) _j.next();

					if (classesToErase.contains(_superSuperInterface)) {
						wbCache.addWork(new Pair(_clazz, _superSuperInterface));
					}
				}
			} else if (_superClass.hasSuperclass()) {
				final SootClass _superSuperClass = _superClass.getSuperclass();
				_clazz.setSuperclass(_superSuperClass);

				if (classesToErase.contains(_superSuperClass)) {
					wbCache.addWork(new Pair(_clazz, _superSuperClass));
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootField)
	 */
	public void callback(final SootField field) {
		super.callback(field);
		field.setType(getType(field.getType()));
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		super.callback(method);

		final List _c = new ArrayList();
		final Iterator _i = method.getExceptions().iterator();
		final int _iEnd = method.getExceptions().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = (SootClass) _i.next();
			_i.remove();
			_c.add(getClass(_sc));
		}

		method.getExceptions().addAll(_c);

		_c.clear();

		final Iterator _j = method.getParameterTypes().iterator();
		final int _jEnd = method.getParameterTypes().size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Type _type = (Type) _j.next();
			_j.remove();
			_c.add(_type);
		}
		method.setParameterTypes(_c);

		method.setReturnType(getType(method.getReturnType()));
	}

	/**
	 * @see AbstractProcessor#callback(ValueBox, Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		super.callback(vBox, context);

		final Value _v = vBox.getValue();

		if (_v instanceof Local) {
			final Local _l = (Local) _v;
			_l.setType(getType(_l.getType()));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(ProcessingController ppc) {
		ppc.registerForAllValues(this);
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(ProcessingController ppc) {
		ppc.unregisterForAllValues(this);
		ppc.unregister(this);
	}

	/**
	 * Retrieves the class that can replace the given class.
	 *
	 * @param clazz that can be replaced.
	 *
	 * @return the replacement class.  This can be the same as <code>clazz</code>.
	 *
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
	 *
	 * @return the replacement class.  This can be the same as <code>type</code>.
	 *
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
			final Type _bType = (_aType).baseType;

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
