
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

import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class provides generic framework and support required by analyses (DA) to calculate dependence information.  It is
 * adviced that specific analyses extend this class.
 * 
 * <p>
 * It is an abstract class as it does not implement the method that actually does the analysis. Also, it contains member data
 * that are necessary to store any sort dependency information. However, it is the responsibility of the subclasses to store
 * the data and provide the same via concrete implementation of abstract methods.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant doesPreProcessing() implies getPreProcessor() != null
 * @invariant getPreProcessing() != null implies doesPreProcessing()
 */
public abstract class DependencyAnalysis
  extends AbstractAnalysis {
	/**
	 * This identifies class-level data dependency analysis.
	 */
	public static final Object REFERENCE_BASED_DATA_DA = "REFERENCE_BASED_DATA_DA";

	/**
	 * This identifies control dependency analysis.
	 */
	public static final Object CONTROL_DA = "CONTROL_DA";

	/**
	 * This identifies divergence dependency analysis.
	 */
	public static final Object DIVERGENCE_DA = "DIVERGENCE_DA";

	/**
	 * This identifies inteference dependency analysis.
	 */
	public static final Object INTERFERENCE_DA = "INTERFERENCE_DA";

	/**
	 * This identifies identifier based data dependency analysis.
	 */
	public static final Object IDENTIFIER_BASED_DATA_DA = "IDENTIFIER_BASED_DATA_DA";

	/**
	 * This identifies ready dependency analysis.
	 */
	public static final Object READY_DA = "READY_DA";

	/**
	 * This identifies synchronization dependency analysis.
	 */
	public static final Object SYNCHRONIZATION_DA = "SYNCHRONIZATION_DA";

	/**
	 * This can used to store dependent->dependee direction of dependence information.  Hence, it is recommended that the
	 * subclass use this store dependence information.
	 *
	 * @invariant dependeeMap != null
	 */
	protected final Map dependeeMap = new HashMap();

	/**
	 * This is similar to <code>dependeeMap</code> except the direction is dependee->dependent. Hence, it is recommended that
	 * the subclass use this store dependence information.
	 *
	 * @invariant dependentMap != null
	 */
	protected final Map dependentMap = new HashMap();

	/**
	 * This indicates if the information provided by this analysis has stablized.
	 */
	protected boolean stable;

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
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * Resets all internal data structures.  General protocol is that data acquired via setup is not reset or forgotten.
	 *
	 * @post dependeeMap.size() == 0 and dependentMap.size() == 0
	 */
	public void reset() {
		dependeeMap.clear();
		dependentMap.clear();
		super.reset();
	}
}

/*
   ChangeLog:
   $Log$
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
