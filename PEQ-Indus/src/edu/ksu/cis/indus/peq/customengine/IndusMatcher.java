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
import java.util.Map;
import java.util.Set;

import edu.ksu.cis.indus.peq.constructors.GeneralConstructor;
import edu.ksu.cis.indus.peq.fsm.FSMToken;
import edu.ksu.cis.indus.peq.graph.Edge;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.queryengine.IMatcher;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IndusMatcher implements IMatcher {

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.IMatcher#getMatch(edu.ksu.cis.peq.graph.interfaces.IEdge, edu.ksu.cis.peq.fsm.interfaces.ITransition)
     */
    public IFSMToken getMatch(IEdge edge, ITransition transition) {
        FSMToken _token = null;
        final GeneralConstructor _gc = (GeneralConstructor)  edge.getConstructor();
        try {
            _token = (FSMToken)_gc.match((GeneralConstructor) transition.getLabel(), (Edge) edge);
            if (!_token.isEmpty()) {
                _token.setTheedge(edge);
                _token.setThetransition(transition);
            }
        } catch (IllegalAccessException e) {            
            _token = new FSMToken();
            _token.setEmpty(true);
        }
        return _token;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.IMatcher#merge(edu.ksu.cis.peq.fsm.interfaces.IFSMToken, edu.ksu.cis.peq.fsm.interfaces.IFSMToken)
     */
    public IFSMToken merge(IFSMToken sourceToken, IFSMToken childToken) {
        final FSMToken _parent  = (FSMToken) sourceToken;
        final FSMToken _child = (FSMToken) childToken;
        final Map _childMap = _child.getSubstituitionMap();
        final Map _parentMap = _parent.getSubstituitionMap();
        final Set _resultMap = _parentMap.entrySet();
        
        FSMToken _mergeResult = _child;
        for (Iterator iter = _resultMap.iterator(); iter.hasNext();) {
            final Map.Entry _entry = (Map.Entry) iter.next();
            final Object _varName = _entry.getKey();
            if (_childMap.containsKey(_varName)) {
                final Object _childVal = _childMap.get(_varName);
                if (!_childVal.equals(_entry.getValue())) {
                    // Incompatible substitution. Discard this result.
                    _mergeResult = new FSMToken();
                    _mergeResult.setEmpty(true);
                    break;
                }                        
            } else {
                _childMap.put(_entry.getKey(), _entry.getValue()); // Union.
            }
        }        
        
        if (!_mergeResult.isEmpty()) {
            _mergeResult.setParent(sourceToken);
        }
        return _mergeResult;
    }

}
