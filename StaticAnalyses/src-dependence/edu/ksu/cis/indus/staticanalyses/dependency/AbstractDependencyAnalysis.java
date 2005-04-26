
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.Constants;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * This class provides generic framework and support required by analyses (DA) to calculate dependence information.  It is
 * adviced that specific analyses extend this class.
 * 
 * <p>
 * It is an abstract class as it does not implement the method that actually does the analysis. Also, it contains member data
 * that are necessary to store any sort dependency information. However, it is the responsibility of the subclasses to store
 * the data and provide the same via concrete implementation of abstract methods.  It is required to call
 * <code>initialize()</code> before any processing is triggered on the analysis.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant doesPreProcessing() implies getPreProcessor() != null
 * @invariant getPreProcessing() != null implies doesPreProcessing()
 */
public abstract class AbstractDependencyAnalysis
  extends AbstractAnalysis
  implements IDependencyAnalysis {
	/** 
	 * The collection of dependence analysis identifiers.
	 */
	public static final Collection IDENTIFIERS;

	static {
		IDENTIFIERS = new HashSet();
		IDENTIFIERS.add(CONTROL_DA);
		IDENTIFIERS.add(IDENTIFIER_BASED_DATA_DA);
		IDENTIFIERS.add(REFERENCE_BASED_DATA_DA);
		IDENTIFIERS.add(SYNCHRONIZATION_DA);
		IDENTIFIERS.add(DIVERGENCE_DA);
		IDENTIFIERS.add(INTERFERENCE_DA);
		IDENTIFIERS.add(READY_DA);
	}

	/** 
	 * This is similar to <code>dependent2dependee</code> except the direction is dependee->dependent. Hence, it is
	 * recommended that the subclass use this store dependence information.
	 *
	 * @invariant dependee2dependent != null
	 */
	protected final Map dependee2dependent = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This can used to store dependent->dependee direction of dependence information.  Hence, it is recommended that the
	 * subclass use this store dependence information.
	 *
	 * @invariant dependent2dependee != null
	 */
	protected final Map dependent2dependee = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This manages pair objects.
	 */
	private PairManager pairMgr;

	/**
	 * Return the entities on which the <code>dependent</code> depends on in the given <code>context</code>.
	 *
	 * @param dependent of interest.
	 * @param context in which the dependency information is requested.
	 *
	 * @return a collection of objects.
	 *
	 * @pre dependent != null
	 * @post result != null
	 */
	public abstract Collection getDependees(final Object dependent, final Object context);

	/**
	 * Returns the entities which depend on the <code>dependee</code> in the given <code>context</code>.
	 *
	 * @param dependee of interest.
	 * @param context in which the dependency information is requested.
	 *
	 * @return a collection of objects.  The subclasses will further specify the  types of these entities.
	 *
	 * @pre dependee != null
	 * @post result != null
	 */
	public abstract Collection getDependents(final Object dependee, final Object context);

	/**
	 * Resets all internal data structures.  General protocol is that data acquired via setup is not reset or forgotten.
	 *
	 * @post dependent2dependee.size() == 0 and dependee2dependent.size() == 0
	 */
	public void reset() {
		dependent2dependee.clear();
		dependee2dependent.clear();
		super.reset();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws InitializationException when pair manager is not provided.
	 *
	 * @pre info.get(PairManager.ID) != null and info.get(PairManager.ID).oclIsTypeOf(PairManager)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in the info.");
		}
	}

	/**
	 * Retrieves an object that can be used key that represents the combination of <code>entity</code> and
	 * <code>method</code>.
	 *
	 * @param entity to be represented.
	 * @param method to be represented.
	 *
	 * @return a key object.
	 *
	 * @post result != null
	 */
	Object getKeyFor(final Object entity, final Object method) {
		return pairMgr.getPair(entity, method);
	}
}

// End of File
