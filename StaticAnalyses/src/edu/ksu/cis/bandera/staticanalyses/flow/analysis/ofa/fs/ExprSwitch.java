package edu.ksu.cis.bandera.bfa.analysis.ofa.fs;

import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;

import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.SimpleLocalDefs;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.ValueBox;
import ca.mcgill.sable.util.Iterator;

import org.apache.log4j.Category;

/**
 * ExprSwitch.java
 *
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ExprSwitch extends edu.ksu.cis.bandera.bfa.analysis.ofa.fi.ExprSwitch {

	private static final Category cat = Category.getInstance(ExprSwitch.class.getName());

	public ExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	public void process(ValueBox vb) {
		ValueBox temp = context.getProgramPoint();
		context.setProgramPoint(vb);
		super.process(vb);
		context.setProgramPoint(temp);
	}

	public Object prototype(Object o) {
		return new ExprSwitch((AbstractStmtSwitch)o, connector);
	}

}// ExprSwitch
