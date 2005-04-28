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

package edu.ksu.cis.indus.peq.test;

import edu.ksu.cis.indus.peq.fsm.EFreeNFA2DFATransformer;
import edu.ksu.cis.indus.peq.fsm.EpsClosureConvertor;
import edu.ksu.cis.indus.peq.fsm.FSMBuilder$v1_2;
import edu.ksu.cis.indus.peq.queryglue.QueryConvertor;
import edu.ksu.cis.indus.peq.queryglue.QueryObject;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;


/**
 * @author ganeshan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public final class TesterCLI {

    /**
     * Constructor.
     */
    private TesterCLI() {
    }

    /**
     * Main function.
     * 
     * @param args
     *            The arguments.
     */
    public static void main(final String[] args) {
        System.out.println("Enter query: ");
        final BufferedReader _br = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            final String _query = _br.readLine();
            final QueryConvertor _qc = new QueryConvertor();
            final QueryObject _qo = _qc.getQueryObject(_query);
            if (_qo != null) {
                final FSMBuilder$v1_2 _builder = new FSMBuilder$v1_2(_qo);
                final IState _state = _builder.getInitialState();
                EpsClosureConvertor _ecc = new EpsClosureConvertor(_builder);
                _ecc.processShallow();
                final IFSM _eFreeFSM = _ecc.getResult();
                final EFreeNFA2DFATransformer _efn2dt = new EFreeNFA2DFATransformer(_eFreeFSM);
				_efn2dt.process();				
                describeFSM(_efn2dt.getDfaAutomata().getInitialState());
            }
        } catch (IOException _ie) {
            _ie.printStackTrace();
        }

    }

    /**
     * Put a user understandable presentation of the fsm.
     * 
     * @param state
     *            The current state.
     */
    private static void describeFSM(final IState state) {
        System.out.println(state + " isFinal: " + state.isFinalState());
        final IState _currState = state;
        final Set _set = _currState.getExitingTransitions();
        for (final Iterator _iter = _set.iterator(); _iter.hasNext();) {
            final ITransition _trans = (ITransition) _iter.next();            
                System.out.println(_trans + " srcstate: "
                        + _trans.getSrcState() + " dstnState: "
                        + _trans.getDstnState() + " variable : "
                        + _trans.getLabel().getVariableName());            
            if (_trans.getDstnState() != _currState) {
                describeFSM(_trans.getDstnState());
            }
        }
    }
}