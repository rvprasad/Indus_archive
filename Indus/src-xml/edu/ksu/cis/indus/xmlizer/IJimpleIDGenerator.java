
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
import soot.ValueBox;

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
	 * @param clazz DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String getIdForClass(SootClass clazz);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param field DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String getIdForField(SootField field);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param v DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String getIdForLocal(Local v, SootMethod method);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
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
	 * <p></p>
	 *
	 * @param box DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String getIdForValueBox(ValueBox box, Stmt stmt, SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/11/30 09:44:53  venku
   - renamed getIdForValue to getIdForValueBox.
   Revision 1.3  2003/11/17 15:57:03  venku
   - removed support to retrieve new statement ids.
   - added support to retrieve id for value boxes.
   Revision 1.2  2003/11/07 11:14:44  venku
   - Added generator class for xmlizing purpose.
   - XMLizing of Jimple works, but takes long.
     Probably, reachable method dump should fix it.  Another rainy day problem.
   Revision 1.1  2003/11/07 06:27:03  venku
   - Made the XMLizer classes concrete by moving out the
     id generation logic outside.
   - Added an interface which provides the id required for
     xmlizing Jimple.
 */
