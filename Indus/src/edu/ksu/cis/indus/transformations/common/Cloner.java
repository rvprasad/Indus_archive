
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

package edu.ksu.cis.indus.transformations.common;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import soot.util.Chain;

import edu.ksu.cis.indus.interfaces.ISystemInfo;

import java.util.Iterator;


/**
 * This class is responsible for cloning of parts of the system during program transformation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Cloner
  implements ASTCloner.IASTClonerHelper {
	/**
	 * This instance is used to clone Jimple AST chunks.
	 *
	 * @invariant astCloner != null
	 */
	private final ASTCloner astCloner = new ASTCloner(this);

	/**
	 * This is a reference to the jimple body representation.
	 *
	 * @invariant jimple != null
	 */
	private final Jimple jimple = Jimple.v();

	/**
	 * This provides information about the system such as statement graphs and such that are common to analyses and
	 * transformations.
	 */
	private ISystemInfo sysInfo;

	/**
	 * The class manager which manages clonee classes.
	 */
	private Scene clazzManager;

	/**
	 * The class manager which manages clone classes.
	 */
	private Scene cloneClazzManager;

	/**
	 * Clones of the given class if one does not exists.  If one exists, that is returned.
	 *
	 * @param clazz to be cloned.
	 *
	 * @return the clone of <code>clazz</code>.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	public SootClass getCloneOf(final SootClass clazz) {
		String clazzName = clazz.getName();
		boolean declares = cloneClazzManager.containsClass(clazzName);
		SootClass result;

		if (declares) {
			result = cloneClazzManager.getSootClass(clazzName);
		} else {
			result = clone(clazz);
		}
		return result;
	}

	/**
	 * Clones of the given field if one does not exists.  If one exists, that is returned.
	 *
	 * @param field to be cloned.
	 *
	 * @return the cone of <code>field</code>.
	 *
	 * @pre field != null
	 * @post result != null
	 */
	public SootField getCloneOf(final SootField field) {
		SootClass clazz = getCloneOf(field.getDeclaringClass());
		String name = field.getName();
		Type type = field.getType();

		if (!clazz.declaresField(name, type)) {
			clazz.addField(new SootField(name, type, field.getModifiers()));
		}
		return clazz.getField(name, type);
	}

	/**
	 * Clones the given method if one does not exists.  If one exists, that is returned.  The statement list of the method
	 * body of the clone is equal in length to that of the given method but it only contains <code>NopStmt</code>s.
	 *
	 * @param cloneeMethod is the method to be cloned.
	 *
	 * @return the clone of <code>cloneMethod</code>.
	 *
	 * @pre cloneeMethod != null
	 * @post result != null
	 */
	public SootMethod getCloneOf(final SootMethod cloneeMethod) {
		SootClass sc = getCloneOf(cloneeMethod.getDeclaringClass());
		boolean declares =
			sc.declaresMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType());
		SootMethod result;

		if (declares) {
			result = sc.getMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType());
		} else {
			result =
				new SootMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType(),
					cloneeMethod.getModifiers());

			for (Iterator i = cloneeMethod.getExceptions().iterator(); i.hasNext();) {
				SootClass exception = (SootClass) i.next();
				result.addException(exception);
			}

			JimpleBody jb = jimple.newBody(result);
			Chain sl = jb.getUnits();
			Stmt nop = jimple.newNopStmt();

			for (int i = sysInfo.getStmtGraph(cloneeMethod).getBody().getUnits().size() - 1; i >= 0; i--) {
				sl.addLast(nop);
			}
			result.setActiveBody(jb);
		}
		return result;
	}

	/**
	 * Returns the clone of the class named by <code>clazz</code> if one exists.
	 *
	 * @param clazz is the name of the class whose clone is requested.
	 *
	 * @return the clone of the requested class if it exists; <code>null</code> is returned otherwise.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	public SootClass getCloneOf(final String clazz) {
		SootClass sc = clazzManager.getSootClass(clazz);
		SootClass result = null;

		if (sc != null) {
			result = getCloneOf(sc);
		}

		return result;
	}

	/**
	 * Retrieves the jimple entity associated with the given local in the given method.
	 *
	 * @param local of interest.
	 * @param method in which the local occurs.
	 *
	 * @return the jimple entity corresponding to the requested local.
	 *
	 * @pre name != null and method != null
	 */
	public Local getLocal(final Local local, final SootMethod method) {
		SootMethod tranformedMethod = getCloneOf(method);
		Body body = tranformedMethod.getActiveBody();
		Local result = null;
		String localName = local.getName();

		for (Iterator i = body.getLocals().iterator(); i.hasNext();) {
			Local temp = (Local) i.next();

			if (temp.getName().equals(localName)) {
				result = local;
				break;
			}
		}
		return result;
	}

	/**
	 * Clones a given Jimple statement that occurs in the given method.
	 *
	 * @param stmt is the Jimple statement to be cloned.
	 * @param cloneeMethod in which <code>stmt</code> occurs.
	 *
	 * @return the clone of <code>stmt</code>.
	 *
	 * @pre stmt != null and cloneeMethod != null
	 * @post result != null and result.oclIsTypeOf(stmt.evaluationType())
	 */
	public Stmt cloneASTFragment(final Stmt stmt, final SootMethod cloneeMethod) {
		return astCloner.cloneASTFragment(stmt, cloneeMethod);
	}

	/**
	 * Clones a given Jimple value that occurs in the given method.
	 *
	 * @param value is the Jimple value to be cloned.
	 * @param cloneeMethod in which <code>value</code> occurs.
	 *
	 * @return the clone of <code>value</code>.
	 *
	 * @pre value != null and cloneeMethod != null
	 * @post result != null and result.oclIsTypeOf(stmt.evaluationType())
	 */
	public Value cloneASTFragment(final Value value, final SootMethod cloneeMethod) {
		return astCloner.cloneASTFragment(value, cloneeMethod);
	}

	/**
	 * Initializes the data structures.
	 *
	 * @param theSystem is the classes that form the system to be clone.
	 * @param cloneSystem is the system after slicing.
	 * @param systemInfo that provides information about the system.
	 *
	 * @pre cloneeSystem != null and cloneSystem != null and theController != null
	 */
	public void initialize(final Scene theSystem, final Scene cloneSystem, final ISystemInfo systemInfo) {
		clazzManager = theSystem;
		cloneClazzManager = cloneSystem;
		sysInfo = systemInfo;
	}

	/**
	 * Resets the internal data structures.  For safe and meaningful operation after call to this method,
	 * <code>initialize()</code> should be called before calling any other methods.
	 */
	public void reset() {
		sysInfo = null;
		clazzManager = null;
	}

	/**
	 * Clones <code>clazz</code> in terms of inheritence and modifiers only.  The clone class has an empty body.
	 *
	 * @param clazz to clone
	 *
	 * @return the clone of <code>clazz</code>.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	private SootClass clone(final SootClass clazz) {
		SootClass result = new SootClass(clazz.getName(), clazz.getModifiers());

		if (clazz.hasSuperclass()) {
			SootClass superClass = getCloneOf(clazz.getSuperclass());
			result.setSuperclass(superClass);
		}

		for (Iterator i = clazz.getInterfaces().iterator(); i.hasNext();) {
			SootClass cloneeInterface = (SootClass) i.next();
			SootClass cloneInterface = getCloneOf(cloneeInterface);
			result.addInterface(cloneInterface);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/19 12:44:39  venku
   Changed the signature of ITransformer.getLocal()
   Introduced reset() in ITransformer.
   Ripple effect of the above changes.
   Revision 1.3  2003/08/19 11:58:53  venku
   Remove any reference to slicing from the documentation.

   Revision 1.2  2003/08/18 04:45:31  venku
   Moved the code such that code common to transformations are in one location
   and independent of any specific transformation.

   Revision 1.1  2003/08/18 04:01:52  venku
   Major changes:
    - Teased apart cloning logic in the slicer.  Made it transformation independent.
    - Moved it under transformation common location under indus.
 */
