
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

import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

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
	///CLOVER:OFF

	/**
	 * A private constructor to prevent the instantiation of this class.
	 */
	private Util() {
	}

	///CLOVER:ON

	/**
	 * Retrieves all ancestors (classes/interfaces) of the given class.
	 *
	 * @param sootClass for which the ancestors are requested.
	 *
	 * @return a collection of classes.
	 *
	 * @pre sootClass != null
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 */
	public static Collection getAncestors(final SootClass sootClass) {
		final Collection _result = new HashSet();
		final Collection _temp = new HashSet();
		final IWorkBag _wb = new LIFOWorkBag();
		_wb.addWork(sootClass);

		while (_wb.hasWork()) {
			final SootClass _work = (SootClass) _wb.getWork();

			if (_work.hasSuperclass()) {
				final SootClass _superClass = _work.getSuperclass();
				_temp.add(_superClass);
			}
			_temp.addAll(_work.getInterfaces());

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				final SootClass _sc = (SootClass) _i.next();

				if (!_result.contains(_sc)) {
					_result.add(_sc);
					_wb.addWork(_sc);
				}
			}
		}
		return _result;
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
			if (hasSuperclass(_contains)) {
				_contains = _contains.getSuperclass();
			} else {
				_contains = null;
				break;
			}
		}

		return _contains;
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
				if (hasSuperclass(_temp)) {
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
	 * Collects all the method with signature identical to <code>method</code> in the superclasses/interfaces of
	 * <code>method</code>'s declaring class.
	 *
	 * @param method that needs to be included in the heirarchy to make the slice executable.
	 *
	 * @return a collection of methods.
	 *
	 * @pre method != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	public static Collection findMethodInSuperClassesAndInterfaces(final SootMethod method) {
		final IWorkBag _toProcess = new FIFOWorkBag();
		final Collection _processed = new HashSet();
		final Collection _result = new HashSet();
		_toProcess.addWork(method.getDeclaringClass());

		final List _parameterTypes = method.getParameterTypes();
		final Type _retType = method.getReturnType();
		final String _methodName = method.getName();

		while (_toProcess.hasWork()) {
			final SootClass _sc = (SootClass) _toProcess.getWork();
			_processed.add(_sc);

			if (_sc.declaresMethod(_methodName, _parameterTypes, _retType)) {
				_result.add(_sc.getMethod(_methodName, _parameterTypes, _retType));
			}

			if (_sc.hasSuperclass()) {
				final SootClass _superClass = _sc.getSuperclass();

				if (!_processed.contains(_superClass)) {
					_toProcess.addWorkNoDuplicates(_superClass);
				}
			}

			for (final Iterator _i = _sc.getInterfaces().iterator(); _i.hasNext();) {
				final SootClass _interface = (SootClass) _i.next();

				if (!_processed.contains(_interface)) {
					_toProcess.addWorkNoDuplicates(_interface);
				}
			}
		}

		return _result.isEmpty() ? Collections.EMPTY_SET
								 : _result;
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
			final SootClass _declClass = scm.getSootClass("java.lang.Thread");
			final SootMethod _sm = _declClass.getMethodByName("start");

			if (!_sm.isConcrete()) {
				_declClass.setApplicationClass();
				_declClass.setPhantom(false);
				_sm.setModifiers(_sm.getModifiers() & ~Modifier.NATIVE);
				_sm.setPhantom(false);

				final Jimple _jimple = Jimple.v();
				final JimpleBody _threadStartBody = _jimple.newBody(_sm);
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
				_sm.setActiveBody(_threadStartBody);
			}
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

		while (!_result && (_temp.getInterfaceCount() > 0 || hasSuperclass(_temp))) {
			if (_temp.implementsInterface(ancestor)) {
				_result = true;
			} else {
				_temp = _temp.getSuperclass();
			}
		}
		return _result;
	}

	/**
	 * Checks if the given class has a super class.  <code>java.lang.Object</code> will not have a super class, but others
	 * will.
	 *
	 * @param sc is the class to  be tested.
	 *
	 * @return <code>true</code> if <code>sc</code> has a superclass; <code>false</code>, otherwise.
	 *
	 * @pre sc != null
	 * @post sc.getName().equals("java.lang.Object") implies result = true
	 * @post (not sc.getName().equals("java.lang.Object")) implies result = false
	 */
	private static boolean hasSuperclass(SootClass sc) {
		boolean _result = false;

		if (!sc.getName().equals("java.lang.Object")) {
			_result = sc.hasSuperclass();
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.11  2004/01/19 22:44:04  venku
   - added method declarations in interfaces to be found by
     findMethodsInClassesAndInterfaces().
   Revision 1.10  2004/01/19 11:38:03  venku
   - moved findMethodInSuperClasses into Util.
   Revision 1.9  2004/01/08 23:44:09  venku
   - SootClass.hasSuperclass() is finicky.  I have introduced a
     simple local version.
   Revision 1.8  2003/12/31 10:32:26  venku
   - safe fixing of thread method is desired.  FIXED.
   Revision 1.7  2003/12/31 10:05:08  venku
   - only application classes can be modified. So,
     we shall make Thread one.
   Revision 1.6  2003/12/31 10:01:16  venku
   - removed unused code.
   Revision 1.5  2003/12/31 09:52:20  venku
   - removed unused code.
   Revision 1.4  2003/12/31 09:34:22  venku
   - formatting and clover directives.
   Revision 1.3  2003/12/31 09:30:18  venku
   - removed unused code.
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
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
     empty log message
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
    Major:
     - Moved the package under indus umbrella.
     - Renamed isEmpty() to hasWork() in IWorkBag.
 */
