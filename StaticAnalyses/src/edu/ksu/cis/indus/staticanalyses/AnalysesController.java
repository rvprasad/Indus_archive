
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.SootMethod;

import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.indus.interfaces.ISystemInfo;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * This class is provides the control class for the analyses suite. The analyses progress in phases. It may be so that some
 * application require a particular sequence in which each analysis should progress. Hence, the applications provide an
 * implementation of controller interface to drive the analyses in a particular sequence of phases.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractAnalysesController
  implements ISystemInfo {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractAnalysesController.class);

	/**
	 * The collection of names used to identify various analyses.  This is just a collection of the above defined constants.
	 *
	 * @invariant participatingAnalysesIDs != null
	 */
//	protected Collection participatingAnalysesIDs;

	/**
	 * The collection of analysis which want to preprocess the system.
	 *
	 * @invariant preprocessors != null
	 */
	protected final Collection preprocessors;

	/**
	 * A map from methods(<code>SootMethod</code>) to their complete statement graph(<code>CompleteStmtGraph</code>).
	 *
	 * @invariant method2cmpltstmtGraph != null
	 */
	protected final Map method2cmpltStmtGraph;

	/**
	 * The map of analysis being controlled by this object. It maps names of analysis to the analysis object.
	 *
	 * @invariant participatingAnalyses != null
	 * @invariant participatingAnalyses.oclIsTypeOf(Map(Object, DependencyAnalysis)
	 */
	protected final Map participatingAnalyses;

	/**
	 * This is the preprocessing controlling agent.
	 *
	 * @invariant preprocessController != null;
	 */
	protected final ProcessingController preprocessController;

	/**
	 * The status of this controller and it's controllees.  The information provided by it's controllees is valid only when
	 * this field is <code>true</code>.
	 */
	protected boolean stable = false;

	/**
	 * This is a map of name to objects which provide information that maybe used by analyses, but is of no use to the
	 * controller.
	 */
	private Map info;

	/**
	 * Creates a new AbstractAnalysesController object.
	 *
	 * @param infoPrm is a map of name to objects which provide information that maybe used by analyses, but is of no use to
	 * 		  the controller.
	 * @param pc is the preprocessing controller.
	 *
	 * @pre pc != null;
	 */
	public AbstractAnalysesController(final Map infoPrm, final ProcessingController pc) {
		participatingAnalyses = new HashMap();
		method2cmpltStmtGraph = new HashMap();
		preprocessors = new HashSet();
		this.info = infoPrm;
		this.preprocessController = pc;
	}

	/**
	 * Sets the implementation to be used for an analysis.
	 *
	 * @param id of the analysis.
	 * @param analysis is the implementation of the named analysis.
	 *
	 * @throws IllegalArgumentException when <code>name</code> is not one of the <code>XXXX_DA</code> defined in this class.
	 *
	 * @pre id != null and analysis != null
	 */
	public final void setAnalysis(final Object id, final AbstractAnalysis analysis) {
		participatingAnalyses.put(id, analysis);

		if (analysis.doesPreProcessing()) {
			IProcessor p = analysis.getPreProcessor();
			preprocessors.add(p);
			p.hookup(preprocessController);
		}
	}

	/**
	 * Provides the implementation registered for the given analysis purpose.
	 *
	 * @param id of the requested analysis.  This has to be one of the names(XXX_DA) defined in this class.
	 *
	 * @return the implementation registered for the given purpose.  <code>null</code>, if there is no registered analysis.
	 */
	public final AbstractAnalysis getAnalysis(final Object id) {
		AbstractAnalysis result = null;

		if (participatingAnalyses != null) {
			result = (AbstractAnalysis) participatingAnalyses.get(id);
		}
		return result;
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
	public UnitGraph getStmtGraph(final SootMethod method) {
		return (UnitGraph) method2cmpltStmtGraph.get(method);
	}

	/**
	 * Executes the analyses in the registered order.
	 */
	public void execute() {
		boolean analyzing;
		Collection done = new ArrayList();

		do {
			analyzing = false;

			for (Iterator i = participatingAnalyses.keySet().iterator(); i.hasNext();) {
				String daName = (String) i.next();
				AbstractAnalysis temp = (AbstractAnalysis) participatingAnalyses.get(daName);

				if (temp != null && !done.contains(temp)) {
					boolean t = temp.analyze();

					if (t) {
						done.add(temp);
					}
					analyzing |= t;
				}
			}
		} while (analyzing);
		stable = true;
	}

	/**
	 * Initializes the controller.  The data structures dependent on <code>methods</code> are initialized, the interested
	 * analyses are asked to preprocess the data, and then the analyses are initialized.
	 *
	 * @param methods that form the system to be analyzed.
	 *
	 * @pre methods != null
	 */
	public void initialize(final Collection methods) {
		stable = false;

		Collection failed = new ArrayList();

		preprocessController.process();

		for (Iterator i = methods.iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			CompleteUnitGraph sg = new CompleteUnitGraph(method.retrieveActiveBody());
			method2cmpltStmtGraph.put(method, sg);
		}

		for (Iterator k = participatingAnalyses.keySet().iterator(); k.hasNext();) {
			Object key = k.next();
			AbstractAnalysis da = (AbstractAnalysis) participatingAnalyses.get(key);

			try {
				da.initialize(method2cmpltStmtGraph, info);
			} catch (InitializationException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(da.getClass() + " failed to initialize, hence, it will not executed.", e);
				}
				failed.add(key);
			}
		}

		for (Iterator i = failed.iterator(); i.hasNext();) {
			participatingAnalyses.remove(i.next());
		}
	}

	/**
	 * Resets the internal data structures of the controller.  This resets the participating analyses.  This does not reset
	 * the Object Flow AbstractAnalysis instance.
	 */
	public void reset() {
		preprocessors.clear();

		for (Iterator i = participatingAnalyses.values().iterator(); i.hasNext();) {
			AbstractAnalysis element = (DependencyAnalysis) i.next();
			element.reset();
		}
		participatingAnalyses.clear();
		method2cmpltStmtGraph.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2003/08/25 08:06:39  venku
   Renamed participatingAnalysesNames to participatingAnalysesIDs.
   AbstractAnalysesController now has a method to extract the above field.
   Revision 1.11  2003/08/25 07:28:01  venku
   Ripple effect of renaming AbstractController to AbstractAnalysesController.
   Revision 1.10  2003/08/18 04:44:35  venku
   Established an interface which will provide the information about the underlying system as required by transformations.
   It is called ISystemInfo.
   Ripple effect of the above change.
   Revision 1.9  2003/08/18 04:10:10  venku
   Documentation change.
   Revision 1.8  2003/08/18 04:08:22  venku
   Removed unnecessary method.
   Revision 1.7  2003/08/18 00:59:50  venku
   Changed specification to fit the last change.
   Revision 1.6  2003/08/18 00:59:11  venku
   Changed the type of the IDs to java.lang.Object to provide extensibility.
   Ripple effect of that happens in AbstractController.
   Revision 1.5  2003/08/16 02:41:37  venku
   Renamed AController to AbstractController.
   Renamed AAnalysis to AbstractAnalysis.
   Revision 1.4  2003/08/15 08:23:09  venku
   Renamed getDAnalysis to getAnalysis.
   Revision 1.3  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.2  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
