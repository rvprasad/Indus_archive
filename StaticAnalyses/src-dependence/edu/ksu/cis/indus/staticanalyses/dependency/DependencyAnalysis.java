
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
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
	 * Resets all internal data structures.
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
