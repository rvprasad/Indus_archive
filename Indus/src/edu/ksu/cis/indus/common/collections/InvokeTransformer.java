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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <I> DOCUMENT ME!
 * @param <O> DOCUMENT ME!
 */
public class InvokeTransformer<I, O>
		implements ITransformer<I, O> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(InvokeTransformer.class);

	/**
	 * 
	 */
	private final String name;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param methodName
	 */
	public InvokeTransformer(final String methodName) {
		super();
		name = methodName;
	}

	/**
	 * @see edu.ksu.cis.indus.common.collections.ITransformer#transform(I)
	 */
	public O transform(final I input) {
		final Class<?> _t = input.getClass();
		final Method _m;
		try {
			_m = _t.getMethod(name, (Class[]) null);
			return (O) _m.invoke(input, (Object[]) null);
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
