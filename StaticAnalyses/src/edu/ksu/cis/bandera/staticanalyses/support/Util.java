
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.support;

import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.NoSuchMethodException;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;
import ca.mcgill.sable.soot.VoidType;

import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.StmtList;

import ca.mcgill.sable.util.List;
import ca.mcgill.sable.util.VectorList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;


/**
 * General utility class providing common chore methods.
 *
 * @author <a href="mailto:rvprasad@cis.ksu.edu">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class Util {
	/**
	 * An empty list to be used for queries on no parameter method.  The contents of this list should not be altered.
	 */
	public static final List EMPTY_PARAM_LIST = new VectorList();

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(Util.class);

	/**
	 * A private constructor to prevent the instantiation of this class.
	 */
	private Util() {
	}

	/**
	 * Provides the <code>SootClass</code> which injects the given method into the specific branch of the inheritence
	 * hierarchy which contains <code>sc</code>.
	 *
	 * @param sc class that defines the branch in which the injecting class exists.
	 * @param method name of the method (not the fully classified name).
	 * @param parameterTypes list of type of the parameters of the method.
	 * @param returnType return type of the method.
	 *
	 * @return If there is such a class then a <code>SootClass</code> object is returned; <code>null</code> otherwise.
	 *
	 * @throws NoSuchMethodException is thrown when no such method is declared in the given hierarchy.
	 *
	 * @pre sc &lt;> null and method &lt;> null and parameterTypes &lt;> null and returnType &lt;> null
	 */
	public static SootClass getDeclaringClass(SootClass sc, String method, List parameterTypes, Type returnType) {
		SootClass contains = sc;

		while(!contains.declaresMethod(method, parameterTypes, returnType)) {
			if(contains.hasSuperClass()) {
				contains = contains.getSuperClass();
			} else {
				throw new NoSuchMethodException(sc + " does not define " + method + ".");
			}
		}

		return contains;
	}

	/**
	 * Provides the <code>SootClass</code> which injects the given method into the specific branch of the inheritence
	 * hierarchy which contains <code>sc</code>.  This is a shorthand version of Util.getDeclaringClass() where the
	 * <code>parameterTypes</code> is empty and the returnType is <code>VoidType</code>.
	 *
	 * @param sc class in or above which the method may be defined.
	 * @param method name of the method (not the fully classified name).
	 *
	 * @return If there is such a class then a <code>SootClass</code> object is returned; <code>null</code> otherwise.
	 *
	 * @pre sc &lt;> null and method &lt;> null
	 */
	public static SootClass getDeclaringClassFromName(SootClass sc, String method) {
		return getDeclaringClass(sc, method, EMPTY_PARAM_LIST, VoidType.v());
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
	 * @pre child &lt;> null and ancestor &lt;> null
	 */
	public static boolean isDescendentOf(SootClass child, String ancestor) {
		boolean retval = false;

		while((retval == false) && child.hasSuperClass()) {
			if(child.getName().equals(ancestor)) {
				retval = true;
			} else {
				child = child.getSuperClass();
			}
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
	 * @pre child &lt;> null and ancestor &lt;> null
	 */
	public static boolean isDescendentOf(SootClass child, SootClass ancestor) {
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
	 * @pre class1 &lt;> null and class2 &lt;> null
	 */
	public static boolean isHierarchicallyRelated(SootClass class1, SootClass class2) {
		return isDescendentOf(class1, class2.getName()) || isDescendentOf(class2, class1.getName());
	}

	/**
	 * Hooks in a <i>non-native</i><code>start</code> method to facilitate smooth callgraph construction.
	 *
	 * @param sm is the method to be changed.  It is changed only if the method is <code>java.lang.Thread.start</code>.
	 *
	 * @return <code>true</code> if the body of <code>sm</code> changed; <code>false</code>, otherwise.
	 */
	public static boolean setThreadStartBody(SootMethod sm) {
		boolean result = false;
		SootClass declClass = sm.getDeclaringClass();

		if(Modifier.isNative(sm.getModifiers())
				&& sm.getName().equals("start")
				&& sm.getParameterCount() == 0
				&& declClass.getName().equals("java.lang.Thread")) {
			Jimple jimple = Jimple.v();
			sm.setModifiers(sm.getModifiers() ^ Modifier.NATIVE);

			JimpleBody threadStartBody = (JimpleBody) jimple.newBody(sm);
			StmtList sl = threadStartBody.getStmtList();
			Local thisRef = jimple.newLocal("$this", RefType.v(declClass.getName()));
			threadStartBody.addLocal(thisRef);
			// adds $this := @this;
			sl.add(0, jimple.newIdentityStmt(thisRef, jimple.newThisRef(declClass)));
			// adds $this.virtualinvoke[java.lang.Thread.run()]:void;
			sl.add(1,
				jimple.newInvokeStmt(jimple.newVirtualInvokeExpr(thisRef, declClass.getMethod("run"),
						new ca.mcgill.sable.util.ArrayList())));
			sl.add(2, jimple.newReturnVoidStmt());
			sm.storeBody(jimple, threadStartBody);
			result = true;
		}
		return result;
	}

	/**
	 * Creates a new object which is of type <code>ca.mcgill.sable.util.Collection</code> and copies the contents of
	 * <code>src</code> into it.
	 *
	 * @param targetType name of the class which implements <code>ca.mcgill.sable.util.Collection</code> interface and which
	 * 		  will be the actual type of the returned object.
	 * @param src an object which implements <code>java.util.Collection</code> interface and contains values that need to be
	 * 		  copied.
	 *
	 * @return an instance of type <code>targetType</code> which contains all the values in collection <code>src</code>.
	 *
	 * @invariant src.oclType = Bag(Object)
	 * @post result->includesAll(src)
	 */
	public static ca.mcgill.sable.util.Collection convert(String targetType, Collection src) {
		ca.mcgill.sable.util.Collection retval = null;

		try {
			Class collect = Class.forName(targetType);
			retval = (ca.mcgill.sable.util.Collection) collect.newInstance();

			if(src != null) {
				Iterator i = src.iterator();

				while(i.hasNext()) {
					retval.add(i.next());
				}
			}
		} catch(ClassCastException e) {
			LOGGER.warn(targetType + " does not implement java.util.Collection.", e);
		} catch(ClassNotFoundException e) {
			LOGGER.warn("The class named " + targetType + " is not available in the class path.", e);
		} catch(Exception e) {
			LOGGER.warn("Error instantiating an object of class " + targetType + ".", e);
		}
		return retval;
	}

	/**
	 * Creates a new object which is of type <code>java.util.Collection</code> and copies the contents of the
	 * <code>src</code> into it.
	 *
	 * @param targetType name of the class which implements <code>java.util.Collection</code> interface and which will be the
	 * 		  actual type of the returned object.
	 * @param src an object which implements <code>ca.mcgill.sable.util.Collection</code> interface and contains values that
	 * 		  need to be copied.
	 *
	 * @return an instance of type <code>targetType</code> which contains all the values in collection <code>src</code>.
	 *
	 * @invariant src.oclType = Bag(Object)
	 * @post result->includesAll(src)
	 */
	public static Collection convert(String targetType, ca.mcgill.sable.util.Collection src) {
		Collection retval = null;

		try {
			Class collect = Class.forName(targetType);
			retval = (Collection) collect.newInstance();

			if(src != null) {
				ca.mcgill.sable.util.Iterator i = src.iterator();

				while(i.hasNext()) {
					retval.add(i.next());
				}
			}
		} catch(ClassCastException e) {
			LOGGER.warn(targetType + " does not implement java.util.Collection.", e);
		} catch(ClassNotFoundException e) {
			LOGGER.warn("The class named " + targetType + " is not available in the class path.", e);
		} catch(Exception e) {
			LOGGER.warn("Error instantiating an object of class " + targetType + ".", e);
		}
		return retval;
	}
}

/*****
 ChangeLog:

$Log$

*****/
