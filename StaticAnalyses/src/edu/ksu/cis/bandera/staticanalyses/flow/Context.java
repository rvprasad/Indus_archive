package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.ValueBox;
import java.util.Stack;
import org.apache.log4j.Category;

/**
 * Context.java
 *
 *
 * Created: Tue Jan 22 05:29:22 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class Context {

	protected ValueBox progPoint;

	protected ValueBox allocationSite;

	protected Stack callString;

	private static final Category cat = Category.getInstance(Context.class.getName());

	public Context() {
		callString = new Stack();
	}

	public Context(ValueBox progPoint, ValueBox objCreateSite, Stack callString) {
		this.progPoint = progPoint;
		this.allocationSite = allocationSite;
		this.callString = callString;
	}

	public void callNewMethod(SootMethod sm) {
		callString.push(sm);
	}

	public Object clone() {
		Context temp = new Context();
		temp.progPoint = progPoint;
		temp.allocationSite = allocationSite;
		temp.callString = (Stack)callString.clone();
		return temp;
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

	public ValueBox getAllcoationSite() {
		return allocationSite;
	}

	public ValueBox getProgramPoint() {
		return progPoint;
	}

	public SootMethod returnFromCurrentMethod() {
		return (SootMethod)callString.pop();
	}

	public void setAllocationSite(ValueBox ocs) {
		allocationSite = ocs;
	}

	public void setProgramPoint(ValueBox pp) {
		progPoint = pp;
	}

	public void setRootMethod(SootMethod sm) {
		callString.removeAllElements();
		callString.push(sm);
	}

}// Context
