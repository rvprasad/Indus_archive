
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.indus.interfaces.IStatus;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is the skeletal implementation of analyses which are structural or structural-like in nature.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractAnalysis implements IStatus {
	/**
	 * This maps the methods being analyzed to their control graphs.
	 *
	 * @invariant method2stmtGraph.oclIsKindOf(Map(SootMethod, UnitGraph))
	 */
	protected final Map method2stmtGraph = new HashMap();

	/**
	 * The pre-processor for this analysis, if one exists.
	 */
	protected IProcessor preprocessor;

	/**
	 * This contains auxiliary information required by the subclasses. It is recommended that this represent
	 * <code>java.util.Properties</code> but map a <code>String</code> to an <code>Object</code>.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected Map info = new HashMap();

	/**
	 * This manages the basic block graphs of methods.
	 */
	private BasicBlockGraphMgr graphManager;

	/**
	 * Analyzes the given methods and classes for "some" information.
	 */
	public abstract void analyze();

	/**
	 * Returns the pre-processor.
	 *
	 * @return the pre-processor.
	 *
	 * @post doesPreProcessing() == true implies result != null
	 */
	public IProcessor getPreProcessor() {
		return preprocessor;
	}

	/**
	 * Returns the statistics about this analysis in the form of a <code>String</code>.
	 *
	 * @return the statistics about this analysis.
	 */
	public String getStatistics() {
		return getClass() + " does not implement this method.";
	}

	/**
	 * Checks if this analysis does any preprocessing.  Subclasses need not override this method.  Rather they can set
	 * <code>preprocessor</code> field to a preprocessor and this method will use that to provide the correct information to
	 * the caller.
	 *
	 * @return <code>true</code> if the analysis will preprocess; <code>false</code>, otherwise.
	 */
	public boolean doesPreProcessing() {
		return preprocessor != null;
	}

	/**
	 * Initializes the analyzer with the information from the system to perform the analysis.
	 *
	 * @param method2stmtGraphParam maps methods that constitute that analyzed system to their control flow graphs.
	 * @param infoParam contains the value for the member variable<code>info</code>. Refer to {@link #info info} and subclass
	 * 		  documenation for more details.
	 *
	 * @throws InitializationException if the initialization in the sub classes fails.
	 *
	 * @pre method2stmtGraph.oclIsKindOf(java.util.Map(soot.SootMethod, soot.jimple.CompleteUnitGraph))
	 * @pre classes != null and method2stmtGraph != null and info != null
	 */
	public final void initialize(final Map method2stmtGraphParam, final Map infoParam)
	  throws InitializationException {
		this.info.putAll(infoParam);
		this.method2stmtGraph.putAll(method2stmtGraphParam);
		setup();
	}

	/**
	 * Sets the basic block graph manager to use.
	 *
	 * @param bbm is the basic block graph manager.
	 *
	 * @pre bbm != null
	 */
	public void setBasicBlockGraphManager(final BasicBlockGraphMgr bbm) {
		graphManager = bbm;
	}

	/**
	 * Resets all internal data structures.
	 *
	 * @post info.size() == 0 and method2stmtGraph.size() == 0
	 */
	public void reset() {
		method2stmtGraph.clear();
		info.clear();
	}

	/**
	 * Returns the basic block graph constructed from the given control flow graph.  This will also remember the association
	 * between the given method and graph.
	 *
	 * @param stmtGraph is a control flow graph.
	 *
	 * @return the basic block graph corresponding to <code>stmtGraph</code>.
	 *
	 * @pre stmtGraph != null
	 * @post result != null
	 */
	protected BasicBlockGraph getBasicBlockGraph(final UnitGraph stmtGraph) {
		method2stmtGraph.put(stmtGraph.getBody().getMethod(), stmtGraph);
		return graphManager.getBasicBlockGraph(stmtGraph);
	}

	/**
	 * Returns the basic block graph for the given method, if one exists.
	 *
	 * @param method for which the basic block graph is requested.
	 *
	 * @return the basic block graph corresponding to <code>method</code>, if one exists.
	 *
	 * @pre method != null
	 */
	protected BasicBlockGraph getBasicBlockGraph(final SootMethod method) {
		return graphManager.getBasicBlockGraph(method);
	}

	/**
	 * Returns the list of statements in the given method, if it exists.  Each call returns a new list.
	 *
	 * @param method of interest.
	 *
	 * @return the list of statements.
	 *
	 * @pre method != null
	 * @post result.oclIsKindOf(Collection(Stmt))
	 */
	protected List getStmtList(final SootMethod method) {
		List result = null;
		UnitGraph stmtGraph = (UnitGraph) method2stmtGraph.get(method);

		if (stmtGraph != null) {
			result = new ArrayList(stmtGraph.getBody().getUnits());
		}
		return result;
	}

	/**
	 * Setup data structures after initialization.  This is a convenience method for subclasses to do processing after the
	 * calls to <code>initialize</code> and before the call to <code>preprocess</code>.
	 *
	 * @throws InitializationException is never thrown by this implementation.
	 */
	protected void setup()
	  throws InitializationException {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/09/12 01:21:30  venku
   - documentation changes.
   Revision 1.8  2003/09/10 10:52:44  venku
   - new basic block graphs can be added.
   Revision 1.7  2003/09/09 01:13:58  venku
   - made basic block graph manager configurable in AbstractAnalysis
   - ripple effect of the above change in DADriver.  This should also affect Slicer.
   Revision 1.6  2003/08/21 01:35:05  venku
   Documentation changes.
   reset() is not called in initialize.  The user needs to do this.
   Revision 1.5  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.4  2003/08/17 10:37:08  venku
   Fixed holes in documentation.
   Removed addRooMethods in FA and added the equivalent logic into analyze() methods.
   Revision 1.3  2003/08/16 02:41:37  venku
   Renamed AController to AbstractController.
   Renamed AAnalysis to AbstractAnalysis.
   Revision 1.2  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
