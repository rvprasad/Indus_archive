/*
 * Transition.java
 *
 * Created on April 17, 2005, 12:50 PM
 */

package edu.ksu.cis.automata.entities;

import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ISymbol;
import edu.ksu.cis.automata.interfaces.ITransition;

/**
 * The concerete transition implementation.
 * @author ganeshan
 */
public class Transition implements ITransition {
    
    /**
     * The source state of the transition.
     */
    private IState srcState;
    
    /**
     * The destination state of the transition.
     */
    private IState dstnState;
    
    /**
     * Indicates if this transition is an epsilon transition.
     */
    private boolean bEpsTransition;
    
    /**
     * The symbol corresponding to the transition.
     */
    private ISymbol symbol;
    
    /** Creates a new instance of Transition */
    public Transition() {
    }

    /**
     * Returns a string representation of the transition.     
     * @return  a string representation of the object.
     */
    public String toString() {

        String retValue;
        
        retValue = "<Transition id=" + hashCode() + ">\n"; 
        retValue += "<SrcState>" + srcState + "</SrcState>\n";
        retValue += "<DstnState>" + dstnState + "</DstnState>\n";
        retValue += "<Symbol>" + symbol + "</Symbol>\n";
        retValue += " <EpsTransition>" + bEpsTransition + "</EpsTransition>\n";
        retValue += "</Transition>\n";
        
        return retValue;
    }

    /**
     * Indicates if the given transition is an epsilon transition.
     */
    public boolean isEpsTransition() {
        return bEpsTransition;
    }

    /**
     * Returns the symbol associated with this transition.
     * @return ISymbol The symbol on which the transition will be followed.
     */
    public ISymbol getSymbol() {
        return symbol;
    }

    /**
     * Returns the source state of the transition.
     * @return IState The source state of this transition.
     */
    public IState getSourceState() {
        return srcState;
    }

    /**
     * Returns the destination state of the transition.
     * @return IState The destination state of this transition.
     */
    public IState getDstnState() {
        return dstnState;
    }

    /**
     * Sets the source state of this transition.
     * @param srcState The source state of this transition.
     */
    public void setSrcState(IState srcState) {
        this.srcState = srcState;
    }

   /**
    * Set the destination state for this transition.
    * @param dstnState The destinatation state.
    */
    public void setDstnState(IState dstnState) {
        this.dstnState = dstnState;
    }

    /**
     * Covert this transition to an epsilon transition.
     * @param bEpsTransition Indicates if this transition is an epsilon transition.
     */
    public void setEpsTransition(boolean bEpsTransition) {
        this.bEpsTransition = bEpsTransition;
    }

    /**
     * Sets the symbol of the transition.
     * @param symbol The symbol accepted by the transition.
     */
    public void setSymbol(ISymbol symbol) {
        this.symbol = symbol;
    }    
}
