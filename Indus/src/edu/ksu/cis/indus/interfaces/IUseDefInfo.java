
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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;

import soot.Local;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;


/**
 * This interface will be used to retrieve use-def information of a system.
 * 
 * <p>
 * Subtypes of this class have to return the constant <code>ID</code> defined in this class as a result of
 * <code>getId</code>.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IUseDefInfo
  extends IStatus,
	  IIdentification {
	/** 
	 * This is one of the ID of this interface.
	 */
	String ALIASED_USE_DEF_ID = "Aliased Use-Def Information";

	/** 
	 * This is the ID of this interface.
	 */
	String LOCAL_USE_DEF_ID = "Local Use-Def Information";

	/**
	 * Retrieves the def sites that reach the given use site in the given context.
	 *
	 * @param useStmt is the statement containing the use site.
	 * @param context in which the use-site occurs.
	 *
	 * @return a collection of def sites.
	 *
	 * @pre useStmt != null
	 */
	Collection getDefs(Stmt useStmt, Object context);

	/**
	 * Retrieves the def sites that reach the given local at the given use site in the given context.
	 *
	 * @param local for which the definition is requested.
	 * @param useStmt is the statement containing the use site.
	 * @param context in which the use-site occurs.
	 *
	 * @return a collection of def sites.
	 *
	 * @pre local != null and useStmt != null
	 */
	Collection getDefs(Local local, Stmt useStmt, Object context);

	/**
	 * Retrieves the use sites that reach the given def site in the given context.
	 *
	 * @param defStmt is the statement containing the def site.
	 * @param context in which the def-site occurs.
	 *
	 * @return a collection of use sites.
	 *
	 * @pre defStmt != null
	 */
	Collection getUses(DefinitionStmt defStmt, Object context);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2004/07/11 14:17:41  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.3  2004/02/08 19:08:03  venku
   - documentation
   Revision 1.2  2003/12/13 02:28:54  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/08 12:20:40  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.9  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
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
