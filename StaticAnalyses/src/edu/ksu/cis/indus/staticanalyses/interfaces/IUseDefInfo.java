
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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.jimple.AssignStmt;

import edu.ksu.cis.indus.staticanalyses.Context;

import java.util.Collection;


/**
 * This interface will be used to retrieve use-def information of a system.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IUseDefInfo {
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
	Collection getDefs(AssignStmt useStmt, Context context);

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
	Collection getUses(AssignStmt defStmt, Context context);
}

/*
   ChangeLog:

   $Log$
   
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
