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
 
package edu.ksu.cis.peq.test.misc;



import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.queryengine.IUQMatcher;

import edu.ksu.cis.peq.test.constructors.BadConstructor;
import edu.ksu.cis.peq.test.fsm.State;
import edu.ksu.cis.peq.test.fsm.Transition;
import edu.ksu.cis.peq.test.fsm.FSMToken;


/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Matcher implements IUQMatcher {

    private State badInitState = new State("bad init state");
    private State badEndState = new State("bad end state");
    private Transition badTrans = new Transition("Bad Transition");
    
    public Matcher() {
        badTrans.setSrcState(badInitState);
        badTrans.setDstnState(badEndState);
        badTrans.setLabel(new BadConstructor());
        badInitState.addExitingTransitions(badTrans);
        badEndState.addEnteringTransitions(badTrans);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.IMatcher#getMatch(edu.ksu.cis.peq.graph.interfaces.IEdge, edu.ksu.cis.peq.fsm.interfaces.ITransition)
     */
    public IFSMToken getMatch(IEdge arg0, ITransition arg1) {
        edu.ksu.cis.peq.test.fsm.FSMToken token = new edu.ksu.cis.peq.test.fsm.FSMToken();
        
        if (arg0.getConstructor().getClass().equals(arg1.getLabel().getClass())) {
            token.setTheedge(arg0);
            token.setThetransition(arg1);
            token.setEmpty(false);
            
        } else {
            token.setEmpty(true);
        }
        return token;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.IMatcher#merge(edu.ksu.cis.peq.fsm.interfaces.IFSMToken, edu.ksu.cis.peq.fsm.interfaces.IFSMToken)
     */
    public IFSMToken merge(IFSMToken arg0, IFSMToken arg1) {
        arg1.setParent(arg0);
        return arg1;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.queryengine.IUQMatcher#createBadToken(edu.ksu.cis.peq.graph.interfaces.IEdge)
     */
    public IFSMToken createBadToken(IEdge edge) {
        FSMToken token = new FSMToken();        
        token.setTheedge(edge);
        token.setThetransition(badTrans);
        return token;
        
    }

}
