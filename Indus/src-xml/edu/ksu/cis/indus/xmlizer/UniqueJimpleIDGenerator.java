
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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


/**
 * This class generates unique id's for each part of the system represented as Jimple.  The most atomic parts are program
 * points or <code>ValueBox</code> soot types. Given a system containing a set of classes, this generator will generate the
 * different id's for the same class, method, field, local, statement, or program point over different runs. The user should
 * use an external controller with deterministic traversal order to ensure that the id's for same entities are identical
 * over different runs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class UniqueJimpleIDGenerator
  implements IJimpleIDGenerator {
	/**
	 * This maps classes to a sequence of fields that occur in it.  This is used to generate unique id.
	 *
	 * @invariant class2fields.oclIsKindOf(Map(SootClass, Sequence(SootField)))
	 */
	private final Map class2fields = new HashMap();

	/**
	 * This maps methods to a sequence of locals that occur in it.  This is used to generate unique id.
	 *
	 * @invariant method2locals.oclIsKindOf(Map(SootMethod, Sequence(Local)))
	 */
	private final Map method2locals = new HashMap();

	/**
	 * This is a sequence of classes.  This is used to generate unique id.
	 */
	private List classes = new ArrayList();

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getNewClassId()
	 */
	public String getIdForClass(final SootClass clazz) {
		if (!classes.contains(clazz)) {
			classes.add(clazz);
		}
		return "c" + String.valueOf(classes.indexOf(clazz));
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForField(soot.SootField)
	 */
	public String getIdForField(final SootField field) {
		List _fields = (List) class2fields.get(field.getDeclaringClass());
		String _result;

		if (_fields == null) {
			_fields = new ArrayList();
			class2fields.put(field.getDeclaringClass(), _fields);
		}

		if (!_fields.contains(field)) {
			_fields.add(field);
		}
		_result = getIdForClass(field.getDeclaringClass()) + "_f" + _fields.indexOf(field);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForLocal(soot.Local)
	 */
	public String getIdForLocal(final Local v, final SootMethod method) {
		List _locals = (List) method2locals.get(method);
		String _result;

		if (_locals == null) {
			_locals = new ArrayList();
			method2locals.put(method, _locals);
		}

		if (!_locals.contains(v)) {
			_locals.add(v);
		}
		_result = getIdForMethod(method) + "_l" + _locals.indexOf(v);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getNewMethodId()
	 */
	public String getIdForMethod(final SootMethod method) {
		final SootClass _sc = method.getDeclaringClass();
		return getIdForClass(_sc) + "_m" + _sc.getMethods().indexOf(method);
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForStmt(soot.jimple.Stmt)
	 */
	public String getIdForStmt(final Stmt stmt, final SootMethod method) {
		String _result = "?";

		if (method.isConcrete()) {
			final PatchingChain _c = method.getActiveBody().getUnits();
			int _count = 0;

			for (final Iterator _i = _c.iterator(); _i.hasNext();) {
				if (stmt == _i.next()) {
					break;
				}
				_count++;
			}
			_result = String.valueOf(_count);
		}
		return getIdForMethod(method) + "_s" + _result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForType(soot.Type)
	 */
	public String getIdForType(final Type type) {
		String _result;

		if (type instanceof RefType) {
			_result = getIdForClass(((RefType) type).getSootClass());
		} else if (type instanceof ArrayType) {
			final ArrayType _arrayType = (ArrayType) type;
			final StringBuffer _t = new StringBuffer(getIdForType(_arrayType.baseType));
			_t.append(".." + _arrayType.numDimensions);
			_result = _t.toString();
		} else {
			_result = type.toString().replaceAll("[\\[\\]]", "_.").replaceAll("\\p{Blank}", "");
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForType(ValueBox, Stmt, SootMethod)
	 */
	public String getIdForValueBox(final ValueBox box, final Stmt stmt, final SootMethod method) {
		final List _vBoxes = stmt.getUseAndDefBoxes();
		return getIdForStmt(stmt, method) + "_v" + _vBoxes.indexOf(box);
	}

	/**
	 * Resets the internal data structures.
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
   Revision 1.10  2003/12/28 00:44:15  venku
   - coding convention.
   Revision 1.9  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.8  2003/12/02 11:36:16  venku
   - coding convention.
   Revision 1.7  2003/12/02 09:42:24  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.6  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
   Revision 1.5  2003/11/30 09:44:53  venku
   - renamed getIdForValue to getIdForValueBox.
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
