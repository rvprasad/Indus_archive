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
 * Created on March 8, 2005, 9:55 PM
 */

package edu.ksu.cis.peq.queryengine;

import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import java.util.Collection;
import java.util.Collections;

/**
 * The existential query engine with wilcard and constructor matching (First Cut)
 * @author ganeshan
 * This is the first cut of the existential query engine.
 */
public class ExistentialQueryEngine$v1 extends AbstractQueryEngine {
    
    private Collection results = Collections.EMPTY_LIST;
            
    
    /**
     * Creates a new instance of ExistentialQueryEngine$v1
     * @param engine The graph engine instance
     * @param fsm The finite state machine instance
     * @param matcher The matcher instance.
     */
    public ExistentialQueryEngine$v1(final IGraphEngine engine, final IFSM fsm, final IMatcher matcher) {
        super(engine, fsm, matcher);
    } 
    
    
    /**
     * Runs the engine on the query
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#execute()             
     */
    public void execute() {
        // TODO - Implement me!!
    }

    /**
     * Returns the results.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#getResults()
     */
    public Collection getResults() {
        return results;
    }
    
}
