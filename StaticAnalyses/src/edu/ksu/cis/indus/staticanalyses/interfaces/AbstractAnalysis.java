
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.interfaces.IStatus;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * This class is the skeletal implementation of analyses which are structural or structural-like in nature.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractAnalysis
  implements IStatus {
	/**
	 * The pre-processor for this analysis, if one exists.
	 */
	protected IValueAnalyzerBasedProcessor preprocessor;

	/**
	 * This contains auxiliary information required by the subclasses. It is recommended that this represent
	 * <code>java.util.Properties</code> but map a <code>String</code> to an <code>Object</code>.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

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
	public IValueAnalyzerBasedProcessor getPreProcessor() {
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
	 * @param infoParam contains the value for the member variable<code>info</code>. Refer to {@link #info info} and subclass
	 * 		  documenation for more details.
	 *
	 * @throws InitializationException if the initialization in the sub classes fails.
	 *
	 * @pre classes != null
	 */
	public final void initialize(final Map infoParam)
	  throws InitializationException {
		info.putAll(infoParam);
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
	 * Resets all internal data structures.  This will <i>not</i> reset data structures provided by the application.
	 *
	 * @post info.size() == 0
	 */
	public void reset() {
		info.clear();
	}

	/**
	 * Returns the basic block graph for the given method, if available.  If not it will try to acquire the unit graph from
	 * the application. From that unit graph it will construct a basic block graph and return it.
	 *
	 * @param method for which the basic block graph is requested.
	 *
	 * @return the basic block graph corresponding to <code>method</code>.
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
		UnitGraph stmtGraph = graphManager.getUnitGraph(method);

		if (stmtGraph != null) {
			result = new ArrayList(stmtGraph.getBody().getUnits());
		}
		return result;
	}

	/**
	 * Retrives the unit graph of the given method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the unit graph of the method.
	 *
	 * @post result != null
	 */
	protected UnitGraph getUnitGraph(final SootMethod method) {
		return graphManager.getUnitGraph(method);
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
   Revision 1.17  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.16  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.15  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.14  2003/11/01 23:50:34  venku
   - documentation.
   Revision 1.13  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.12  2003/09/28 03:08:03  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.11  2003/09/13 05:42:07  venku
   - What if the unit graphs for all methods are unavailable?  Hence,
     added a method to AbstractAnalysis to retrieve the methods to
     process.  The subclasses work only on this methods.
   Revision 1.10  2003/09/12 22:33:09  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
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
    - Renamed isEmpty() to hasWork() in IWorkBag.
 */
