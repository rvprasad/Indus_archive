/*
 * IAtmBuilder.java
 *
 * Created on April 17, 2005, 5:22 PM
 */

package edu.ksu.cis.automata.interfaces;

/**
 * This is the interface for the general automata builder.
 * @author ganeshan
 */
public interface IAtmBuilder {
    /**
     * Create and return the initial state of the automata.
     * @return IState The initial state of the automata.
     */
    IState initialize();
    
    /**
     * Finalize the automata creation.
     * @return IAutomata The final automata.
     */
    IAutomata finalizeAtm();
}
