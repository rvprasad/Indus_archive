
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
 * This class provides the abstract implementation of <code>ITokenmanager</code>.  It is advised that all token managers
 * extend this class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractTokenManager
  implements ITokenManager {
	/**
	 * The type manager that manages the types of the tokens managed by this object.
	 *
	 * @invariant typeMgr != null
	 */
	protected final ITypeManager typeMgr;

	/**
	 * Creates an instance of this class.
	 *
	 * @param typeManager manages the types of the tokens managed by this object.
	 *
	 * @pre typeManager != null
	 */
	public AbstractTokenManager(final ITypeManager typeManager) {
		typeMgr = typeManager;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeManager()
	 */
	public ITypeManager getTypeManager() {
		return typeMgr;
	}
}

/*
   ChangeLog:
   $Log$
 */
