
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

import edu.ksu.cis.indus.common.Constants;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;

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

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/08/15 08:37:26  venku
   - REFACTORING pertaining to feature request #426
     - refactored dependence retriever interface.
     - refactored direction sensitive dependence information creation.

   Revision 1.6  2004/08/08 10:11:35  venku
   - added a new class to configure constants used when creating data structures.
   - ripple effect.
   Revision 1.5  2004/07/20 06:36:12  venku
   - documentation.
   - deleted BI_DIRECTIONAL as it is rarely true that a dependence is bi-directional.
   Revision 1.4  2004/07/11 14:17:39  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.3  2004/07/11 09:42:13  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.2  2004/05/14 09:02:57  venku
   - refactored:
     - The ids are available in IDependencyAnalysis, but their collection is
       available via a utility class, DependencyAnalysisUtil.
     - DependencyAnalysis will have a sanity check via Unit Tests.
   - ripple effect.
   Revision 1.1  2004/05/14 06:27:24  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.17  2004/03/03 10:11:40  venku
   - formatting.
   Revision 1.16  2004/03/03 10:07:24  venku
   - renamed dependeeMap as dependent2dependee
   - renamed dependentmap as dependee2dependent
   Revision 1.15  2004/02/09 16:50:56  venku
    - formatting.
   Revision 1.14  2004/02/09 16:50:36  venku
   - added a cache of collection of dependence ids.
   Revision 1.13  2003/12/16 06:53:04  venku
   - documentation.
   Revision 1.12  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.11  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.10  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.9  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.8  2003/09/12 22:33:08  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.7  2003/08/25 09:30:41  venku
   Renamed AliasedDataDA to ReferenceBasedDataDA.
   Renamed NonAliasedDataDA to IdentifierBasedDataDA.
   Renamed the IDs for the above analyses.
   Revision 1.6  2003/08/25 08:43:51  venku
   Moved XXX_DA constants from SimpleController to here.
   Revision 1.5  2003/08/16 02:41:37  venku
   Renamed AController to AbstractController.
   Renamed AAnalysis to AbstractAnalysis.
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
