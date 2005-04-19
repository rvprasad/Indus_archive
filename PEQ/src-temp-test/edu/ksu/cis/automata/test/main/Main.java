/*
 * Main.java
 *
 * Created on April 17, 2005, 6:38 PM
 */

package edu.ksu.cis.automata.test.main;

import edu.ksu.cis.automata.builders.RegexBuilder;
import edu.ksu.cis.automata.entities.State;
import edu.ksu.cis.automata.entities.test.Symbol1;
import edu.ksu.cis.automata.entities.test.Symbol2;
import edu.ksu.cis.automata.entities.test.Symbol3;
import edu.ksu.cis.automata.entities.test.Symbol4;
import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.transformers.EpsRemovalTransformer;
import edu.ksu.cis.automata.utilities.AutomataDescriber;

/**
 *
 * @author ganeshan
 */
public class Main {
    
public static void main(String args[]) {
       RegexBuilder builder = new RegexBuilder();
       // Initilialise the builder and get the initial state.
       final IState initState = builder.initialize();
       final Symbol1 sym1 = new Symbol1("sym1", null);
       final Symbol2 sym2 = new Symbol2("sym2", null);
       final Symbol3 sym3 = new Symbol3("sym3", null);
       final Symbol4 sym4 = new Symbol4("sym4", null);
       // Create a chain of automata transitions.
       IState newState = builder.createNormal(sym1, initState);
       newState = builder.createKleene(sym1, newState);
       //newState = builder.createOneOrMore(sym3, newState);
       // Set the final state
       ((State) newState).setFinalState(true);
       // Finalize the automata.
       final IAutomata atm = builder.finalizeAtm();
       // Get a string representation of the automata.
       AutomataDescriber ad = new AutomataDescriber(atm);
       ad.process();
       System.out.println(ad.printAutomata());
       
       System.out.println("Converting the automata to an epsilon free automata");
       // Convert to eps free automata.
       
       final EpsRemovalTransformer ert = new EpsRemovalTransformer(atm);
       
       ert.process();
       final IAutomata epsFreeAtm = ert.getResult();
       ad = new AutomataDescriber(epsFreeAtm);
       ad.process();
       System.out.println(ad.printAutomata());       
   }
    
}
