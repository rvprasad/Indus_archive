
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

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootMethod;
import soot.toolkits.graph.UnitGraph;

/**
 * DOCUMENT ME!
 * <p></p>
 * 
 * @version $Revision$ 
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 */
public abstract class AAnalysis {
    
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
        private final BasicBlockGraphMgr graphManager = new BasicBlockGraphMgr();

    
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
     * @pre method2stmtGraph.oclIsKindOf(java.util.Map(soot.SootMethod,
     *         soot.jimple.CompleteUnitGraph))
     * @pre classes <> null and method2stmtGraph <> null and info <> null
     */
    public final void initialize(Map method2stmtGraph, Map info) throws InitializationException {
        reset();
        this.info.putAll(info);
        this.method2stmtGraph.putAll(method2stmtGraph);
        setup();
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
        protected BasicBlockGraph getBasicBlockGraph(UnitGraph stmtGraph) {
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
            return graphManager.getBasicBlockGraph((UnitGraph) method2stmtGraph.get(method));
        }

        /**
         * Returns the list of statements in the given method.  Each call returns a new list.
         *
         * @param method of interest.
         *
         * @return the list of statements.
         *
         * @pre method <> null and result->forall(o | o.isOclKindOf(Stmt))
         */
        protected List getStmtList(SootMethod method) {
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
         */
        protected void setup() throws InitializationException {
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
 Resets all internal data structures.
   *
    * @post info.size() == 0 and method2stmtGraph.size() == 0
   */
        public void reset() {
            method2stmtGraph.clear();
                    info.clear();
        }

}

/*****
 ChangeLog:

$Log$

*****/
