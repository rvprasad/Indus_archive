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
package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This transformer dynamically invokes a method on a input object and provides the return value as the result.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <R> is the type of receiver object of the invoked method.
 * @param <O> is the type of return value from the invoked method.
 */
public class InvokeTransformer<R, O>
		implements ITransformer<R, O> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(InvokeTransformer.class);

	/**
	 * 
	 */
	private final String name;

	/**
	 * Creates an instance of this class. The method should return a value.
	 * 
	 * @param methodName is the name of the method to be invoked.
	 */
	public InvokeTransformer(@NonNull final String methodName) {
		super();
		name = methodName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public O transform(@NonNull final R input) {
		final Class<?> _t = input.getClass();
		final Method _m;
		try {
			_m = _t.getMethod(name, (Class[]) null);
			@SuppressWarnings("unchecked") final O _invoke = (O) _m.invoke(input, (Object[]) null);
			return _invoke;
		} catch (final SecurityException _e) {
			LOGGER.error("Not enough permission to invoke method " + name + " on object " + input.toString(), _e);
			throw new RuntimeException(_e);
		} catch (final NoSuchMethodException _e) {
			LOGGER.error("Method with name " + name + " does not exist in type " + input.getClass(), _e);
			throw new RuntimeException(_e);
		} catch (final IllegalArgumentException _e) {
			LOGGER.error("Illegal argument to invoke method named " + name + " on object " + input.toString(), _e);
			throw new RuntimeException(_e);
		} catch (final IllegalAccessException _e) {
			LOGGER.error("Illegal access to method " + name + " on class " + input.getClass(), _e);
			throw new RuntimeException(_e);
		} catch (final InvocationTargetException _e) {
			LOGGER.error("Error invoking method " + name + " on object " + input.getClass(), _e);
			throw new RuntimeException(_e);
		}
	}
}

// End of File
