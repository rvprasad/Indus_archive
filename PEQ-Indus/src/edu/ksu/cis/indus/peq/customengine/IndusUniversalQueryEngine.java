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
 
package edu.ksu.cis.indus.peq.customengine;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.ksu.cis.indus.peq.graph.GraphBuilder;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.graph.interfaces.IGraphEngine;
import edu.ksu.cis.peq.graph.interfaces.INode;
import edu.ksu.cis.peq.queryengine.IUQMatcher;
import edu.ksu.cis.peq.queryengine.MatchPair;
import edu.ksu.cis.peq.queryengine.UniversalQueryEngine$v1;

/**
 * @author ganeshan
 *
 * Adaptation of the generalized univeral query engine for Indus.
 */
public class IndusUniversalQueryEngine extends UniversalQueryEngine$v1 {

    /**
     * ConstructorAST.
     * @param gEngine
     * @param fsm
     * @param matcher
     */
    public IndusUniversalQueryEngine(IGraphEngine gEngine, IFSM fsm, IUQMatcher matcher) {
        super(gEngine, fsm, matcher);        
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchAndMergeReach(edu.ksu.cis.peq.graph.interfaces.INode, edu.ksu.cis.peq.fsm.interfaces.IState, edu.ksu.cis.peq.fsm.interfaces.IFSMToken, java.util.Set)
     */
    protected Set matchAndMergeReach(INode node, IState state,
            IFSMToken parent, Set reachSet) {
        final Set _matchSet = new LinkedHashSet();        
        final GraphBuilder _gBuilder = (GraphBuilder) gEngine;
        final Set _edgeSet =  _gBuilder.getOutgoingEdges(node);
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
     * @see edu.ksu.cis.peq.queryengine.AbstractQueryEngine#matchNodeAndState(edu.ksu.cis.peq.graph.interfaces.INode, edu.ksu.cis.peq.fsm.interfaces.IState)
     */
    protected Set matchNodeAndState(INode node, IState state) {
        final Set _matchSet = new LinkedHashSet();
        final GraphBuilder _gBuilder = (GraphBuilder) gEngine;
        final Set _edgeSet =  _gBuilder.getOutgoingEdges(node);
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
            }
            
        }                       
        
        return _matchSet;
    }
}
