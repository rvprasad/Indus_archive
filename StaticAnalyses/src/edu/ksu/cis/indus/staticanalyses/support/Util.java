
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;

import java.util.Collections;
import java.util.List;


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
		SootClass contains = sc;

		while (!contains.declaresMethod(method, parameterTypes, returnType)) {
			if (contains.hasSuperclass()) {
				contains = contains.getSuperclass();
			} else {
				contains = null;
				break;
			}
		}

		return contains;
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
		boolean retval = false;
		SootClass temp = child;

		while (!retval) {
			if (temp.getName().equals(ancestor)) {
				retval = true;
			} else {
				if (temp.hasSuperclass()) {
					temp = temp.getSuperclass();
				} else {
					break;
				}
			}
		}

		if (!retval) {
			retval = implementsInterface(child, ancestor);
		}

		return retval;
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
		boolean result = false;

		if (t1.equals(t2)) {
			result = true;
		} else if (t1 instanceof RefType && t2 instanceof RefType) {
			result = isDescendentOf(env.getClass(((RefType) t1).getClassName()), env.getClass(((RefType) t2).getClassName()));
		}
		return result;
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
		boolean result = false;

		if (t1.equals(t2)) {
			result = true;
		} else if (t1.numDimensions == t2.numDimensions) {
			result = isSameOrSubType(t1.baseType, t2.baseType, env);
		}
		return result;
	}

	/**
	 * Hooks in a <i>non-native</i><code>start</code> method to facilitate smooth callgraph construction.
	 *
	 * @param sm is the method to be changed.  It is changed only if the method is <code>java.lang.Thread.start</code>.
	 *
	 * @return <code>true</code> if the body of <code>sm</code> changed; <code>false</code>, otherwise.
	 */
	public static boolean setThreadStartBody(final SootMethod sm) {
		boolean result = false;
		SootClass declClass = sm.getDeclaringClass();
		declClass.setApplicationClass();

		if (sm.isNative()
			  && sm.getName().equals("start")
			  && sm.getParameterCount() == 0
			  && declClass.getName().equals("java.lang.Thread")) {
			sm.setModifiers(sm.getModifiers() ^ Modifier.NATIVE);

			Jimple jimple = Jimple.v();
			JimpleBody threadStartBody = jimple.newBody(sm);
			PatchingChain sl = threadStartBody.getUnits();
			Local thisRef = jimple.newLocal("$this", RefType.v(declClass.getName()));
			VirtualInvokeExpr ve;
			threadStartBody.getLocals().add(thisRef);
			// adds $this := @this;
			sl.addFirst(jimple.newIdentityStmt(thisRef, jimple.newThisRef(RefType.v(declClass))));
			// adds $this.virtualinvoke[java.lang.Thread.run()]:void;
			ve = jimple.newVirtualInvokeExpr(thisRef, declClass.getMethodByName("run"), Collections.EMPTY_LIST);
			sl.addLast(jimple.newInvokeStmt(ve));
			sl.addLast(jimple.newReturnVoidStmt());
			sm.setActiveBody(threadStartBody);
			result = true;
		}
		return result;
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
		boolean result = false;
		SootClass temp = child;

		while (!result && (temp.getInterfaceCount() > 0 || temp.hasSuperclass())) {
			if (temp.implementsInterface(ancestor)) {
				result = true;
			} else {
				temp = temp.getSuperclass();
			}
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
