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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * Maps states to a boolean indicating if all the matching states are 
     * final states.
     * @inv Map.keys.oclIsKindOf(IState) and Map.values.oclIsKindOf(Boolean)
     */
    private Map tMap = new HashMap();
    
    /**
     * Maps states to the query results.
     * @inv Map.keys.oclIsKindOf(IState) and Map.values.oclIsKindOf(Collection(IFSMToken)). 
     */
    private Map uMap = new HashMap();
    
    
    /**
     * Constructor.
     * @param gEngine The graph engine instance.
     * @param fsm The fsm instance.
     * @param matcher The matcher instance.
     */
    public UniversalQueryEngine$v1(IGraphEngine gEngine, IFSM fsm, IUQMatcher matcher) {
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
            MatchPair  _mPair = null;
            final Iterator _transIterator = _transitionSet.iterator();    
            for (; _transIterator.hasNext();) {
                final ITransition _transition = (ITransition) _transIterator.next();
                final IFSMToken _token = matcher.getMatch(_edge, _transition);
                if (!_token.isEmpty()) {
                    if (_mPair == null) {
                        _mPair = new MatchPair();
                        _mPair.setTransition(_transition);
                        _mPair.setSubstMap(_token.getSubstituitionMap());
                        _matchSet.add(_token);
                    } else { // Check determinism
                         final boolean _dCheck = _mPair.getTransitionEdge().equals(_token.getTransitionEdge()) &&
                          _mPair.getSubstituitionMap().equals(_token.getSubstituitionMap());
                         if (!_dCheck) {
                             throw new RuntimeException("Determinism check failed");
                         }
                    }
                    
                }
                if (_mPair  == null) {
                    _matchSet.add(((IUQMatcher) matcher).createBadToken(_edge));
                }
                // have to inject <v, badstate, badsubst> but dont see its use.
                // revised. algorithm doesnt work without that.
            }
            
        }                       
        
        return _matchSet;
    }

    /** Match and merge the reach set.
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchAndMergeReach(edu.ksu.cis.peq.graph.interfaces.INode, edu.ksu.cis.peq.fsm.interfaces.IState, edu.ksu.cis.peq.fsm.interfaces.IFSMToken, java.util.Set)
     */
    protected Set matchAndMergeReach(INode node, IState state,
            IFSMToken parent, Set reachSet) {
        final Set _matchSet = new LinkedHashSet();
        final Set _edgeSet = node.getExitingEdges();
        final Set _transitionSet = state.getExitingTransitions();
        
        // TODO - Find a linear matching algorithm, quadratic is too expensive
        final Iterator _edgeIterator = _edgeSet.iterator();
        for (; _edgeIterator.hasNext();) {
            final IEdge _edge = (IEdge) _edgeIterator.next();
            MatchPair _mPair = null;
            final Iterator _transIterator = _transitionSet.iterator();    
            for (; _transIterator.hasNext();) {
                final ITransition _transition = (ITransition) _transIterator.next();
                final IFSMToken _token = matcher.getMatch(_edge, _transition);
                if (!_token.isEmpty()) {
                    final IFSMToken _mergedToken = matcher.merge(parent, _token);
                    if (!_mergedToken.isEmpty()) {
                        if (_mPair == null) {
                            _mPair = new MatchPair();
                            _mPair.setTransition(_mergedToken.getTransitionEdge());
                            _mPair.setSubstMap(_mergedToken.getSubstituitionMap());
                            if (!reachSet.contains(_mergedToken)) {
                                _matchSet.add(_mergedToken);
                            }
                        } else {
                            final boolean _dCheck = _mPair.getTransitionEdge().equals(_token.getTransitionEdge()) &&
                            _mPair.getSubstituitionMap().equals(_token.getSubstituitionMap());
                           if (!_dCheck) {
                               throw new RuntimeException("Determinism check failed");
                           }
                        }                        
                    }                    
                }
                if (_mPair == null) {
                    final IFSMToken _badToken = ((IUQMatcher) matcher).createBadToken(_edge);
                    if (!reachSet.contains(_badToken)) {
                        _matchSet.add(_badToken);
                    }
                }
            }
        }
        
        return _matchSet;
    }

   
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#execute()
     */
    public void execute() {
        final Set _reachSet = new HashSet();        
        final WorkList  _workList = new WorkList();
        
        fireProgressEvent(this, "Initializing work list", null);
        
        _workList.addAll(initializeWorkSet());        
        
        while (_workList.hasWork()) {
            final IFSMToken _fsmToken = (IFSMToken) _workList.getWork();
            _reachSet.add(_fsmToken);
            
            // Reach information calculation
            final INode _reachNode = _fsmToken.getGraphEdge().getDstnNode();
            final IState _reachState = _fsmToken.getTransitionEdge().getDstnState();
            if (_reachNode != null && _reachState != null) {
                _workList.addAll(matchAndMergeReach(_reachNode, _reachState, _fsmToken, _reachSet));
            }
             
           // matcher.createEmptyToken();
            Boolean _b = (Boolean) tMap.get(_reachNode);
            if (_b == null || _b.booleanValue()) {
                final boolean _result = _reachState.isFinalState();
                final Boolean _rBool = new Boolean(_result);
                tMap.put(_reachNode, _rBool);
            }
            _b = (Boolean) tMap.get(_reachNode);
            if (_b.booleanValue()) {
                final IFSMToken _tok = (IFSMToken) uMap.get(_reachNode);
                if (_tok == null) {
                    uMap.put(_reachNode, _fsmToken);
                } else {
                    updateMap(_tok.getSubstituitionMap(), _fsmToken.getSubstituitionMap());
                }
            } else {
                uMap.put(_reachNode, null);	
            }
            final String _msg = "Processed node : " + _reachNode + " State : " + _reachState;
            fireProgressEvent(this, _msg, null);
        }

        postProcess();
    }
    
    
    /**
     * Update the target map with the contents of the source map. 
     * @param substituitionMap
     * @param substituitionMap2
     */
    private void updateMap(Map targetMap, Map sourceMap) {
        final Set _parentSet = sourceMap.entrySet();
        for (final Iterator iter = _parentSet.iterator(); iter.hasNext();) {
            final Map.Entry _entry = (Map.Entry) iter.next();
            final Object _key = _entry.getKey();
            final Object _val = _entry.getValue();
            if (targetMap.containsKey(_key)) {
                final Object _oldObj = targetMap.get(_key);
                if (!_oldObj.equals(_val)) {
                    throw new RuntimeException("Parameter update conflict");
                }
                
            } else {
                targetMap.put(_key, _val);
            }
        }
        
    }

    /**
     * Post process the results.
     */
    private void postProcess() {
        final Set _result = new HashSet();
        final Set _resultSet = tMap.entrySet();
        for (Iterator iter = _resultSet.iterator(); iter.hasNext();) {
            final Map.Entry _entry = (Map.Entry) iter.next();
            final INode _node  = (INode) _entry.getKey();
            final Boolean _val = (Boolean) _entry.getValue();
            if (_val.booleanValue()) {
               final IFSMToken _token = (IFSMToken) uMap.get(_node);
               if (_token != null && !_token.isEmpty()) {
                   _result.add(_token);
               }
            }            
        }
        meetResultPC(_result);
        
    }

    /**
     * Meets the post condition by changing the given set of fsm tokens to the form amenable 
     * for the GUI.
     * @param resultSet The collection of IFSMToken results.
     */
    private void meetResultPC(Set resultSet) {
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
                    _lstResult.add(_currToken.getParent());
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
