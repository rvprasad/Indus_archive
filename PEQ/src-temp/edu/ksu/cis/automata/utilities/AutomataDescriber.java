/*
 * AutomataDescriber.java
 *
 * Created on April 17, 2005, 1:23 PM
 */

package edu.ksu.cis.automata.utilities;

import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ITransition;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Desribes the automata in a user friendly form.
 * @author ganeshan
 */
public class AutomataDescriber {
    /**
     * The automata instance.
     */
    private IAutomata automata;
    
    /**
     * The number of states in the automata.
     */
    private int noOfStates;
    
    /**
     * The number of transitions in the automata.
     */
    private int noOfTransitions;
    
    /**
     * The number of symbols in the automata.
     */
    private int noOfSymbols;
    
    /**
     * The stringized description of the states and the transitions.
     */
    private String stateAndTransDescrption;
    
    /** Creates a new instance of AutomataDescriber.
     * @param automata The automata instance.
     * @pre automata != null. 
     */
    public AutomataDescriber(final IAutomata automata) {
        this.automata = automata;
    }
    
    /**
     * Process the automata to compute the statistics.
     */
    public void process() {
        final StringBuffer sb = new StringBuffer();
        final IState initState = automata.getInitialState();
        
        final List workSet = new LinkedList();
        final Set reachSet = new HashSet();
        final Set symbolSet = new HashSet();
        workSet.add(initState);
        
        while (!workSet.isEmpty()) {
         final IState state =  (IState) workSet.remove(0);
         if (!reachSet.contains(state)) {
             reachSet.add(state);
             noOfStates++;
             final Set transSet = state.getExitingTransitions();
             for (final Iterator it = transSet.iterator(); it.hasNext();) {
                 final ITransition trans = (ITransition) it.next();
                 noOfTransitions++;
                 if (!trans.isEpsTransition()) {
                     symbolSet.add(trans.getSymbol());
                 }
                 workSet.add(trans.getDstnState());
                 sb.append(trans);
             }
         }         
        }
        noOfSymbols = symbolSet.size();
        stateAndTransDescrption = sb.toString();
    }
    
    /**
     * Returns an stringized version of the automata.
     * Subclasses can override this to implement a more compatible version.
     * @pre process() has been called on this instance.
     */
    public String printAutomata() {
        final StringBuffer sb = new StringBuffer();
        
        sb.append("<automata>\n");
        sb.append("<stateCount>" + noOfStates + "</stateCount>\n");        
        sb.append("<transitionCount>" + noOfTransitions + "</transitionCount>\n");
        sb.append("<symbolCount>" + noOfSymbols + "</symbolCount>\n");
        sb.append(stateAndTransDescrption);
        sb.append("\n</automata>");
        return sb.toString();
    }
}
