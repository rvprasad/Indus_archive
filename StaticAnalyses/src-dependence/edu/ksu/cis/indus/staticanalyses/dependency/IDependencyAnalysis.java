
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.interfaces.IIdentification;

import java.util.Collection;


/**
 * The interface to dependency analysis information.
 * 
 * <p>
 * Subtypes of this class have to return one of the XXXX_DA constants defined in this class as a result of
 * <code>getId</code>.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDependencyAnalysis
  extends IIdentification {
	/** 
	 * This identifies backward directional analaysis.
	 */
	Object BACKWARD_DIRECTION = "BACKWARD_DIRECTION";

	/** 
	 * This identifies control dependency analysis.
	 */
	Object CONTROL_DA = "CONTROL_DA";

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

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/07/11 14:17:39  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.4  2004/07/11 11:45:54  venku
   - renamed constants.
   Revision 1.3  2004/07/11 09:42:13  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.2  2004/05/21 22:30:53  venku
   - documentation.
   Revision 1.1  2004/05/14 09:02:57  venku
   - refactored:
     - The ids are available in IDependencyAnalysis, but their collection is
       available via a utility class, DependencyAnalysisUtil.
     - DependencyAnalysis will have a sanity check via Unit Tests.
   - ripple effect.
 */
