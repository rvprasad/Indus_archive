
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

import soot.ArrayType;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.ValueBox;

import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class UniqueJimpleIDGenerator
  implements IJimpleIDGenerator {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map class2fields = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map method2locals = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private List classes = new ArrayList();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private List tempList = new ArrayList();

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getNewClassId()
	 */
	public String getIdForClass(SootClass clazz) {
		if (!classes.contains(clazz)) {
			classes.add(clazz);
		}
		return "c" + String.valueOf(classes.indexOf(clazz));
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForField(soot.SootField)
	 */
	public String getIdForField(SootField field) {
		List fields = (List) class2fields.get(field.getDeclaringClass());
		String result;

		if (fields == null) {
			fields = new ArrayList();
			class2fields.put(field.getDeclaringClass(), fields);
		}

		if (!fields.contains(field)) {
			fields.add(field);
		}
		result = getIdForClass(field.getDeclaringClass()) + "_f" + fields.indexOf(field);
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForLocal(soot.Local)
	 */
	public String getIdForLocal(Local v, SootMethod method) {
		List locals = (List) method2locals.get(method);
		String result;

		if (locals == null) {
			locals = new ArrayList();
			method2locals.put(method, locals);
		}

		if (!locals.contains(v)) {
			locals.add(v);
		}
		result = getIdForMethod(method) + "_l" + locals.indexOf(v);
		return result;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getNewMethodId()
	 */
	public String getIdForMethod(SootMethod method) {
		SootClass sc = method.getDeclaringClass();
		return getIdForClass(sc) + "_m" + sc.getMethods().indexOf(method);
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForStmt(soot.jimple.Stmt)
	 */
	public String getIdForStmt(Stmt stmt, SootMethod method) {
		String result = "?";

		if (method.isConcrete()) {
			PatchingChain c = method.getActiveBody().getUnits();
			tempList.clear();
			tempList.addAll(c);
			result = String.valueOf(tempList.indexOf(stmt));
		}
		return getIdForMethod(method) + "_s" + result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForType(soot.Type)
	 */
	public String getIdForType(Type type) {
		String result;

		if (type instanceof RefType) {
			result = getIdForClass(((RefType) type).getSootClass());
		} else if (type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType) type;
			StringBuffer t = new StringBuffer(getIdForType(arrayType.baseType));
			t.append(".." + arrayType.numDimensions);
			result = t.toString();
		} else {
			result = type.toString().replaceAll("[\\[\\]]", "_.").replaceAll("\\p{Blank}", "");
		}
		return result;
	}

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
	public String getIdForValueBox(ValueBox box, Stmt stmt, SootMethod method) {
		List vBoxes = stmt.getUseAndDefBoxes();
		return getIdForStmt(stmt, method) + "_v" + vBoxes.indexOf(box);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void reset() {
		method2locals.clear();
		class2fields.clear();
		classes.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/11/28 09:40:55  venku
   - id generation for types changed.

   Revision 1.3  2003/11/26 18:25:32  venku
   - modified the id returned for types.

   Revision 1.2  2003/11/17 15:57:03  venku
   - removed support to retrieve new statement ids.
   - added support to retrieve id for value boxes.
   Revision 1.1  2003/11/16 18:37:42  venku
   - renamed UniqueIDGenerator to UniqueJimpleIDGenerator.
   Revision 1.1  2003/11/07 11:14:44  venku
   - Added generator class for xmlizing purpose.
   - XMLizing of Jimple works, but takes long.
     Probably, reachable method dump should fix it.  Another rainy day problem.
 */
