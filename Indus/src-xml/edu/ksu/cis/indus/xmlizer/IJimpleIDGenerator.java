
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

package edu.ksu.cis.indus.xmlizer;

import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * <p></p>
 * 
 * @version $Revision$ 
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 */
public interface IJimpleIDGenerator {
	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object getIdForNextStmt();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	Object getIdForStmt(Stmt stmt);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object getNewClassId();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object getNewMethodId();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object getNewStmtId();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object getNewValueId();
}

/*
   ChangeLog:
   $Log$
 */
