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

import edu.ksu.cis.indus.common.ToStringBasedComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.ValueBox;

import soot.jimple.Stmt;

/**
 * This class generates unique id's for each part of the system represented as Jimple. The most atomic parts are program
 * points or <code>ValueBox</code> soot types. Given a system containing a set of classes, this generator will generate the
 * different id's for the same class, method, field, local, statement, or program point over different runs. The user should
 * use an external controller with deterministic traversal order to ensure that the id's for same entities are identical over
 * different runs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class UniqueJimpleIDGenerator
		implements IJimpleIDGenerator {

	/**
	 * This maps classes to a sequence of fields that occur in it. This is used to generate unique id.
	 */
	private final Map<SootClass, List<SootField>> class2fields = new HashMap<SootClass, List<SootField>>();

	/**
	 * This maps classes to a sequence of methods that occur in it. This is used to generate unique id.
	 * 
	 * @invariant class2methods.oclIsKindOf(Map(SootClass, Sequence(SootMethod)))
	 */
	private final Map<SootClass, List<SootMethod>> class2methods = new HashMap<SootClass, List<SootMethod>>();

	/**
	 * This is a sequence of classes. This is used to generate unique id.
	 */
	private List<SootClass> classes = new ArrayList<SootClass>();

	/**
	 * This maps methods to a sequence of locals that occur in it. This is used to generate unique id.
	 * 
	 * @invariant method2locals.oclIsKindOf(Map(SootMethod, Sequence(Local)))
	 */
	private final Map<SootMethod, List<Local>> method2locals = new HashMap<SootMethod, List<Local>>();

	/**
	 * Creates an instance of this class.
	 */
	public UniqueJimpleIDGenerator() {
		super();
	}

	/**
	 * Sorts the given elements based on the return value of <code>toString()</code> call on them.
	 * 
	 * @param <T> the type of elements in the collection.
	 * @param collection to be sorted.
	 * @return the sorted collection.
	 * @pre collection != null
	 * @post result != null and result.containsAll(collection) and collection.containsAll(result)
	 */
	private static <T> List<T> sort(final Collection<T> collection) {
		final List<T> _result = new ArrayList<T>(collection);
		Collections.sort(_result, ToStringBasedComparator.SINGLETON);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForClass(SootClass)
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
		final SootClass _declClass = field.getDeclaringClass();
		final List<SootField> _fields = class2fields.get(_declClass);

		if (class2fields.containsKey(_declClass)) {
			@SuppressWarnings("unchecked") final List<SootField> _f = new ArrayList<SootField>(_declClass.getFields());
			class2fields.put(_declClass, sort(_f));
		}

		return getIdForClass(_declClass) + "_f" + _fields.indexOf(field);
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForLocal(Local, SootMethod)
	 */
	public String getIdForLocal(final Local v, final SootMethod method) {
		if (!method2locals.containsKey(method)) {
			@SuppressWarnings("unchecked") final List<Local> _l = new ArrayList<Local>(method.getActiveBody().getLocals());
			method2locals.put(method, sort(_l));
		}
		final List<Local> _locals = method2locals.get(method);
		return getIdForMethod(method) + "_l" + _locals.indexOf(v);
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForMethod(SootMethod)
	 */
	public String getIdForMethod(final SootMethod method) {
		final SootClass _declClass = method.getDeclaringClass();

		if (!class2methods.containsKey(_declClass)) {
			@SuppressWarnings("unchecked") final List<SootMethod> _c = new ArrayList<SootMethod>(_declClass.getMethods());
			class2methods.put(_declClass, sort(_c));
		}
		
		final List<SootMethod> _methods = class2methods.get(_declClass);
		return getIdForClass(method.getDeclaringClass()) + "_m" + _methods.indexOf(method);
	}

	/**
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForStmt(soot.jimple.Stmt, soot.SootMethod)
	 */
	public String getIdForStmt(final Stmt stmt, final SootMethod method) {
		String _result = "?";

		if (method.isConcrete()) {
			@SuppressWarnings("unchecked") final Collection<Stmt> _c = method.getActiveBody().getUnits();
			int _count = 0;

			for (final Iterator<Stmt> _i = _c.iterator(); _i.hasNext();) {
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
	 * @see edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator#getIdForValueBox(ValueBox, Stmt, SootMethod)
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
		class2methods.clear();
		classes.clear();
	}
}

// End of File
