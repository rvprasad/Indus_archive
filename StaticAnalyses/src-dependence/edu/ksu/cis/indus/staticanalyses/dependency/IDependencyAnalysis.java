
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

import edu.ksu.cis.indus.interfaces.IIdentification;
import edu.ksu.cis.indus.interfaces.IStatus;

import java.util.Collection;


/**
 * The interface to dependency analysis information.
 * 
 * <p>
 * Subtypes of this class have to return one of the XXXX_DA constants defined in this class as a result of <code>getId</code>
 * and one of <code>BACKWARD_DIRECTION, BI_DIRECTIONAL, FORWARD_DIRECTION,</code> and <code>DIRECTIONLESS</code> as a result
 * of <code>getDirection</code>.
 * </p>
 * 
 * <p>
 * There is room for confusion due to <code>BI_DIRECTIONAL</code> behavior of an analysis.  To avoid this, it is required
 * that in a bi-directional analysis implement <code>getDependees</code> and <code>getDependents</code> to provide dependee
 * and dependent info, respectively, in backward  direction while <code>getDependents</code> and <code>getDependees</code>
 * provide dependee and dependent info, respectively, in forward direction.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDependencyAnalysis
  extends IIdentification,
	  IStatus {
	/** 
	 * This identifies backward directional analaysis.
	 */
	Object BACKWARD_DIRECTION = "BACKWARD_DIRECTION";

	/** 
	 * This identifies bi-directional dependence analysis.  This means that if such an analysis is designed for forward
	 * direction then the backward direction information can be obtained by switching <code>getDependees()</code> and
	 * <code>getDependents()</code>.
	 */
	Object BI_DIRECTIONAL = "BI_DIRECTIONAL";

	/** 
	 * This identifies control dependency analysis.
	 */
	Object CONTROL_DA = "CONTROL_DA";

	/** 
	 * This identifies directionless dependence analysis.  This means that the dependence information is directionless.  That
	 * is, <code>getDependees</code> and <code>getDependents</code> will return the same values independent of the
	 * direction.
	 */
	Object DIRECTIONLESS = "DIRECTIONLESS";

	/** 
	 * This identifies divergence dependency analysis.
	 */
	Object DIVERGENCE_DA = "DIVERGENCE_DA";

	/** 
	 * This identifies forward directional analaysis.
	 */
	Object FORWARD_DIRECTION = "FORWARD_DIRECTION";

	/** 
	 * This identifies identifier based data dependency analysis.
	 */
	Object IDENTIFIER_BASED_DATA_DA = "IDENTIFIER_BASED_DATA_DA";

	/** 
	 * This identifies inteference dependency analysis.
	 */
	Object INTERFERENCE_DA = "INTERFERENCE_DA";

	/** 
	 * This identifies ready dependency analysis.
	 */
	Object READY_DA = "READY_DA";

	/** 
	 * This identifies class-level data dependency analysis.
	 */
	Object REFERENCE_BASED_DATA_DA = "REFERENCE_BASED_DATA_DA";

	/** 
	 * This identifies synchronization dependency analysis.
	 */
	Object SYNCHRONIZATION_DA = "SYNCHRONIZATION_DA";

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
	Collection getDependees(final Object dependent, final Object context);

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
	Collection getDependents(final Object dependee, final Object context);

	/**
	 * Returns the direction of the analysis.  This has to be one of the XXXX_DIRECTION constants defined in this class.
	 * There can be two types of analyses.  In analysis that are <code>BACKWARD_DIRECTION</code> oriented, the dependent is
	 * related to the dependee via the dependence against the flow of control.  In analysis that are
	 * <code>FORWARD_DIRECTION</code> oriented, the dependent is related to the dependee via the dependence along the flow
	 * of control.
	 *
	 * @return the direction of the implementation.
	 *
	 * @post result != null
	 */
	Object getDirection();

	/**
	 * Retrieves dependence analysis that provides indirect dependence information corresponding to this dependence analysis.
	 *
	 * @return a dependence analysis.
	 *
	 * @post result != null
	 */
	IDependencyAnalysis getIndirectVersionOfDependence();

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	boolean isStable();

	/**
	 * Resets all internal data structures.  General protocol is that data acquired via setup is not reset or forgotten.
	 *
	 * @post dependent2dependee.size() == 0 and dependee2dependent.size() == 0
	 */
	void reset();
}

// End of File
