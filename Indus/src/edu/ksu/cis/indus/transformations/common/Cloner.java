
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

import java.util.Iterator;

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


/**
 * This class is responsible for cloning of parts of the system during program transformation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class Cloner
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
		final String _clazzName = clazz.getName();
		final boolean _declares = cloneClazzManager.containsClass(_clazzName);
		SootClass _result;

		if (_declares) {
			_result = cloneClazzManager.getSootClass(_clazzName);
		} else {
			_result = clone(clazz);
		}
		return _result;
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
		final SootClass _clazz = getCloneOf(field.getDeclaringClass());
		final String _name = field.getName();
		final Type _type = field.getType();

		if (!_clazz.declaresField(_name, _type)) {
			_clazz.addField(new SootField(_name, _type, field.getModifiers()));
		}
		return _clazz.getField(_name, _type);
	}

	/**
	 * Clones the given method if one does not exists.  If one exists, that is returned.  The statement list of the method
	 * body of the clone is equal in length to that of the given method but it only contains <code>NopStmt</code>s.
	 *
	 * @param cloneeMethod is the method to be cloned.  It is required that cloneeMethod should have an active body.
	 *
	 * @return the clone of <code>cloneMethod</code>.
	 *
	 * @pre cloneeMethod != null
	 * @post result != null
	 */
	public SootMethod getCloneOf(final SootMethod cloneeMethod) {
		final SootClass _sc = getCloneOf(cloneeMethod.getDeclaringClass());
		final boolean _declares =
			_sc.declaresMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType());
		SootMethod _result;

		if (_declares) {
            _result = _sc.getMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType());
		} else {
			_result =
				new SootMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType(),
					cloneeMethod.getModifiers());

			for (final Iterator _i = cloneeMethod.getExceptions().iterator(); _i.hasNext();) {
				final SootClass _exception = (SootClass) _i.next();
				_result.addException(_exception);
			}

			final JimpleBody _jb = jimple.newBody(_result);
			final Chain _sl = _jb.getUnits();
			final Stmt _nop = jimple.newNopStmt();

			for (int _i = cloneeMethod.getActiveBody().getUnits().size() - 1; _i >= 0; _i--) {
				_sl.addLast(_nop);
			}
			_result.setActiveBody(_jb);
		}
		return _result;
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
		final SootClass _sc = clazzManager.getSootClass(clazz);
		SootClass _result = null;

		if (_sc != null) {
			_result = getCloneOf(_sc);
		}

		return _result;
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
		final SootMethod _tranformedMethod = getCloneOf(method);
		final Body _body = _tranformedMethod.getActiveBody();
		Local _result = null;
		final String _localName = local.getName();

		for (final Iterator _i = _body.getLocals().iterator(); _i.hasNext();) {
			final Local _temp = (Local) _i.next();

			if (_temp.getName().equals(_localName)) {
				_result = local;
				break;
			}
		}
		return _result;
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
	 *
	 * @pre cloneeSystem != null and cloneSystem != null and theController != null
	 */
	public void initialize(final Scene theSystem, final Scene cloneSystem) {
		clazzManager = theSystem;
		cloneClazzManager = cloneSystem;
	}

	/**
	 * Resets the internal data structures.  For safe and meaningful operation after call to this method,
	 * <code>initialize()</code> should be called before calling any other methods.
	 */
	public void reset() {
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
		final SootClass _result = new SootClass(clazz.getName(), clazz.getModifiers());

		if (clazz.hasSuperclass()) {
			final SootClass _superClass = getCloneOf(clazz.getSuperclass());
			_result.setSuperclass(_superClass);
		}

		for (final Iterator _i = clazz.getInterfaces().iterator(); _i.hasNext();) {
			final SootClass _cloneeInterface = (SootClass) _i.next();
			final SootClass _cloneInterface = getCloneOf(_cloneeInterface);
			_result.addInterface(_cloneInterface);
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.8  2003/09/28 06:54:17  venku
   - one more small change to the interface.
   Revision 1.7  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
   Revision 1.6  2003/09/27 23:21:42  venku
 *** empty log message ***
     Revision 1.5  2003/09/26 15:06:05  venku
     - Formatting.
     - ITransformer has a new method initialize() via which the system
       being transformed can be specified.
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
