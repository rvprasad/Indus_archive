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
 * @version $Revision$ $Date$
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
