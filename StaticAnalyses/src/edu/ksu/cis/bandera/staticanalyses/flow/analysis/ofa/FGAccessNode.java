package edu.ksu.cis.bandera.bfa.analysis.ofa;

import edu.ksu.cis.bandera.bfa.AbstractWork;
import edu.ksu.cis.bandera.bfa.WorkList;

import ca.mcgill.sable.soot.jimple.Value;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * FGAccessNode.java
 *
 *
 * Created: Tue Jan 22 04:30:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FGAccessNode extends OFAFGNode {

	private final AbstractWork work;

	private static final Logger logger = Logger.getLogger(FGAccessNode.class.getName());

	public FGAccessNode (AbstractWork work, WorkList worklist) {
		super(worklist);
		this.work = work;
	}

	public void onNewValues(Collection values) {
		super.onNewValues(values);
		logger.debug("Values: "+ values + "\nSuccessors: " + succs);
		work.addValues(values);
		worklist.addWork(work);
	}

	public void onNewValue(Object value) {
		super.onNewValue(value);
		logger.debug("Value: "+ value + "\nSuccessors: " + succs);
		work.addValue(value);
		worklist.addWork(work);
	}

}// FGAccessNode
