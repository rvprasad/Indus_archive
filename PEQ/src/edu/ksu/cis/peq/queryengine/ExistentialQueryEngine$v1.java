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


import edu.ksu.cis.peq.datastructures.WorkList;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import edu.ksu.cis.peq.graph.interfaces.INode;
import edu.ksu.cis.peq.queryengine.IQueryProgressListener.QueryProgressEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * The existential query engine with wilcard and constructor matching (First Cut)
 * @author ganeshan
 * This is the first cut of the existential query engine.
 */
public class ExistentialQueryEngine$v1 extends AbstractQueryEngine {
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
        final Set _reachSet = new HashSet();        
        final WorkList  _workList = new WorkList();
        
        fireProgressEvent(this, "Initializing work list", null);
        
        _workList.addAll(initializeWorkSet());        
        final Set _resultSet  = new HashSet();
        
        while (_workList.hasWork()) {
            final IFSMToken _fsmToken = (IFSMToken) _workList.getWork();
            _reachSet.add(_fsmToken);
            
            // Reach information calculation
            final INode _reachNode = _fsmToken.getGraphEdge().getDstnNode();
            final IState _reachState = _fsmToken.getTransitionEdge().getDstnState();
            if (_reachNode != null && _reachState != null) {
               _workList.addAll(matchAndMergeReach(_reachNode, _reachState, _fsmToken));
            }
            
            final String _msg = "Processed node : " + _reachNode + " State : " + _reachState;
            fireProgressEvent(this, _msg, null);
            
            if (_reachState.isFinalState()) {
                _resultSet.add(_fsmToken);
                fireProgressEvent(this, "Query result complete" , null);
            }
        }
        postProcess(_resultSet);
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
     * Matches the labels on the edges from node to the transitions from state.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchNodeAndState()             
     */
    protected Set matchNodeAndState(final INode node, final IState state) {
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
    
    /**
     * Matches the labels on the edges from node to the transitions from state for the reach information.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchAndmergeReach()             
     */
    protected Set matchAndMergeReach(final INode node, final IState state, final IFSMToken parent) {
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
                    final IFSMToken _mergedToken = matcher.merge(parent, _token);
                    if (!_mergedToken.isEmpty()) {
                        _matchSet.add(_mergedToken);
                    }
                }
            }
        }
        
        return _matchSet;
    }
    
    
    /**
     * Process the result tokens to preserve the post condition of the result viz Collection(Sequence(IFSMToken))
     * @pre resultSet.oclIsKindOf(Set(IFSMToken))
     */
    protected void postProcess(final Set resultSet) {
        if (resultSet.size() > 0) {            
            results = new LinkedHashSet();
            
            final Iterator _tokenIterator = resultSet.iterator();
            for (; _tokenIterator.hasNext();) {
                final IFSMToken _rtoken  = (IFSMToken) _tokenIterator.next();                
                
                // Push the result token                
                final List _lstResult = new LinkedList();
                _lstResult.add(_rtoken);                
                
                // Create the path to the root node token
                IFSMToken _currToken = _rtoken;                
                while (_currToken.getParent() != null) {
                    _lstResult.add(_currToken);
                    _currToken = _currToken.getParent();                    
                }        
                // This is ok because it runs in linear time, the time complexity
                // is only increased by a constant factor.
                Collections.reverse(_lstResult);
                results.add(_lstResult);
            }
            
        }
    }
    
    /**
     * Intimates the listeners on the progress inside the query engine.
     * @param source The source of the event
     * @param message The message associated with the event
     * @param information The information object associated with the event
     */
    private void fireProgressEvent(final Object source, final String message, final Object information) {
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
    
    
    /**
     * Returns the results.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#getResults()
     */
    public Collection getResults() {        
        return results;        
    }
    
}
