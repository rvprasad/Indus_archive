package edu.ksu.cis.bandera.staticanalyses.flow;


import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//Context.java
/**
 * <p>The context information is encapsulated in this class.  It can support flow-sensitive, allocation-site-sensitive, and
 * call-stack sensitive context information.</p>
 *
 * <p>Created: Tue Jan 22 05:29:22 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class Context implements Cloneable {

	/**
	 * <p>The program point component of the context.  This is relevant in the flow-sensitive mode of analysis.</p>
	 *
	 */
	protected ValueBox progPoint;

	/**
	 * <p>The allocation-site component of the context.  This is relevant in the allocation-site sensitive mode of
	 * analysis.  In particular, when dealing with instance fields and arrays.</p>
	 *
	 */
	protected Object allocationSite;

	/**
	 * <p>The call-stack sensitive component of the context.  This is relevant in call-site sensitive mode of analysis.</p>
	 *
	 */
	protected Stack callString;

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(Context.class.getName());

	/**
	 * <p>Creates a new <code>Context</code> instance with an emtpy call stack and <code>null</code> for program point and
	 * allocation site.</p>
	 *
	 */
	public Context() {
		callString = new Stack();
	}

	/**
	 * <p>Creates a new <code>Context</code> instance.</p>
	 *
	 * @param progPoint the program point captured by this object.
	 * @param allocationSite the allocation site captured by this object.
	 * @param callString the call stack captured by this object.  This cannot be <code>null</code>.
	 */
	public Context(ValueBox progPoint, Object allocationSite, Stack callString) {
		this.progPoint = progPoint;
		this.allocationSite = allocationSite;
		this.callString = callString;
	}

	/**
	 * <p>Updates the call stack to reflect a new method call in the current context.</p>
	 *
	 * @param sm the method being called in the current context.  This cannot be <code>null</code>.
	 */
	public void callNewMethod(SootMethod sm) {
		logger.debug("Adding method " + sm);
		callString.push(sm);
	}

	/**
	 * <p>Clones the current object.  The objects representing the call stacks are deep cloned.</p>
	 *
	 * @return the clone of the current context.
	 */
	public Object clone() {
		Context temp = null;
		try {
			temp = (Context)super.clone();
			temp.callString = (Stack)callString.clone();
		} catch (CloneNotSupportedException e) {
			logger.error("This should not happen.", e);
		} finally {
			return temp;
		}
	}

	/**
	 * <p>Checks if the given context and this context represent the same context.</p>
	 *
	 * @param c the context to be compared for equality with this context.  This cannot be <code>null</code>.
	 * @return <code>true</code> if <code>c</code> and this context represent the same context; <code>false</code> otherwise.
	 */
	public boolean equals(Context c) {
		boolean ret = true;

		if (progPoint == null && c.progPoint == null) {
			ret &= true;
		} else if (progPoint != null) {
			ret &= progPoint.equals(c.progPoint);
		} else {
			ret &= c.progPoint.equals(progPoint);
		} // end of else

		if (allocationSite == null && c.allocationSite == null) {
			ret &= true;
		} else if (allocationSite != null) {
			ret &= allocationSite.equals(c.allocationSite);
		} else {
			ret &= c.allocationSite.equals(allocationSite);
		} // end of else

		if (callString == null && c.callString == null) {
			ret &= true;
		} else if (callString != null) {
			ret &= callString.equals(c.callString);
		} else {
			ret &= c.callString.equals(callString);
		} // end of else

		return ret;
	}

	/**
	 * <p>Returns the call stack of this context.</p>
	 *
	 * @return the call stack of the this context.  Any operation on this object affects the call stack of this context.
	 */
	public Stack getCallString() {
		return callString;
	}

	/**
	 * <p>Returns the current method in this context.</p>
	 *
	 * @return the current method in this context.
	 */
	public SootMethod getCurrentMethod() {
		return (SootMethod)callString.peek();
	}

	/**
	 * <p>Returns the allocation site in this context.</p>
	 *
	 * @return the allocation site in this context.
	 */
	public Object getAllocationSite() {
		return allocationSite;
	}

	/**
	 * <p>Returns the program point in this context.</p>
	 *
	 * @return the program point in this context.
	 */
	public ValueBox getProgramPoint() {
		return progPoint;
	}

	/**
	 * <p>Updates the call stack to reflect the return from the current method in this context.</p>
	 *
	 * @return the method returned from.
	 * @throws <code>EmptyStackException</code> if there were no method calls in this context.
	 */
	public SootMethod returnFromCurrentMethod() {
		return (SootMethod)callString.pop();
	}

	/**
	 * <p>Sets the allocation site in this context.</p>
	 *
	 * @param site the allocation site in this context.
	 * @return the allocation site previously represented by this context.
	 */
	public Object setAllocationSite(Object site) {
		Object temp = allocationSite;
		allocationSite = site;
		return temp;
	}

	/**
	 * <p>Sets the program point in this context.</p>
	 *
	 * @param pp the program point in this context.
	 * @return the program point previously represented by this context.
	 */
	public ValueBox setProgramPoint(ValueBox pp) {
		ValueBox temp = progPoint;
		progPoint = pp;
		return temp;
	}

	/**
	 * <p>Updates the call stack to reflect that the given method is the first method call in this context.  It empties the
	 * call stack and installs the given method as the current method.</p>
	 *
	 * @param sm the method to be installed as the current method and the only method on the call stack in this context.  This
	 * cannot be <code>null</code>.
	 */
	public void setRootMethod(SootMethod sm) {
		callString.removeAllElements();
		callString.push(sm);
	}

	/**
	 * <p>Returns the stringized representation of this context.</p>
	 *
	 * @return the stringized representation of this context.
	 */
	public String toString() {
		return "Context:\n\tProgram Point: " + progPoint + "\n\tAllocation Site: " + allocationSite + "\n\tCallStack: " +
			callString + "\n";
	}

}// Context
