
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.VirtualInvokeExpr;


/**
 * General utility class providing common chore methods.
 *
 * @author <a href="mailto:rvprasad@cis.ksu.edu">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class Util {
	/**
	 * A private constructor to prevent the instantiation of this class.
	 */
	private Util() {
	}

	/**
	 * Provides the class which injects the given method into the specific branch of the inheritence hierarchy which contains
	 * the given class.
	 *
	 * @param sc is the class in or above which the method may be defined.
	 * @param method is the name of the method (not the fully classified name).
	 * @param parameterTypes list of type of the parameters of the method.
	 * @param returnType return type of the method.
	 *
	 * @return if such a method exists, the class that injects the method is returned. <code>null</code> is returned,
	 * 		   otherwise.
	 *
	 * @pre sc != null and method != null and parameterTypes != null and returnType != null
	 * @pre parameterTypes->forall(o | o.oclIsKindOf(Type))
	 */
	public static SootClass getDeclaringClass(final SootClass sc, final String method, final List parameterTypes,
		final Type returnType) {
		SootClass _contains = sc;

		while (!_contains.declaresMethod(method, parameterTypes, returnType)) {
			if (_contains.hasSuperclass()) {
				_contains = _contains.getSuperclass();
			} else {
				_contains = null;
				break;
			}
		}

		return _contains;
	}

	/**
	 * Provides the class which injects the given method into the specific branch of the inheritence hierarchy which contains
	 * the given class.  This is a shorthand version of <code>Util.getDeclaringClass()</code> where the
	 * <code>parameterTypes</code> is empty and the returnType is <code>VoidType</code>.
	 *
	 * @param sc is the class in or above which the method may be defined.
	 * @param method is the name of the method (not the fully classified name).
	 *
	 * @return if such a method exists, the class that injects the method is returned. <code>null</code> is returned,
	 * 		   otherwise.
	 *
	 * @pre sc != null and method != null
	 */
	public static SootClass getDeclaringClassFromName(final SootClass sc, final String method) {
		return getDeclaringClass(sc, method, Collections.EMPTY_LIST, VoidType.v());
	}

	/**
	 * Checks if one class is the descendent of another.  It is assumed that a class cannot be it's own ancestor.
	 *
	 * @param child class whose ancestor is of interest.
	 * @param ancestor fully qualified name of the ancestor class.
	 *
	 * @return <code>true</code> if a class by the name of <code>ancestor</code> is indeed the ancestor of <code>child</code>
	 * 		   class; false otherwise.
	 *
	 * @pre child != null and ancestor != null
	 * @post result == (child.evaluationType().allSuperTypes()->forall(o | o.name() = ancestor))
	 */
	public static boolean isDescendentOf(final SootClass child, final String ancestor) {
		boolean _retval = false;
		SootClass _temp = child;

		while (!_retval) {
			if (_temp.getName().equals(ancestor)) {
				_retval = true;
			} else {
				if (_temp.hasSuperclass()) {
					_temp = _temp.getSuperclass();
				} else {
					break;
				}
			}
		}

		if (!_retval) {
			_retval = implementsInterface(child, ancestor);
		}

		return _retval;
	}

	/**
	 * Checks if one class is the descendent of another.  It is assumed that a class cannot be it's own ancestor.
	 *
	 * @param child class whose ancestor is of interest.
	 * @param ancestor the ancestor class.
	 *
	 * @return <code>true</code> if <code>ancestor</code> class is indeed the ancestor of <code>child</code> class; false
	 * 		   otherwise.
	 *
	 * @pre child != null and ancestor != null
	 * @post result == child.oclIsKindOf(ancestor)
	 */
	public static boolean isDescendentOf(final SootClass child, final SootClass ancestor) {
		return isDescendentOf(child, ancestor.getName());
	}

	/**
	 * Checks if the given classes are on the same class hierarchy branch.  This means either one of the classes should be
	 * the subclass of the other class.
	 *
	 * @param class1 one of the two classes to be checked for relation.
	 * @param class2 one of the two classes to be checked for relation.
	 *
	 * @return <code>true</code> if <code>class1</code> is reachable from <code>class2</code>; <code>false</code>, otherwise.
	 *
	 * @pre class1 != null and class2 != null
	 * @post result == (class1.oclIsKindOf(class2) or class2.oclIsKindOf(class1))
	 */
	public static boolean isHierarchicallyRelated(final SootClass class1, final SootClass class2) {
		return isDescendentOf(class1, class2.getName()) || isDescendentOf(class2, class1.getName());
	}

	/**
	 * Checks if type <code>t1</code> is the same/sub-type of type <code>t2</code>.
	 *
	 * @param t1 is the type to be checked for equivalence or sub typing.
	 * @param t2 is the type against which the check is conducted.
	 * @param env in which these types exists.
	 *
	 * @return <code>true</code> if <code>t1</code> is same as or sub type of <code>t2</code>; <code>false</code>, otherwise.
	 *
	 * @post result == t1.oclIsKindOf(t2)
	 */
	public static boolean isSameOrSubType(final Type t1, final Type t2, final IEnvironment env) {
		boolean _result = false;

		if (t1.equals(t2)) {
			_result = true;
		} else if (t1 instanceof RefType && t2 instanceof RefType) {
			_result =
				isDescendentOf(env.getClass(((RefType) t1).getClassName()), env.getClass(((RefType) t2).getClassName()));
		}
		return _result;
	}

	/**
	 * Checks if type <code>t1</code> is the same/sub-type of type <code>t2</code>.  For the typing relation to be true the
	 * types should have the same dimension and the base types should have the subtyping relation in requested direction.
	 *
	 * @param t1 is the array type to be checked for equivalence or sub typing.
	 * @param t2 is the array type against which the check is conducted.
	 * @param env in which these types exists.
	 *
	 * @return <code>true</code> if <code>t1</code> is same as or sub type of <code>t2</code>; <code>false</code>, otherwise.
	 *
	 * @post result == t1.oclIsKindOf(t2)
	 */
	public static boolean isSameOrSubType(final ArrayType t1, final ArrayType t2, final IEnvironment env) {
		boolean _result = false;

		if (t1.equals(t2)) {
			_result = true;
		} else if (t1.numDimensions == t2.numDimensions) {
			_result = isSameOrSubType(t1.baseType, t2.baseType, env);
		}
		return _result;
	}

	/**
	 * Fixes the body of <code>java.lang.Thread.start()</code> (only if it is native) to call <code>run()</code> on the
	 * target or self.  This is required to complete the call graph.  This leaves the body untouched if it is not native.
	 *
	 * @param scm is the scene in which the alteration occurs.
	 */
	public static void fixupThreadStartBody(final Scene scm) {
		boolean _flag = false;

		for (final Iterator _i = scm.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (Util.implementsInterface(_sc, "java.lang.Runnable")) {
				_flag = true;
				break;
			}
		}

		if (_flag) {
			final SootClass _sc = scm.getSootClass("java.lang.Thread");
			final SootMethod _sm = _sc.getMethodByName("start");
			Util.setThreadStartBody(_sm);
		}
	}

	/**
	 * Checks if the given class implements the named interface.
	 *
	 * @param child is the class to be tested for implementation.
	 * @param ancestor is the fully qualified name of the interface to be checked for implementation.
	 *
	 * @return <code>true</code> if <code>child</code> implements the named interface; <code>false</code>, otherwise.
	 *
	 * @post result == (child.evaluationType().allSuperTypes()->forall(o | o.name() = ancestor))
	 */
	public static boolean implementsInterface(final SootClass child, final String ancestor) {
		boolean _result = false;
		SootClass _temp = child;

		while (!_result && (_temp.getInterfaceCount() > 0 || _temp.hasSuperclass())) {
			if (_temp.implementsInterface(ancestor)) {
				_result = true;
			} else {
				_temp = _temp.getSuperclass();
			}
		}
		return _result;
	}

	/**
	 * Hooks in a <i>non-native</i><code>start</code> method to facilitate smooth callgraph construction.
	 *
	 * @param sm is the method to be changed.  It is changed only if the method is <code>java.lang.Thread.start</code>.
	 *
	 * @return <code>true</code> if the body of <code>sm</code> changed; <code>false</code>, otherwise.
	 */
	private static boolean setThreadStartBody(final SootMethod sm) {
		boolean _result = false;
		final SootClass _declClass = sm.getDeclaringClass();
		_declClass.setApplicationClass();

		if (sm.isNative()
			  && sm.getName().equals("start")
			  && sm.getParameterCount() == 0
			  && _declClass.getName().equals("java.lang.Thread")) {
			sm.setModifiers(sm.getModifiers() ^ Modifier.NATIVE);

			final Jimple _jimple = Jimple.v();
			final JimpleBody _threadStartBody = _jimple.newBody(sm);
			final PatchingChain _sl = _threadStartBody.getUnits();
			final Local _thisRef = _jimple.newLocal("$this", RefType.v(_declClass.getName()));
			_threadStartBody.getLocals().add(_thisRef);
			// adds $this := @this;
			_sl.addFirst(_jimple.newIdentityStmt(_thisRef, _jimple.newThisRef(RefType.v(_declClass))));

			// adds $this.virtualinvoke[java.lang.Thread.run()]:void;
			final VirtualInvokeExpr _ve =
				_jimple.newVirtualInvokeExpr(_thisRef, _declClass.getMethodByName("run"), Collections.EMPTY_LIST);
			_sl.addLast(_jimple.newInvokeStmt(_ve));
			_sl.addLast(_jimple.newReturnVoidStmt());
			sm.setActiveBody(_threadStartBody);
			_result = true;
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.6  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.5  2003/11/02 20:14:33  venku
   - thread body is fixed external to the driver in Util.
   Revision 1.4  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/11 07:13:58  venku
 *** empty log message ***
           Revision 1.2  2003/08/11 04:20:19  venku
           - Pair and Triple were changed to work in optimized and unoptimized mode.
           - Ripple effect of the previous change.
           - Documentation and specification of other classes.
           Revision 1.1  2003/08/07 06:42:16  venku
           Major:
            - Moved the package under indus umbrella.
            - Renamed isEmpty() to hasWork() in IWorkBag.
 */
