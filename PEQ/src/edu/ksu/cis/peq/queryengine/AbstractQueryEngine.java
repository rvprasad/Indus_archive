/*
 * PEQ, a parameteric regular path query library
 * Copyright (c) 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 *
 *
 * Created on March 8, 2005, 8:54 PM
 */

package edu.ksu.cis.peq.queryengine;

import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import java.util.Collection;

/**
 * @author ganeshan
 *
 * This is the abstrat implementation of the query engine.
 * Subclasses are expected to implement the execute() method
 * according to the particular algorithm employed.
 */
public abstract class AbstractQueryEngine {
    
    /** This is the graph engine instance. */
    protected IGraphEngine gEngine;
    /** This is the fsm instance */
    protected IFSM fsm;
    /** This is the matcher instance */
    protected IMatcher matcher;
    
    
    /** Creates a new instance of QueryEngine 
     * @param gEngine The graph engine instance.
     * @param fsm The finite state machine instance
     * @param matcher The label matcher instance
     * @pre gEngine != null
     * @pre fsm != null
     * @pre matcher != null
     */
    public AbstractQueryEngine(final IGraphEngine gEngine, final IFSM fsm, final IMatcher matcher) {
        this.gEngine = gEngine;
        this.fsm = fsm;
        this.matcher = matcher;
    }
    
    
    /**
     * Gets the results of the query evaluation.
     * @post Result.size > 0 => Result.oclIsKindOf(Collection(Sequence(IFSMToken)))
     */
    public abstract Collection getResults();    
    
    /** Run the query engine */
    public abstract void execute();
}
