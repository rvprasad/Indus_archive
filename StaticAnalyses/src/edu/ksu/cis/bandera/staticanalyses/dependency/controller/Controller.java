
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

package edu.ksu.cis.bandera.staticanalyses.dependency.controller;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.StmtGraph;

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.bandera.staticanalyses.interfaces.IProcessor;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * <p>
 * This class is provides the control interface to the dependency analyses suite. The analyses progress in phases. It may be
 * so that some application require a particular sequence in which each analysis should progress. Hence, the applications
 * provide an implementation of controller interface to drive the analyses in a particular sequence of phases.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class Controller {
	/**
	 * <p>
	 * This identifies class data related dependency analysis.
	 * </p>
	 */
	public static final String CLASS_DATA_DA = "CLASS_DATA_DA";

	/**
	 * <p>
	 * This identifies control dependency analysis.
	 * </p>
	 */
	public static final String CONTROL_DA = "CONTROL_DA";

	/**
	 * <p>
	 * This identifies divergence dependency analysis.
	 * </p>
	 */
	public static final String DIVERGENCE_DA = "DIVERGENCE_DA";

	/**
	 * <p>
	 * This identifies inteference dependency analysis.
	 * </p>
	 */
	public static final String INTERFERENCE_DA = "INTERFERENCE_DA";

	/**
	 * <p>
	 * This identifies method local data dependency analysis.
	 * </p>
	 */
	public static final String METHOD_LOCAL_DATA_DA = "METHOD_LOCAL_DATA_DA";

	/**
	 * <p>
	 * This identifies ready dependency analysis.
	 * </p>
	 */
	public static final String READY_DA = "READY_DA";

	/**
	 * <p>
	 * This identifies synchronization dependency analysis.
	 * </p>
	 */
	public static final String SYNCHRONIZATION_DA = "SYNCHRONIZATION_DA";

	/**
	 * <p>
	 * The collection of names used to identify various analyses.  This is just a collection of the above defined constants.
	 * </p>
	 */
	protected Collection participatingAnalysesNames;

	/**
	 * <p>
	 * The collection of analysis which want to preprocess the system.
	 * </p>
	 */
	protected final Collection preprocessors;

	/**
	 * <p>
	 * A map from methods(<code>SootMethod</code>) to their complete statement graph(<code>CompleteStmtGraph</code>).
	 * </p>
	 */
	protected final Map method2cmpltStmtGraph;

	/**
	 * <p>
	 * The map of analysis being controlled by this object. It maps names(<code>String</code>) of analysis to the analysis
	 * (<code>DependencyAnalysis</code>) object.
	 * </p>
	 */
	protected final Map participatingAnalyses;

	/**
	 * This is the preprocessing controlling agent.
	 */
	protected final ProcessingController preprocessController;

	/**
	 * <p>
	 * The status of this controller and it's controllees.  The information provided by it's controllees is valid only when
	 * this field is true.
	 * </p>
	 */
	protected boolean stable = false;

	/**
	 * <p>
	 * This is a map of name to objects which provide information that maybe used by analyses, but is of no use to the
	 * controller.
	 * </p>
	 */
	private Map info;

	/**
	 * <p>
	 * Creates a new Controller object.
	 * </p>
	 *
	 * @param info is a map of name to objects which provide information that maybe used by analyses, but is of no use to the
	 *           controller.
	 * @param pc is the preprocessing controller.
	 *
	 * @pre pc != null;
	 */
	public Controller(Map info, ProcessingController pc) {
		participatingAnalyses = new HashMap();
		method2cmpltStmtGraph = new HashMap();
		preprocessors = new HashSet();
		this.info = info;
		this.preprocessController = pc;
	}

	/**
	 * <p>
	 * Executes the registered analyses.  This should be implemented by the subclasses.  The subclasses should set
	 * <code>stable</code> to a suitable value before returning.
	 * </p>
	 */
	public abstract void execute();

	/**
	 * <p>
	 * Provides the implementation registered for the given analysis purpose.
	 * </p>
	 *
	 * @param name of the requested analysis.  This has to be one of the names(XXX_DA) defined in this class.
	 *
	 * @return the implementation registered for the given purpose.  <code>null</code>, if there is no registered analysis.
	 */
	public final DependencyAnalysis getDAnalysis(String name) {
		return (DependencyAnalysis) participatingAnalyses.get(name);
	}

	/**
	 * <p>
	 * Provides the execution status of this controllers and it's controllees.
	 * </p>
	 *
	 * @return <code>true</code> if all the participating analyses have completed; <code>false</code>, otherwise.
	 */
	public final boolean isStable() {
		return stable;
	}

	/**
	 * Provides the statement graph for the given method.
	 *
	 * @param method for which the statement graph is requested.
	 *
	 * @return the statement graph.  <code>null</code> is returned if the method was not processed.
	 *
	 * @pre method != null
	 * @post method2cmpltStmtGraph.contains(method) == false implies result = null
	 */
	public StmtGraph getStmtGraph(SootMethod method) {
		return (StmtGraph) method2cmpltStmtGraph.get(method);
	}

	/**
	 * <p>
	 * Initializes the controller.  The data structures dependent on <code>methods</code> are initialized, the interested
	 * analyses are asked to preprocess the data, and then the analyses are initialized.
	 * </p>
	 *
	 * @param methods that form the system to be analyzed.
	 */
	public void initialize(Collection methods) {
		stable = false;

		preprocessController.process();

		for (Iterator i = methods.iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			CompleteStmtGraph sg = new CompleteStmtGraph((Util.getJimpleBody(method)).getStmtList());
			method2cmpltStmtGraph.put(method, sg);
		}

		for (Iterator k = participatingAnalyses.values().iterator(); k.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) k.next();
			da.initialize(method2cmpltStmtGraph, info);
		}
	}

	/**
	 * <p>
	 * Resets the internal data structures of the controller.  This resets the participating analyses.  This does not reset
	 * the Object Flow Analysis instance.
	 * </p>
	 */
	public void reset() {
		preprocessors.clear();

		for (Iterator i = participatingAnalyses.values().iterator(); i.hasNext();) {
			DependencyAnalysis element = (DependencyAnalysis) i.next();
			element.reset();
		}
		participatingAnalyses.clear();
		method2cmpltStmtGraph.clear();
	}

	/**
	 * <p>
	 * Sets the implementation to be used for an analysis.
	 * </p>
	 *
	 * @param name of the analysis.
	 * @param analysis is the implementation of the named analysis.
	 *
	 * @throws IllegalArgumentException when <code>name</code> is not one of the <code>XXXX_DA</code> defined in this class.
	 */
	protected final void setDAnalysis(String name, DependencyAnalysis analysis) throws IllegalArgumentException {
		if (!participatingAnalysesNames.contains(name)) {
			throw new IllegalArgumentException("name argument has to be one of the XXXX_DA.");
		}
		participatingAnalyses.put(name, analysis);

		if (analysis.doesPreProcessing()) {
			IProcessor p = analysis.getPreProcessor();
			preprocessors.add(p);
			p.hookup(preprocessController);
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
