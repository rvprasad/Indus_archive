
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

/**
 * This class contains utility methods and fields concerning token and type system management logic.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TokenUtil {
	/** 
	 * The name of the property the user can use to configure the token manager class.
	 */
	public static final String INDUS_STATICANALYSES_TOKENMANAGERTYPE = "edu.ksu.cis.indus.staticanalyses.TokenManagerClass";

	///CLOVER:OFF

	/**
	 * <i>Prevents creation instances of this class.</i>
	 */
	private TokenUtil() {
	}

	///CLOVER:ON

	/**
	 * Retrieves a token manager based on the value of the system property "indus.staticanalyses.TokenManagerClass".  The
	 * user can specify the name of the class of the manager to instanstiate via this property.  It has to be one of the
	 * following.
	 * 
	 * <ul>
	 * <li>
	 * edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager
	 * </li>
	 * <li>
	 * edu.ksu.cis.indus.staticanalyses.tokens.CollectionTokenManager
	 * </li>
	 * <li>
	 * edu.ksu.cis.indus.staticanalyses.tokens.IntegerTokenManager
	 * </li>
	 * </ul>
	 * 
	 * <p>
	 * By default, an instance of <code>edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager</code> is returned.
	 * </p>
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
	public static ITokenManager getTokenManager(final ITypeManager typeManager) {
		ITokenManager _tokenMgr = null;
		final String _tmType = System.getProperty(INDUS_STATICANALYSES_TOKENMANAGERTYPE);

		if (_tmType != null) {
			if (_tmType.equals(CollectionTokenManager.class.getName())) {
				_tokenMgr = new CollectionTokenManager(typeManager);
			} else if (_tmType.equals(IntegerTokenManager.class.getName())) {
				_tokenMgr = new IntegerTokenManager(typeManager);
			}
		}

		if (_tokenMgr == null) {
			_tokenMgr = new BitSetTokenManager(typeManager);
		}
		return _tokenMgr;
	}
}

// End of File
