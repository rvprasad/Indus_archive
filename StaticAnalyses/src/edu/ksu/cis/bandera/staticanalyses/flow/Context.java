package edu.ksu.cis.bandera.bfa;


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

	protected ValueBox objCreateSite;

	protected Stack callString;

	private static final Category cat = Category.getInstance(Context.class.getName());

	public Context() {}

	public Context(ValueBox progPoint, ValueBox objCreateSite, Stack callString) {
		this.progPoint = progPoint;
		this.objCreateSite = objCreateSite;
		this.callString = callString;
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

		if (objCreateSite == null && c.objCreateSite == null) {
			ret &= true;
		} else if (objCreateSite != null) {
			ret &= objCreateSite.equals(c.objCreateSite);
		} else {
			ret &= c.objCreateSite.equals(objCreateSite);
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

	public ValueBox getObjectCreationSite() {
		return objCreateSite;
	}

	public ValueBox getProgramPoint() {
		return progPoint;
	}

	public void setCallString(Stack cs) {
		callString = cs;
	}

	public void setObjectCreationSite(ValueBox ocs) {
		objCreateSite = ocs;
	}

	public void setProgramPoint(ValueBox pp) {
		progPoint = pp;
	}

}// Context
