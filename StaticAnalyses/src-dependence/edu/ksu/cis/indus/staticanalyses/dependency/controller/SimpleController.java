
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

package edu.ksu.cis.indus.staticanalyses.dependency.controller;

import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * An naive implementation of AbstractAnalysesController. This implementation will run the analyses in the order following
 * order: IntraProcedural Data dependency analysis, Control dependency analysis, Synchronization dependency analysis,
 * Interference dependency analysis, Ready dependency analysis, Divergence dependency analysis
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant participatingAnalyses.values()->forall(o | o.oclIsKindOf(DependencyAnalysis))
 */
public class SimpleController
  extends AbstractAnalysesController {
	/**
	 * This identifies class-level data dependency analysis.
	 */
	public static final Object CLASS_DATA_DA = "CLASS_DATA_DA";

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
	 * This identifies method local data dependency analysis.
	 */
	public static final Object METHOD_LOCAL_DATA_DA = "METHOD_LOCAL_DATA_DA";

	/**
	 * This identifies ready dependency analysis.
	 */
	public static final Object READY_DA = "READY_DA";

	/**
	 * This identifies synchronization dependency analysis.
	 */
	public static final Object SYNCHRONIZATION_DA = "SYNCHRONIZATION_DA";

	/**
	 * Creates a new SimpleController object.
	 *
	 * @param info is a map from name to objects which provide information that analyses may use, but is of no use to the
	 * 		  controller.
	 * @param pc is the preprocess controller.
	 */
	public SimpleController(final Map info, final ProcessingController pc) {
		super(info, pc);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/08/25 08:06:39  venku
   Renamed participatingAnalysesNames to participatingAnalysesIDs.
   AbstractAnalysesController now has a method to extract the above field.

   Revision 1.8  2003/08/25 07:28:01  venku
   Ripple effect of renaming AbstractController to AbstractAnalysesController.

   Revision 1.7  2003/08/18 00:59:11  venku
   Changed the type of the IDs to java.lang.Object to provide extensibility.
   Ripple effect of that happens in AbstractController.
   Revision 1.6  2003/08/16 02:41:37  venku
   Renamed AController to AbstractController.
   Renamed AAnalysis to AbstractAnalysis.
   Revision 1.5  2003/08/11 08:52:56  venku
   Moved an invariant from constructor to type level.
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
