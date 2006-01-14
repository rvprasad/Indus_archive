
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

package edu.ksu.cis.indus.processing;

import java.util.EmptyStackException;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * The context information is encapsulated in this class.  It can support flow-sensitive, allocation-site-sensitive, and
 * call-stack sensitive context information.    Created: Tue Jan 22 05:29:22 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class Context
  implements Cloneable {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

	/**
	 * The call-stack sensitive component of the context.  This is relevant in call-site sensitive mode of analysis.
	 */
	protected Stack<SootMethod> callString;

	/**
	 * The statement component of the context.  This component can be used when the entity associated with the context is an
	 * expression.
	 */
	protected Stmt stmt;

	/**
	 * The program point component of the context.  This is relevant in the flow-sensitive mode of analysis.
	 */
	protected ValueBox progPoint;

	/**
	 * Creates a new <code>Context</code> instance with an emtpy call stack and <code>null</code> for program point and
	 * allocation site.
	 */
	public Context() {
		callString = new Stack<SootMethod>();
	}

	/**
	 * Returns the call stack of this context.
	 *
	 * @return the call stack of the this context.  Any operation on this object affects the call stack of this context.
	 */
	public final Stack<SootMethod> getCallString() {
		final Stack<SootMethod> _temp = new Stack<SootMethod>();
		_temp.addAll(callString);
		return _temp;
	}

	/**
	 * Returns the current method in this context.
	 *
	 * @return the current method in this context.
	 */
	public final SootMethod getCurrentMethod() {
		SootMethod _result = null;

		try {
			_result = callString.peek();
		} catch (final EmptyStackException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There are no methods in the call stack.", _e);
			}
		}
		return _result;
	}

	/**
	 * Sets the program point in this context.
	 *
	 * @param pp the program point in this context.
	 *
	 * @return the program point previously represented by this context.
	 */
	public final ValueBox setProgramPoint(final ValueBox pp) {
		final ValueBox _temp = progPoint;
		progPoint = pp;

		return _temp;
	}

	/**
	 * Returns the program point in this context.
	 *
	 * @return the program point in this context.
	 */
	public final ValueBox getProgramPoint() {
		return progPoint;
	}

	/**
	 * Updates the call stack to reflect that the given method is the first method call in this context.  It empties the call
	 * stack and installs the given method as the current method.
	 *
	 * @param sm the method to be installed as the current method and the only method on the call stack in this context.
	 */
	public final void setRootMethod(final SootMethod sm) {
		callString.removeAllElements();
		callString.push(sm);
	}

	/**
	 * Sets the given statement as the statement in this context.
	 *
	 * @param stmtParam to be set as the statement in this context.
	 *
	 * @return the previous statement in this context.
	 */
	public final Stmt setStmt(final Stmt stmtParam) {
		final Stmt _temp = this.stmt;
		this.stmt = stmtParam;
		return _temp;
	}

	/**
	 * Returns the statement in this context.
	 *
	 * @return the statement in this context.
	 */
	public final Stmt getStmt() {
		return stmt;
	}

	/**
	 * Updates the call stack to reflect a new method call in the current context.
	 *
	 * @param sm the method being called in the current context.  This cannot be <code>null</code>.
	 */
	public final void callNewMethod(final SootMethod sm) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding method " + sm);
		}

		callString.push(sm);
	}

	/**
	 * Clones the current object.  The objects representing the call stacks are deep cloned.
	 *
	 * @return the clone of the current context.
	 */
	@Override public Context clone() {
		Context _temp = null;

		try {
			_temp = (Context) super.clone();
			_temp.callString = (Stack<SootMethod>) callString.clone();
		} catch (final CloneNotSupportedException _e) {
			LOGGER.error("This should not happen.", _e);
		}
		return _temp;
	}

	/**
	 * Checks if the given context and this context represent the same context.
	 *
	 * @param o the context to be compared for equality with this context.
	 *
	 * @return <code>true</code> if <code>c</code> and this context represent the same context; <code>false</code> otherwise.
	 */
	@Override public boolean equals(final Object o) {
		boolean _ret = false;

		if (o != null && o instanceof Context) {
			final Context _temp = (Context) o;
			_ret = (this == o) || (progPoint == _temp.progPoint);

			if (_ret) {
				_ret = stmt == _temp.stmt;

				if (_ret) {
					_ret = (callString == _temp.callString) || ((callString != null) && callString.equals(_temp.callString));
				}
			}
		}
		return _ret;
	}

	/**
	 * Returns the hash code of this object.  It is derived from the call stack and the program point.
	 *
	 * @return the hash code of this object.
	 */
	@Override public int hashCode() {
		int _result = 17;

		if (progPoint != null) {
			_result = 37 * _result + progPoint.hashCode();
		}

		if (stmt != null) {
			_result = 37 * _result + stmt.hashCode();
		}

		_result = 37 * _result + callString.hashCode();
		return _result;
	}

	/**
	 * Updates the call stack to reflect the return from the current method in this context.
	 *
	 * @return the method returned from.
	 */
	public final SootMethod returnFromCurrentMethod() {
		return callString.pop();
	}

	/**
	 * Returns the stringized representation of this context.
	 *
	 * @return the stringized representation of this context.
	 */
	@Override public String toString() {
		return "Context:\n\tProgram Point: " + progPoint + "\n\tStmt: " + stmt + "\n\tCallStack: " + callString + "\n";
	}
}

// End of File
