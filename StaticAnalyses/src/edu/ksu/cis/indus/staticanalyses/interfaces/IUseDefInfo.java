
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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import edu.ksu.cis.indus.interfaces.IStatus;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;


/**
 * This interface will be used to retrieve use-def information of a system.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IUseDefInfo
  extends IStatus {
	/**
	 * This is the ID of this interface.
	 */
	String ID = "Aliased Use-Def Information";

	/**
	 * Retrieves the def sites that reach the given use site in the given context.
	 *
	 * @param useStmt is the statement containing the use site.
	 * @param context in which the use-site occurs.
	 *
	 * @return a collection of def sites.
	 *
	 * @pre usesStmt != null and context != null
	 */
	Collection getDefs(Stmt useStmt, Context context);

	/**
	 * Retrieves the use sites that reach the given def site in the given context.
	 *
	 * @param defStmt is the statement containing the def site.
	 * @param context in which the def-site occurs.
	 *
	 * @return a collection of use sites.
	 *
	 * @pre defStmt != null and context != null
	 */
	Collection getUses(DefinitionStmt defStmt, Context context);
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2003/11/12 03:50:13  venku
   - getDefs operates on statements and
     getUses operates on Def statements.
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:08:03  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/21 03:32:37  venku
   Incorporated IStatus interface into any interface that provides analysis information.
   Revision 1.4  2003/08/13 08:49:10  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosed later on in implementaions.
   Revision 1.3  2003/08/12 01:52:00  venku
   Removed redundant final in parameter declaration in methods of interfaces.
   Revision 1.2  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.
   Revision 1.1  2003/08/09 23:26:20  venku
   - Added an interface to provide use-def information.
   - Added an implementation to the above interface.
   - Extended call graph processor to retrieve call tree information rooted at arbitrary node.
   - Modified IValueAnalyzer interface such that only generic queries are possible.
     If required, this can be extended in the future.
 */
