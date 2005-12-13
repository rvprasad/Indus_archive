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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;

import java.util.Collection;
import java.util.Collections;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractStmt;

/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class ApplicationInitInfoFactory {

	/**
	 * DOCUMENT ME!
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static final class ThreadCreatingStmt
			extends AbstractStmt
			implements AssignStmt {

		/**
		 * @see soot.Unit#branches()
		 */
		public boolean branches() {
			return false;
		}

		/**
		 * @see soot.AbstractUnit#clone()
		 */
		@Override public Object clone() {
			return null;
		}

		/**
		 * @see soot.Unit#fallsThrough()
		 */
		public boolean fallsThrough() {
			return false;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getLeftOp()
		 */
		public Value getLeftOp() {
			return null;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getLeftOpBox()
		 */
		public ValueBox getLeftOpBox() {
			return null;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getRightOp()
		 */
		public Value getRightOp() {
			return null;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getRightOpBox()
		 */
		public ValueBox getRightOpBox() {
			return null;
		}

		/**
		 * @see soot.jimple.AssignStmt#setLeftOp(soot.Value)
		 */
		public void setLeftOp(@SuppressWarnings("unused") final Value variable) {
			// does nothing
		}

		/**
		 * @see soot.jimple.AssignStmt#setRightOp(soot.Value)
		 */
		public void setRightOp(@SuppressWarnings("unused") final Value rvalue) {
			// does nothing
		}

		/**
		 * @see soot.jimple.Stmt#toString(soot.UnitPrinter)
		 */
		public void toString(@SuppressWarnings("unused") final UnitPrinter up) {
			// does nothing
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static final class ThreadStartingStmt
			extends AbstractStmt
			implements InvokeStmt {

		/**
		 * @see soot.Unit#branches()
		 */
		public boolean branches() {
			return false;
		}

		/**
		 * @see soot.AbstractUnit#clone()
		 */
		@Override public Object clone() {
			return null;
		}

		/**
		 * @see soot.Unit#fallsThrough()
		 */
		public boolean fallsThrough() {
			return false;
		}

		/**
		 * @see soot.jimple.InvokeStmt#setInvokeExpr(soot.Value)
		 */
		public void setInvokeExpr(@SuppressWarnings("unused") final Value invokeExpr) {
			// does nothing
		}

		/**
		 * @see soot.jimple.Stmt#toString(soot.UnitPrinter)
		 */
		public void toString(final UnitPrinter up) {
			// does nothing
		}

	}

	/**
	 * DOCUMENT ME!
	 */
	public static final String APPLICATION_STARTING_METHOD = "applicationStartingMethod";

	/**
	 * DOCUMENT ME!
	 */
	public static final String CLASS_INIT_THREAD_STARTING_METHOD = "classInitThreadStartingMethod";

	/**
	 * DOCUMENT ME!
	 */
	public static final String SYSTEM_INIT_CLASS = "$SystemInitClass$";

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Triple<InvokeStmt, SootMethod, SootClass> getApplicationStartingThread() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			setupSystemInitClass(_scene);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			setupAppStartingMethod(_sc);
		}
		final SootMethod _sm = _sc.getMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final Object _first = _sm.getActiveBody().getUnits().getFirst();
		return new Triple<InvokeStmt, SootMethod, SootClass>((InvokeStmt) _sm.getActiveBody().getUnits().getSuccOf(_first),
				_sm, _sc);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<AssignStmt, SootMethod> getApplicationStartingThreadAllocationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			setupSystemInitClass(_scene);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			setupAppStartingMethod(_sc);
		}
		final SootMethod _sm = _sc.getMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Pair<AssignStmt, SootMethod>((AssignStmt) _sm.getActiveBody().getUnits().getFirst(), _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<InvokeStmt, SootMethod> getApplicationStartingThreadCreationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			setupSystemInitClass(_scene);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			setupAppStartingMethod(_sc);
		}
		final SootMethod _sm = _sc.getMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final Object _first = _sm.getActiveBody().getUnits().getFirst();
		return new Pair<InvokeStmt, SootMethod>((InvokeStmt) _sm.getActiveBody().getUnits().getSuccOf(_first), _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Triple<InvokeStmt, SootMethod, SootClass> getClassInitExecutingThread() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			setupSystemInitClass(_scene);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc.getMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final Object _first = _sm.getActiveBody().getUnits().getFirst();
		return new Triple<InvokeStmt, SootMethod, SootClass>((InvokeStmt) _sm.getActiveBody().getUnits().getSuccOf(_first),
				_sm, _sc);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<AssignStmt, SootMethod> getClassInitExecutingThreadAllocationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			setupSystemInitClass(_scene);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			setupClassInitializingThreadStartingMethod(_sc);
		}
		final SootMethod _sm = _sc.getMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final Object _first = _sm.getActiveBody().getUnits().getFirst();
		return new Pair<AssignStmt, SootMethod>((AssignStmt) _first, _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<InvokeStmt, SootMethod> getClassInitExecutingThreadCreationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			setupSystemInitClass(_scene);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			setupClassInitializingThreadStartingMethod(_sc);
		}
		final SootMethod _sm = _sc.getMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final Object _first = _sm.getActiveBody().getUnits().getFirst();
		return new Pair<InvokeStmt, SootMethod>((InvokeStmt) _sm.getActiveBody().getUnits().getSuccOf(_first), _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param sc DOCUMENT ME!
	 */
	private static void setupAppStartingMethod(final SootClass sc) {
		final SootMethod _sootMethod = new SootMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final JimpleBody _body = Jimple.v().newBody();
		_body.setMethod(_sootMethod);
		final Collection<Stmt> _units = _body.getUnits();
		_units.add(new ThreadCreatingStmt());
		_units.add(new ThreadStartingStmt());
		_units.add(Jimple.v().newReturnVoidStmt());
		_sootMethod.setActiveBody(_body);
		sc.addMethod(_sootMethod);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param sc DOCUMENT ME!
	 */
	private static void setupClassInitializingThreadStartingMethod(final SootClass sc) {
		final SootMethod _sootMethod = new SootMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final JimpleBody _body = Jimple.v().newBody();
		_body.setMethod(_sootMethod);
		final Collection<Stmt> _units = _body.getUnits();
		_units.add(new ThreadCreatingStmt());
		_units.add(new ThreadStartingStmt());
		_units.add(Jimple.v().newReturnVoidStmt());
		_sootMethod.setActiveBody(_body);
		sc.addMethod(_sootMethod);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param scene DOCUMENT ME!
	 */
	private static void setupSystemInitClass(final Scene scene) {
		final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
		_sc.setSuperclass(scene.getSootClass("java.lang.Object"));
		scene.addClass(_sc);
	}
}

// End of File
