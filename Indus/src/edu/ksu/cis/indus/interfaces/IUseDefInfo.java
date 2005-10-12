
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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;

import soot.Local;
import soot.SootMethod;

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
 * @param <D> DOCUMENT ME!
 * @param <U> DOCUMENT ME!
 */
public interface IUseDefInfo<D, U>
  extends IStatus,
	  IIdentification {
	/** 
	 * This is an ID of this interface. This is used in conjuction with instance-based reference use-def info.
	 */
	Comparable  ALIASED_USE_DEF_ID = "Aliased Use-Def Information";

	/** 
	 * This is an ID of this interface. This is used in conjuction with class-based reference use-def info.
	 */
	Comparable  GLOBAL_USE_DEF_ID = "Global Use-Def Information";

	/** 
	 * This is an ID of this interface. This is used in conjuction with method local variable use-def info.
	 */
	Comparable LOCAL_USE_DEF_ID = "Local Use-Def Information";

	/**
	 * Retrieves the def sites that reach the given use site in the given context.
	 *
	 * @param useStmt is the statement containing the use site.
	 * @param method in which the use-site occurs.
	 *
	 * @return a collection of def sites.
	 *
	 * @pre useStmt != null
	 */
	Collection<D> getDefs(Stmt useStmt, SootMethod method);

	/**
	 * Retrieves the def sites that reach the given local at the given use site in the given context.
	 *
	 * @param local for which the definition is requested.
	 * @param useStmt is the statement containing the use site.
	 * @param method in which the use-site occurs.
	 *
	 * @return a collection of def sites.
	 *
	 * @throws UnsupportedOperationException when the implementation does not support this operation.
	 *
	 * @pre local != null and useStmt != null
	 */
	Collection<D> getDefs(Local local, Stmt useStmt, SootMethod method)
	  throws UnsupportedOperationException;

	/**
	 * Retrieves the use sites that reach the given def site in the given context.
	 *
	 * @param defStmt is the statement containing the def site.
	 * @param method in which the def-site occurs.
	 *
	 * @return a collection of use sites.
	 *
	 * @pre defStmt != null
	 */
	Collection<U> getUses(DefinitionStmt defStmt, SootMethod method);
}

// End of File
