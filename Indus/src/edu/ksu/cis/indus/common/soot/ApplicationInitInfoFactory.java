package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;

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
	 */
	private static final String APPLICATION_STARTING_METHOD = "applicationStartingMethod";

	/**
	 * DOCUMENT ME!
	 */
	private static final String CLASS_INITIALIZING_THREAD_EXECUTING_METHOD = "classInitializingThreadExecutingMethod";

	/**
	 * DOCUMENT ME!
	 */
	private static final String SYSTEM_INIT_CLASS = "$SystemInitClass$";

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Triple<InvokeStmt, SootMethod, SootClass> getClassInitExecutingThread() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
			_scene.addClass(_sc);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc
				.getMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Triple<InvokeStmt, SootMethod, SootClass>(null, _sm, _sc);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Triple<InvokeStmt, SootMethod, SootClass> getApplicationStartingThread() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
			_scene.addClass(_sc);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc
				.getMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Triple<InvokeStmt, SootMethod, SootClass>(null, _sm, _sc);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static final class DummyInvokeStmt
			extends AbstractStmt
			implements InvokeStmt {

		/**
		 * @see soot.AbstractUnit#clone()
		 */
		@Override public Object clone() {
			return null;
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

		/**
		 * @see soot.Unit#fallsThrough()
		 */
		public boolean fallsThrough() {
			return false;
		}

		/**
		 * @see soot.Unit#branches()
		 */
		public boolean branches() {
			return false;
		}

	}

	/**
	 * DOCUMENT ME!
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static final class DummyAssignStmt
			extends AbstractStmt
			implements AssignStmt {

		/**
		 * @see soot.AbstractUnit#clone()
		 */
		@Override public Object clone() {
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
		 * @see soot.jimple.DefinitionStmt#getLeftOp()
		 */
		public Value getLeftOp() {
			return null;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getRightOp()
		 */
		public Value getRightOp() {
			return null;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getLeftOpBox()
		 */
		public ValueBox getLeftOpBox() {
			return null;
		}

		/**
		 * @see soot.jimple.DefinitionStmt#getRightOpBox()
		 */
		public ValueBox getRightOpBox() {
			return null;
		}

		/**
		 * @see soot.jimple.Stmt#toString(soot.UnitPrinter)
		 */
		public void toString(@SuppressWarnings("unused") final UnitPrinter up) {
			// does nothing
		}

		/**
		 * @see soot.Unit#fallsThrough()
		 */
		public boolean fallsThrough() {
			return false;
		}

		/**
		 * @see soot.Unit#branches()
		 */
		public boolean branches() {
			return false;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<Stmt, SootMethod> getClassInitExecutingThreadAllocationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
			_scene.addClass(_sc);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc
				.getMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Pair<Stmt, SootMethod>(new DummyAssignStmt(), _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<InvokeStmt, SootMethod> getClassInitExecutingThreadCreationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
			_scene.addClass(_sc);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc
				.getMethod(CLASS_INITIALIZING_THREAD_EXECUTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Pair<InvokeStmt, SootMethod>(new DummyInvokeStmt(), _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<Stmt, SootMethod> getApplicationStartingThreadAllocationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
			_scene.addClass(_sc);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc.getMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Pair<Stmt, SootMethod>(new DummyAssignStmt(), _sm);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final Pair<InvokeStmt, SootMethod> getApplicationStartingThreadCreationSite() {
		final Scene _scene = Scene.v();
		if (!_scene.containsClass(SYSTEM_INIT_CLASS)) {
			final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
			_scene.addClass(_sc);
		}
		final SootClass _sc = _scene.getSootClass(SYSTEM_INIT_CLASS);
		if (!_sc.declaresMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v())) {
			_sc.addMethod(new SootMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v()));
		}
		final SootMethod _sm = _sc.getMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		return new Pair<InvokeStmt, SootMethod>(new DummyInvokeStmt(), _sm);
	}
}

// End of File
