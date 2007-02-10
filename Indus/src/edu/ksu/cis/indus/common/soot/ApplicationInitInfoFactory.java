/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.InternalUse;
import edu.ksu.cis.indus.annotations.NonNull;
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
 * This factory provides information required to boot-strap the application being analyzed but not explicitly provided in the
 * application source.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ApplicationInitInfoFactory {

	/**
	 * This statement creates a thread. This statement is for internal use only.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	@InternalUse static class ThreadCreatingStmt
			extends AbstractStmt
			implements AssignStmt {

		/**
		 * The serial version ID.
		 */
		private static final long serialVersionUID = 2814575491539829268L;

		/**
		 * Creates an instance of this class.
		 */
		ThreadCreatingStmt() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Functional public boolean branches() {
			return false;
		}

		/**
		 * {@inheritDoc} This implementation will always return null.
		 */
		@Functional @Override public Object clone() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Functional public boolean fallsThrough() {
			return false;
		}

		/**
		 * {@inheritDoc} This implementation will always return null.
		 */
		@Functional public Value getLeftOp() {
			return null;
		}

		/**
		 * {@inheritDoc} This implementation will always return null.
		 */
		@Functional public ValueBox getLeftOpBox() {
			return null;
		}

		/**
		 * {@inheritDoc} This implementation will always return null.
		 */
		@Functional public Value getRightOp() {
			return null;
		}

		/**
		 * {@inheritDoc} This implementation will always return null.
		 */
		@Functional public ValueBox getRightOpBox() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Empty public void setLeftOp(@SuppressWarnings("unused") final Value variable) {
			// does nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Empty public void setRightOp(@SuppressWarnings("unused") final Value rvalue) {
			// does nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Empty public void toString(@SuppressWarnings("unused") final UnitPrinter up) {
			// does nothing
		}
	}

	/**
	 * This statement starts a thread. This statement is for internal use only.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	@InternalUse static class ThreadStartingStmt
			extends AbstractStmt
			implements InvokeStmt {

		/**
		 * The serial version ID.
		 */
		private static final long serialVersionUID = -4968749658097774678L;

		/**
		 * Creates an instance of this class.
		 */
		ThreadStartingStmt() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Functional public boolean branches() {
			return false;
		}

		/**
		 * {@inheritDoc} This implementation will always return null.
		 */
		@Functional @Override public Object clone() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Functional public boolean fallsThrough() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Empty public void setInvokeExpr(@SuppressWarnings("unused") final Value invokeExpr) {
			// does nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Empty public void toString(@SuppressWarnings("unused") final UnitPrinter up) {
			// does nothing
		}

	}

	/**
	 * The name of a proxy method that creates the application.
	 */
	public static final String APPLICATION_STARTING_METHOD = "<applicationStartingMethod>";

	/**
	 * The name of a proxy method that starts the class initializing thread.
	 */
	public static final String CLASS_INIT_THREAD_STARTING_METHOD = "<classInitThreadStartingMethod>";

	/**
	 * The name of a proxy class that initializes the system.
	 */
	public static final String SYSTEM_INIT_CLASS = "$SystemInitClass$";

	/**
	 * Creates an instance of this class.
	 */
	@Empty private ApplicationInitInfoFactory() {
		super();
	}

	/**
	 * Retrieves the thread that starts the application.
	 * 
	 * @return a triple identifying the application starting thread.
	 */
	@NonNull public static Triple<InvokeStmt, SootMethod, SootClass> getApplicationStartingThread() {
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
	 * Retrieves the allocation site of the thread that starts the application.
	 * 
	 * @return a pair identifying the allocation of the application starting thread.
	 */
	@NonNull public static Pair<AssignStmt, SootMethod> getApplicationStartingThreadAllocationSite() {
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
	 * Retrieves the call (creation) site of the thread that starts the application.
	 * 
	 * @return a pair identifying the call (creation) site of the application starting thread.
	 */
	@NonNull public static Pair<InvokeStmt, SootMethod> getApplicationStartingThreadCreationSite() {
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
	 * Retrieves the thread that initializes the classes.
	 * 
	 * @return a triple identifying the class initializing thread.
	 */
	@NonNull public static Triple<InvokeStmt, SootMethod, SootClass> getClassInitExecutingThread() {
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
	 * Retrieves the allocation site of the thread that starts the application.
	 * 
	 * @return a pair identifying the allocation of the application starting thread.
	 */
	@NonNull public static Pair<AssignStmt, SootMethod> getClassInitExecutingThreadAllocationSite() {
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
	 * Retrieves the creation site of the class initializing thread.
	 * 
	 * @return a pair identifying the creation site of the class initializing thread.
	 */
	@NonNull public static Pair<InvokeStmt, SootMethod> getClassInitExecutingThreadCreationSite() {
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
	 * Sets up the method that starts the application in the given class. It adds a new method of name
	 * APPLICATION_STARTING_METHOD to the given class as the application startup method.
	 * 
	 * @param sc is the class to be extended (in-place) with application starting method.
	 */
	@Immutable private static void setupAppStartingMethod(@NonNull final SootClass sc) {
		final SootMethod _sm = new SootMethod(APPLICATION_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final JimpleBody _body = Jimple.v().newBody();
		_body.setMethod(_sm);
		@SuppressWarnings("unchecked") final Collection<Stmt> _units = _body.getUnits();
		_units.add(new ThreadCreatingStmt());
		_units.add(new ThreadStartingStmt());
		_units.add(Jimple.v().newReturnVoidStmt());
		_sm.setActiveBody(_body);
		sc.addMethod(_sm);
	}

	/**
	 * Sets up a method that starts and creates class initialization thread in the given class.
	 * 
	 * @param sc is the class to be extended (in-place) with method that starts and creates class init thread.
	 */
	private static void setupClassInitializingThreadStartingMethod(final SootClass sc) {
		final SootMethod _sm = new SootMethod(CLASS_INIT_THREAD_STARTING_METHOD, Collections.EMPTY_LIST, VoidType.v());
		final JimpleBody _body = Jimple.v().newBody();
		_body.setMethod(_sm);
		@SuppressWarnings("unchecked") final Collection<Stmt> _units = _body.getUnits();
		_units.add(new ThreadCreatingStmt());
		_units.add(new ThreadStartingStmt());
		_units.add(Jimple.v().newReturnVoidStmt());
		_sm.setActiveBody(_body);
		sc.addMethod(_sm);
	}

	/**
	 * Sets up a class that performs system initialization.
	 * 
	 * @param scene to be modified.
	 */
	@Immutable private static void setupSystemInitClass(@NonNull final Scene scene) {
		final SootClass _sc = new SootClass(SYSTEM_INIT_CLASS);
		_sc.setSuperclass(scene.getSootClass("java.lang.Object"));
		scene.addClass(_sc);
	}
}

// End of File
