
package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;

import ca.mcgill.sable.soot.jimple.Value;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.WorkList;

import java.util.Collection;

import org.apache.log4j.Logger;


// FGAccessNode.java

/**
 * <p>This class extends the flow graph node by associating a work peice with it.  This optimizes the worklist by adding new
 * values to the work peice already on the work list and not generating a new work peice.</p>
 *
 * Created: Tue Jan 22 04:30:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FGAccessNode
  extends OFAFGNode {
	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = Logger.getLogger(FGAccessNode.class.getName());

	/**
	 * <p>The work associated with this node.</p>
	 *
	 */
	private final AbstractWork work;

	/**
	 * <p>Creates a new <code>FGAccessNode</code> instance.</p>
	 *
	 * @param work the work peice associated with this node.
	 * @param worklist the worklist in which <code>work</code> will be placed.
	 */
	public FGAccessNode(AbstractWork work, WorkList worklist) {
		super(worklist);
		this.work = work;
	}

	/**
	 * <p>Adds the given value to the work peice for processing.</p>
	 *
	 * @param value the value that needs to be processed at the given node.
	 */
	public void onNewValue(Object value) {
		super.onNewValue(value);
		logger.debug("Value: " + value + "\nSuccessors: " + succs);
		work.addValue(value);
		worklist.addWork(work);
	}

	/**
	 * <p>Adds the given values to the work peice for processing.</p>
	 *
	 * @param values the collection of values that need to be processed at the given node.
	 */
	public void onNewValues(Collection values) {
		super.onNewValues(values);
		logger.debug("Values: " + values + "\nSuccessors: " + succs);
		work.addValues(values);
		worklist.addWork(work);
	}
} // FGAccessNode
