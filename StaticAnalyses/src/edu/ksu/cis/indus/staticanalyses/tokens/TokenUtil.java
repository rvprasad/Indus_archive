
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
	 * The name of the property the user can use to configure the token manager type.
	 */
	public static final String INDUS_STATICANALYSES_TOKENMANAGERTYPE = "indus.staticanalyses.TokenManagerType";

	///CLOVER:OFF
    
    /**
     * <i>Prevents creation instances of this class.</i>
     */
	private TokenUtil() {
	}

	///CLOVER:ON

	/**
	 * Retrieves a token manager based on the value of the system property "indus.staticanalyses.TokenManagerType".
	 *
	 * @return a token manager.
	 *
	 * @post result != null
	 */
	public static ITokenManager getTokenManager() {
		ITokenManager _tokenMgr = null;
		final String _tmType = System.getProperty(INDUS_STATICANALYSES_TOKENMANAGERTYPE);

		if (_tmType != null) {
			if (_tmType.equals(CollectionTokenManager.class.getName())) {
				_tokenMgr = new CollectionTokenManager(new SootValueTypeManager());
			} else if (_tmType.equals(IntegerTokenManager.class.getName())) {
				_tokenMgr = new IntegerTokenManager(new SootValueTypeManager());
			}
		}

		if (_tokenMgr == null) {
			_tokenMgr = new BitSetTokenManager(new SootValueTypeManager());
		}
		return _tokenMgr;
	}
}

/*
   ChangeLog:
   $Log$
 */
