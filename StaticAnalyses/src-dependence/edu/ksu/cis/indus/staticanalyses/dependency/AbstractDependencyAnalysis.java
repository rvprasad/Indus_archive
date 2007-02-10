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
 * This class provides generic framework and support required by analyses (DA) to calculate dependence information. It is
 * adviced that specific analyses extend this class.
 * <p>
 * It is an abstract class as it does not implement the method that actually does the analysis. Also, it contains member data
 * that are necessary to store any sort dependency information. However, it is the responsibility of the subclasses to store
 * the data and provide the same via concrete implementation of abstract methods. It is required to call
 * <code>initialize()</code> before any processing is triggered on the analysis.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <E1> is the type of dependee object in the context of dependee-to-dependent info maintenance.
 * @param <C1> is the type of context object in the context of dependee-to-dependent info maintenance.
 * @param <T1> is the type of dependent object in the context of dependee-to-dependent info maintenance.
 * @param <KE> is the type of key object in the context of dependee-to-dependent info maintenance.
 * @param <VT> is the type of value object in the context of dependee-to-dependent info maintenance.
 * @param <T2> is the type of dependent object in the context of dependent-to-dependee info maintenance.
 * @param <C2> is the type of context object in the context of dependent-to-dependee info maintenance.
 * @param <E2> is the type of dependee object in the context of dependent-to-dependee info maintenance.
 * @param <KT> is the type of key object in the context of dependent-to-dependee info maintenance.
 * @param <VE> is the type of value object in the context of dependent-to-dependee info maintenance.
 * @invariant doesPreProcessing() implies getPreProcessor() != null
 * @invariant getPreProcessing() != null implies doesPreProcessing()
 */
public abstract class AbstractDependencyAnalysis<T1, C1, E1, KT, VE, E2, C2, T2, KE, VT>
		extends AbstractAnalysis
		implements IDependencyAnalysis<T1, C1, E1, E2, C2, T2> {

	/**
	 * The collection of dependence analysis identifiers.
	 */
	public static final Collection<Comparable<?>> IDENTIFIERS;

	static {
		IDENTIFIERS = new HashSet<Comparable<?>>();
		IDENTIFIERS.add(DependenceSort.CONTROL_DA);
		IDENTIFIERS.add(DependenceSort.IDENTIFIER_BASED_DATA_DA);
		IDENTIFIERS.add(DependenceSort.REFERENCE_BASED_DATA_DA);
		IDENTIFIERS.add(DependenceSort.SYNCHRONIZATION_DA);
		IDENTIFIERS.add(DependenceSort.DIVERGENCE_DA);
		IDENTIFIERS.add(DependenceSort.INTERFERENCE_DA);
		IDENTIFIERS.add(DependenceSort.READY_DA);
	}

	/**
	 * This is similar to <code>dependent2dependee</code> except the direction is dependee->dependent. Hence, it is
	 * recommended that the subclass use this store dependence information.
	 * 
	 * @invariant dependee2dependent != null
	 */
	protected final Map<KE, VT> dependee2dependent = new HashMap<KE, VT>(Constants.getNumOfMethodsInApplication());

	/**
	 * This can used to store dependent->dependee direction of dependence information. Hence, it is recommended that the
	 * subclass use this store dependence information.
	 * 
	 * @invariant dependent2dependee != null
	 */
	protected final Map<KT, VE> dependent2dependee = new HashMap<KT, VE>(Constants.getNumOfMethodsInApplication());

	/**
	 * This manages pair objects.
	 */
	private PairManager pairMgr;

	/**
	 * The direction of the analysis.
	 */
	private final Direction theDirection;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param direction of the analysis.
	 */
	public AbstractDependencyAnalysis(final Direction direction) {
		super();
		theDirection = direction;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDirection()
	 */
	public final Direction getDirection() {
		return theDirection;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis<T1, C1, E1, E2, C2, T2> getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis<T1, C1, E1, KT, VE, E2, C2, T2, KE, VT>(this, getDependenceRetriever());
	}

	/**
	 * Resets all internal data structures. General protocol is that data acquired via setup is not reset or forgotten.
	 * 
	 * @post dependent2dependee.size() == 0 and dependee2dependent.size() == 0
	 */
	@Override public void reset() {
		dependent2dependee.clear();
		dependee2dependent.clear();
		super.reset();
	}

	/**
	 * Provides the dependence retriever.
	 * 
	 * @return the dependence retriever.
	 */
	protected abstract IDependenceRetriever<T1, C1, E1, E2, C2, T2> getDependenceRetriever();

	/**
	 * {@inheritDoc}
	 * 
	 * @throws InitializationException when pair manager is not provided.
	 * @pre info.get(PairManager.ID) != null and info.get(PairManager.ID).oclIsTypeOf(PairManager)
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	@Override protected void setup() throws InitializationException {
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
	 * @return a key object.
	 * @post result != null
	 */
	Object getKeyFor(final Object entity, final Object method) {
		return pairMgr.getPair(entity, method);
	}
}

// End of File
