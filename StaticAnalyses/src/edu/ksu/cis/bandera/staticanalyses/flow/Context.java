package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.util.Collections;

/**
 * Context.java
 *
 *
 * Created: Tue Jan 22 05:29:22 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class Context implements Cloneable {

	protected ValueBox progPoint;

	protected Object allocationSite;

	protected Stack callString;

	private static final Logger logger = LogManager.getLogger(Context.class.getName());

	public Context() {
		callString = new Stack();
	}

	public Context(ValueBox progPoint, Object allocationSite, Stack callString) {
		this.progPoint = progPoint;
		this.allocationSite = allocationSite;
		this.callString = callString;
	}

	public void callNewMethod(SootMethod sm) {
		logger.debug("Adding method " + sm);
		callString.push(sm);
	}

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

	public Stack getCallString() {
		return callString;
	}

	public SootMethod getCurrentMethod() {
		return (SootMethod)callString.peek();
	}

	public Object getAllocationSite() {
		return allocationSite;
	}

	public ValueBox getProgramPoint() {
		return progPoint;
	}

	public SootMethod returnFromCurrentMethod() {
		return (SootMethod)callString.pop();
	}

	public Object setAllocationSite(Object sites) {
		Object temp = allocationSite;
		allocationSite = sites;
		return temp;
	}

	public ValueBox setProgramPoint(ValueBox pp) {
		ValueBox temp = progPoint;
		progPoint = pp;
		return temp;
	}

	public void setRootMethod(SootMethod sm) {
		callString.removeAllElements();
		callString.push(sm);
	}

	public String toString() {
		return "Context:\n\tProgram Point: " + progPoint + "\n\tAllocation Site: " + allocationSite + "\n\tCallStack: " +
			callString + "\n";
	}

}// Context
