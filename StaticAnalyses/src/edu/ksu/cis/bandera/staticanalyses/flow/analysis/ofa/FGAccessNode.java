package edu.ksu.cis.bandera.bfa.analysis.ofa;

import edu.ksu.cis.bandera.bfa.AbstractWork;
import edu.ksu.cis.bandera.bfa.WorkList;

import ca.mcgill.sable.soot.jimple.Value;

import java.util.Collection;

import org.apache.log4j.Category;

/**
 * FGAccessNode.java
 *
 *
 * Created: Tue Jan 22 04:30:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FGAccessNode extends FGNode {

	private final AbstractWork work;

	private static final Category cat = Category.getInstance(FGAccessNode.class.getName());

	public FGAccessNode (AbstractWork work, WorkList worklist) {
		super(worklist);
		this.work = work;
	}

	protected void doWorkOnNewValues(Collection values) {
		super.doWorkOnNewValues(values);
		work.addValues(values);
		worklist.addWork(work);
	}

	protected void doWorkOnNewValue(Object o) {
		super.doWorkOnNewValue(o);
		work.addValue(o);
		worklist.addWork(work);
	}

}// FGAccessNode
