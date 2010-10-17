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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
	@NonNullContainer @NonNull private final Collection<SootClass> classesToErase;

	/**
	 * A work bag cache.
	 */
	@NonNullContainer private final IWorkBag<Pair<SootClass, SootClass>> wbCache = new LIFOWorkBag<Pair<SootClass, SootClass>>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param classes to be erased.
	 */
	ClassEraser(@NonNull @NonNullContainer @Immutable final Collection<SootClass> classes) {
		classesToErase = new HashSet<SootClass>(classes);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ClassEraser(classes = " + classes + ") - Classes to be erased");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@NonNull final SootClass clazz) {
		super.callback(clazz);

		wbCache.clear();

		if (clazz.hasSuperclass()) {
			final SootClass _superClass = clazz.getSuperclass();

			if (classesToErase.contains(_superClass)) {
				wbCache.addWork(new Pair<SootClass, SootClass>(clazz, _superClass));
			}
		}

		@SuppressWarnings("unchecked") final Iterator<SootClass> _i = clazz.getInterfaces().iterator();
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
				@SuppressWarnings("unchecked") final Collection<SootClass> _interfaces = _clazz.getInterfaces();
				_interfaces.remove(_superClass);

				@SuppressWarnings("unchecked") final Collection<SootClass> _superSuperInterfaces = _superClass
						.getInterfaces();
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
	 * {@inheritDoc}
	 */
	@Override public void callback(@NonNull final SootField field) {
		super.callback(field);
		field.setType(getType(field.getType()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@NonNull final SootMethod method) {
		super.callback(method);

		final List<Object> _c = new ArrayList<Object>();
		@SuppressWarnings("unchecked") final Iterator<SootClass> _i = method.getExceptions().iterator();
		final int _iEnd = method.getExceptions().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();
			_i.remove();
			_c.add(getClass(_sc));
		}

		method.getExceptions().addAll(_c);

		_c.clear();

		@SuppressWarnings("unchecked") final Iterator<Type> _j = method.getParameterTypes().iterator();
		final int _jEnd = method.getParameterTypes().size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Type _type = _j.next();
			_j.remove();
			_c.add(getType(_type));
		}
		method.setParameterTypes(_c);

		method.setReturnType(getType(method.getReturnType()));

		if (method.hasActiveBody()) {
			@SuppressWarnings("unchecked") final Collection<Trap> _traps = new HashSet<Trap>(method.getActiveBody()
					.getTraps());

			final Iterator<Trap> _k = _traps.iterator();
			final int _kEnd = _traps.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final Trap _t = _k.next();
				_t.setException(getClass(_t.getException()));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(@Immutable @NonNull final ValueBox vBox, @Immutable @NonNull final Context context) {
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
	 * {@inheritDoc}
	 */
	public void hookup(@NonNull @Immutable final ProcessingController ppc) {
		ppc.registerForAllValues(this);
		ppc.register(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unhook(@NonNull @Immutable final ProcessingController ppc) {
		ppc.unregisterForAllValues(this);
		ppc.unregister(this);
	}

	/**
	 * Retrieves the class that can replace the given class.
	 * 
	 * @param clazz that can be replaced.
	 * @return the replacement class. This can be the same as <code>clazz</code>.
	 */
	private SootClass getClass(@NonNull final SootClass clazz) {
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
	 */
	@NonNull @Functional private Type getType(@NonNull final Type type) {
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
