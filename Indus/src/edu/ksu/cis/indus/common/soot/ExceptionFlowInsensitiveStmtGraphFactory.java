package edu.ksu.cis.indus.common.soot;

import soot.jimple.JimpleBody;
import soot.toolkits.graph.BriefUnitGraph;

/**
 * This class provides <code>BriefUnitGraph</code>s. These graphs do not capture control flow due to exceptions.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class ExceptionFlowInsensitiveStmtGraphFactory
		extends AbstractStmtGraphFactory<BriefUnitGraph> {

	/**
	 * Creates an instance of this class.
	 */
	public ExceptionFlowInsensitiveStmtGraphFactory() {
		super();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractStmtGraphFactory#getStmtGraphForBody(soot.jimple.JimpleBody)
	 */
	@Override protected BriefUnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new BriefUnitGraph(body);
	}

}

// End of File
