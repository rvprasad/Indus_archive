
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.dependency;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.StmtGraph;
import ca.mcgill.sable.soot.jimple.StmtList;

import edu.ksu.cis.bandera.staticanalyses.interfaces.IProcessor;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraphMgr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class provides generic methods to calculate and provide dependency analysis (DA) information.
 *
 * <p>
 * It is an abstract class as it does not implement the method that actually does the analysis. Also, it contains member data
 * that are necessary to store any sort dependency information. However, it is the responsibility of the subclasses to store
 * the data and provide it via concrete implementation of abstract methods.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant doesPreProcessing() implies getPreProcessor() != null
 * @invariant getPreProcessing() != null implies doesPreProcessing()
 */
public abstract class DependencyAnalysis {
	/**
	 * This can used to store dependent->dependee direction of dependence information.  This map is reset via
	 * <code>reset</code> call.  Hence, it is recommended that the subclass use this store dependence information.
	 */
	protected final Map dependeeMap = new HashMap();

	/**
	 * This is similar to <code>dependeeMap</code> except the direction is dependee->dependent.
	 */
	protected final Map dependentMap = new HashMap();

	/**
	 * This maps the methods being analyzed to their control graphs.
	 *
	 * @invariant method2stmtGraph.oclIsKindOf(Map(SootMethod, StmtGraph))
	 */
	protected final Map method2stmtGraph = new HashMap();

	/**
	 * This contains auxiliary information required by the subclasses. It is recommended that this represent
	 * <code>java.util.Properties</code> but map a <code>String</code> to an <code>Object</code>.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected Map info = new HashMap();

	/**
	 * The pre-processor for this analysis, if one exists.
	 */
	protected IProcessor preprocessor;

	/**
	 * This manages the basic block graphs of methods.
	 */
	private final BasicBlockGraphMgr graphManager = new BasicBlockGraphMgr();

	/**
	 * Return the entities on which the <code>dependent</code> depends on in the given <code>context</code>.
	 *
	 * @param dependent of interest.
	 * @param context in which the dependency information is requested.
	 *
	 * @return a collection of objects.  The subclasses will further specify the  types of these entities.
	 *
	 * @post result != null
	 */
	public abstract Collection getDependees(Object dependent, Object context);

	/**
	 * Returns the entities which depend on the <code>dependee</code> in the given <code>context</code>.  The subclasses will
	 * further specify the  types of these entities.
	 *
	 * @param dependee of interest.
	 * @param context in which the dependency information is requested.
	 *
	 * @return a collection of objects.  The subclasses will further specify the  types of these entities.
	 *
	 * @post result != null
	 */
	public abstract Collection getDependents(Object dependee, Object context);

	/**
	 * Analyzes the given methods and classes for dependency information.
	 *
	 * @return <code>true</code> if the analysis completed; <code>false</code>, otherwise.  This is useful if the analysis
	 *            will proceed in stages/phases.
	 */
	public abstract boolean analyze();

	/**
	 * Returns the pre-processor.
	 *
	 * @return the pre-processor.
	 */
	public IProcessor getPreProcessor() {
		return preprocessor;
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
	 * Initializes the dependency analyzer with the information from the system to perform the analysis.  This will also
	 * reset the analysis.
	 *
	 * @param method2stmtGraph maps methods that constitute that analyzed system to their control flow graphs.
	 * @param info contains the value for the member variable <code>info</code>. Refer to {@link #info info} and subclass
	 *           documenation for more details.
	 *
	 * @pre method2stmtGraph.oclIsKindOf(java.util.Map(ca.mcgill.sable.soot.SootMethod,
	 *         ca.mcgill.sable.soot.jimple.CompleteStmtGraph))
	 * @pre classes <> null and method2stmtGraph <> null and info <> null
	 */
	public final void initialize(Map method2stmtGraph, Map info) {
		reset();
		this.info.putAll(info);
		this.method2stmtGraph.putAll(method2stmtGraph);
		setup();
	}

	/**
	 * Returns the statistics about dependency analysis in the form of a <code>String</code>.
	 *
	 * @return the statistics about dependency analysis.
	 */
	public String getStatistics() {
		return getClass() + " does not implement this method.";
	}

	/**
	 * Resets all internal data structures.
	 *
	 * @post dependeeMap.size() = 0 and dependentMap.size() = 0 and method2stmtGraph.size() = 0
	 */
	public void reset() {
		dependeeMap.clear();
		dependentMap.clear();
		method2stmtGraph.clear();
		info.clear();
	}

	/**
	 * Returns the basic block graph constructed from the given control flow graph.
	 *
	 * @param stmtGraph is a control flow graph.
	 *
	 * @return the basic block graph corresponding to <code>stmtGraph</code>.
	 *
	 * @pre stmtGraph <> null
	 */
	protected BasicBlockGraph getBasicBlockGraph(StmtGraph stmtGraph) {
		return graphManager.getBasicBlockGraph(stmtGraph);
	}

	/**
	 * Returns the basic block graph constructed from the given control flow graph.
	 *
	 * @param method for which the basic block graph is requested.
	 *
	 * @return the basic block graph corresponding to <code>stmtGraph</code>.
	 *
	 * @pre method <> null
	 */
	protected BasicBlockGraph getBasicBlockGraph(SootMethod method) {
		return graphManager.getBasicBlockGraph((StmtGraph) method2stmtGraph.get(method));
	}

	/**
	 * Returns the list of statements in the given method.
	 *
	 * @param method of interest.
	 *
	 * @return the list of statements.
	 *
	 * @pre method <> null
	 */
	protected StmtList getStmtList(SootMethod method) {
		StmtList result = null;
		StmtGraph stmtGraph = (StmtGraph) method2stmtGraph.get(method);

		if (stmtGraph != null) {
			result = ((JimpleBody) stmtGraph.getBody()).getStmtList();
		}
		return result;
	}

	/**
	 * Setup data structures after initialization.  This is a convenience method for subclasses to do processing after the
	 * calls to <code>initialize</code> and before the call to <code>preprocess</code>.
	 */
	protected void setup() {
	}
}

/*****
 ChangeLog:

$Log$

*****/
