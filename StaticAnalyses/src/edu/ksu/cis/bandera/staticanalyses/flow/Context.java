
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

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.ValueBox;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.EmptyStackException;
import java.util.Stack;


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
	 * An instance of <code>Logger</code> used for logging purposes.
	 */
	private static final Logger LOGGER = LogManager.getLogger(Context.class.getName());

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
	public Stack getCallString() {
		Stack temp = new Stack();
		temp.addAll(callString);
		return temp;
	}

	/**
	 * Returns the current method in this context.
	 *
	 * @return the current method in this context.
	 */
	public SootMethod getCurrentMethod() {
		SootMethod result = null;
		try {
			result = (SootMethod) callString.peek(); 
		} catch (EmptyStackException e) {
			LOGGER.info("There are no methods in the call stack.", e);
		}
		return result;
	}

	/**
	 * Sets the program point in this context.
	 *
	 * @param pp the program point in this context.
	 *
	 * @return the program point previously represented by this context.
	 */
	public ValueBox setProgramPoint(ValueBox pp) {
		ValueBox temp = progPoint;
		progPoint = pp;

		return temp;
	}

	/**
	 * Returns the program point in this context.
	 *
	 * @return the program point in this context.
	 */
	public ValueBox getProgramPoint() {
		return progPoint;
	}

	/**
	 * Updates the call stack to reflect that the given method is the first method call in this context.  It empties the call
	 * stack and installs the given method as the current method.
	 *
	 * @param sm the method to be installed as the current method and the only method on the call stack in this context. This
	 * 		  cannot be <code>null</code>.
	 */
	public void setRootMethod(SootMethod sm) {
		callString.removeAllElements();
		callString.push(sm);
	}

	/**
	 * Sets the given statement as the statement in this context.
	 *
	 * @param stmt to be set as the statement in this context.
	 *
	 * @return the previous statement in this context.
	 */
	public Stmt setStmt(Stmt stmt) {
		Stmt result = this.stmt;
		this.stmt = stmt;
		return result;
	}

	/**
	 * Returns the statement in this context.
	 *
	 * @return the statement in this context.
	 */
	public Stmt getStmt() {
		return stmt;
	}

	/**
	 * Updates the call stack to reflect a new method call in the current context.
	 *
	 * @param sm the method being called in the current context.  This cannot be <code>null</code>.
	 */
	public void callNewMethod(SootMethod sm) {
		LOGGER.debug("Adding method " + sm);
		callString.push(sm);
	}

	/**
	 * Clones the current object.  The objects representing the call stacks are deep cloned.
	 *
	 * @return the clone of the current context.
	 */
	public Object clone() {
		Context temp = null;

		try {
			temp = (Context) super.clone();
			temp.callString = (Stack) callString.clone();
		} catch(CloneNotSupportedException e) {
			LOGGER.error("This should not happen.", e);
		} finally {
			return temp;
		}
	}

	/**
	 * Checks if the given context and this context represent the same context.
	 *
	 * @param c the context to be compared for equality with this context.  This cannot be <code>null</code>.
	 *
	 * @return <code>true</code> if <code>c</code> and this context represent the same context; <code>false</code> otherwise.
	 */
	public boolean equals(Context c) {
		boolean ret = true;

		if(progPoint == null && c.progPoint == null) {
			ret &= true;
		} else if(progPoint != null) {
			ret &= progPoint.equals(c.progPoint);
		} else {
			ret &= c.progPoint.equals(progPoint);
		}

		if(callString == null && c.callString == null) {
			ret &= true;
		} else if(callString != null) {
			ret &= callString.equals(c.callString);
		} else {
			ret &= c.callString.equals(callString);
		}
		return ret;
	}

	/**
	 * Updates the call stack to reflect the return from the current method in this context.
	 *
	 * @return the method returned from.
	 */
	public SootMethod returnFromCurrentMethod() {
		return (SootMethod) callString.pop();
	}

	/**
	 * Returns the stringized representation of this context.
	 *
	 * @return the stringized representation of this context.
	 */
	public String toString() {
		return "Context:\n\tProgram Point: " + progPoint + "\n\tAllocation Site: " + "\n\tCallStack: " + callString + "\n";
	}
}

/*****
 ChangeLog:

$Log$

*****/
