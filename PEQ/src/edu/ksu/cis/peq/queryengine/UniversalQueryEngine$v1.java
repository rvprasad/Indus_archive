/*
 *
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
 
package edu.ksu.cis.peq.queryengine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.ksu.cis.peq.datastructures.WorkList;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import edu.ksu.cis.peq.graph.interfaces.INode;
import edu.ksu.cis.peq.queryengine.IQueryProgressListener.QueryProgressEvent;

/**
 * @author ganeshan
 *
 * This class implements the universal query engine as described 
 * in the liu paper.
 */
public class UniversalQueryEngine$v1 extends AbstractQueryEngine {
    /**
     * The result collection.
     */
    private Collection results = Collections.EMPTY_LIST;
            
    /**
     * The collection of listeners.
     * @inv listeners.oclIsKindOf(Set(IQueryProgressListener))
     */
    private Set listeners = new HashSet();
    
    
    
    /**
     * Constructor.
     * @param gEngine The graph engine instance.
     * @param fsm The fsm instance.
     * @param matcher The matcher instance.
     */
    public UniversalQueryEngine$v1(IGraphEngine gEngine, IFSM fsm, IMatcher matcher) {
        super(gEngine, fsm, matcher);
    }

    /** 
     * Return the results.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#getResults()
     */
    public Collection getResults() {
        return results;
    }

    /** Matches the labels on the edges from node to the transitions from state.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchNodeAndState(edu.ksu.cis.peq.graph.interfaces.INode, edu.ksu.cis.peq.fsm.interfaces.IState)
     */
    protected Set matchNodeAndState(INode node, IState state) {
        final Set _matchSet = new LinkedHashSet();
        final Set _edgeSet = node.getExitingEdges();
        final Set _transitionSet = state.getExitingTransitions();
        
        // TODO - Find a linear matching algorithm, quadratic is too expensive
        final Iterator _edgeIterator = _edgeSet.iterator();
        for (; _edgeIterator.hasNext();) {
            final IEdge _edge = (IEdge) _edgeIterator.next();
            final Iterator _transIterator = _transitionSet.iterator();    
            for (; _transIterator.hasNext();) {
                final ITransition _transition = (ITransition) _transIterator.next();
                final IFSMToken _token = matcher.getMatch(_edge, _transition);
                if (!_token.isEmpty()) {
                    _matchSet.add(_token);
                }                        
            }
        }                       
        
        return _matchSet;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchAndMergeReach(edu.ksu.cis.peq.graph.interfaces.INode, edu.ksu.cis.peq.fsm.interfaces.IState, edu.ksu.cis.peq.fsm.interfaces.IFSMToken, java.util.Set)
     */
    protected Set matchAndMergeReach(INode node, IState state,
            IFSMToken parent, Set reachSet) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#execute()
     */
    public void execute() {
        final Set _reachSet = new HashSet();        
        final WorkList  _workList = new WorkList();
        
        fireProgressEvent(this, "Initializing work list", null);
        
        _workList.addAll(initializeWorkSet());        
        final Set _resultSet  = new HashSet();

    }
    
    
    /**
     * Initialize the work set with the information pertaining to the starting nodes.
     * @return Set The collection of initial items to process.
     * @pre Graph != null and FSM != null
     * @post Result.oclIsKindOf(Set(IFSMToken))
     */
    private Set initializeWorkSet() {
        final Set _initSet = new LinkedHashSet();
        final Set _initNodes = gEngine.getInitialNodes();
        
        final Iterator _nodeIterator = _initNodes.iterator();
        final IState _state = fsm.getInitialState();
        
        for (; _nodeIterator.hasNext();) {
            final INode _node = (INode) _nodeIterator.next();
            _initSet.addAll(matchNodeAndState(_node, _state));            
        }                       
        
        return _initSet;
    }
    
    /**
     * Send the event message to the listeners.
     * @param source The source of the message.
     * @param message The message to be sent.
     * @param information The information.
     */
    private void fireProgressEvent(UniversalQueryEngine$v1 source, String message, Object information) {
        final QueryProgressEvent _event = new QueryProgressEvent(source, message, information);
        
        final Thread _t = new Thread() {
            public void run() {
                synchronized(listeners) {
                    final Iterator _it = listeners.iterator();
                    while (_it.hasNext()) {
                        ((IQueryProgressListener) _it.next()).queryProgress(_event);
                    }
                }
            }
        };
        _t.start();
        
    }

    /**
     * Adds the given listener to the set of listeners.
     * Has no effect if the listener is already present.
     * @param listener The query progress listener
     */
    public void addListener(final IQueryProgressListener queryListener) {
        synchronized(listeners) {
            if (!listeners.contains(queryListener)) {
                listeners.add(queryListener);
            }            
        }
    }
    
    /**
     * Removes the given listener from the registered listeners list.
     * Has no effect if the listener is not already present.
     * @param queryListner The query listener to remove.
     */
    public void removeListener(final IQueryProgressListener queryListener) {
        synchronized(listeners) {
            if (listeners.contains(queryListener)) {
                listeners.remove(queryListener);
            }
        }
    }

}
