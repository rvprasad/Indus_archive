
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

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IJimpleIDGenerator {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	String getIdForClass(SootClass clazz);

	/**
	 * DOCUMENT ME!
	 *
	 * @param field
	 *
	 * @return
	 */
	String getIdForField(SootField field);

	/**
	 * DOCUMENT ME!
	 *
	 * @param v
	 *
	 * @return
	 */
	String getIdForLocal(Local v, SootMethod method);

	/**
	 * DOCUMENT ME!
	 *
	 * @param method
	 *
	 * @return
	 */
	String getIdForMethod(SootMethod method);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String getIdForStmt(Stmt stmt, SootMethod method);

	/**
	 * DOCUMENT ME!
	 *
	 * @param type
	 *
	 * @return
	 */
	String getIdForType(Type type);

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	String getNewStmtId(SootMethod method);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	String getNewValueId(Stmt stmt, SootMethod method);

	/**
	 *
	 */
	void resetStmtCounter();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	void resetValueCounter();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/07 06:27:03  venku
   - Made the XMLizer classes concrete by moving out the
     id generation logic outside.
   - Added an interface which provides the id required for
     xmlizing Jimple.
 */
