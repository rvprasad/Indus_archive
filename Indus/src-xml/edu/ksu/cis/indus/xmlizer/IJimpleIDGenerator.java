
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
 * This interface is used to retrieve id's for various parts of a java system represented in Soot.  This is typically useful
 * in serialization of Jimple for various purposes as testing and visualization of analysis data.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IJimpleIDGenerator {
	/**
	 * Retrieves an id for the given class.
	 *
	 * @param clazz is the class for which the id is requested for.
	 *
	 * @return an id.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	String getIdForClass(SootClass clazz);

	/**
	 * Retrieves an id for the given field.
	 *
	 * @param field for which the id is requested for.
	 *
	 * @return an id.
	 *
	 * @pre field != null
	 * @post result != null
	 */
	String getIdForField(SootField field);

	/**
	 * Retrieves an id for the given local in the given method.
	 *
	 * @param local for which the id is requested for.
	 * @param method in which <code>local</code> occurs.
	 *
	 * @return an id.
	 *
	 * @pre local != null and method != null
	 * @post result != null
	 */
	String getIdForLocal(Local local, SootMethod method);

	/**
	 * Retrieves an id for the given method.
	 *
	 * @param method for which the id is requested for.
	 *
	 * @return an id.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	String getIdForMethod(SootMethod method);

	/**
	 * Retrieves an id for the given statement in the given method.
	 *
	 * @param stmt for which the id is requested for.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return an id.
	 *
	 * @pre stmt != null and method != null
	 * @post result != null
	 */
	String getIdForStmt(Stmt stmt, SootMethod method);

	/**
	 * Retrieves an id for the given type.
	 *
	 * @param type for which the id is requested for.
	 *
	 * @return an id.
	 *
	 * @pre type != null
	 * @post result != null
	 */
	String getIdForType(Type type);

	/**
	 * Retrieves an id for the given program point in the given statement in the given method.
	 *
	 * @param programPoint for which the id is requested for.
	 * @param stmt in which <code>programPoint</code> occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return an id.
	 *
	 * @pre programPoint != null and stmt != null and method != null
	 * @post result != null
	 */
	String getIdForValueBox(ValueBox programPoint, Stmt stmt, SootMethod method);

	/**
	 * Resets the generator.
	 */
	void reset();
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.6  2003/12/02 09:42:24  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
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
