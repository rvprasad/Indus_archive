
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

package edu.ksu.cis.indus.processing;

import java.util.EmptyStackException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static final Log LOGGER = LogFactory.getLog(Context.class);

	/**
	 * The call-stack sensitive component of the context.  This is relevant in call-site sensitive mode of analysis.
	 */
	protected Stack callString;

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
		callString = new Stack();
	}

	/**
	 * Returns the call stack of this context.
	 *
	 * @return the call stack of the this context.  Any operation on this object affects the call stack of this context.
	 */
	public final Stack getCallString() {
		final Stack _temp = new Stack();
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
			_result = (SootMethod) callString.peek();
		} catch (final EmptyStackException e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There are no methods in the call stack.", e);
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
	public Object clone() {
		Context _temp = null;

		try {
			_temp = (Context) super.clone();
			_temp.callString = (Stack) callString.clone();
		} catch (final CloneNotSupportedException e) {
			LOGGER.error("This should not happen.", e);
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
	public boolean equals(final Object o) {
		boolean _ret = false;

		if (o != null && o instanceof Context) {
			final Context _temp = (Context) o;

			if (progPoint != null) {
				_ret = progPoint.equals(_temp.progPoint);
			} else {
				_ret = progPoint == _temp.progPoint;
			}

			if (_ret) {
				if (stmt != null) {
					_ret = stmt.equals(_temp.stmt);
				} else {
					_ret = stmt == _temp.stmt;
				}
			}

			if (_ret) {
				if (callString != null) {
					_ret &= callString.equals(_temp.callString);
				} else {
					_ret &= callString == _temp.callString;
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
	public int hashCode() {
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
		return (SootMethod) callString.pop();
	}

	/**
	 * Returns the stringized representation of this context.
	 *
	 * @return the stringized representation of this context.
	 */
	public String toString() {
		return "Context:\n\tProgram Point: " + progPoint + "\n\tStmt: " + stmt + "\n\tCallStack: " + callString + "\n";
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/12/02 11:31:57  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.2  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.1  2003/11/06 05:15:05  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.4  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/09/25 03:20:17  venku
   - pruned finally block.
   Revision 1.2  2003/08/11 08:12:26  venku
   Major changes in equals() method of Context, Pair, Marker, and Triple.
   Similar changes in hashCode()
   Spruced up Documentation and Specification.
   Formatted code.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 0.10  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
