
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.staticanalyses.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class contains utility methods and fields concerning token and type system management logic.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TokenUtil {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenUtil.class);

	///CLOVER:OFF

	/**
	 * <i>Prevents creation instances of this class.</i>
	 */
	@Empty private TokenUtil() {
		//does nothing
	}

	///CLOVER:ON

	/**
	 * Retrieves a token manager based on the value of returned by
	 * <code>edu.ksu.cis.indus.staticanalyses.Constants.getTokenMgrType()</code>.
	 * @param <T> DOCUMENT ME!
	 * @param <V> DOCUMENT ME!
	 * @param <R> DOCUMENT ME!
	 *
	 * @param typeManager being used by the user.
	 *
	 * @return a token manager.
	 *
	 * @pre typeManager != null
	 * @post result != null
	 * @post result.oclIsKindOf(BitSetTokenManager) or result.oclIsKindOf(CollectionTokenManager) or
	 * 		 result.oclIsKindOf(IntegerTokenManager)
	 */
	public static <T extends ITokens<T, V>, V, R> ITokenManager<T, V, R> getTokenManager(final ITypeManager<R, V> typeManager) {
		ITokenManager<T, V, R> _tokenMgr = null;
		final String _tmType = Constants.getTokenManagerType();

		try {
			final Class _class = TokenUtil.class.getClassLoader().loadClass(_tmType);
			final Constructor _ctstr = _class.getConstructor(new Class[] { ITypeManager.class });

			if (_ctstr != null) {
				_tokenMgr = (ITokenManager) _ctstr.newInstance(new Object[] { typeManager });
			}
		} catch (final InstantiationException _e) {
			LOGGER.error("getTokenManager() - Unable to creat an instance of the given token manager class. : _tmType = "
				+ _tmType, _e);

			final Error _t = new InstantiationError();
			_t.initCause(_e);
			throw _t;
		} catch (final IllegalAccessException _e) {
			LOGGER.error("getTokenManager() - Unable to access the contructor of the given token manager class. : _tmType = "
				+ _tmType, _e);

			final Error _t = new IllegalAccessError();
			_t.initCause(_e);
			throw _t;
		} catch (final ClassNotFoundException _e) {
			LOGGER.error("getTokenManager() - Unable to find the given token manager class. : _tmType = " + _tmType, _e);

			final Error _t = new NoClassDefFoundError();
			_t.initCause(_e);
			throw _t;
		} catch (final NoSuchMethodException _e) {
			LOGGER.error("getTokenManager() - security exception. : _tmType = " + _tmType, _e);

			final Error _t = new NoSuchMethodError();
			_t.initCause(_e);
			throw _t;
		} catch (final InvocationTargetException _e) {
			LOGGER.error("getTokenManager() - the constructor threw an exception. : _tmType = " + _tmType, _e);

			final Error _t = new NoSuchMethodError();
			_t.initCause(_e);
			throw _t;
		}

		return _tokenMgr;
	}
}

// End of File
