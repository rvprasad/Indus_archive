
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

// End of File
