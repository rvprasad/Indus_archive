
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

import java.util.HashMap;
import java.util.Map;


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
	 * The mapping between types to the type based filter.
	 *
	 * @invariant type2filter.oclIsKindOf(Map(IType, ITokenFilter))
	 */
	private final Map type2filter = new HashMap();

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
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeBasedFilter(IType)
	 */
	public final ITokenFilter getTypeBasedFilter(final IType type) {
		ITokenFilter _result = (ITokenFilter) type2filter.get(type);

		if (_result == null) {
			_result = getNewFilterForType(type);
			type2filter.put(type, _result);
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeManager()
	 */
	public ITypeManager getTypeManager() {
		return typeMgr;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	public void reset() {
		typeMgr.reset();
		type2filter.clear();
	}

	/**
	 * Retrieves a new token filter for the given type.
	 *
	 * @param type for which the filter is requested.
	 *
	 * @return a new token filter.
	 *
	 * @pre type != null
	 */
	protected abstract ITokenFilter getNewFilterForType(final IType type);
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
 */
