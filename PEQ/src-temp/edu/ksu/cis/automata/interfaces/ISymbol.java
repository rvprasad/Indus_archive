/*
 * ISymbol.java
 *
 * Created on April 17, 2005, 11:26 AM
 */

package edu.ksu.cis.automata.interfaces;

/**
 * Represents a symbol accepted by the automata.
 * @author ganeshan
 */
public interface ISymbol {
    /**
     * Returns a name for this symbol as a humanly identifiable string.
     * @return String The name for this symbol.
     */
    String getName();
    
    /**
     * Returns the value associated with this label.
     * @return Object The value attached to the label.
     */
    Object getValue();
    
    /**
     * Indicates if both the symbols match each other.
     * @param sym The symbol to match.
     * @return boolean Indicates whether the symbols can be matched.
     */
    boolean match(final ISymbol sym);
}
