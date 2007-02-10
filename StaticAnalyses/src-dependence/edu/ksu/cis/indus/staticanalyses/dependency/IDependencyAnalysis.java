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

import edu.ksu.cis.indus.interfaces.IIdentification;
import edu.ksu.cis.indus.interfaces.IStatus;
import edu.ksu.cis.indus.staticanalyses.interfaces.IAnalysis;

import java.util.Collection;

/**
 * The interface to dependency analysis information.
 * <p>
 * There is room for confusion due to <code>BI_DIRECTIONAL</code> behavior of an analysis. To avoid this, it is required
 * that in a bi-directional analysis implement <code>getDependees</code> and <code>getDependents</code> to provide
 * dependee and dependent info, respectively, in backward direction while <code>getDependents</code> and
 * <code>getDependees</code> provide dependee and dependent info, respectively, in forward direction.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <E1> is the type of dependee object in the context of dependee-to-dependent info maintenance.
 * @param <C1> is the type of context object in the context of dependee-to-dependent info maintenance.
 * @param <T1> is the type of dependent object in the context of dependee-to-dependent info maintenance.
 * @param <T2> is the type of dependent object in the context of dependent-to-dependee info maintenance.
 * @param <C2> is the type of context object in the context of dependent-to-dependee info maintenance.
 * @param <E2> is the type of dependee object in the context of dependent-to-dependee info maintenance.
 */
public interface IDependencyAnalysis<T1, C1, E1, E2, C2, T2>
		extends IIdentification, IStatus, IAnalysis {

	/**
	 * The enumeration of sorts of dependences.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	enum DependenceSort {
		/**
		 * This identifies control dependency analysis.
		 */
		CONTROL_DA,

		/**
		 * This identifies divergence dependency analysis.
		 */
		DIVERGENCE_DA,

		/**
		 * This identifies identifier based data dependency analysis.
		 */
		IDENTIFIER_BASED_DATA_DA,

		/**
		 * This identifies inteference dependency analysis.
		 */
		INTERFERENCE_DA,

		/**
		 * This identifies ready dependency analysis.
		 */
		READY_DA,

		/**
		 * This identifies class-level data dependency analysis.
		 */
		REFERENCE_BASED_DATA_DA,

		/**
		 * This identifies synchronization dependency analysis.
		 */
		SYNCHRONIZATION_DA
	}

	/**
	 * The enumeration of the dependence direction.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	enum Direction {
		/**
		 * This identifies backward directional analaysis.
		 */
		BACKWARD_DIRECTION,
		/**
		 * This identifies bi-directional dependence analysis. This means that if such an analysis is designed for forward
		 * direction then the backward direction information can be obtained by switching <code>getDependees()</code> and
		 * <code>getDependents()</code>.
		 */
		BI_DIRECTIONAL,

		/**
		 * This identifies forward directional analaysis.
		 */
		FORWARD_DIRECTION
	};

	/**
	 * Return the entities on which the <code>dependent</code> depends on in the given <code>context</code>.
	 * 
	 * @param dependent of interest.
	 * @param context in which the dependency information is requested.
	 * @return a collection of objects.
	 * @pre dependent != null
	 * @post result != null
	 */
	Collection<E1> getDependees(final T1 dependent, final C1 context);

	/**
	 * Returns the entities which depend on the <code>dependee</code> in the given <code>context</code>.
	 * 
	 * @param dependee of interest.
	 * @param context in which the dependency information is requested.
	 * @return a collection of objects. The subclasses will further specify the types of these entities.
	 * @pre dependee != null
	 * @post result != null
	 */
	Collection<T2> getDependents(final E2 dependee, final C2 context);

	/**
	 * Returns the direction of the analysis. In analysis that are <code>BACKWARD_DIRECTION</code> oriented, the dependent
	 * is related to the dependee via the dependence against the flow of control. In analysis that are
	 * <code>FORWARD_DIRECTION</code> oriented, the dependent is related to the dependee via the dependence along the flow
	 * of control. Analysis that are direction independent should return <code>BI_DIRECTIONAL</code>.
	 * 
	 * @return the direction of the implementation.
	 * @post result != null
	 */
	Direction getDirection();

	/**
	 * Retrieves dependence analysis that provides indirect dependence information corresponding to this dependence analysis.
	 * 
	 * @return a dependence analysis.
	 * @post result != null
	 */
	IDependencyAnalysis<T1, C1, E1, E2, C2, T2> getIndirectVersionOfDependence();

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	boolean isStable();

	/**
	 * Resets all internal data structures. General protocol is that data acquired via setup is not reset or forgotten.
	 */
	void reset();

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	Collection<IDependencyAnalysis.DependenceSort> getIds();
}

// End of File
